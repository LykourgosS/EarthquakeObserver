package com.unipi.lykourgoss.earthquakeobserver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void graph(View v) {
        startActivity(new Intent(this, GraphActivity.class));
    }

    public void fakeBoot(View v) {
        sendBroadcast(new Intent(Constant.FAKE_BOOT));
    }

    public void fakePowerDisconnected(View v) {
        sendBroadcast(new Intent(Constant.FAKE_POWER_DISCONNECTED));
    }
}