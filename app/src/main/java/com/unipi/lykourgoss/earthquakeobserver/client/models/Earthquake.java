package com.unipi.lykourgoss.earthquakeobserver.client.models;

import java.util.Map;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 03,September,2019.
 */

public class Earthquake {

    public static final String ID = "id";
    public static final String DEVICES = "devices";
    public static final String IS_ACTIVE = "isActive";
    public static final String END_TIME = "endTime";

    private String id;

    private Map<String, Boolean> devices;

    private boolean isActive;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the
     * Earthquake started
     */
    private long startTime;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the
     * Earthquake ended (stop being active)
     */
    private long endTime;

    public Earthquake() {
    }

    public Earthquake(Map<String, Boolean> devices, boolean isActive, long startTime) {
        this.devices = devices;
        this.isActive = isActive;
        this.startTime = startTime;
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

    public boolean getIsActive() {
        return isActive;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Earthquake{" +
                "id='" + id + '\'' +
                ", devices=" + devices.size() +
                ", isActive=" + isActive +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
