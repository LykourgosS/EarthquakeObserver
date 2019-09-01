package com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.servicesandbackground.b_ForegroundService;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.broadcastreceiver.b_DynamicReceivers.ChargingReceiver;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 10,July,2019.
 */

public class App extends Application {

    private ChargingReceiver receiver = new ChargingReceiver();

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        // register receiver to receive charge mode changes while app is running
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, filter);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ // notification channels are available for API v.26 and higher
            NotificationChannel serviceChannel = new NotificationChannel(
                    Constant.OBSERVER_SERVICE_CHANNEL_ID,
                    "Channel name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            // if make any changes (i.e. notification's behavior) here uninstall and re-install the app
            serviceChannel.setDescription("This is channel's description");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
