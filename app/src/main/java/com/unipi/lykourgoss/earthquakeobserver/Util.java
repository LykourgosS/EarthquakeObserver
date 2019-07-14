package com.unipi.lykourgoss.earthquakeobserver;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 14,July,2019.
 */

public class Util {

    public static void scheduleStartJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, StartObserverJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(Constant.JOB_ID, serviceComponent)
                .setRequiresCharging(true)
                //.setRequiresDeviceIdle(true)
                .setOverrideDeadline(1000)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    public static void scheduleStopJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, StopObserverJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(Constant.JOB_ID, serviceComponent)
                .setRequiresCharging(false)
                //.setRequiresDeviceIdle(true)
                .setOverrideDeadline(1000)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }
}
