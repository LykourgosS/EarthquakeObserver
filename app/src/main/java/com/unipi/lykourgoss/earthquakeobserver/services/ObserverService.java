package com.unipi.lykourgoss.earthquakeobserver.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.activities.MainActivity;
import com.unipi.lykourgoss.earthquakeobserver.entities.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.entities.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.listeners.EarthquakeManager;
import com.unipi.lykourgoss.earthquakeobserver.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.DatabaseHandler;
import com.unipi.lykourgoss.earthquakeobserver.receivers.PowerDisconnectedReceiver;
import com.unipi.lykourgoss.earthquakeobserver.tools.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 10,July,2019.
 */

public class ObserverService extends Service implements EarthquakeManager.OnEarthquakeListener {

    public static final String TAG = "ObserverService";

    private boolean isStarted = false;

    // Binder given to clients
    private final IBinder binder = new ObserverBinder();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEvent lastEvent;
    private List<MinimalEarthquakeEvent> eventList;

    private EarthquakeManager earthquakeManager;

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
        public ObserverService getService() {
            // Return this instance of ObserverService so clients can call public methods
            return ObserverService.this;
        }
    }

    public ObserverService() {
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
        earthquakeManager = new EarthquakeManager(this);
        earthquakeManager.registerListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // triggered every time we call startService()
        Log.d(TAG, "onStartCommand");
        isStarted = true;

        Intent intentNotification = new Intent(this, MainActivity.class);
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
        deviceId = Util.getUniqueId(this);
        databaseHandler = new DatabaseHandler(this, deviceId);
        databaseHandler.updateDeviceStatus(deviceId, true);

        sharedPrefManager = SharedPrefManager.getInstance(this);
        balanceValue = sharedPrefManager.read(Constant.SENSOR_BALANCE_VALUE, Constant.DEFAULT_SENSOR_BALANCE_VALUE);
        eventList = new ArrayList<>();

        // to stop service from here (it will trigger onDestroy())
        //stopSelf();

        // START_NOT_STICKY = when the system kills the service it won't be recreated again
        // START_STICKY = when the system kills the service it will be recreated with a null intent
        // START_REDELIVER_INTENT = when the system kills the service it will be recreated with the last intent
        return START_STICKY; // TODO is it ok to use START_STICKY ?
    }

    @Override
    public void onDestroy() { // triggered when service is stopped
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        //Util.scheduleStartJob(this); // todo (is it needed) if user stop our service schedule to re-start it
        unregisterReceiver(receiver);
        if (isStarted) {
            databaseHandler.updateDeviceStatus(deviceId, false);
        }
        earthquakeManager.unregisterListener();
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

    @Override
    public void onSensorChanged(SensorEvent event, float acceleration) {
        lastEvent = event;
        // this.acceleration = acceleration;
    }

    @Override
    public void addEvent(List<MinimalEarthquakeEvent> eventList, float sensorValue) {
        EarthquakeEvent earthquakeEvent = new EarthquakeEvent.Builder(eventList)
                .setDeviceId(deviceId)
                .addSensorValue(sensorValue)
                .setLatitude(0/*lastLocation.getLatitude()*/)
                .setLongitude(0/*lastLocation.getLongitude()*/)
                .build();
        databaseHandler.addEvent(earthquakeEvent);
    }

    @Override
    public void updateEvent(int valueIndex, float sensorValue, long endTime) {
        databaseHandler.updateEvent(valueIndex, sensorValue, endTime);
    }

    @Override
    public void terminateEvent() {
        databaseHandler.terminateEvent();
    }
}
