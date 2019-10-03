package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        updateUiForAdmin(SharedPrefManager.getInstance(this).read(Constant.USER_IS_ADMIN, false));

        if (checkLocationPermission()) {
            Util.scheduleObserverService(this);
        }
    }

    public void sensorConfiguration(View view) {
        startActivity(new Intent(this, ConfigDeviceActivity.class));
    }

    public void logLocation(View view) {
        startActivity(new Intent(this, LogLocationActivity.class));
    }

    public void graphOnlyNorm(View view) {
        startActivity(new Intent(this, GraphOnlyNormActivity.class));
    }

    public void graphAll(View view) {
        startActivity(new Intent(this, GraphAllActivity.class));
    }

    public void clearEvents(View view) {
        EventHandler handler = new EventHandler(Util.getUniqueId(this));
        handler.deleteSavedEvents();
    }

    public void signIn(View view) {
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

    public void share(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("EarthquakeObserver's link", "www.google.com");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "EarthquakeObserver's link copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void updateUiForAdmin(boolean isAdmin) {
        int visibility = View.VISIBLE;
        // regular user features
        findViewById(R.id.button_start_service).setVisibility(visibility);
        findViewById(R.id.button_stop_service).setVisibility(visibility);
        if (!isAdmin) visibility = View.GONE;
        // admin user features
        findViewById(R.id.button_sensor_configuration).setVisibility(visibility);
        findViewById(R.id.button_log_location).setVisibility(visibility);
        findViewById(R.id.button_graph_only_norm).setVisibility(visibility);
        findViewById(R.id.button_graph_all).setVisibility(visibility);
        findViewById(R.id.button_clear_event).setVisibility(visibility);
        findViewById(R.id.button_sign_in).setVisibility(visibility);
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