package com.unipi.lykourgoss.earthquakeobserver.Entities;

import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 21,August,2019.
 */

public class FirebaseEarthquakeEvent {

    private String id;

    private String deviceId;

    /**
     * normalized mean sensorValue √(x²+y²+z²) (using accelerometer output: x, y, z), calculated by
     * n (ex. 10) measurements/second and use the mean of those and re-adjust it using the next
     * */
    private float meanSensorValue;


    /**
     * todo see if use following instead of {@link #meanSensorValue}
     */
    private List<Float> sensorValues;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * started
     * */
    private long startTime;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * ended
     * */
    private long endTime;

    /**
     * dateTime (according to this format: yyyy-MM-dd HH:mm:ss.SSS z) in which the event started
     * */
    private String dateTime;

    private double latitude;

    private double longitude;
}
