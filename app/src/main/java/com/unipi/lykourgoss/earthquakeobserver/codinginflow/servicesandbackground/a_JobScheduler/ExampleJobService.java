package com.unipi.lykourgoss.earthquakeobserver.codinginflow.servicesandbackground.a_JobScheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 09,July,2019.
 */

public class ExampleJobService extends JobService { // JobService runs in the UI thread by default!!!

    private static final String TAG = "StartObserverJobService";
    private boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started");
        doBackgroundWork(jobParameters);

        // false if the task is short and can be executed here (means job is over),
        // true if will be executed in a background thread
        return true;
}

    private void doBackgroundWork(final JobParameters jobParameters) {
         new Thread(new Runnable() {
             @Override
             public void run() {
                 for (int i = 0; i < 10; i++){
                     Log.d(TAG, "run: " + i);
                     if (jobCancelled){
                         return;
                     }
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
                 Log.d(TAG, "Job finished");
                 // tell the system that job is finished!!!
                 jobFinished(jobParameters, false); // reschedule could be used when job fails
             }
         }).start();
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) { // called when the job is cancelled
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true; // return boolean value means if we want to reschedule or not
    }
}
