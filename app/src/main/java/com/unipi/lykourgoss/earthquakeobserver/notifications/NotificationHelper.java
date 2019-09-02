package com.unipi.lykourgoss.earthquakeobserver.notifications;

import android.app.Notification;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 02,September,2019.
 */

public class NotificationHelper {

    // notification id must be different from the id when you call
    // startForeground(int id, Notification notification)
    public static final int NOTIFICATION_ID = 2;

    public static void showNotification(Context context, String title, String body) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, Constant.EARTHQUAKES_FEED_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notification.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }
}
