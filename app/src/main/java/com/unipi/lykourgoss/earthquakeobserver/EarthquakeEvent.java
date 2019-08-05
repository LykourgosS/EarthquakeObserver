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
    private long timeInMillis;

    /**
     * dateTime (according to this format: yyyy-MM-dd HH:mm:ss.SSS z) in which the event occurred
     * */
    private String dateTime;

    private double latitude;

    private double longitude;

    //private String uuid; // todo or use firebase uid


    public EarthquakeEvent(float[] eventValues, long timeInMillis/*, double latitude, double longitude*/) {
        this.measurement = normalizeMeasurement(eventValues[0], eventValues[1], eventValues[2]);
        this.timeInMillis = timeInMillis;
        this.dateTime = Util.toDateTime(timeInMillis);
        /*this.latitude = latitude;
        this.longitude = longitude;*/
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getMeasurement() {
        return measurement;
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

    public static float normalizeMeasurement(float x, float y, float z) {
        float normXYZ = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return normXYZ;
    }
}
