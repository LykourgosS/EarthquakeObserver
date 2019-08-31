package com.unipi.lykourgoss.earthquakeobserver.tools;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.services.StartObserverJobService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

        // log all pending and started jobs
        List<JobInfo> jobs = jobScheduler.getAllPendingJobs();
        for (JobInfo job : jobs) {
            Log.d(TAG, "scheduleStartJob: " + job.toString());
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

    /*public static void addDeviceToFirebase(Context context, SensorInfo sensorInfo) {
        Device device = new Device.Builder()
                .setDeviceId(Util.getUniqueId(context))
                .setFirebaseAuthUid(AuthHandler.getInstance().getCurrentUser().getUid())
                .setSensorInfo(sensorInfo)
                .build();
        DatabaseHandler databaseHandler = new DatabaseHandler(context, device.getDeviceId());
        databaseHandler.addDevice(device);
    }*/

    /*public void setUpDeviceAddedSharedPref(Context context) {
        SharedPrefManager manager = SharedPrefManager.getInstance(context);
        manager.write(Constant.DEVICE_ADDED_TO_FIREBASE, true);
        manager.write(Constant.SENSOR_BALANCE_VALUE)
    }*/
}
