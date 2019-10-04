package com.unipi.lykourgoss.earthquakeobserver.client.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.activities.EarthquakeActivity;
import com.unipi.lykourgoss.earthquakeobserver.client.activities.LaunchScreenActivity;
import com.unipi.lykourgoss.earthquakeobserver.client.activities.MainActivity;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 02,September,2019.
 */

public class NotificationHelper {

    // notification id must be different from the id when you call
    // startForeground(int id, Notification notification)
    private static final int EARTHQUAKE_NOTIFICATION_ID = 2;
    private static final int UPDATE_NOTIFICATION_ID = 3;

    public static void sendEarthquakeNotification(Context context, String title, String body, String id) {
        Intent intent = new Intent(context, EarthquakeActivity.class);
        intent.putExtra(Constant.EXTRA_EARTHQUAKE_ID, id);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // todo maybe change flags on following
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, Constant.EARTHQUAKES_FEED_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true) // when clicked notification will be dismissed
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //.setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(EARTHQUAKE_NOTIFICATION_ID, notification.build());
    }

    public static NotificationCompat.Builder getUpdateNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // todo maybe change flags on following
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, Constant.UPDATE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_update_white_24dp)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setContentTitle("Updating...")
                //.setContentText("body")
                .setAutoCancel(true) // when clicked notification will be dismissed
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(0, 0, true)
                .setContentIntent(pendingIntent);

        // todo remove
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }
        return notification;
    }

    public static NotificationCompat.Builder getObserverNotification(Context context, String text) {
        Intent intentNotification = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentNotification, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, Constant.OBSERVER_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_track_changes_white_24dp)
                .setColor(context.getResources().getColor(R.color.colorAccent))
                .setContentTitle("Observing for earthquakes")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true) // todo used when the pauser class is added (incoming calls etc.)
                .setContentIntent(pendingIntent);
        return notification;
    }
}
