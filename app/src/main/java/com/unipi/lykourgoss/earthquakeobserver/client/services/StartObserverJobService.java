package com.unipi.lykourgoss.earthquakeobserver.client.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 09,July,2019.
 */

public class StartObserverJobService extends JobService { // JobService runs in the UI thread by default!!!

    private static final String TAG = "StartObserverJobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");

        // tell the system that job is finished!!!
        //jobFinished(jobParameters, false); // reschedule could be used when job fails

        /*if (checkAcCharge()){
            startService();
        } else{
            Util.scheduleStartJob(this);
        }*/
        startService();
        return false; // false if the task is short and can be executed here (means job is over), true if will be executed in a background thread
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) { // Called if the job was cancelled before being finished or when we manually call jobFinished()
        Log.d(TAG, "Job cancelled before completion");
        return true; // return boolean value means if we want to reschedule or not
    }

    // TODO: 09/06/2019 remove useless comments
    /*private boolean checkAcCharge() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = registerReceiver(null, intentFilter);
        int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        Log.d(TAG, "checkAcCharge: AC = " + (chargePlug == BatteryManager.BATTERY_PLUGGED_AC));
        Toast.makeText(this, String.valueOf(chargePlug), Toast.LENGTH_SHORT).show();
        return chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
    }*/

    private void startService() {
        Intent service = new Intent(this, ObserverService.class);

        // to start the service while app is on the background call
        // startForegroundService(intentService), but after 5 seconds max should call
        // startForeground(...) within Service onStartCommand()!!!
        ContextCompat.startForegroundService(this, service);

        // startService(): If this service is not already running, it will be instantiated and
        // started (creating a process for it if needed); if it is running then it remains running
    }
}
