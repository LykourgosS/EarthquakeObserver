package com.unipi.lykourgoss.earthquakeobserver;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 12,July,2019.
 */

public class Constant {

    // used to pass something to intent and do something with that inside a service
    public static final String EXTRA_INPUT = "com.unipi.lykourgoss.earthquakeobserver.Constant.EXTRA_INPUT";

    // Notification channel id
    public static final String CHANNEL_ID = "ServiceChannel";

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
    public static final int SAMPLING_PERIOD = 100000;

    public static final String EVENT_FIREBASE_REF = "events";

    // used for setting up location Requests
    public static final int LOCATION_REQUEST_INTERVAL = 1000 * 30; // 30 seconds
    public static final int LOCATION_REQUEST_FAST_INTERVAL = 1000 * 10; // 10 seconds

    // used for logging locations in LogLocationActivity and ObserverService
    public static final String EXTRA_LOCATION_LOG = "com.unipi.lykourgoss.earthquakeobserver.Constant.EXTRA_LOCATION_LOG";
}
