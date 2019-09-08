package com.unipi.lykourgoss.earthquakeobserver.client.tools;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Settings;
import com.unipi.lykourgoss.earthquakeobserver.client.services.StartObserverJobService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.unipi.lykourgoss.earthquakeobserver.client.models.Settings.CONFIG_DEVICE_REJECT_SAMPLE_THRESHOLD;
import static com.unipi.lykourgoss.earthquakeobserver.client.models.Settings.DEFAULT_BALANCE_SENSOR_VALUE;
import static com.unipi.lykourgoss.earthquakeobserver.client.models.Settings.LAST_UPDATE_TIMESTAMP;
import static com.unipi.lykourgoss.earthquakeobserver.client.models.Settings.MIN_EVENT_DURATION;
import static com.unipi.lykourgoss.earthquakeobserver.client.models.Settings.SAMPLES_BATCH_COUNT;
import static com.unipi.lykourgoss.earthquakeobserver.client.models.Settings.SAMPLING_PERIOD;
import static com.unipi.lykourgoss.earthquakeobserver.client.models.Settings.SENSOR_VALUE_THRESHOLD;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 14,July,2019.
 */

public class Util {

    public static final String TAG = "Util";

    public static void scheduleObserverService(Context context) {
        ComponentName serviceComponent = new ComponentName(context, StartObserverJobService.class);
        // JobId must be unique, otherwise it will replace the previous scheduled job, by our
        // application, with the same jobId
        JobInfo jobInfo = new JobInfo.Builder(Constant.START_OBSERVER_SERVICE_JOB_ID, serviceComponent)
                .setRequiresCharging(true)
                //.setRequiresDeviceIdle(true)
                //.setOverrideDeadline(1000) // The job will be run by this deadline even if other requirements are not met
                //.setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        int resultCode = jobScheduler.schedule(jobInfo);

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "ObserverService scheduled");
            Toast.makeText(context, "ObserverService scheduled", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "ObserverService scheduling failed");
            Toast.makeText(context, "ObserverService scheduling failed", Toast.LENGTH_SHORT).show();
        }

        // log all pending and started jobs
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        for (JobInfo job : jobs) {
            Log.d(TAG, "listOfJobs: " + job.toString());
        }
    }

    public static void scheduleUpdateService(Context context) {
        ComponentName serviceComponent = new ComponentName(context, StartObserverJobService.class);
        // JobId must be unique, otherwise it will replace the previous scheduled job, by our
        // application, with the same jobId
        JobInfo jobInfo = new JobInfo.Builder(Constant.START_UPDATE_SERVICE_JOB_ID, serviceComponent)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        int resultCode = jobScheduler.schedule(jobInfo);

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "UpdateService scheduled");
            Toast.makeText(context, "UpdateService scheduled", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "UpdateService scheduling failed");
            Toast.makeText(context, "UpdateService scheduling failed", Toast.LENGTH_SHORT).show();
        }

        // log all pending and started jobs
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        for (JobInfo job : jobs) {
            Log.d(TAG, "listOfJobs: " + job.toString());
        }
    }

    public static long nanosFromBootToMillis(long timeInNanos) {
        return (new Date()).getTime() - SystemClock.elapsedRealtime() + timeInNanos / (1000 * 1000);
    }

    public static String millisToDateTime(long timeInMillis) {
        Date date = new Date(timeInMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
        return dateFormat.format(date);
    }

    /**
     * Method for generating unique id for devices (installations of each user), called to access
     * the unique id of the device from SharedPreferences (the first time that will be called it
     * will generate the unique id which will be used from this time and on)
     * */
    public synchronized static String getUniqueId(Context context) {
        SharedPrefManager manager = SharedPrefManager.getInstance(context);
        String uniqueId = manager.read(Constant.DEVICE_ID, null);
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString();
            manager.write(Constant.DEVICE_ID, uniqueId);
        }
        return uniqueId;
    }

    public static void updateSettings(Context context, Settings settings) {
        SharedPrefManager manager = SharedPrefManager.getInstance(context);
        manager.write(SAMPLING_PERIOD, settings.getSamplingPeriod());
        manager.write(SAMPLES_BATCH_COUNT, settings.getSamplesBatchCount());
        manager.write(MIN_EVENT_DURATION, settings.getMinEventDuration());
        manager.write(DEFAULT_BALANCE_SENSOR_VALUE, settings.getDefaultBalanceSensorValue());
        manager.write(SENSOR_VALUE_THRESHOLD, settings.getSensorValueThreshold());
        manager.write(CONFIG_DEVICE_REJECT_SAMPLE_THRESHOLD, settings.getConfigDeviceRejectSampleThreshold());
        manager.write(LAST_UPDATE_TIMESTAMP, settings.getLastUpdateTimestamp());
    }
}
