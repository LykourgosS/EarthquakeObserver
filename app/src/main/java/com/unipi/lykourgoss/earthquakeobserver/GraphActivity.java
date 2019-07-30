package com.unipi.lykourgoss.earthquakeobserver;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;

public class GraphActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
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

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        lineChart = findViewById(R.id.line_chart);

        // enable description text
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Accelerometer Output");

        // enable touch gestures
        //lineChart.setTouchEnabled(true);

        // enable scaling and dragging
//        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);

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
        xAxis.setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextColor(Color.BLACK);
//        yAxis.setAxisMaximum(10f);
//        yAxis.setCenterAxisLabels(true);
//        yAxis.setAxisMinimum(-10f);
        yAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        feedMultiple();

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
        // will be greater than zero and x,y,z, it only measures the distance from zero)
        float normXYZ = (float) Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
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
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(lineWidth);
        set.setColor(color);
        set.setHighlightEnabled(false);
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
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
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
    protected void onPause() {
        super.onPause();

        thread.interrupt();
        sensorManager.unregisterListener(this);
    }
}