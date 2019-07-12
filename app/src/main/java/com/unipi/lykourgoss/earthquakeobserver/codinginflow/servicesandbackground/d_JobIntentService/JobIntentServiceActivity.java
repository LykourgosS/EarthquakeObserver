package com.unipi.lykourgoss.earthquakeobserver.codinginflow.servicesandbackground.d_JobIntentService;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;

public class JobIntentServiceActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextInput;

    private Button buttonEnqueueWork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_intent_service);

        editTextInput = findViewById(R.id.edit_text_input);

        buttonEnqueueWork = findViewById(R.id.button_enqueue_work);
        buttonEnqueueWork.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_enqueue_work) {
            enqueueWork();
        }
    }

    private void enqueueWork() {
        String input = editTextInput.getText().toString().trim();

        Intent intentService = new Intent(this, ExampleJobIntentService.class);
        intentService.putExtra(Constant.EXTRA_INPUT, input);

        // using a JobIntentService cannot configure under which circumstances service will run
        // like in JobScheduler (i.e. wifi is on or if charging etc.)
        ExampleJobIntentService.enqueueWork(this, intentService);
    }
}
