package com.unipi.lykourgoss.earthquakeobserver.codinginflow.servicesandbackground.c_IntentService;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 11,July,2019.
 */

public class ExampleIntentService extends IntentService { // runs in the background by default, stop by itself when there aren't any other intents

    public static final String TAG = "ExampleIntentService";

    private PowerManager.WakeLock wakeLock;

    public ExampleIntentService() {
        super("ExampleIntentService"); // the parameter name is just for debugging purposes only
        // false = START_NOT_STICKY,
        // true = START_REDELIVER_INTENT (i.e. means that the service will be started again and
        // the last intent will be delivered in onHandleIntent(...))
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        // making a wakeLock used to keep the CPU running while phone's screen is turned off
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        // PARTIAL_WAKE_LOCK = means screen can turn off but keep CPU running
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ExampleIntentService:WakeLock"); // tag: is for debugging purposes
        wakeLock.acquire();
        Log.d(TAG, "WakeLock acquired");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // if API is v.26 and higher start a foreground service
            Notification notification = new NotificationCompat.Builder(this, Constant.CHANNEL_ID)
                    .setContentTitle("ExampleIntentService")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_track_changes_white_24dp)
                    .build();

            startForeground(1, notification);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // here we are doing our job, it will be executed in a background thread
        // incoming intents are executed sequentially one after another in one single thread
        Log.d(TAG, "onHandleIntent");

        String input = intent.getStringExtra(Constant.EXTRA_INPUT);

        for (int i = 0; i < 10; i++) {
            Log.d(TAG, input + " - " + i);
            SystemClock.sleep(1000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        wakeLock.release();
        Log.d(TAG, "Wakelock released");
    }
}
