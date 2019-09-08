package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Device;
import com.unipi.lykourgoss.earthquakeobserver.client.models.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.models.SensorInfo;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.DeviceHandler;

public class ConfigDeviceActivity extends BaseActivity implements View.OnClickListener, SensorEventListener, DeviceHandler.OnDeviceAddListener {

    private static final String TAG = ConfigDeviceActivity.class.getSimpleName();

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private SharedPrefManager sharedPrefManager;

    private float valueCount;
    private float valueSum;

    private DeviceHandler deviceHandler;

    private Button buttonConfigDevice;
    private ProgressBar progressBarConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_device);

        buttonConfigDevice = findViewById(R.id.button_config_device);
        buttonConfigDevice.setOnClickListener(this);
        progressBarConfig = findViewById(R.id.progress_config_device);
        progressBarConfig.setMax(Constant.CONFIG_DEVICE_SAMPLE_COUNT);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sharedPrefManager = SharedPrefManager.getInstance(this);

        deviceHandler = new DeviceHandler(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float normValue = MinimalEarthquakeEvent.normalizeValue(event.values);
        if (Math.abs(normValue - Constant.DEFAULT_BALANCE_SENSOR_VALUE) <= Constant.CONFIG_DEVICE_REJECT_SAMPLE_THRESHOLD) {
            valueSum += normValue;
            valueCount++;
            Log.d(TAG, "onSensorChanged: mean = " + valueSum / valueCount + ", count = " + valueCount);
            if (valueCount == Constant.CONFIG_DEVICE_SAMPLE_COUNT) {
                float meanValue = valueSum / valueCount;
                boolean result = sharedPrefManager.write(Constant.SENSOR_BALANCE_VALUE, meanValue);
                Log.d(TAG, "onSensorChanged: finish mean = " + meanValue + ", write: " + result);
                addDeviceToFirebase(meanValue);
            }
        } else {
            Log.d(TAG, "onSensorChanged: value out of range = " + normValue);
            valueSum = 0;
            valueCount = 0;
        }
        progressBarConfig.setProgress((int) valueCount);
    }

    private void addDeviceToFirebase(float balanceValue) {
        showProgressDialog();
        sensorManager.unregisterListener(ConfigDeviceActivity.this);
        SensorInfo sensorInfo = new SensorInfo(accelerometer, balanceValue);
        Device device = new Device.Builder()
                .setDeviceId(Util.getUniqueId(this))
                .setFirebaseAuthUid(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setSensorInfo(sensorInfo)
                .build();
        deviceHandler.addDevice(device);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDeviceAdded(boolean deviceAddedSuccessfully) {
        Log.d(TAG, "onDeviceAdded");
        hideProgressDialog();
        // save to shared preferences that device added to Firebase, if not app might not work properly
        sharedPrefManager.write(Constant.DEVICE_ADDED_TO_FIREBASE, deviceAddedSuccessfully);
        if (deviceAddedSuccessfully) {
            Toast.makeText(this, "Device configuration completed successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // todo restart this activity
            // todo show dialog something went wrong click start again
            buttonConfigDevice.setEnabled(true);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_config_device) {
            configDevice();
        }
    }

    private void configDevice() {
        buttonConfigDevice.setEnabled(false);
        progressBarConfig.setProgress(0);
        valueCount = 0;
        valueSum = 0;
        sensorManager.registerListener(this, accelerometer, Constant.SAMPLING_PERIOD);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        // todo maybe unregister in onPause or onStop
        sensorManager.unregisterListener(ConfigDeviceActivity.this);
    }
}
