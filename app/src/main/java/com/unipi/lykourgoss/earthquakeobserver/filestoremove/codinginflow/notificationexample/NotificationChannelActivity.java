package com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.notificationexample;

import android.app.Notification;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;

public class NotificationChannelActivity extends AppCompatActivity implements View.OnClickListener {

    private NotificationManagerCompat notificationManager;
    private EditText editTextTitle;
    private EditText editTextMessage;
    private Button buttonSendOnChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_channel);

        notificationManager = NotificationManagerCompat.from(this);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextMessage = findViewById(R.id.edit_text_message);

        buttonSendOnChannel = findViewById(R.id.button_send_on_channel);
        buttonSendOnChannel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_send_on_channel){
            String title = editTextTitle.getText().toString().trim();
            String message = editTextMessage.getText().toString().trim();

            Notification notification = new NotificationCompat.Builder(NotificationChannelActivity.this, Constant.OBSERVER_SERVICE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_track_changes_white_24dp)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();

            // if there is already a notification with the same id it will override the old one
            notificationManager.notify(1, notification);
        }
    }
}
