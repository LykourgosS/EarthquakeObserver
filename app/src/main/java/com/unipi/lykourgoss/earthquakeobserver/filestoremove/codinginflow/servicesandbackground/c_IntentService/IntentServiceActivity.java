package com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.servicesandbackground.c_IntentService;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.servicesandbackground.d_JobIntentService.JobIntentServiceActivity;

public class IntentServiceActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextInput;

    private Button buttonStartService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_service);

        startActivity(new Intent(this, JobIntentServiceActivity.class));

        editTextInput = findViewById(R.id.edit_text_input);

        buttonStartService = findViewById(R.id.button_start_service);
        buttonStartService.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_start_service) {
            String input = editTextInput.getText().toString().trim();

            Intent intentService = new Intent(this, ExampleIntentService.class);
            intentService.putExtra(Constant.EXTRA_INPUT, input);

            // ContextCompat.startForegroundService(...):
            // - API v.26 (Oreo) and higher: startForegroundService(...)
            // - lower: startService(...)
            ContextCompat.startForegroundService(this, intentService);
        }
    }
}
