package com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.servicesandbackground.b_ForegroundService;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.servicesandbackground.c_IntentService.IntentServiceActivity;

public class ForegroundServiceActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextInput;
    private Button buttonStartService;
    private Button buttonStopService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreground_service);

        startActivity(new Intent(this, IntentServiceActivity.class));

        editTextInput = findViewById(R.id.edit_text_input);

        buttonStartService = findViewById(R.id.button_start_service);
        buttonStartService.setOnClickListener(this);

        buttonStopService = findViewById(R.id.button_stop_service);
        buttonStopService.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start_service:
                startService();
                break;
            case R.id.button_stop_service:
                stopService();
                break;
        }
    }

    private void startService() {
        String input = editTextInput.getText().toString().trim();

        Intent intentService = new Intent(this, ExampleService.class);
        intentService.putExtra(Constant.EXTRA_INPUT, input);

        // to start the service while app is on the background call
        // startForegroundService(intentService), but after 5 seconds max should call
        // startForeground(...) within Service onStartCommand()!!!
        ContextCompat.startForegroundService(this, intentService);
    }

    private void stopService() {
        Intent intentService = new Intent(this, ExampleService.class);
        stopService(intentService);
    }
}
