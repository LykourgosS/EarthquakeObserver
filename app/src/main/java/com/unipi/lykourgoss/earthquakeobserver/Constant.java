package com.unipi.lykourgoss.earthquakeobserver;

import android.hardware.SensorManager;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 12,July,2019.
 */

public class Constant {

    // used to pass something to intent and do something with that inside a service
    public static final String EXTRA_INPUT = "com.unipi.lykourgoss.earthquakeobserver.Constant.EXTRA_INPUT";

    // Notification channel id
    public static final String OBSERVER_SERVICE_CHANNEL_ID = "ObserverServiceChannel";
    public static final String EARTHQUAKE_NOTIFICATION_CHANNEL_ID = "EarthquakeNotificationChannel";

    public static final int OBSERVER_SERVICE_ID = 1;

    // used for scheduling jobs
    public static final int START_SERVICE_JOB_ID = 2;
    public static final int STOP_SERVICE_JOB_ID = 3;

    // for Debugging purposes: custom action broadcasts
    public static final String FAKE_BOOT = "com.unipi.lykourgoss.earthquakeobserver.FAKE_BOOT";
    public static final String FAKE_POWER_DISCONNECTED = "com.unipi.lykourgoss.earthquakeobserver.FAKE_POWER_DISCONNECTED";
    public static final String DEVICE_IS_MOVING = "com.unipi.lykourgoss.earthquakeobserver.DEVICE_IS_MOVING";

    // used for identifying the user and the installation
    public static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    // 10 samples/s => 1 sample in 0.1 s = 100 ms = 100000 μs
    public static final int SAMPLING_PERIOD = 100 * 1000;

    /**
     * minimum duration in milliseconds for an earthquake event used to check while the event is terminated, to
     * know if event should be saved to Firebase Database (from active-events to saved-events)
     * */
    public static final long MIN_EVENT_DURATION = 5 * 1000;

    // used for passing info about sensor from SensorConfigurationActivity back to SignInActivity
    // to add device to firebase
    public static final String EXTRA_SENSOR_INFO = "com.unipi.lykourgoss.earthquakeobserver.EXTRA_SENSOR_INFO";

    // according to documentation, might defer from the real one
    // (ex. instead of 9.8 to be 9.87 or 9.9)
    // (used 1000 samples)
    // 1. ZTE API 21:
    // meanX = -0.029400, meanY = -0.318933, meanZ = 9.862784
    // meanNormXYZ = 9.867907
    //
    // 2. Samsung J5 API 22:
    // meanX = -0.327567, meanY = 0.117220, meanZ = 9.671708
    // meanNormXYZ = 9.677995
    //
    // 3. Samsung T580 API 27:
    // meanX = -0,271595, meanY = -0,018965, meanZ = 9,772408
    // meanNormXYZ = 9.776207
    public static final float DEFAULT_SENSOR_BALANCE_VALUE = 9.8f;

    // key of device SENSOR_BALANCE_VALUE in balance to be stored in SharedPreferences
    public static final String SENSOR_BALANCE_VALUE = "com.unipi.lykourgoss.earthquakeobserver.SENSOR_BALANCE_VALUE";

    // SensorValue threshold, used for recognize if values from accelerometer are big enough for
    // adding to Firebase (reporting) an event
    public static final float SENSOR_THRESHOLD = 0.1f;

    // key of device id (DEVICE_ID) to be stored in SharedPreferences only the first time
    public static final String DEVICE_ID = "com.unipi.lykourgoss.earthquakeobserver.DEVICE_ID";
    //
    public static final String DEVICE_ADDED_TO_FIREBASE = "com.unipi.lykourgoss.earthquakeobserver.DEVICE_ADDED_TO_FIREBASE";

    public static final String EVENT_FIREBASE_REF = "events";

    // used for setting up location Requests
    public static final int LOCATION_REQUEST_INTERVAL = 1000 * 30; // 30 seconds
    public static final int LOCATION_REQUEST_FAST_INTERVAL = 1000 * 10; // 10 seconds

    // used for logging locations in LogLocationActivity and ObserverService
    public static final String EXTRA_LOCATION_LOG = "com.unipi.lykourgoss.earthquakeobserver.Constant.EXTRA_LOCATION_LOG";
}
