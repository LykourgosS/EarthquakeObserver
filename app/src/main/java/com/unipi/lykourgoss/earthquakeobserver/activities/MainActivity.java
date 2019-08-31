package com.unipi.lykourgoss.earthquakeobserver.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.listeners.GraphAllActivityTest;
import com.unipi.lykourgoss.earthquakeobserver.receivers.BootCompletedReceiver;
import com.unipi.lykourgoss.earthquakeobserver.services.ObserverService;
import com.unipi.lykourgoss.earthquakeobserver.tools.ConnectivityStatus;
import com.unipi.lykourgoss.earthquakeobserver.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.DatabaseHandler;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private static final int LOCATION_PERMISSION_CODE = 1;

    private TextView textViewBatteryStatus;

    private BootCompletedReceiver receiver = new BootCompletedReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkIfDeviceIsAdded();

        // register receiver for
        IntentFilter filter = new IntentFilter(Constant.FAKE_BOOT);
        registerReceiver(receiver, filter);

        /*if (checkLocationPermission()) {
            Intent service = new Intent(this, ObserverService.class);
            ContextCompat.startForegroundService(this, service);
        }*/

        textViewBatteryStatus = findViewById(R.id.text_view_battery_status);
        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent batteryStatus = registerReceiver(null, intentFilter);
                // Are we charging / charged?
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

                // How are we charging?
                int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

                final String batteryStatusString = String.format("isCharging = %s\nusbCharge = %s\nacCharge = %s", isCharging, usbCharge, acCharge);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewBatteryStatus.setText(batteryStatusString);
                    }
                });
            }
        }, 0, 500);
    }

    private void checkIfDeviceIsAdded() {
        if (ConnectivityStatus.getInstance(this).isOnline()) {
            final SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(MainActivity.this);
            /*new CountDownTimer(5 * 1000, 1000){
                @Override
                public void onTick(long millisUntilFinished) {
                    boolean deviceAddedToFirebase = sharedPrefManager.read(Constant.DEVICE_ADDED_TO_FIREBASE, false);
                    if (deviceAddedToFirebase) {
                        this.cancel();
                        Log.d(TAG, "onTick: device added successfully");
                        // sharedPrefManager.write()
                        hideProgressDialog();
                        finish();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    }
                }

                @Override
                public void onFinish() {
                    hideProgressDialog();
                    boolean deviceAddedToFirebase = sharedPrefManager.read(Constant.DEVICE_ADDED_TO_FIREBASE, false);
                    if (!deviceAddedToFirebase) {
                        Log.d(TAG, "onFinish: user deleted");
                        firebaseAuth.getCurrentUser().delete();
                        signOut();
                        Toast.makeText(MainActivity.this, "Error while creating account. \nTry again.", Toast.LENGTH_SHORT).show();
                    }

                }
            }.start();*/
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void sensorConfiguration(View v) {
        startActivity(new Intent(this, SensorConfigurationActivity.class));
    }

    public void logLocation(View v) {
        startActivity(new Intent(this, LogLocationActivity.class));
    }

    public void graphOnlyNorm(View v) {
        startActivity(new Intent(this, GraphOnlyNormActivity.class));
    }

    public void graphAll(View v) {
        startActivity(new Intent(this, GraphAllActivityTest.class));
    }

    public void fakeBoot(View v) {
        sendBroadcast(new Intent(Constant.FAKE_BOOT));
    }

    public void fakePowerDisconnected(View v) {
        sendBroadcast(new Intent(Constant.FAKE_POWER_DISCONNECTED));
    }

    public void clearEvents(View v) {
        String deviceId = Util.getUniqueId(this);
        DatabaseHandler handler = new DatabaseHandler(this, deviceId);
        handler.deleteSavedEvents();
    }

    public void signIn(View v) {
        startActivity(new Intent(this, SignInActivity.class));
    }

    public void stopService(View view) {
        stopService(new Intent(this, ObserverService.class));
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            requestLocationPermission();
            return false;
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Permission reason")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}