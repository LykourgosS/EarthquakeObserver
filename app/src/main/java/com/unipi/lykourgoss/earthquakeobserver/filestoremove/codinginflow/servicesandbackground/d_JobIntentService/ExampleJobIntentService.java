package com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.servicesandbackground.d_JobIntentService;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.unipi.lykourgoss.earthquakeobserver.Constant;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 12,July,2019.
 */

public class ExampleJobIntentService extends JobIntentService { // jobs have a time limit close to 10 minutes

    public static final String TAG = "ExampleJobIntentService";

    static void enqueueWork(Context context, Intent work) {
        // jobId is hard coded, because it must be the same for all the jobs that we send here
        enqueueWork(context, ExampleJobIntentService.class, 123, work);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // works (intents) are handled sequentially and in the background like IntentService,
        // though wakelock will be held automatically
        Log.d(TAG, "onHandleWork");

        String input = intent.getStringExtra(Constant.EXTRA_INPUT);

        for (int i = 0; i < 10; i++) {
            Log.d(TAG, input + "-" + i);

            // it's better to stop our onHandleWork (like in jobs with jobScheduler) by ourselves when the
            // job is stopped, instead of letting the system to kill our service to avoid misbehaving
            if (isStopped()) return;

            SystemClock.sleep(1000);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
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
