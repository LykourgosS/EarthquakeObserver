package com.unipi.lykourgoss.earthquakeobserver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.notifications.NotificationActivity;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.AuthHandler;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";

    private static final long DELAY = 1 * 1000;

    private AuthHandler authHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //startActivity(new Intent(this, NotificationActivity.class));

        authHandler = AuthHandler.getInstance();

        new CountDownTimer(DELAY, DELAY){
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (authHandler.getCurrentUser() == null) {
                    startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
                } else {
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                }
                finish();
                /*Intent intent;
                if (authHandler.getCurrentUser() == null) {
                    intent = new Intent(SplashScreenActivity.this, SignInActivity.class);
                } else {
                    intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);*/
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
