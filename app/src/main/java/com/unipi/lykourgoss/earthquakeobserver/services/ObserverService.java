package com.unipi.lykourgoss.earthquakeobserver.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.FirebaseHandler;
import com.unipi.lykourgoss.earthquakeobserver.GraphActivity;
import com.unipi.lykourgoss.earthquakeobserver.MainActivity;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.receivers.PowerDisconnectedReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 10,July,2019.
 */

public class ObserverService extends Service implements SensorEventListener {

    public static final String TAG = "ObserverService";

    // Binder given to clients
    private final IBinder binder = new ObserverBinder();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEvent lastEvent = null;

    private Locator locator;
    private Location currentLocation;
    private String locationLog;

    private FirebaseHandler firebaseHandler;

    private PowerDisconnectedReceiver receiver = new PowerDisconnectedReceiver();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class ObserverBinder extends Binder {
        public ObserverService getService() {
            // Return this instance of ObserverService so clients can call public methods
            return ObserverService.this;
        }
    }

    @Override
    public void onCreate() { // triggered only once in the lifetime of the service
        super.onCreate();

        IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Constant.FAKE_POWER_DISCONNECTED);
        registerReceiver(receiver, filter);

        locator =new Locator(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // (slower to faster)
        // - SensorManager.SENSOR_DELAY_NORMAL, delay of 200,000 microseconds = 0.2 seconds
        //      (Measured: events every ~200ms => fs = 5 Hz)
        // - SensorManager.SENSOR_DELAY_UI, delay of 60,000 microseconds = 0.2 seconds
        //      (Measured: events every ~80ms => fs = 12.5 Hz)
        // - SensorManager.SENSOR_DELAY_GAME, delay of 20,000 microseconds = 0.2 seconds
        //      (Measured: events every ~20ms => fs = 50 Hz)
        // - SensorManager.SENSOR_DELAY_FASTEST, 0 microsecond delay
        //      (Measured: events every ~20ms => fs = 50 Hz)
        sensorManager.registerListener(this, accelerometer, Constant.SAMPLING_PERIOD);

        firebaseHandler = new FirebaseHandler();

        locator = new Locator(this);
        currentLocation = locator.getLastLocation();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Location tempLocation = locator.getLastLocation();
                if (currentLocation.getTime() != tempLocation.getTime()) {
                    currentLocation = tempLocation;
                    String location = "Lat: " + currentLocation.getLatitude() + ", Long: " + currentLocation.getLongitude();
                    String speed = currentLocation.getSpeed() + "m/s" + " - " + currentLocation.getSpeed() * 3.6 + " km/h";
                    if (currentLocation.getSpeed() > 0) {
                        locationLog.concat(location + speed);
                        new AlertDialog.Builder(ObserverService.this)
                                .setTitle("Speed update")
                                .setMessage(speed)
                                .setCancelable(true)
                                .create().show();
                    }
                }
            }
        }, 0, 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // triggered every time we call startService()

        Intent intentNotification = new Intent(this, LocationActivity.class);
        intentNotification.putExtra("location_log", locationLog);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentNotification, 0);

        // todo only use foreground service on Oreo an higher -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        if (true) { // if API is v.26 and higher start a foreground service
            Notification notification = new NotificationCompat.Builder(this, Constant.CHANNEL_ID)
                    .setContentTitle("Example Service")
                    .setContentText("Observing...")
                    .setSmallIcon(R.drawable.ic_track_changes_white_24dp)
                    .setContentIntent(pendingIntent)
                    .build();

            // when service started with:
            // 1. startService() -> without the following line system will kill the service after 1 min
            // 2. startForegroundService() -> if not called in 5 seconds max system will kill the service (on API v.26)
            startForeground(Constant.OBSERVER_SERVICE_ID, notification); // id must be greater than 0
        }

        // todo do heavy work on a background thread

        // to stop service from here (it will trigger onDestroy())
        //stopSelf();

        // START_NOT_STICKY = when the system kills the service it won't be recreated again
        // START_STICKY = when the system kills the service it will be recreated with a null intent
        // START_REDELIVER_INTENT = when the system kills the service it will be recreated with the last intent
        return START_REDELIVER_INTENT; // TODO probably use START_STICKY
    }

    @Override
    public void onDestroy() { // triggered when service is stopped
        super.onDestroy();
        unregisterReceiver(receiver);
        //Util.scheduleStartJob(this); // todo (is it needed) if user stop our service schedule to re-start it
        Log.d(TAG, "onDestroy: receiver unregistered");
        sensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

//    private float[] gravity = new float[3];

    public SensorEvent getLastEvent() {
        /*final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * lastEvent.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * lastEvent.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * lastEvent.values[2];

        lastEvent.values[0] = lastEvent.values[0] - gravity[0];
        lastEvent.values[1] = lastEvent.values[1] - gravity[1];
        lastEvent.values[2] = lastEvent.values[2] - gravity[2];*/
        return lastEvent;
    }

//    private long millis = SystemClock.elapsedRealtime();

    @Override
    public void onSensorChanged(SensorEvent event) {
//        long nowMillis = SystemClock.elapsedRealtime();
//        Log.d(TAG, "onSensorChanged: diff between sensorEvents in millis: " + (nowMillis - millis));
//        millis = nowMillis;
        // SystemClock.elapsedRealtime() (i.e. time in millis that onSensorChanged() triggered) - event.timestamp =~ 0.3 millis
        EarthquakeEvent earthquakeEvent = new EarthquakeEvent(event.values, event.timestamp);
        if (Math.abs(earthquakeEvent.getMeasurement() - 9.87)  > 1) {
            firebaseHandler.addEvent(earthquakeEvent);
        }
        lastEvent = event;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
