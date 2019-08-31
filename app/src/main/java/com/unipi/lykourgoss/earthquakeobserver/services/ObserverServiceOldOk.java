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
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.activities.LogLocationActivity;
import com.unipi.lykourgoss.earthquakeobserver.entities.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.entities.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.receivers.PowerDisconnectedReceiver;
import com.unipi.lykourgoss.earthquakeobserver.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 10,July,2019.
 */

public class ObserverServiceOldOk extends Service implements SensorEventListener {

    public static final String TAG = "ObserverService";

    private boolean isStarted = false;

    // Binder given to clients
    private final IBinder binder = new ObserverBinder();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEvent lastEvent;
    private List<MinimalEarthquakeEvent> eventList;

    private Locator locator;
    private Location lastLocation;

    private DatabaseHandler databaseHandler;

    private SharedPrefManager sharedPrefManager;
    private float balanceValue;
    private String deviceId;

    private PowerDisconnectedReceiver receiver = new PowerDisconnectedReceiver();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class ObserverBinder extends Binder {
        public ObserverServiceOldOk getService() {
            // Return this instance of ObserverService so clients can call public methods
            return ObserverServiceOldOk.this;
        }
    }

    public ObserverServiceOldOk() {
        Log.d(TAG, "ObserverService: Constructor");
    }

    @Override
    public void onCreate() { // triggered only once in the lifetime of the service
        super.onCreate();
        Log.d(TAG, "onCreate");
        // register receiver for
        IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Constant.FAKE_POWER_DISCONNECTED);
        filter.addAction(Constant.DEVICE_IS_MOVING);
        registerReceiver(receiver, filter);

