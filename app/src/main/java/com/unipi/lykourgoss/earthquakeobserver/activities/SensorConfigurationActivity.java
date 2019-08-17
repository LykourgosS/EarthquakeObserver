package com.unipi.lykourgoss.earthquakeobserver.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.R;

public class SensorConfigurationActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = SensorConfigurationActivity.class.getSimpleName();

    private static final float DEFAULT_MEAN_SENSOR_VALUE = 9.8f;

    private static final int TIMER_DURATION = 10 * 1000; // 10 seconds

    private SensorManager sensorManager;

    private float valueCount;
    private float valueSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_sensor_calm_state);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, Constant.SAMPLING_PERIOD);

        setTimer();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float normValue = EarthquakeEvent.normalizeSensorValue(event.values[0], event.values[1], event.values[2]);
        if (normValue >= DEFAULT_MEAN_SENSOR_VALUE - 0.5 && normValue <= DEFAULT_MEAN_SENSOR_VALUE + 0.5) {
            valueSum += normValue;
            valueCount++;
        } else {
            Log.d(TAG, "onSensorChanged: big value = " + normValue);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void setTimer() {
        new CountDownTimer(TIMER_DURATION, 100){
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "onTick: mean = " + valueSum / valueCount);
            }

            @Override
            public void onFinish() {
                sensorManager.unregisterListener(SensorConfigurationActivity.this);
                float meanValue = valueSum / valueCount;
                Log.d(TAG, "onFinish: mean = " + meanValue);
                finish();
            }
        }.start();
    }

    private void setMeanValueToPreferences(float meanValue) {

    }
}
