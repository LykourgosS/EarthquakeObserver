package com.unipi.lykourgoss.earthquakeobserver.client.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Earthquake;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 02,September,2019.
 */

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options

        // todo remove log
        Log.d(TAG, "onMessageReceived");

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getData().get(Earthquake.ID) != null) {
                // show notification for new earthquake
                Log.d(TAG, "onMessageReceived: new earthquake");
                String title = remoteMessage.getNotification().getTitle();
                String body = remoteMessage.getNotification().getBody();
                String id = remoteMessage.getData().get(Earthquake.ID);

                NotificationHelper.sendEarthquakeNotification(this, title, body, id);
            }
        } else if (Constant.SETTINGS_UPDATE_TOPIC.equals(remoteMessage.getData().get("title"))) {
            // update settings
            Log.d(TAG, "onMessageReceived: settings updated");
            Util.scheduleUpdateService(this);
        }
    }
}
