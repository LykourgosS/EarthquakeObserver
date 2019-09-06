package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Earthquake;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;

public class LaunchScreenActivity extends AppCompatActivity {

    private static final String TAG = "LaunchScreenActivity";

    private static final long LAUNCH_SCREEN_TIME_OUT = 1 * 1000;

    private FirebaseAuth firebaseAuth;

    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanch_screen);

        Log.d(TAG, "onCreate");

        sharedPrefManager = SharedPrefManager.getInstance(this);

        firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(LaunchScreenActivity.this, SignInActivity.class));
                } else {
                    if (sharedPrefManager.read(Constant.DEVICE_ADDED_TO_FIREBASE, false)) {
                        Bundle bundle = getIntent().getExtras();
                        if (bundle != null) {
                            for (String key : bundle.keySet()) {
                                Object value = bundle.get(key);
                                Log.d(TAG, "Key: " + key + " Value: " + value);
                            }
                            Intent intent = new Intent(LaunchScreenActivity.this, EarthquakeActivity.class);
                            intent.putExtra(Constant.EXTRA_EARTHQUAKE_ID, bundle.get(Earthquake.ID).toString());
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(LaunchScreenActivity.this, MainActivity.class));
                        }
                    } else {
                        startActivity(new Intent(LaunchScreenActivity.this, ConfigDeviceActivity.class));
                    }
                }
                finish();
            }
        }, LAUNCH_SCREEN_TIME_OUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
