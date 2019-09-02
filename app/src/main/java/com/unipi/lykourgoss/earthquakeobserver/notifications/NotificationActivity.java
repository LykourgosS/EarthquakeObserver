package com.unipi.lykourgoss.earthquakeobserver.notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;

public class NotificationActivity extends AppCompatActivity {

    // notification id must be different from the id when you call
    // startForeground(int id, Notification notification)
    public static final int NOTIFICATION_ID = 2;

    private TextView textViewFCMToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        textViewFCMToken = findViewById(R.id.text_view_fcm_token);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    textViewFCMToken.setText("Token: " + task.getResult().getToken());
                } else {
                    textViewFCMToken.setText(task.getException().getMessage());
                }
            }
        });
    }

    public void showNotification(View v) {
        Notification notification = new NotificationCompat.Builder(this, Constant.EARTHQUAKES_FEED_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setContentTitle("Notification title")
                .setContentText("Notification text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
