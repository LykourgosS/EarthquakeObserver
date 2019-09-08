package com.unipi.lykourgoss.earthquakeobserver.client.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 08,September,2019.
 */

public class StartUpdateJobService extends JobService { // JobService runs in the UI thread by default!!!

    private static final String TAG = "StartUpdateJobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob");

        Intent service = new Intent(this, UpdateService.class);
        ContextCompat.startForegroundService(this, service);

        return false; // false if the task is short and can be executed here (means job is over), true if will be executed in a background thread
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) { // Called if the job was cancelled before being finished or when we manually call jobFinished()
        Log.d(TAG, "Job cancelled before completion");
        return true; // return boolean value means if we want to reschedule or not
    }
}