        initSensor();
        initLocator();
    }

    public void initLocator() {
        if (true/*!isLocatorInitialized*/) {
            locator = new Locator(this) {
                @Override
                public void onLocationChanged(Location location) {
                    super.onLocationChanged(location);
                    lastLocation = location;
                }
            };
            lastLocation = locator.getLastLocation();
            // todo important line (the following!!!)
            //isLocatorInitialized = true;
        }
    }

    public void initSensor() {
        if (true/*!isSensorInitialized*/) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            // (slower to faster)
            // - SensorManager.SENSOR_DELAY_NORMAL, delay of 200,000 microseconds = 0.2 seconds (Documentation)
            //      (Measured: events every ~200ms => fs = 5 Hz)
            // - Constant.SAMPLING_PERIOD, delay of 100,000 microseconds = 0.1 seconds (Custom)
            //      (Measured: events every ~100ms => fs = 10 Hz)
            // - SensorManager.SENSOR_DELAY_UI, delay of 60,000 microseconds = 0.06 seconds (Documentation)
            //      (Measured: events every ~80ms => fs = 12.5 Hz)
            // - SensorManager.SENSOR_DELAY_GAME, delay of 20,000 microseconds = 0.02 seconds (Documentation)
            //      (Measured: events every ~20ms => fs = 50 Hz)
            // - SensorManager.SENSOR_DELAY_FASTEST, 0 microsecond delay (Documentation)
            //      (Measured: events every ~20ms => fs = 50 Hz)
            sensorManager.registerListener(this, accelerometer, Constant.SAMPLING_PERIOD);
            // todo important line (the following!!!)
            //isSensorInitialized = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // triggered every time we call startService()
        Log.d(TAG, "onStartCommand");
        isStarted = true;

        Intent intentNotification = new Intent(this, LogLocationActivity.class);
//        intentNotification.putExtra(Constant.EXTRA_LOCATION_LOG, locationLog);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentNotification, 0);

        // todo only use foreground service on Oreo an higher -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        if (true) { // if API is v.26 and higher start a foreground service
            Notification notification = new NotificationCompat.Builder(this, Constant.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_track_changes_white_24dp)
                    .setContentTitle("Example Service")
                    .setContentText("Observing...")
                    .setContentIntent(pendingIntent)
                    .build();

            // when service started with:
            // 1. startService() -> without the following line system will kill the service after 1 min
            // 2. startForegroundService() -> if not called in 5 seconds max system will kill the service (on API v.26)
            startForeground(Constant.OBSERVER_SERVICE_ID, notification); // id must be greater than 0
        }

        // todo do heavy work on a background thread
        // following are used for observing events and if needed save them to Firebase Database
        sharedPrefManager = SharedPrefManager.getInstance(this);
        //deviceId = sharedPrefManager.read(Constant.DEVICE_ID, "not-registered-device");
        deviceId = Util.getUniqueId(this);

        databaseHandler = new DatabaseHandler(this, deviceId);
        databaseHandler.updateDeviceStatus(deviceId, true);
        balanceValue = sharedPrefManager.read(Constant.SENSOR_BALANCE_VALUE, Constant.DEFAULT_SENSOR_BALANCE_VALUE);
        eventList = new ArrayList<>();

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
        Log.d(TAG, "onDestroy");

        //Util.scheduleStartJob(this); // todo (is it needed) if user stop our service schedule to re-start it
        unregisterReceiver(receiver);
        if (isQuaking) {
            //todo terminate event
            Log.d(TAG, "onSensorChanged: Terminate last event");
            databaseHandler.terminateEvent();
            isQuaking = false;
        }
        if (isStarted) {
            databaseHandler.updateDeviceStatus(deviceId, false);
        }
        sensorManager.unregisterListener(this);
        locator.removeUpdates();
        // todo not sure if needed!
        //isStarted = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    public SensorEvent getLastEvent() {
        return lastEvent;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    private boolean isQuaking = false;

    //    private long millis = SystemClock.elapsedRealtime();
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, "onSensorChanged");
//        long nowMillis = SystemClock.elapsedRealtime();
//        Log.d(TAG, "onSensorChanged: diff between sensorEvents in millis: " + (nowMillis - millis));
//        millis = nowMillis;
        // SystemClock.elapsedRealtime() (i.e. time in millis that onSensorChanged() triggered) - event.timestamp =~ 0.3 millis
        if (isStarted /*&& lastLocation != null*/) {
            MinimalEarthquakeEvent minimalEarthquakeEvent = new MinimalEarthquakeEvent(sensorEvent, balanceValue);
            eventList.add(minimalEarthquakeEvent);
            if (eventList.size() == 10) {
                float meanValue = MinimalEarthquakeEvent.getMeanValue(eventList);
                boolean possibleEarthquake = MinimalEarthquakeEvent.getIfPossibleEarthquake(eventList);
                if (meanValue > Constant.SENSOR_THRESHOLD && possibleEarthquake) {
                    if (!isQuaking) {
                        //todo add all measurements from MinimalEarthquakeEvent objects instead of only meanValue
                        Log.d(TAG, "onSensorChanged: Add new event");
                        EarthquakeEvent earthquakeEvent = new EarthquakeEvent.Builder(eventList)
                                .setDeviceId(deviceId)
                                .addSensorValue(meanValue)
                                .setLatitude(0/*lastLocation.getLatitude()*/)
                                .setLongitude(0/*lastLocation.getLongitude()*/)
                                .build();
                        databaseHandler.addEvent(earthquakeEvent);
                        isQuaking = true;
                    } else {
                        //todo update
                        Log.d(TAG, "onSensorChanged: Update existing event");
                        long endTime = eventList.get(eventList.size() - 1).getTimeInMillis();
                        //databaseHandler.updateEvent(meanValue, endTime);
                    }
                } else {
                    if (isQuaking) {
                        //todo terminate
                        Log.d(TAG, "onSensorChanged: Terminate last event");
                        databaseHandler.terminateEvent();
                        isQuaking = false;
                    }
                }
                eventList.clear();
            }
        }
        lastEvent = sensorEvent;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
