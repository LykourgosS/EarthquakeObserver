package com.unipi.lykourgoss.earthquakeobserver.entities;

import android.hardware.SensorEvent;

import com.unipi.lykourgoss.earthquakeobserver.tools.Util;

import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 25,August,2019.
 */

public class MinimalEarthquakeEvent {

    /**
     * normalized sensorValue √(x²+y²+z²) (using accelerometer output: x, y, z)
     * */
    private float sensorValue;

    /**
     * time in milliseconds, since January 1, 1970 UTC (1970-01-01-00:00:00), in which the event
     * occurred
     * */
    private long timeInMillis;

    public MinimalEarthquakeEvent(SensorEvent event, float balanceValue) {
        this.sensorValue = normalizeValueToZero(event.values, balanceValue);
        this.timeInMillis = Util.nanosFromBootToMillis(event.timestamp);
    }

    public float getSensorValue() {
        return sensorValue;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    /**
     * returns a normalized sensorValue √(x²+y²+z²) minus the balance value (using accelerometer output: x, y, z)
     * */
    public static float normalizeValueToZero(float[] values, float balanceValue) {
        float x = values[0];
        float y = values[1];
        float z = values[2];
        float normXYZ = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return normXYZ - balanceValue;
    }

    /**
     * returns a normalized sensorValue √(x²+y²+z²) (using accelerometer output: x, y, z)
     * */
    public static float normalizeValue(float[] values) {
        float x = values[0];
        float y = values[1];
        float z = values[2];
        float normXYZ = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return normXYZ;
    }

    public static float getMeanValue(List<MinimalEarthquakeEvent> list) {
        float sum = 0;
        for (MinimalEarthquakeEvent event : list) {
            sum += event.getSensorValue();
        }
        return sum / list.size();
    }
}
