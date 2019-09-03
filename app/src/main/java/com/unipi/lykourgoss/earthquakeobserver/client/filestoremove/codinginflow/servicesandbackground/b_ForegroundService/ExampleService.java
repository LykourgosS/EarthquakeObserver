package com.unipi.lykourgoss.earthquakeobserver.client.filestoremove.codinginflow.servicesandbackground.b_ForegroundService;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 10,July,2019.
 */

public class ExampleService extends Service {

    @Override
    public void onCreate() { // triggered only once in the lifetime of the service
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // triggered every time we call startService()
        String input = intent.getStringExtra(Constant.EXTRA_INPUT);

        Intent intentNotification = new Intent(this, ForegroundServiceActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentNotification, 0);

        Notification notification = new NotificationCompat.Builder(this, Constant.OBSERVER_SERVICE_CHANNEL_ID)
                .setContentTitle("Example Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_track_changes_white_24dp)
                .setContentIntent(pendingIntent)
                .build();

        // when service started with:
        // 1. startService() -> without the following line system will kill the service after 1 min
        // 2. startForegroundService() -> if not called in 5 seconds max system will kill the service (on API v.26)
        startForeground(1, notification); // id must be greater than 0

        // todo do heavy work on a background thread

        // to stop service from here (it will trigger onDestroy())
        //stopSelf();

        return START_NOT_STICKY; // START_NOT_STICKY = when the system kills the service it won't be recreated again
    }

    @Override
    public void onDestroy() { // triggered when service is stopped
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { // we have to implement this, though it's not needed here
        return null;
    }
}
