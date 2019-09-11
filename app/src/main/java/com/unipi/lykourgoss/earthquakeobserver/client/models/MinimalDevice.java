package com.unipi.lykourgoss.earthquakeobserver.client.models;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 11,September,2019.
 */

public class MinimalDevice {

    private String deviceId;

    /**
     * eventId is the id of the EarthquakeEvent object that ti is stored in saved events under
     * deviceId
     */
    private String eventId;

    private long timestamp;

    private double latitude;

    private double longitude;

    public MinimalDevice() {
    }

    public MinimalDevice(EarthquakeEvent event) {
        this.deviceId = event.getDeviceId();
        this.eventId = event.getEventId();
        this.timestamp = event.getStartTime();
        this.latitude = event.getLatitude();
        this.longitude = event.getLongitude();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getEventId() {
        return eventId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
