package com.unipi.lykourgoss.earthquakeobserver.Entities;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 20,August,2019.
 */

public class UserDevice {

    enum Status {
        RUNNING,
        STOPPED,
    }

    private String id;

    private float balanceSensorValue;

    private int minDelay;

    private String sensorToString;

    private String lastObservingDateTime;

    private long lastObservingTimeInMillis;

    private boolean isOnline;

    private Status serviceStatus;
}
