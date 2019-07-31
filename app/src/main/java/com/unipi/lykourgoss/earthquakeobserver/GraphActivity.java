package com.unipi.lykourgoss.earthquakeobserver;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.unipi.lykourgoss.earthquakeobserver.services.ObserverService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphActivity extends AppCompatActivity implements
        SensorEventListener, OnChartGestureListener, ServiceConnection {

    private static final String TAG = "MainActivity";

    // 100 samples/s => 1 sample in 0.01 s = 10 ms = 10000 μs
    private static final int SAMPLING_PERIOD = 10000;
    
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
        lineChart.setOnChartGestureListener(this);

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
//        yAxis.setSpaceTop(50);
//        yAxis.setSpaceBottom(50);
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

        if (x > normXYZ || y > normXYZ || z > normXYZ) {
            notifyForError(x, y, z, normXYZ);
        }

    }

    private void notifyForError(float x, float y, float z, float normXYZ) {
        thread.interrupt();
        sensorManager.unregisterListener(this);

        String message = String.format("x = %f\ny = %f\nz = %f\n√(x²+y²+z²) = %f\n", x, y, z, normXYZ);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sensorManager.registerListener(GraphActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                    }
                })
                .create();
        dialog.show();
    }

    private LineDataSet createSet(String label, float lineWidth, int color) {
        LineDataSet set = new LineDataSet(null, label);
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(lineWidth);
        set.setColor(color);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
//        set.setMode(LineDataSet.Mode.LINEAR);
//        set.setCubicIntensity(0.1f);
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

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
                        String dateTime = dateFormat.format(new Date());
                        Log.d(TAG, "feedMultiple: dateTime " + dateTime);

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
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (plotData) {

            long elapsedRealTime = SystemClock.elapsedRealtime();

            // time in milliseconds since January 1, 1970 UTC (1970-01-01-00:00:00)
            long timeInMillis = (new Date()).getTime() - SystemClock.elapsedRealtime() + event.timestamp / 1000000L;

            Date date = new Date(timeInMillis);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
            String dateTime = dateFormat.format(date);

            Log.d(TAG, "onSensorChanged: diff " + (elapsedRealTime - event.timestamp / 1000000));
            Log.d(TAG, "onSensorChanged: timeInMillis " + timeInMillis);
            Log.d(TAG, "onSensorChanged: dateTime " + dateTime);
            addEntry(event.values[0], event.values[1], event.values[2]);
            plotData = false;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        // used for taking accelerometer data from service
        Intent intent = new Intent(this, ObserverService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        thread.interrupt();
        sensorManager.unregisterListener(this);

        // used for taking accelerometer data from service
        unbindService(this);
    }

    private boolean graphIsStopped = false;

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.d(TAG, "onChartGestureStart: " + lastPerformedGesture);
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.d(TAG, "onChartDoubleTapped: " + me.getAction());
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.d(TAG, "onChartSingleTapped: " + me.getAction());
        if (graphIsStopped) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
            graphIsStopped = false;
        } else {
            thread.interrupt();
            sensorManager.unregisterListener(this);
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            graphIsStopped = true;
        }
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    private ObserverService observerService;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ObserverService.MyBinder binder = (ObserverService.MyBinder) service;
        observerService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}