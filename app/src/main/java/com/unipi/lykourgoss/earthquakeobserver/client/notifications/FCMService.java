package com.unipi.lykourgoss.earthquakeobserver.client.notifications;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Earthquake;

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

        Log.d(TAG, "onMessageReceived");

        if (remoteMessage.getNotification() != null && remoteMessage.getData().get(Earthquake.ID) != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            String id = remoteMessage.getData().get(Earthquake.ID);

            NotificationHelper.sendNotification(this, title, body, id);
        }
    }
}
