package com.unipi.lykourgoss.earthquakeobserver;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 31,July,2019.
 */

public class EarthquakeEvent {

    /**
     * event unique id, using Firebase push()
     * */
    private String id;

    // todo see if we need x, y, z for extra information !!!

    /**
    * normalized measurement √(x²+y²+z²) (using accelerometer output: x, y, z)
    * */
    private float measurement;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * occurred
     * */
    private int timeInMillis;

    /**
     * dateTime (according to this format: yyyy-MM-dd HH:mm:ss.SSS z) in which the event occurred
     * */
    private String dateTime;

    private double latitude;

    private double longitude;

    private String uuid; // todo or use firebase uid
}
