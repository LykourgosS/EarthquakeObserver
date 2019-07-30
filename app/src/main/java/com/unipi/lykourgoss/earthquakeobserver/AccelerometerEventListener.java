package com.unipi.lykourgoss.earthquakeobserver;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 28,July,2019.
 */

public class AccelerometerEventListener implements SensorEventListener {

    XYZVector acceleration;

    public AccelerometerEventListener() {
        acceleration = new XYZVector();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acceleration.x = event.values[0];
            acceleration.y = event.values[1];
            acceleration.z = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    class XYZVector {
        float x;
        float y;
        float z;
    }
}
