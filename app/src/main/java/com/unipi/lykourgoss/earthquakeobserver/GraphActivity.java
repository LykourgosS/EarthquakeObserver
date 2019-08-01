package com.unipi.lykourgoss.earthquakeobserver;

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
import com.unipi.lykourgoss.earthquakeobserver.services.ObserverService;

import java.util.Timer;
import java.util.TimerTask;

public class GraphActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "GraphActivity";

    private ObserverService observerService;

    private LineChart lineChart;
    private Timer timer;

    private boolean graphIsStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

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

        LineDataSet XDataSet = createSet("X", 0.5f, Color.GREEN);
        data.addDataSet(XDataSet);
        LineDataSet YDataSet = createSet("Y", 0.5f, Color.BLUE);
        data.addDataSet(YDataSet);
        LineDataSet ZDataSet = createSet("Z", 0.5f, Color.RED);
        data.addDataSet(ZDataSet);

        // √(x²+y²+z²) DataSet
        LineDataSet XYZDataSet = createSet("√(x²+y²+z²)", 1f, Color.MAGENTA);
        data.addDataSet(XYZDataSet);

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

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // used for taking accelerometer data from service
        Intent intent = new Intent(this, ObserverService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        timer.cancel();

        // used for taking accelerometer data from service
        unbindService(this);
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

    private void addEntry(float x, float y, float z) {
        LineData data = lineChart.getData();

        ILineDataSet XDataSet = data.getDataSetByIndex(0);
        data.addEntry(new Entry(XDataSet.getEntryCount(), x), 0);

        ILineDataSet YDataSet = data.getDataSetByIndex(1);
        data.addEntry(new Entry(YDataSet.getEntryCount(), y), 1);

        ILineDataSet ZDataSet = data.getDataSetByIndex(2);
        data.addEntry(new Entry(ZDataSet.getEntryCount(), z), 2);

        // √(x²+y²+z²) : to normalize the value, like the magnitude of a vector (now always it
        // will be greater than zero, x, y and z, it only measures the distance from zero)
        float normXYZ = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - 9.81f;

        ILineDataSet XYZDataSet = data.getDataSetByIndex(3);
        data.addEntry(new Entry(XYZDataSet.getEntryCount(), normXYZ), 3);

        data.notifyDataChanged();

        lineChart.getAxisLeft().setSpaceTop(data.getYMax());
        lineChart.getAxisLeft().setSpaceBottom(data.getYMax());

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
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final SensorEvent event = observerService.getLastEvent();
                if (event != null) {
                    GraphActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addEntry(event.values[0], event.values[1], event.values[2]);
                        }
                    });
                }
            }
        }, 0, 50);
    }

    public void playPauseGraph(View v) {
        if (graphIsStopped) {
            startGraphing();
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
            graphIsStopped = false;
        } else {
            timer.cancel();
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            graphIsStopped = true;
        }
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