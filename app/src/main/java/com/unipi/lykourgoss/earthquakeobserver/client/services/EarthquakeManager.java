package com.unipi.lykourgoss.earthquakeobserver.client.services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.models.MinimalEarthquakeEvent;

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

    private final float balanceValue;

    public EarthquakeManager(Context context, float balanceValue) {
        this.context = context;
        this.balanceValue = balanceValue;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        eventList = new ArrayList<>();
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
    }

    /*public void setOnShakeListener(OnEarthquakeListener listener) {
        this.listener = listener;
    }*/

    private boolean listenerIsRegistered = false;

    /**
     * if true means the service is started and we want to upload events, else the service is just
     * bound and we don't want to upload events
     * */
    //private boolean uploadEvents;

    public void registerListener(OnEarthquakeListener listener/*, boolean uploadEvents*/) {
        if (!listenerIsRegistered) {
            Log.d(TAG, "registerListener");
            this.listener = listener;
            //this.uploadEvents = uploadEvents;
            sensorManager.registerListener(this, accelerometer, Constant.SAMPLING_PERIOD);
            listenerIsRegistered = true;
        }
    }

    public void unregisterListener() {
        if (listenerIsRegistered) {
            Log.d(TAG, "unregisterListener");
            sensorManager.unregisterListener(this);
            if (isQuaking) {
                Log.d(TAG, "unregisterListener: Terminate last event");
                //if (uploadEvents) {
                    listener.terminateEvent();
                //}
                isQuaking = false;
            }
            listenerIsRegistered = false;
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

        return new MinimalEarthquakeEvent(event);
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

    private long millis = SystemClock.elapsedRealtime();

    private int valueCount;

    //private long startTime;

    private long lastTimestamp;
    private boolean isMajor = false;

    private EarthquakeEvent lastEarthquakeEvent;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // to ensure we have constant sampling period -> check timestamp between new and last
        // timestamp (SAMPLING_PERIOD is in microseconds and timestamp is in nanoseconds)
        if (sensorEvent.timestamp - lastTimestamp >= Constant.SAMPLING_PERIOD * 1000) {
            // do work every SAMPLING_PERIOD
            //[START_OF_WORK]
            performLowPassFilter(sensorEvent);
            // todo use one of the 2 following ways !!!
            // todo probably remove performLowPassFilter(...) abnormal behavior, not following the automato
            // MinimalEarthquakeEvent minimalEarthquakeEvent = performLowPassFilter(event);
            MinimalEarthquakeEvent minimalEarthquakeEvent = new MinimalEarthquakeEvent(sensorEvent, balanceValue);
            listener.onSensorChanged(sensorEvent, minimalEarthquakeEvent, acceleration);
            eventList.add(minimalEarthquakeEvent);
            if (eventList.size() == Constant.SAMPLES_BATCH_COUNT/* && uploadEvents*/) {
                float meanValue = MinimalEarthquakeEvent.getMeanValue(eventList);
                boolean possibleEarthquake = MinimalEarthquakeEvent.getIfPossibleEarthquake(eventList);
                if (meanValue > Constant.SENSOR_THRESHOLD /* todo && possibleEarthquake*/) {
                    if (!isQuaking) {
                        // add event
                        valueCount = 0;
                        // todo remove startTime = eventList.get(0).getTimeInMillis();
                        // todo add all measurements from MinimalEarthquakeEvent objects instead of only meanValue
                        Log.d(TAG, "onSensorChanged: new local event");
                        lastEarthquakeEvent = listener.createMinorEvent(eventList, meanValue);
                        isQuaking = true;
                    } else {
                        // update event
                        valueCount++;
                        long endTime = eventList.get(eventList.size() - 1).getTimeInMillis();
                        // updating the local minor active event
                        if (lastEarthquakeEvent != null) {
                            lastEarthquakeEvent.addSensorValue(meanValue);
                            lastEarthquakeEvent.setEndTime(endTime);
                        }
                        if (lastEarthquakeEvent != null && lastEarthquakeEvent.getDuration() >= Constant.MIN_EVENT_DURATION) {
                            if (!isMajor) {
                                // event is the first time to become major, will be added in major-active-events
                                listener.addEvent(lastEarthquakeEvent);
                                isMajor = true;
                            } else {
                                // updating the existing major event in firebase (in major-active-events)
                                listener.updateEvent(valueCount, meanValue, endTime);
                            }
                        } else {
                            Log.d(TAG, "onSensorChanged: updating event locally");
                        }
                    }
                } else {
                    if (isQuaking) {
                        // terminate event
                        if (isMajor) {
                            listener.terminateEvent();
                            isMajor = false;
                        } else {
                            Log.d(TAG, "onSensorChanged: terminating local event");
                        }
                        isQuaking = false;
                        lastEarthquakeEvent = null;
                    }
                }
                eventList.clear();
            }
            //[END_OF_WORK]
            lastTimestamp = sensorEvent.timestamp;
        }

        //listener.onSensorChanged(sensorEvent, minimalEarthquakeEvent, acceleration);
    }

    /*private void getNewEarthquakeEvent() {
        EarthquakeEvent earthquakeEvent = new EarthquakeEvent.Builder(eventList)
                .setDeviceId(deviceId)
                .addSensorValue(sensorValue)
                .setLatitude(locator.getLastLocation().getLatitude())
                .setLongitude(locator.getLastLocation().getLongitude())
                .build();
        eventHandler.addEventToMinors(earthquakeEvent);
    }*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged");
    }

    public interface OnEarthquakeListener {
        void onSensorChanged(SensorEvent sensorEvent, MinimalEarthquakeEvent minimalEarthquakeEvent, float acceleration);

        EarthquakeEvent createMinorEvent(List<MinimalEarthquakeEvent> eventList, float sensorValue);

        void addEvent(EarthquakeEvent earthquakeEvent);

        void updateEvent(int valueIndex, float sensorValue, long endTime);

        void terminateEvent();
    }
}