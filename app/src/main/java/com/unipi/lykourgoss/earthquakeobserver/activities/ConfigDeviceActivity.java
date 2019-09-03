package com.unipi.lykourgoss.earthquakeobserver.activities;

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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.models.Device;
import com.unipi.lykourgoss.earthquakeobserver.models.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.models.SensorInfo;
import com.unipi.lykourgoss.earthquakeobserver.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.AuthHandler;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.DatabaseHandler;

public class ConfigDeviceActivity extends BaseActivity implements View.OnClickListener, SensorEventListener, DatabaseHandler.DatabaseListener {

    private static final String TAG = ConfigDeviceActivity.class.getSimpleName();

    // todo 1000 samples is it ok?
    private static final int SAMPLES_SUM = 10;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private SharedPrefManager sharedPrefManager;

    private float valueCount;
    private float valueSum;

    private DatabaseHandler databaseHandler;

    private Button buttonConfigDevice;
    private ProgressBar progressBarConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_device);

        buttonConfigDevice = findViewById(R.id.button_config_device);
        buttonConfigDevice.setOnClickListener(this);
        progressBarConfig = findViewById(R.id.progress_config_device);
        progressBarConfig.setMax(SAMPLES_SUM);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sharedPrefManager = SharedPrefManager.getInstance(this);

        databaseHandler = new DatabaseHandler(Util.getUniqueId(this));
        databaseHandler.setDatabaseListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float normValue = MinimalEarthquakeEvent.normalizeValue(event.values);
        //if (normValue >= Constant.DEFAULT_SENSOR_BALANCE_VALUE - 0.5 && normValue <= Constant.DEFAULT_SENSOR_BALANCE_VALUE + 0.5) {
        if (Math.abs(normValue - Constant.DEFAULT_SENSOR_BALANCE_VALUE) <= 0.5) {
            valueSum += normValue;
            valueCount++;
            Log.d(TAG, "onSensorChanged: mean = " + valueSum / valueCount + ", count = " + valueCount);
            if (valueCount == SAMPLES_SUM) {
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
        databaseHandler.addDevice(device);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onUserAdded(boolean userAddedSuccessfully) {
        // useless because we don't do anything with users here
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
        sensorManager.unregisterListener(ConfigDeviceActivity.this);
    }
}
