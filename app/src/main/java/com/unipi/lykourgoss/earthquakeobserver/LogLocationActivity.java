package com.unipi.lykourgoss.earthquakeobserver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.lykourgoss.earthquakeobserver.services.ObserverService;

import java.util.Timer;
import java.util.TimerTask;

public class LogLocationActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "LogLocationActivity";

    private ObserverService observerService;

    private TextView textViewLogLocation;

    private Location lastLocation;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_location);

        textViewLogLocation = findViewById(R.id.text_view_location_log);

        Intent intent = new Intent(this, ObserverService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        unbindService(this);
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ObserverService.ObserverBinder binder = (ObserverService.ObserverBinder) service;
        observerService = binder.getService();

        lastLocation = observerService.getLastLocation();
        textViewLogLocation.append(getLocationLog(lastLocation, lastLocation));

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final Location tempLocation = observerService.getLastLocation();
                if (lastLocation.getTime() != tempLocation.getTime()) {
                    LogLocationActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewLogLocation.append(getLocationLog(lastLocation, tempLocation));
                        }
                    });
                    lastLocation = tempLocation;
                }

            }
        }, 0, 10000); // location period with network is ~20000 millis
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        observerService = null;
    }

    private String getLocationLog(Location oldLocation, Location newLocation) {
        String distance = "\nDistance: " + oldLocation.distanceTo(newLocation) + " m";
        String speed = "\nSpeed: " + newLocation.getSpeed() + "m/s" + " - " + newLocation.getSpeed() * 3.6 + " km/h";
        String locationLog = "\n\n" + newLocation + distance + speed;
        return locationLog;
    }
}
