package com.unipi.lykourgoss.earthquakeobserver;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 09,July,2019.
 */

public class StopObserverJobService extends JobService { // JobService runs in the UI thread by default!!!

    private static final String TAG = "StartObserverJobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");

        // todo if service is started then stop it
        stopService();
        return true; // false if the task is short and can be executed here (means job is over), true if will be executed in a background thread
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) { // called when the job is cancelled
        Log.d(TAG, "Job cancelled before completion");
        return true; // return boolean value means if we want to reschedule or not
    }

    private void startService() {
        Intent intentService = new Intent(this, ExampleService.class);

        // to start the service while app is on the background call
        // startForegroundService(intentService), but after 5 seconds max should call
        // startForeground(...) within Service onStartCommand()!!!
        ContextCompat.startForegroundService(this, intentService);
    }

    private void stopService() {
        Intent intentService = new Intent(this, ExampleService.class);
        stopService(intentService);
    }
}
