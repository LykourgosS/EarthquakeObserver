package com.unipi.lykourgoss.earthquakeobserver.client.models;

import com.google.firebase.database.Exclude;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 31,July,2019.
 */

public class EarthquakeEvent {

    public static final String SENSOR_VALUES = "sensorValues";
    public static final String END_TIME = "endTime";
    public static final String END_DATE_TIME = "endDateTime";

    /**
     * event unique eventId, acquired using Firebase push() method
     */
    private String eventId;

    private String deviceId;

    // todo see if we need x, y, z for extra information !!!

    /**
     * normalized mean sensorValue √(x²+y²+z²) (using accelerometer output: x, y, z), calculated by
     * n (ex. 10) measurements/second and use the mean of those and re-adjust it using the next
     * */
    // private float meanSensorValue;


    /**
     * todo see if use following instead of {@link #meanSensorValue}
     */
    private List<Float> sensorValues;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * started
     */
    private long startTime;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * ended
     */
    private long endTime;

    /**
     * dateTime (according to this format: yyyy-MM-dd HH:mm:ss.SSS z) in which the event started
     */
    private String startDateTime;

    /**
     * dateTime (according to this format: yyyy-MM-dd HH:mm:ss.SSS z) in which the event ended
     */
    private String endDateTime;

    private double latitude;

    private double longitude;

    public EarthquakeEvent() {
    }

    /*public EarthquakeEvent(List<MinimalEarthquakeEvent> eventList, double latitude, double longitude) {
        float[] values = sensorEvent.values;
        this.sensorValues.add(normalizeSensorValue(values[0], values[1], values[2]) - balanceValue);
        this.startTime = sensorEvent.timestamp;
        this.startDateTime = Util.nanosFromBootToDateTime(sensorEvent.timestamp);
        this.latitude = latitude;
        this.longitude = longitude;
    }*/

    private EarthquakeEvent(String eventId, String deviceId, /*float meanSensorValue, */List<Float> sensorValues, long startTime, long endTime, String startDateTime, String endDateTime, double latitude, double longitude) {
        this.eventId = eventId;
        this.deviceId = deviceId;
        //this.meanSensorValue = meanSensorValue;
        this.sensorValues = sensorValues;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void addSensorValue(float value) {
        this.sensorValues.add(value);
    }

    public List<Float> getSensorValues() {
        return sensorValues;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        this.endDateTime = Util.millisToDateTime(endTime);
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Exclude
    public long getDuration() {
        return endTime - startTime;
    }

    public static class Builder {
        private String eventId;
        private String deviceId;
        //private float meanSensorValue;
        private List<Float> sensorValues;
        private long startTime;
        private long endTime;
        private String startDateTime;
        private String endDateTime;
        private double latitude;
        private double longitude;

        public Builder(List<MinimalEarthquakeEvent> eventList) {
            this.startTime = eventList.get(0).getTimeInMillis();
            this.startDateTime = Util.millisToDateTime(this.startTime);
            this.endTime = eventList.get(eventList.size() - 1).getTimeInMillis();
            this.endDateTime = Util.millisToDateTime(this.endTime);
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder addSensorValue(float value) {
            this.sensorValues = new ArrayList<>();
            this.sensorValues.add(value);
            return this;
        }

        public Builder setLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder setLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public EarthquakeEvent build() {
            return new EarthquakeEvent(eventId, deviceId, sensorValues, startTime, endTime, startDateTime, endDateTime, latitude, longitude);
        }
    }

    /**
     * returns a normalized sensorValue √(x²+y²+z²) (using accelerometer output: x, y, z)
     * */
    /*public static float normalizeSensorValue(float x, float y, float z) {
        float normXYZ = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return normXYZ;
    }*/
}
