package com.unipi.lykourgoss.earthquakeobserver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.IBinder;
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

public class GraphActivityPrototype extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "GraphActivityPrototype";

    // 100 samples/s => 1 sample in 0.01 s = 10 ms = 10000 μs
    private static final int SAMPLING_PERIOD = 10000;

    private LineChart lineChart;
    private Thread thread;
//    private boolean plotData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_prototype);

        lineChart = findViewById(R.id.line_chart);

        // enable description text
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Accelerometer Output");

        // enable touch gestures
        lineChart.setTouchEnabled(false);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(true);

        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);

        LineData data = new LineData();

        LineDataSet XDataSet = createSet("X", 0.5f, Color.GREEN);
        data.addDataSet(XDataSet);
        LineDataSet YDataSet = createSet("Y", 0.5f, Color.BLUE);
        data.addDataSet(YDataSet);
        LineDataSet ZDataSet = createSet("Z", 0.5f, Color.RED);
        data.addDataSet(ZDataSet);

        // X + Y + Z DataSet
        LineDataSet XYZDataSet = createSet("√(x²+y²+z²)", 1f, Color.MAGENTA);
        data.addDataSet(XYZDataSet);

        lineChart.setData(data);

        // get the legend, the text that describes what every color represent
        // (only possible after setting data)
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextColor(Color.BLACK);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextColor(Color.BLACK);
        yAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void addEntry(float x, float y, float z) {

        LineData data = lineChart.getData();

        ILineDataSet XDataSet = data.getDataSetByIndex(0);
        data.addEntry(new Entry(XDataSet.getEntryCount(), x), 0);

        ILineDataSet YDataSet = data.getDataSetByIndex(1);
        data.addEntry(new Entry(YDataSet.getEntryCount(), y), 1);

        ILineDataSet ZDataSet = data.getDataSetByIndex(2);
        data.addEntry(new Entry(ZDataSet.getEntryCount(), z), 2);

        ILineDataSet XYZDataSet = data.getDataSetByIndex(3);
        // √(x²+y²+z²) : to normalize the value, like the magnitude of a vector (now always it
        // will be greater than zero, x, y and z, it only measures the distance from zero)
        float normXYZ = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        data.addEntry(new Entry(XYZDataSet.getEntryCount(), normXYZ), 3);

        data.notifyDataChanged();

        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();

        // limit the number of visible entries
        lineChart.setVisibleXRangeMaximum(100);
        // mChart.setVisibleYRange(30, AxisDependency.LEFT);

        // move to the latest entry
        lineChart.moveViewToX(data.getEntryCount());
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

    private void feedMultiple() {

        if (thread != null) {
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
//                    plotData = true;
                    try {
                        SensorEvent event = observerService.getLastEvent();
                        if (event != null) {
                            addEntry(event.values[0], event.values[1], event.values[2]);
                        }
                        Thread.sleep(50); // 20 samples/s
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // used for taking accelerometer data from service
        Intent intent = new Intent(this, ObserverService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        thread.interrupt();

        // used for taking accelerometer data from service
        unbindService(this);
    }

    private boolean graphIsStopped = false;


    public void playPauseGraph(View v) {
        if (graphIsStopped) {
//            thread.start();
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
            graphIsStopped = false;
        } else {
//            thread.interrupt();
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            graphIsStopped = true;
        }
    }

    private ObserverService observerService;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ObserverService.MyBinder binder = (ObserverService.MyBinder) service;
        observerService = binder.getService();
        feedMultiple();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        observerService = null;
    }
}