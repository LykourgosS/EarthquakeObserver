package com.unipi.lykourgoss.earthquakeobserver.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.MainActivity;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.Util;
import com.unipi.lykourgoss.earthquakeobserver.receivers.PowerDisconnectedReceiver;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 10,July,2019.
 */

public class ObserverService extends Service {

    public static final String TAG = "ObserverService";

    private PowerDisconnectedReceiver receiver = new PowerDisconnectedReceiver();

    @Override
    public void onCreate() { // triggered only once in the lifetime of the service
        super.onCreate();

        IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Constant.FAKE_POWER_DISCONNECTED);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // triggered every time we call startService()

        Intent intentNotification = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentNotification, 0);

        // todo only use foreground service on Oreo an higher -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        if (true) { // if API is v.26 and higher start a foreground service
            Notification notification = new NotificationCompat.Builder(this, Constant.CHANNEL_ID)
                    .setContentTitle("Example Service")
                    .setContentText("Observing...")
                    .setSmallIcon(R.drawable.ic_track_changes_white_24dp)
                    .setContentIntent(pendingIntent)
                    .build();

            // when service started with:
            // 1. startService() -> without the following line system will kill the service after 1 min
            // 2. startForegroundService() -> if not called in 5 seconds max system will kill the service (on API v.26)
            startForeground(Constant.OBSERVER_SERVICE_ID, notification); // id must be greater than 0
        }

        // todo do heavy work on a background thread

        // to stop service from here (it will trigger onDestroy())
        //stopSelf();

        // START_NOT_STICKY = when the system kills the service it won't be recreated again
        // START_STICKY = when the system kills the service it will be recreated with a null intent
        // START_REDELIVER_INTENT = when the system kills the service it will be recreated with the last intent
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() { // triggered when service is stopped
        super.onDestroy();
        unregisterReceiver(receiver);
        //Util.scheduleStartJob(this); // todo (is it needed) if user stop our service schedule to re-start it
        Log.d(TAG, "onDestroy: receiver unregistered");
    }

    @Override
    public IBinder onBind(Intent intent) { // we have to implement this, though it's not needed here
        return null;
    }
}
