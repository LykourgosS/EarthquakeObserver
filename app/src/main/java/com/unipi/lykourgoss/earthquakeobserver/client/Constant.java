package com.unipi.lykourgoss.earthquakeobserver.client;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 12,July,2019.
 */

public class Constant {

    // used to pass something to intent and do something with that inside a service
    public static final String EXTRA_INPUT = "com.unipi.lykourgoss.earthquakeobserver.client.Constant.EXTRA_INPUT";

    // to pass the id of the earthquake when the FCM notification is clicked
    public static final String EXTRA_EARTHQUAKE_ID = "com.unipi.lykourgoss.earthquakeobserver.client.Constant.EXTRA_EARTHQUAKE_ID";

    // Notification channel ids
    public static final String OBSERVER_SERVICE_CHANNEL_ID = "ObserverServiceChannel";
    public static final String EARTHQUAKES_FEED_CHANNEL_ID = "EarthquakeFeedChannel";
    public static final String UPDATE_CHANNEL_ID = "UpdateChannel";

    // Topic of FCM for Earthquake Updates
    public static final String EARTHQUAKES_FEED_TOPIC = "earthquakes-feed";
    public static final String SETTINGS_UPDATE_TOPIC = "settings-update";

    /*// shows if device is updated
    public static final String IS_UPDATED = "com.unipi.lykourgoss.earthquakeobserver.client.Constant.IS_UPDATED";*/

    // Notification ids
    public static final int OBSERVER_SERVICE_ID = 4;
    public static final int UPDATE_SERVICE_ID = 3;

    // used for scheduling jobs
    public static final int START_OBSERVER_SERVICE_JOB_ID = 1;
    public static final int START_UPDATE_SERVICE_JOB_ID = 2;
    public static final String EXTRA_SETTINGS = "com.unipi.lykourgoss.earthquakeobserver.client.Constant.EXTRA_SETTINGS";

    // 10 samples/s => 1 sample in 0.1 s = 100 ms = 100000 μs
    public static final int SAMPLING_PERIOD = 100 * 1000;

    /**
     * minimum duration in milliseconds for an earthquakeobserver event used to check while the event is terminated, to
     * know if event should be saved to Firebase Database (from active-events to saved-events)
     * */
    public static final long MIN_EVENT_DURATION = 2 * 1000;

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
    public static final float DEFAULT_BALANCE_SENSOR_VALUE = 9.8f;

    // key of device SENSOR_BALANCE_VALUE in balance to be stored in SharedPreferences
    public static final String SENSOR_BALANCE_VALUE = "com.unipi.lykourgoss.earthquakeobserver.client.Constant.SENSOR_BALANCE_VALUE";

    // SensorValue threshold, used for recognize if values from accelerometer are big enough for
    // adding to Firebase (reporting) an event
    public static final float SENSOR_THRESHOLD = 0.1f;
    public static final int SAMPLES_BATCH_COUNT = 10;

    public static final String USER_IS_ADMIN = "com.unipi.lykourgoss.earthquakeobserver.client.Constant.USER_IS_ADMIN";

    // key of device id (DEVICE_ID) to be stored in SharedPreferences only the first time
    public static final String DEVICE_ID = "com.unipi.lykourgoss.earthquakeobserver.client.Constant.DEVICE_ID";
    //
    public static final String DEVICE_ADDED_TO_FIREBASE = "com.unipi.lykourgoss.earthquakeobserver.client.Constant.DEVICE_ADDED_TO_FIREBASE";

    // used in ConfigDeviceActivity, how many samples should take to get the mean sensor value in
    // calm state
    public static final int CONFIG_DEVICE_SAMPLE_COUNT = 1000;
    // if the absolute difference of a sample and the DEFAULT_BALANCE_SENSOR_VALUE is greater than
    // CONFIG_DEVICE_REJECT_SAMPLE_THRESHOLD the configuration will restart
    public static final float CONFIG_DEVICE_REJECT_SAMPLE_THRESHOLD = 0.5f;

    // TODO: 09/07/2019 add following to client settings
    // used for setting up location Requests
    public static final int LOCATION_REQUEST_INTERVAL = 1000 * 30; // 30 seconds
    public static final int LOCATION_REQUEST_FAST_INTERVAL = 1000 * 10; // 10 seconds
}
