package com.unipi.lykourgoss.earthquakeobserver.client.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.models.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.notifications.NotificationHelper;
import com.unipi.lykourgoss.earthquakeobserver.client.receivers.PowerDisconnectedReceiver;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.DeviceHandler;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.EventHandler;

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

    private SensorEvent sensorEvent;
    private MinimalEarthquakeEvent minimalEarthquakeEvent;
    private float acceleration;

    private EarthquakeManager earthquakeManager;

    private Locator locator;
    private Location lastLocation;

    private EventHandler eventHandler;

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

    // todo remove (used for debugging)
    public ObserverService() {
        Log.d(TAG, "ObserverService: Constructor");
    }

    @Override
    public void onCreate() { // triggered only once in the lifetime of the service
        super.onCreate();
        Log.d(TAG, "onCreate");

        // register receiver for actions:
        IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Constant.FAKE_POWER_DISCONNECTED);
        filter.addAction(Constant.DEVICE_IS_MOVING);
        registerReceiver(receiver, filter);

        initSensor();
        initLocator();

        // following are used for observing events and if needed save them to Firebase Database
        deviceId = Util.getUniqueId(this);
        eventHandler = new EventHandler(deviceId);
        DeviceHandler.updateDeviceStatus(deviceId, true);
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
        float balanceValue = SharedPrefManager.getInstance(this).read(Constant.SENSOR_BALANCE_VALUE, Constant.DEFAULT_BALANCE_SENSOR_VALUE);
        earthquakeManager = new EarthquakeManager(this, balanceValue);
        earthquakeManager.registerListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // triggered every time we call startService()
        Log.d(TAG, "onStartCommand");
        isStarted = true;

        // todo only use foreground service on Oreo an higher -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        if (true) { // if API is v.26 and higher start a foreground service
            NotificationCompat.Builder notification = NotificationHelper.getObserverNotification(this);

            // when service started with:
            // 1. startService() -> without the following line system will kill the service after 1 min
            // 2. startForegroundService() -> if not called in 5 seconds max system will kill the service (on API v.26)
            startForeground(Constant.OBSERVER_SERVICE_ID, notification.build()); // id must be greater than 0
        }

        // todo do heavy work on a background thread
        /*// following are used for observing events and if needed save them to Firebase Database
        deviceId = Util.getUniqueId(this);
        eventHandler = new DatabaseHandler(deviceId);
        eventHandler.updateDeviceStatus(deviceId, true);*/

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

        //Util.scheduleObserverService(this); // todo (is it needed) if user stop our service schedule to re-start it
        unregisterReceiver(receiver);
        if (isStarted) {
            DeviceHandler.updateDeviceStatus(deviceId, false);
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

    public SensorEvent getSensorEvent() {
        return sensorEvent;
    }

    public MinimalEarthquakeEvent getMinimalEarthquakeEvent() {
        return minimalEarthquakeEvent;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, MinimalEarthquakeEvent minimalEarthquakeEvent, float acceleration) {
        this.sensorEvent = sensorEvent;
        this.minimalEarthquakeEvent = minimalEarthquakeEvent;
        this.acceleration = acceleration;
    }

    @Override
    public void addMinorEvent(List<MinimalEarthquakeEvent> eventList, float sensorValue) {
        EarthquakeEvent earthquakeEvent = new EarthquakeEvent.Builder(eventList)
                .setDeviceId(deviceId)
                .addSensorValue(sensorValue)
                .setLatitude(0/*todo use lastLocation.getLatitude()*/)
                .setLongitude(0/*todo use lastLocation.getLongitude()*/)
                .build();
        eventHandler.addEventToMinors(earthquakeEvent);
    }

    @Override
    public void addMajorEvent() {
        eventHandler.addEventToMajors();
    }

    @Override
    public void updateEvent(boolean isMajor, int valueIndex, float sensorValue, long endTime) {
        eventHandler.updateEvent(isMajor, valueIndex, sensorValue, endTime);
    }

    @Override
    public void terminateEvent(boolean isMajor) {
        eventHandler.terminateEvent(isMajor);
    }
}
