package com.unipi.lykourgoss.earthquakeobserver.listeners;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.models.MinimalEarthquakeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 28,August,2019.
 */

public class EarthquakeManager implements SensorEventListener {

    private static final String TAG = "EarthquakeManager";

    private Context context;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private OnEarthquakeListener listener;

    private List<MinimalEarthquakeEvent> eventList;

    private float acceleration; // acceleration apart from gravity
    private float currentAcceleration; // current acceleration including gravity
    private float lastAcceleration; // last acceleration including gravity

    private boolean isQuaking = false;

    public EarthquakeManager(Context context) {
        this.context = context;
        eventList = new ArrayList<>();
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
    }

    /*public void setOnShakeListener(OnEarthquakeListener listener) {
        this.listener = listener;
    }*/

    public void registerListener(OnEarthquakeListener listener) {
        this.listener = listener;
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

    private float[] gravity;

    private MinimalEarthquakeEvent performLowPassFilterOriginal(SensorEvent event) {
        // In this example, alpha is calculated as t / (t + dT),
        // where t is the low-pass filter's time-constant and
        // dT is the event delivery rate.

        final float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        event.values[0] = event.values[0] - gravity[0];
        event.values[1] = event.values[1] - gravity[1];
        event.values[2] = event.values[2] - gravity[2];

        return new MinimalEarthquakeEvent(event, 9.91f);
    }

    private MinimalEarthquakeEvent performLowPassFilter(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;
        return new MinimalEarthquakeEvent(acceleration, event.timestamp);
    }

    public float getAcceleration() {
        return acceleration;
    }

    private long millis = SystemClock.elapsedRealtime();

    private int valueCount;

    @Override
    public void onSensorChanged(SensorEvent event) {
        long nowMillis = SystemClock.elapsedRealtime();
        Log.d(TAG, "onSensorChanged: diff = " + (nowMillis - millis));
        millis = nowMillis;
        performLowPassFilter(event);
        // todo use one of the 2 following ways !!!
        // todo probably remove performLowPassFilter(...) abnormal behavior, not following the automato
        // MinimalEarthquakeEvent minimalEarthquakeEvent = performLowPassFilter(event);
        MinimalEarthquakeEvent minimalEarthquakeEvent = new MinimalEarthquakeEvent(event, 9.91f); // todo use actual balanceValue
        eventList.add(minimalEarthquakeEvent);
        if (eventList.size() == 10) {
            float meanValue = MinimalEarthquakeEvent.getMeanValue(eventList);
            Log.d(TAG, "onSensorChanged: diff = " + (nowMillis - millis) + ", meanValue = " + String.format("%.4f", meanValue));
            boolean possibleEarthquake = MinimalEarthquakeEvent.getIfPossibleEarthquake(eventList);
            if (meanValue > Constant.SENSOR_THRESHOLD /* todo && possibleEarthquake*/) {
                if (!isQuaking) {
                    valueCount = 0;
                    // add event
                    // todo add all measurements from MinimalEarthquakeEvent objects instead of only meanValue
                    listener.addEvent(eventList, meanValue);
                    isQuaking = true;
                } else {
                    valueCount++;
                    // update event
                    long endTime = eventList.get(eventList.size() - 1).getTimeInMillis();
                    listener.updateEvent(valueCount, meanValue, endTime);
                }
            } else {
                if (isQuaking) {
                    // terminate event
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

        void updateEvent(int valueIndex, float sensorValue, long endTime);

        void terminateEvent();
    }
}