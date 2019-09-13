package com.unipi.lykourgoss.earthquakeobserver.client;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 10,July,2019.
 */

public class App extends Application {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        // every time the app is launched onCreate is triggered, but once a channel is already
        // created trying to created again does nothing...
        createNotificationChannels();
        subscribeToTopics();
    }

    private void subscribeToTopics() {
        Log.d(TAG, "subscribeToTopics");
        FirebaseMessaging.getInstance().subscribeToTopic(Constant.EARTHQUAKES_FEED_TOPIC);
        FirebaseMessaging.getInstance().subscribeToTopic(Constant.SETTINGS_UPDATE_TOPIC);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // notification channels are available for API v.26 and higher
            //will be used for displaying ObserverService foreground service's notification
            NotificationChannel observerServiceChannel = new NotificationChannel(
                    Constant.OBSERVER_SERVICE_CHANNEL_ID,
                    "Observer Service Channel (name)",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            // todo set description for each channel!!!
            // if make any changes (i.e. notification's behavior) here uninstall and re-install the app
            observerServiceChannel.setDescription("This is channel's description");

            NotificationChannel updateNotificationChannel = new NotificationChannel(
                    Constant.UPDATE_CHANNEL_ID,
                    "Update Notification Channel (name)",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            updateNotificationChannel.setDescription("Description");

            //will be used for displaying notifications when an earthquakeobserver occurred (messages send
            // from FCM)
            NotificationChannel earthquakeNotificationChannel = new NotificationChannel(
                    Constant.EARTHQUAKES_FEED_CHANNEL_ID,
                    "Earthquake Notification Channel (name)",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            // todo set description for each channel!!!
            // if make any changes (i.e. notification's behavior) here uninstall and re-install the app
            earthquakeNotificationChannel.setDescription("This is channel's description");
            //todo !!! earthquakeNotificationChannel.setSound();
            earthquakeNotificationChannel.enableVibration(true);

            // creating notification channels
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(observerServiceChannel);
            manager.createNotificationChannel(updateNotificationChannel);
            manager.createNotificationChannel(earthquakeNotificationChannel);
        }
    }
}
