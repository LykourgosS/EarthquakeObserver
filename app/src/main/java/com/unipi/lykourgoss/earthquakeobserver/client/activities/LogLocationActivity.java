package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.client.services.ObserverService;

import java.util.Timer;
import java.util.TimerTask;

public class LogLocationActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "LogLocationActivity";

    private ObserverService observerService;

    private ScrollView scrollViewLog;
    private TextView textViewLogLocation;
    private TextView textViewLogCount;
    private int logCount = 1;

    private Location lastLocation;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_location);

        scrollViewLog = findViewById(R.id.scroll_view_log);
        textViewLogLocation = findViewById(R.id.text_view_location_log);
        textViewLogCount = findViewById(R.id.text_view_log_count);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Intent intent = new Intent(this, ObserverService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        timer.cancel();
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ObserverService.ObserverBinder binder = (ObserverService.ObserverBinder) service;
        observerService = binder.getService();
        Intent intentService = new Intent(this, ObserverService.class);
        ContextCompat.startForegroundService(this, intentService);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final Location tempLocation = observerService.getLastLocation();
                if (tempLocation != null) { // means provider in locator is not fixed yet
                    Log.d(TAG, "Locator is fixed!");
                    if (lastLocation != null) { // means provider in locator wasn't fixed, but now it is!
                        if (lastLocation.getTime() != tempLocation.getTime()) { // if tempLocation isn't the old (last) one
                            updateLogTextViews(tempLocation);
                        }
                    } else {
                        lastLocation = tempLocation;
                        updateLogTextViews(lastLocation);
                    }
                } else {
                    Log.d(TAG, "Locator is not fixed yet");
                }
            }
        }, 0, 10000); // location period with network is ~20000 millis
    }

    private void updateLogTextViews(final Location location) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewLogLocation.append(getLocationLog(location));
                textViewLogCount.setText(String.valueOf(logCount));
                logCount++;
                scrollViewLog.fullScroll(View.FOCUS_DOWN);
                lastLocation = location;
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        observerService = null;
    }

    private String getLocationLog(Location location) {
        String dateTime = logCount +". (" + Util.millisToDateTime(location.getTime()) + ")";

        String distance = "\nDistance: " + lastLocation.distanceTo(location) + " m";
        Log.d(TAG, "getLocationLog: " + distance);
        String speed = "\nSpeed: " + location.getSpeed() + "m/s" + " - " + location.getSpeed() * 3.6 + " km/h";
        String locationLog = dateTime + "\n" + location + distance + speed + "\n\n";
        return locationLog;
    }
}
