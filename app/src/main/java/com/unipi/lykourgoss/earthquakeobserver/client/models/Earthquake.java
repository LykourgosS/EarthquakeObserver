package com.unipi.lykourgoss.earthquakeobserver.client.models;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 03,September,2019.
 */

public class Earthquake {

    private String id;

    private long deviceCount;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * ended
     */
    private long timestamp;

    public Earthquake() {
    }

    public Earthquake(long deviceCount, long timestamp) {
        this.deviceCount = deviceCount;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDeviceCount() {
        return deviceCount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
