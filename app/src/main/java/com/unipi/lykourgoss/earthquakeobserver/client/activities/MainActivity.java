package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.services.ObserverService;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.EventHandler;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static final int LOCATION_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        findViewById(R.id.button_sensor_configuration).setOnClickListener(this);
        findViewById(R.id.button_log_location).setOnClickListener(this);
        findViewById(R.id.button_graph_only_norm).setOnClickListener(this);
        findViewById(R.id.button_graph_all).setOnClickListener(this);
        findViewById(R.id.button_clear_events).setOnClickListener(this);
        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_start_service).setOnClickListener(this);
        findViewById(R.id.button_stop_service).setOnClickListener(this);
        findViewById(R.id.button_share).setOnClickListener(this);

        updateUiForAdmin(SharedPrefManager.getInstance(this).read(Constant.USER_IS_ADMIN, false));

        if (checkLocationPermission()) {
            Util.scheduleObserverService(this);
        }
    }

    private void sensorConfiguration() {
        startActivity(new Intent(this, ConfigDeviceActivity.class));
    }

    private void logLocation() {
        startActivity(new Intent(this, LogLocationActivity.class));
    }

    private void graphOnlyNorm() {
        startActivity(new Intent(this, GraphOnlyNormActivity.class));
    }

    private void graphAll() {
        startActivity(new Intent(this, GraphAllActivity.class));
    }

    private void clearEvents() {
        EventHandler handler = new EventHandler(Util.getUniqueId(this));
        handler.deleteSavedEvents();
    }

    private void signIn() {
        startActivity(new Intent(this, SignInActivity.class));
    }

    private void startService() {
        Intent intentService = new Intent(this, ObserverService.class);
        ContextCompat.startForegroundService(this, intentService);
    }

    private void stopService() {
        stopService(new Intent(this, ObserverService.class));
    }

    private void share() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("EarthquakeObserver's link", "https://drive.google.com/open?id=1pL5CPYEZNBhLe7TtMiOabMaiOPQeTMol");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "EarthquakeObserver's link copied", Toast.LENGTH_SHORT).show();
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
        findViewById(R.id.button_clear_events).setVisibility(visibility);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sensor_configuration:
                sensorConfiguration();
                break;
            case R.id.button_log_location:
                logLocation();
                break;
            case R.id.button_graph_only_norm:
                graphOnlyNorm();
                break;
            case R.id.button_graph_all:
                graphAll();
                break;
            case R.id.button_clear_events:
                clearEvents();
                break;
            case R.id.button_sign_in:
                signIn();
                break;
            case R.id.button_start_service:
                startService();
                break;
            case R.id.button_stop_service:
                stopService();
                break;
            case R.id.button_share:
                share();
                break;
        }
    }
}