package com.unipi.lykourgoss.earthquakeobserver.client.models;

import java.util.Map;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 03,September,2019.
 */

public class Earthquake {

    public static final String ID = "id";

    public static final String DEVICES = "devices";

    private String id;

    private Map<String, Boolean> devices;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * ended
     */
    private long timestamp;

    public Earthquake() {
    }

    public Earthquake(Map<String, Boolean> devices, long timestamp) {
        this.devices = devices;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Boolean> getDevices() {
        return devices;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Earthquake{" +
                "id='" + id + '\'' +
                ", devices=" + devices.size() +
                ", timestamp=" + timestamp +
                '}';
    }
}
