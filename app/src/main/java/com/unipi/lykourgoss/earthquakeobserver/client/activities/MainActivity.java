package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.receivers.BootCompletedReceiver;
import com.unipi.lykourgoss.earthquakeobserver.client.services.ObserverService;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.EventHandler;

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
        Log.d(TAG, "onCreate");

        updateUiForAdmin(SharedPrefManager.getInstance(this).read(Constant.USER_IS_ADMIN, false));

        // register receiver for
        IntentFilter filter = new IntentFilter(Constant.FAKE_BOOT);
        registerReceiver(receiver, filter);

        /*if (todo checkLocationPermission()) {
            Intent service = new Intent(this, ObserverService.class);
            ContextCompat.startForegroundService(this, service);
        }*/

        textViewBatteryStatus = findViewById(R.id.text_view_battery_status);
        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        // todo remove Timer
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void sensorConfiguration(View v) {
        startActivity(new Intent(this, ConfigDeviceActivity.class));
    }

    public void logLocation(View v) {
        startActivity(new Intent(this, LogLocationActivity.class));
    }

    public void graphOnlyNorm(View v) {
        startActivity(new Intent(this, GraphOnlyNormActivity.class));
    }

    public void graphAll(View v) {
        startActivity(new Intent(this, GraphAllActivity.class));
    }

    public void fakeBoot(View v) {
        sendBroadcast(new Intent(Constant.FAKE_BOOT));
    }

    public void fakePowerDisconnected(View v) {
        sendBroadcast(new Intent(Constant.FAKE_POWER_DISCONNECTED));
    }

    public void clearEvents(View v) {
        EventHandler handler = new EventHandler(Util.getUniqueId(this));
        handler.deleteSavedEvents();
    }

    public void signIn(View v) {
        startActivity(new Intent(this, SignInActivity.class));
    }

    public void startService(View view) {
        Intent intentService = new Intent(this, ObserverService.class);
        ContextCompat.startForegroundService(this, intentService);
    }

    // todo remove onClick from XML files and make them private
    public void stopService(View view) {
        stopService(new Intent(this, ObserverService.class));
    }

    private void updateUiForAdmin(boolean isAdmin) {
        int visibility = View.VISIBLE;
        if (!isAdmin) visibility = View.GONE;
        findViewById(R.id.button_sensor_configuration).setVisibility(visibility);
        findViewById(R.id.button_log_location).setVisibility(visibility);
        findViewById(R.id.button_graph_only_norm).setVisibility(visibility);
        findViewById(R.id.button_graph_all).setVisibility(visibility);
        findViewById(R.id.button_boot).setVisibility(visibility);
        findViewById(R.id.button_disconnect_power).setVisibility(visibility);
        findViewById(R.id.button_clear_event).setVisibility(visibility);
        findViewById(R.id.button_sign_in).setVisibility(visibility);
        findViewById(R.id.button_start_service).setVisibility(visibility);
        findViewById(R.id.button_stop_service).setVisibility(visibility);
        findViewById(R.id.text_view_battery_status).setVisibility(visibility);
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