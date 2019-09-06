package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Earthquake;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.DatabaseHandler;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

public class EarthquakeActivity extends BaseActivity implements DatabaseHandler.OnEarthquakeFetchListener {

    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);

        String earthquakeId = getIntent().getStringExtra(Constant.EXTRA_EARTHQUAKE_ID);
        if (earthquakeId != null) {
            showProgressDialog();
            databaseHandler = new DatabaseHandler(Util.getUniqueId(this));
            databaseHandler.addOnEarthquakeFetchListener(this);
            databaseHandler.getEarthquake(earthquakeId);
        } else {
            Toast.makeText(this, "Error loading earthquake info", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onEarthquakeFetched(Earthquake earthquake) {
        hideProgressDialog();
        if (earthquake != null) {
            ((TextView) findViewById(R.id.text_view_earthquake_info)).setText(earthquake.toString());
        } else {
            Toast.makeText(this, "Error loading earthquake info", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
