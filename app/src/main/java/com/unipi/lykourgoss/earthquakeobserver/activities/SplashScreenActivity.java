package com.unipi.lykourgoss.earthquakeobserver.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.AuthHandler;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long DELAY = 1 * 1000;

    private AuthHandler authHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

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
            }
        }.start();
    }
}
