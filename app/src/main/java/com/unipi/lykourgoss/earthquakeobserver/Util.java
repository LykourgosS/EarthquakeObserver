package com.unipi.lykourgoss.earthquakeobserver;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.services.StartObserverJobService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 14,July,2019.
 */

public class Util {

    public static final String TAG = "Util";

    public static void scheduleStartJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, StartObserverJobService.class);
        // JobId must be unique, otherwise it will replace the previous scheduled job, by our
        // application, with the same jobId
        JobInfo jobInfo = new JobInfo.Builder(Constant.START_SERVICE_JOB_ID, serviceComponent)
                .setRequiresCharging(true)
                //.setRequiresDeviceIdle(true)
                //.setOverrideDeadline(1000) // The job will be run by this deadline even if other requirements are not met
                //.setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        int resultCode = jobScheduler.schedule(jobInfo);

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
            Toast.makeText(context, "Job scheduled", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Job scheduling failed");
            Toast.makeText(context, "Job scheduling failed", Toast.LENGTH_SHORT).show();
        }
    }

    public static String toDateTime(long timestampInNanos) {
        long timeInMillis = (new Date()).getTime() - SystemClock.elapsedRealtime() + timestampInNanos / 1000000L;
        Date date = new Date(timeInMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
        return dateFormat.format(date);
    }
}
