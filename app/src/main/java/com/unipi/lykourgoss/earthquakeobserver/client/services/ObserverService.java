package com.unipi.lykourgoss.earthquakeobserver.client.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.models.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.notifications.NotificationHelper;
import com.unipi.lykourgoss.earthquakeobserver.client.receivers.StopObserverReceiver;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.DeviceHandler;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.EventHandler;

import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 10,July,2019.
 */

public class ObserverService extends Service implements EarthquakeManager.OnEarthquakeListener, Locator.LocatorUpdatesListener {

    public static final String TAG = "ObserverService";

    // Binder given to clients
    private final IBinder binder = new ObserverBinder();

    private SensorEvent sensorEvent;
    private MinimalEarthquakeEvent minimalEarthquakeEvent;
    private float acceleration;

    private EarthquakeManager earthquakeManager;

    private Locator locator;

    private EventHandler eventHandler;

    private String deviceId;

    private StopObserverReceiver receiver = new StopObserverReceiver();

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

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Constant.FAKE_POWER_DISCONNECTED);
        filter.addAction(Constant.DEVICE_IS_MOVING);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onCreate() { // triggered only once in the lifetime of the service
        super.onCreate();
        Log.d(TAG, "onCreate");

        registerReceivers();

        float balanceValue = SharedPrefManager.getInstance(this).read(Constant.SENSOR_BALANCE_VALUE, Constant.DEFAULT_BALANCE_SENSOR_VALUE);
        earthquakeManager = new EarthquakeManager(this, balanceValue);
        //todo only onStart locator = new Locator(this, this);


    }

    @Override
    public void onLocatorStatusChanged(boolean isFixed) {
        Log.d(TAG, "onLocatorStatusChanged: " + locator.getLastLocation());
        String text;
        if (isFixed) {
            earthquakeManager.registerListener(this);
            text = "Observing...";
        } else {
            earthquakeManager.unregisterListener();
            text = "No location or device is moving.";
        }
        DeviceHandler.updateDeviceStatus(deviceId, isFixed);
        NotificationCompat.Builder notification = NotificationHelper.getObserverNotification(this, text);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(Constant.OBSERVER_SERVICE_ID, notification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // triggered every time we call startService()
        Log.d(TAG, "onStartCommand");

        NotificationCompat.Builder notification = NotificationHelper.getObserverNotification(this, "Initializing...");

        // when service started with:
        // 1. startService() -> without the following line system will kill the service after 1 min
        // 2. startForegroundService() -> if not called in 5 seconds max system will kill the service (on API v.26)
        startForeground(Constant.OBSERVER_SERVICE_ID, notification.build()); // id must be greater than 0

        // to stop service from here (it will trigger onDestroy())
        //stopSelf();

        // following are used for observing events and if needed save them to Firebase Database
        locator = new Locator(this, this);
        deviceId = Util.getUniqueId(this);
        eventHandler = new EventHandler(deviceId);

        // START_NOT_STICKY = when the system kills the service it won't be recreated again
        // START_STICKY = when the system kills the service it will be recreated with a null intent
        // START_REDELIVER_INTENT = when the system kills the service it will be recreated with the last intent
        return START_STICKY;
    }

    @Override
    public void onDestroy() { // triggered when service is stopped
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        unregisterReceiver(receiver);
        earthquakeManager.unregisterListener();

        if (locator != null) { // means the service has been started
            locator.removeUpdates();
            DeviceHandler.updateDeviceStatus(deviceId, false);
        }

        //todo ? Util.scheduleObserverService(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        earthquakeManager.registerListener(this);
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
        return locator.getLastLocation();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, MinimalEarthquakeEvent minimalEarthquakeEvent, float acceleration) {
        this.sensorEvent = sensorEvent;
        this.minimalEarthquakeEvent = minimalEarthquakeEvent;
        this.acceleration = acceleration;
    }

    @Override
    public EarthquakeEvent createMinorEvent(List<MinimalEarthquakeEvent> eventList, float sensorValue) {
        if (locator != null) { // means the service is started
            return new EarthquakeEvent.Builder(eventList)
                    .setDeviceId(deviceId)
                    .addSensorValue(sensorValue)
                    .setLatitude(locator.getLastLocation().getLatitude())
                    .setLongitude(locator.getLastLocation().getLongitude())
                    .build();
        }
        return null;
    }

    @Override
    public void addEvent(EarthquakeEvent earthquakeEvent) {
        if (locator != null) { // means the service is started
            eventHandler.addEvent(earthquakeEvent);
        }
    }

    @Override
    public void updateEvent(int valueIndex, float sensorValue, long endTime) {
        if (locator != null) { // means the service is started
            eventHandler.updateEvent(valueIndex, sensorValue, endTime);
        }
    }

    @Override
    public void terminateEvent() {
        if (locator != null) { // means the service is started
            eventHandler.terminateEvent();
        }
    }
}
