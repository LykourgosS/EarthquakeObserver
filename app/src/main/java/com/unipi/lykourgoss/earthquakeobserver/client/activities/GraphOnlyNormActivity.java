package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.models.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.services.ObserverService;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;

import java.util.Timer;
import java.util.TimerTask;

public class GraphOnlyNormActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "GraphAllActivity";

    private ObserverService observerService;

    private LineChart lineChart;
    private Timer timer;

    private boolean graphIsStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        initializeChart();
    }

    private void initializeChart() {
        lineChart = findViewById(R.id.line_chart);

        // enable description text
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Accelerometer Output");

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(true);

        lineChart.setDoubleTapToZoomEnabled(false);

        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);

        LineData data = new LineData();

        // √(x²+y²+z²) DataSet
        LineDataSet normXYZDataSet = createSet("√(x²+y²+z²)", 1f, Color.MAGENTA);
        data.addDataSet(normXYZDataSet);

        lineChart.setData(data);

        // get the legend, the text that describes what every color represent
        // (only possible after setting data)
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextColor(Color.BLACK);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLabels(true); // todo set to false
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextColor(Color.BLACK);
        yAxis.setDrawGridLines(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setZeroLineWidth(1f);
        yAxis.setZeroLineColor(Color.BLACK);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private LineDataSet createSet(String label, float lineWidth, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(lineWidth);
        set.setColor(color);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        return set;
    }

    private void addEntry(float sensorValue) {
        LineData data = lineChart.getData();

        // √(x²+y²+z²) : to normalize the value, like the magnitude of a vector (now always it
        // will be greater than zero, x, y and z, it only measures the distance from zero)
        //float normXYZ = MinimalEarthquakeEvent.normalizeValueToZero(new float[] {x, y, z}, balanceValue);
        ILineDataSet normXYZDataSet = data.getDataSetByIndex(0);
        data.addEntry(new Entry(normXYZDataSet.getEntryCount(), sensorValue), 0);

        data.notifyDataChanged();

        // todo need removal ?
        //lineChart.getAxisLeft().setSpaceTop(data.getYMax());
        //lineChart.getAxisLeft().setSpaceBottom(data.getYMax());

        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();

        // limit the number of visible entries
        lineChart.setVisibleXRangeMaximum(100);
        // mChart.setVisibleYRange(30, AxisDependency.LEFT);

        // move to the latest entry
        lineChart.moveViewToX(data.getEntryCount());
    }

    private void startGraphing() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final MinimalEarthquakeEvent event = observerService.getMinimalEarthquakeEvent();
                if (event != null) {
                    GraphOnlyNormActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addEntry(event.getSensorValue());
                        }
                    });
                }
            }
        }, 0, 110/*todo replace with Constant.SAMPLING_PERIOD*/); // period = 110, because accelerometer sampling period is 100 millis
    }

    public void stopStartGraph(View v) {
        Button button = (Button) v;
        if (graphIsStopped) {
            startGraphing();
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
            graphIsStopped = false;
            button.setText("Stop");
        } else {
            timer.cancel();
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            graphIsStopped = true;
            button.setText("Start");
        }
    }


    /**
     * {}@link: https://developer.android.com/guide/components/bound-services#Additional_Notes
     *
     * Note: You don't usually bind and unbind during your activity's onResume() and onPause(),
     * because these callbacks occur at every lifecycle transition and you should keep the
     * processing that occurs at these transitions to a minimum. Also, if multiple activities in
     * your application bind to the same service and there is a transition between two of those
     * activities, the service may be destroyed and recreated as the current activity unbinds
     * (during pause) before the next one binds (during resume). This activity transition for how
     * activities coordinate their lifecycles is described in the Activities document.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // used for taking accelerometer data from service
        Intent intent = new Intent(this, ObserverService.class);
        // The third parameter is a flag indicating options for the binding. It should usually be
        // BIND_AUTO_CREATE in order to create the service if it's not already alive. Other possible
        // values are BIND_DEBUG_UNBIND and BIND_NOT_FOREGROUND, or 0 for none
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        timer.cancel();

        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected");
        ObserverService.ObserverBinder binder = (ObserverService.ObserverBinder) service;
        observerService = binder.getService();
        startGraphing();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        observerService = null;
    }
}