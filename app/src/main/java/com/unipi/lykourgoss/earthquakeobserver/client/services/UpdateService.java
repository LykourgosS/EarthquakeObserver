package com.unipi.lykourgoss.earthquakeobserver.client.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Settings;
import com.unipi.lykourgoss.earthquakeobserver.client.notifications.NotificationHelper;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.DeviceHandler;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.SettingsHandler;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 07,September,2019.
 */

public class UpdateService extends Service implements SettingsHandler.OnSettingsListener {

    private static final String TAG = "UpdateService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        NotificationCompat.Builder notification = NotificationHelper.getUpdateNotification(this);
        startForeground(Constant.UPDATE_SERVICE_ID, notification.build());

        SettingsHandler settingsHandler = new SettingsHandler(this);
        settingsHandler.fetchSettings();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onSettingsFetched(Settings settings) {
        Log.d(TAG, "onSettingsFetched");
        if (settings != null) {
            Util.updateSettings(this, settings);
            DeviceHandler.updateSettingsTime(Util.getUniqueId(this), settings.getLastUpdateTimestamp());
        } else {
            Util.scheduleUpdateService(this);
        }
        stopSelf();
    }

    @Override
    public void onSettingsUpdated(boolean successfullyUpdated) {
    }
}
