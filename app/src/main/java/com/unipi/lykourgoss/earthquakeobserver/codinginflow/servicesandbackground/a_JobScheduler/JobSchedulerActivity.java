package com.unipi.lykourgoss.earthquakeobserver.codinginflow.servicesandbackground.a_JobScheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.codinginflow.notificationexample.NotificationChannelActivity;
import com.unipi.lykourgoss.earthquakeobserver.codinginflow.servicesandbackground.b_ForegroundService.ForegroundServiceActivity;

public class JobSchedulerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "JobSchedulerActivity";

    public static final int JOB_ID = 1;

    private Button buttonScheduleJob;
    private Button buttonCancelJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_scheduler);

        startActivity(new Intent(this, ForegroundServiceActivity.class));

        buttonScheduleJob = findViewById(R.id.button_schedule_job);
        buttonScheduleJob.setOnClickListener(this);

        buttonCancelJob = findViewById(R.id.button_cancel_job);
        buttonCancelJob.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_schedule_job:
                scheduleJob();
                break;
            case R.id.button_cancel_job:
                cancelJob();
                break;
        }
    }

    private void scheduleJob() {
        ComponentName componentName = new ComponentName(this, ExampleJobService.class);
        JobInfo info = new JobInfo.Builder(JOB_ID, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true) // keep job alive even device is rebooted, needs permission
                .setPeriodic(15 * 60 * 1000) // can't be less than 15 minutes, otherwise it will be changed internally
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    private void cancelJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
        Log.d(TAG, "Job cancelled");
    }
}