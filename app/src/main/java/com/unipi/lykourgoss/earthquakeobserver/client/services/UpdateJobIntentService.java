package com.unipi.lykourgoss.earthquakeobserver.client.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Settings;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 07,September,2019.
 */

public class UpdateJobIntentService extends JobIntentService {

    private static final String TAG = "UpdateJobIntentService";

    public static void enqueueWork(Context context, Intent data) {
        enqueueWork(context, UpdateJobIntentService.class, Constant.START_UPDATE_SERVICE_JOB_ID, data);
    }

    // TODO: 09/07/2019 remove onCreate is only for debugging
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "onHandleWork");

        Settings settings = (Settings) intent.getSerializableExtra(Constant.EXTRA_SETTINGS);
        Util.updateSettings(this, settings);
    }

    @Override
    public boolean onStopCurrentWork() {
        // if it's using the JobScheduler (API level 26 an higher), it will be triggered when the
        // current job has been stopped (because device is under strong memory pressure, falls into
        // doze mode, or has been running too long, i.e. over 10 minutes they will get stopped and
        // differed). WakeLock will be released here

        // return value: true if we want to start again the job with the sent intents,
        // false: current and following intents will be dropped = job is cancelled
        return super.onStopCurrentWork(); // default superclass's return value = true
    }
}
