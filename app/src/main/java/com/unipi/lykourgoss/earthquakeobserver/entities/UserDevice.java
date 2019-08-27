package com.unipi.lykourgoss.earthquakeobserver.entities;

import android.hardware.Sensor;

import com.unipi.lykourgoss.earthquakeobserver.tools.Util;

import java.util.Date;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 20,August,2019.
 */

public class UserDevice {

    public static final String IS_RUNNING = "isRunning";
    public static final String LAST_OBSERVING_TIME_IN_MILLIS = "lastObservingTimeInMillis";

    // generated using UUID
    private String deviceId;

    // firebaseAuthUid is generated from Firebase Auth when user creating his account,
    // createdDatetime the Datetime when the device added in Firebase
    private String firebaseAuthUid;
    private String createdDatetime;

    // lastObservingDateTime, lastObservingTimeInMillis, isRunning will be updated every time the service is running
    private String lastObservingDateTime;
    private long lastObservingTimeInMillis;
    private boolean isRunning;

    // sensor (accelerometer) info (used for statistic purposes)
    private SensorInfo sensorInfo;

    public UserDevice() {
    }

    public UserDevice(String deviceId, String firebaseAuthUid, String createdDatetime, String lastObservingDateTime, long lastObservingTimeInMillis, boolean isRunning, SensorInfo sensorInfo) {
        this.deviceId = deviceId;
        this.firebaseAuthUid = firebaseAuthUid;
        this.createdDatetime = createdDatetime;
        this.lastObservingDateTime = lastObservingDateTime;
        this.lastObservingTimeInMillis = lastObservingTimeInMillis;
        this.isRunning = isRunning;
        this.sensorInfo = sensorInfo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getFirebaseAuthUid() {
        return firebaseAuthUid;
    }

    public String getCreatedDatetime() {
        return createdDatetime;
    }

    public String getLastObservingDateTime() {
        return lastObservingDateTime;
    }

    public long getLastObservingTimeInMillis() {
        return lastObservingTimeInMillis;
    }

    public void setLastObservingTimeInMillis(long lastObservingTimeInMillis) {
        this.lastObservingTimeInMillis = lastObservingTimeInMillis;
        this.lastObservingDateTime = Util.millisToDateTime(lastObservingTimeInMillis);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public SensorInfo getSensorInfo() {
        return sensorInfo;
    }

    public static class Builder {
        private String deviceId;
        private String firebaseAuthUid;
        private String createdDatetime;
        private String lastObservingDateTime;
        private long lastObservingTimeInMillis;
        private boolean isRunning;
        private SensorInfo sensorInfo;

        public Builder() {
            this.isRunning = false;
            this.createdDatetime = Util.millisToDateTime(new Date().getTime());
        }

        public Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder setFirebaseAuthUid(String firebaseAuthUid) {
            this.firebaseAuthUid = firebaseAuthUid;
            return this;
        }

        public Builder setSensorInfo(SensorInfo sensorInfo) {
            this.sensorInfo = sensorInfo;
            return this;
        }

        public UserDevice build() {
            return new UserDevice(deviceId, firebaseAuthUid, createdDatetime, lastObservingDateTime, lastObservingTimeInMillis, isRunning, sensorInfo);
        }
    }

}
