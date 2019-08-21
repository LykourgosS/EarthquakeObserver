package com.unipi.lykourgoss.earthquakeobserver.Entities;

import com.unipi.lykourgoss.earthquakeobserver.tools.Util;

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
    * normalized sensorValue √(x²+y²+z²) (using accelerometer output: x, y, z)
    * */
    private float sensorValue;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * occurred
     * */
    private long timeInMillis;

    /**
     * dateTime (according to this format: yyyy-MM-dd HH:mm:ss.SSS z) in which the event occurred
     * */
    private String dateTime;

    private double latitude;

    private double longitude;

    //private String uuid; // todo or use firebase uid


    public EarthquakeEvent(float[] eventValues, float balanceValue, long timeInMillis/*, double latitude, double longitude*/) {
        this.sensorValue = normalizeSensorValue(eventValues[0], eventValues[1], eventValues[2]) - balanceValue;
        this.timeInMillis = timeInMillis;
        this.dateTime = Util.nanosFromBootToDateTime(timeInMillis);
        /*this.latitude = latitude;
        this.longitude = longitude;*/
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getSensorValue() {
        return sensorValue;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public String getDateTime() {
        return dateTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * returns a normalized sensorValue √(x²+y²+z²) (using accelerometer output: x, y, z)
     * */
    public static float normalizeSensorValue(float x, float y, float z) {
        float normXYZ = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return normXYZ;
    }
}
