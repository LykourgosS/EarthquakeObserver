package com.unipi.lykourgoss.earthquakeobserver;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unipi.lykourgoss.earthquakeobserver.services.Locator;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int LOCATION_PERMISSION_CODE = 1;

    private Locator locator;

    private TextView textViewLocation;
    private TextView textViewSpeed;
    private TextView textViewSpeedLog;

    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewLocation = findViewById(R.id.text_view_location);
        textViewSpeed = findViewById(R.id.text_view_speed);
        textViewSpeedLog = findViewById(R.id.text_view_speed_log);

        if (checkLocationPermission()) {
            locator = new Locator(this);
            currentLocation = locator.getLastLocation();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Location tempLocation = locator.getLastLocation();
                    if (currentLocation.getTime() != tempLocation.getTime()) {
                        currentLocation = tempLocation;
                        final String location = "Lat: " + currentLocation.getLatitude() + ", Long: " + currentLocation.getLongitude();
                        final String speed = currentLocation.getSpeed() + "m/s" + " - " + currentLocation.getSpeed() * 3.6 + " km/h";
                        if (currentLocation.getSpeed() > 0) {
                            textViewSpeedLog.append("\n -> " + speed + " (" + currentLocation.getProvider() + ")");
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Speed update")
                                    .setMessage(speed)
                                    .setCancelable(true)
                                    .create().show();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewLocation.setText(location);
                                textViewSpeed.setText(speed);
                                /*textViewSpeedLog.append("\n -> " + speed + " (" + currentLocation.getProvider() + ")");*/
                                textViewSpeedLog.append("\n\n - " + currentLocation + "\nspeed: " + speed);
                            }
                        });
                    }/* else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewLocation.setText("Location is null");
                            }
                        });
                        //Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }, 0, 1000);
        }
    }

    public void graph(View v) {
        startActivity(new Intent(this, GraphActivity.class));
    }

    public void fakeBoot(View v) {
        sendBroadcast(new Intent(Constant.FAKE_BOOT));
    }

    public void fakePowerDisconnected(View v) {
        sendBroadcast(new Intent(Constant.FAKE_POWER_DISCONNECTED));
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
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
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