package com.unipi.lykourgoss.earthquakeobserver.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.entities.MinimalEarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 28,August,2019.
 */

public class EarthquakeEventListener implements SensorEventListener {

    private static final String TAG = "EarthquakeEventListener";

    private Context context;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private OnEarthquakeListener listener;

    private List<MinimalEarthquakeEvent> eventList;

    private float acceleration; // acceleration apart from gravity
    private float currentAcceleration; // current acceleration including gravity
    private float lastAcceleration; // last acceleration including gravity

    private boolean isQuaking = false;

    public EarthquakeEventListener(Context context, OnEarthquakeListener listener) {
        this.context = context;
        this.listener = listener;
        eventList = new ArrayList<>();
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
    }

    /*public void setOnShakeListener(OnEarthquakeListener listener) {
        this.listener = listener;
    }*/

    public void registerListener() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, Constant.SAMPLING_PERIOD);
    }

    public void unregisterListener() {
        sensorManager.unregisterListener(this);
        if (isQuaking) {
            Log.d(TAG, "unregisterListener: Terminate last event");
            listener.terminateEvent();
            isQuaking = false;
        }
    }

    private MinimalEarthquakeEvent performLowPassFilter(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x*x + y*y + z*z);
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;
        return new MinimalEarthquakeEvent(acceleration, event.timestamp);
    }

    public float getAcceleration() {
        return acceleration;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged");
        performLowPassFilter(event);
        MinimalEarthquakeEvent minimalEarthquakeEvent = performLowPassFilter(event);
//        MinimalEarthquakeEvent minimalEarthquakeEvent = new MinimalEarthquakeEvent(event, 9.8f);
        eventList.add(minimalEarthquakeEvent);
        if (eventList.size() == 10) {
            float meanValue = MinimalEarthquakeEvent.getMeanValue(eventList);
            boolean possibleEarthquake = MinimalEarthquakeEvent.getIfPossibleEarthquake(eventList);
            if (meanValue > Constant.SENSOR_THRESHOLD && possibleEarthquake) {
                if (!isQuaking) {
                    // add event
                    //todo add all measurements from MinimalEarthquakeEvent objects instead of only meanValue
                    Log.d(TAG, "onSensorChanged: Add new event");
                    listener.addEvent(eventList, meanValue);
                    isQuaking = true;
                } else {
                    // update event
                    Log.d(TAG, "onSensorChanged: Update existing event");
                    long endTime = eventList.get(eventList.size() - 1).getTimeInMillis();
                    listener.updateEvent(meanValue, endTime);
                }
            } else {
                if (isQuaking) {
                    // terminate event
                    //todo terminate
                    Log.d(TAG, "onSensorChanged: Terminate last event");
                    listener.terminateEvent();
                    isQuaking = false;
                }
            }
            eventList.clear();
        }
        listener.onSensorChanged(event, acceleration);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public interface OnEarthquakeListener {
        void onSensorChanged(SensorEvent event, float acceleration);
        void addEvent(List<MinimalEarthquakeEvent> eventList, float sensorValue);
        void updateEvent(float sensorValue, long endTime);
        void terminateEvent();
    }
}