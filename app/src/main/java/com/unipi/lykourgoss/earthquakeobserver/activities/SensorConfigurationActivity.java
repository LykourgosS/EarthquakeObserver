package com.unipi.lykourgoss.earthquakeobserver.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.entities.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.entities.SensorInfo;
import com.unipi.lykourgoss.earthquakeobserver.entities.UserDevice;
import com.unipi.lykourgoss.earthquakeobserver.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.AuthHandler;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.DatabaseHandler;

public class SensorConfigurationActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = SensorConfigurationActivity.class.getSimpleName();

    private static final int TIMER_DURATION = 10 * 1000; // 10 seconds

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private SharedPrefManager sharedPrefManager;

    private float valueCount;
    private float valueSum;

    private float valueCountX;
    private float valueSumX;

    private float valueCountY;
    private float valueSumY;

    private float valueCountZ;
    private float valueSumZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_sensor_calm_state);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, Constant.SAMPLING_PERIOD);

        sharedPrefManager = SharedPrefManager.getInstance(this);
        //setTimer();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float normValue = MinimalEarthquakeEvent.normalizeValue(event.values);
        //float normValue = event.values[0] + event.values[1] + event.values[2];
        if (normValue >= Constant.DEFAULT_SENSOR_BALANCE_VALUE - 0.5 && normValue <= Constant.DEFAULT_SENSOR_BALANCE_VALUE + 0.5) {
            valueSum += normValue;
            valueCount++;
            logForXYZ(event.values[0], event.values[1], event.values[2]);
            Log.d(TAG, "onSensorChanged: mean = " + valueSum / valueCount + ", count = " + valueCount);
            //Log.d(TAG, "onSensorChanged: count = " + valueCount);
            if (valueCount == 1000) {
                float meanValue = valueSum / valueCount;
                boolean result = sharedPrefManager.write(Constant.SENSOR_BALANCE_VALUE, meanValue);
                Log.d(TAG, "onSensorChanged: finish mean = " + meanValue + ", write: " + result);
                putSensorInfoToExtras(meanValue);
                finish();
            }
        } else {
            logForXYZ(event.values[0], event.values[1], event.values[2]);
            Log.d(TAG, "onSensorChanged: value out of range = " + normValue);
            valueSum = 0;
            valueCount = 0;
        }
    }

    private void putSensorInfoToExtras(float balanceValue) {
        SensorInfo sensorInfo = new SensorInfo(accelerometer, balanceValue);
        Intent result = new Intent();
        result.putExtra(Constant.EXTRA_SENSOR_INFO, sensorInfo);
        setResult(RESULT_OK, result);
    }

    private void addDeviceToFirebase(float balanceValue) {
        SensorInfo sensorInfo = new SensorInfo(accelerometer, balanceValue);
        UserDevice device = new UserDevice.Builder()
                .setDeviceId(Util.getUniqueId(this))
                .setFirebaseAuthUid(AuthHandler.getInstance().getCurrentUser().getUid())
                .setSensorInfo(sensorInfo)
                .build();
        DatabaseHandler databaseHandler = new DatabaseHandler(this, device.getDeviceId());
        databaseHandler.addDevice(device);
    }

    private void logForXYZ(float x, float y, float z) {
        Log.d(TAG, String.format("%f. logForXYZ = %f: X = %f, Y = %f, Z = %f", valueCount, (x+y+z), x, y, z));
        // for x
        valueSumX+= x;
        valueCountX++;
        float meanX = valueSumX / valueCountX;
        // for y
        valueSumY += y;
        valueCountY++;
        float meanY = valueSumY / valueCountY;
        // for z
        valueSumZ += z;
        valueCountZ++;
        float meanZ = valueSumZ / valueCountZ;
        Log.d(TAG, String.format("%f. logForXYZ: meanX = %f, meanY = %f, meanZ = %f", valueCount, meanX, meanY, meanZ));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        sensorManager.unregisterListener(SensorConfigurationActivity.this);
    }

    /*private void setTimer() {
        new CountDownTimer(TIMER_DURATION, 100){
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "onTick: mean = " + valueSum / valueCount);
            }

            @Override
            public void onFinish() {
                sensorManager.unregisterListener(SensorConfigurationActivity.this);
                float meanValue = valueSum / valueCount;
                Log.d(TAG, "onFinish: mean = " + meanValue + ", count = " + valueCount);
                finish();
            }
        }.start();
    }*/
}
