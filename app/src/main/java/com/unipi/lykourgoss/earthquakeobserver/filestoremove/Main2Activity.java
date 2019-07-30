package com.unipi.lykourgoss.earthquakeobserver.filestoremove;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.unipi.lykourgoss.earthquakeobserver.R;

public class Main2Activity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "Main2Activity";
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private LineChart lineChart;
    private Thread thread;
    private boolean plotData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }

        lineChart = findViewById(R.id.line_chart);

        // enable description text
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Accelerometer Output");

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(true);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);

        // set an alternative background color
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setDrawGridBackground(true);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK); // todo remove not needed

        LineDataSet dataSetX = createSet("X", Color.GREEN);
        data.addDataSet(dataSetX);
        LineDataSet dataSetY = createSet("Y", Color.BLUE);
        data.addDataSet(dataSetY);
        LineDataSet dataSetZ = createSet("Z", Color.RED);
        data.addDataSet(dataSetZ);

        lineChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = lineChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.setDrawBorders(false);

        feedMultiple();

    }

    private void addEntry(float x, float y, float z) {

        LineData data = lineChart.getData();


        /*ILineDataSet dataSetX = data.getDataSetByIndex(0);
        dataSetX.addEntry(new Entry(dataSetX.getEntryCount(), x));
        ILineDataSet dataSetY = data.getDataSetByIndex(1);
        dataSetY.addEntry(new Entry(dataSetX.getEntryCount(), y));
        ILineDataSet dataSetZ = data.getDataSetByIndex(2);
        dataSetZ.addEntry(new Entry(dataSetX.getEntryCount(), z));*/
        int sec = data.getMaxEntryCountSet().getEntryCount();

        data.addEntry(new Entry(sec, x), 0);
        data.addEntry(new Entry(sec, y), 1);
        data.addEntry(new Entry(sec, z), 2);

        data.notifyDataChanged();

        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();

        // every time a entry is added move chart at the last added value
        lineChart.moveViewToX(sec);

        // limit the number of visible entries
        //lineChart.setVisibleXRangeMaximum(150);
        //lineChart.setVisibleYRange(30, AxisDependency.LEFT);
    }

    private LineDataSet createSet(String label, int color) {

        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(0.5f);
        set.setColor(color);
//        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
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
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
        sensorManager.unregisterListener(this);

    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (plotData) {
            addEntry(event.values[0], event.values[1], event.values[2]);
            plotData = false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this);
        thread.interrupt();
        super.onDestroy();
    }
}