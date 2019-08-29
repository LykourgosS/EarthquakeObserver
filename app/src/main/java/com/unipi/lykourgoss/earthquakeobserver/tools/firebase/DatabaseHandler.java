package com.unipi.lykourgoss.earthquakeobserver.tools.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.entities.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.entities.Device;
import com.unipi.lykourgoss.earthquakeobserver.tools.SharedPrefManager;

import java.util.Date;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 03,August,2019.
 */

public class DatabaseHandler {

    private static final String TAG = "DatabaseHandler";

    private static final String ACTIVE_EVENTS_REF = "active-events";
    private static final String SAVED_EVENTS_REF = "saved-events";
    private static final String DEVICES_REF = "devices";

    private Context context;

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private DatabaseReference activeEventsRef;
    private DatabaseReference savedEventsRef;
    private DatabaseReference devicesRef;

    private ValueEventListener listener = new MyValueEventListener();
    private Query query;

    public DatabaseHandler(Context context, String deviceId) {
        this.context = context;
        activeEventsRef = databaseReference.child(ACTIVE_EVENTS_REF).child(deviceId);
        savedEventsRef = databaseReference.child(SAVED_EVENTS_REF).child(deviceId);
        devicesRef = databaseReference.child(DEVICES_REF);
    }

    private void addListener() {
        query.addValueEventListener(listener);
    }

    private void removeListener() {
        query.removeEventListener(listener);
    }

    public void addEvent(EarthquakeEvent newEvent) {
        String eventId = activeEventsRef.push().getKey();
        newEvent.setEventId(eventId);
        activeEventsRef.setValue(newEvent).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Completed = " + task.isSuccessful());
            }
        });
    }

    public void updateEvent(final float sensorValue, final long endTime) {
        activeEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EarthquakeEvent event = dataSnapshot.getValue(EarthquakeEvent.class);
                event.addSensorValue(sensorValue);
                event.setEndTime(endTime);
                activeEventsRef.setValue(event);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void terminateEvent() {
        activeEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EarthquakeEvent event = dataSnapshot.getValue(EarthquakeEvent.class);
                savedEventsRef.child(event.getEventId()).setValue(event);
                activeEventsRef.setValue(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public void deleteSavedEvents() {
        savedEventsRef.setValue(null);
    }

    public void addDevice(Device device) {
        devicesRef.child(device.getDeviceId()).setValue(device).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // save to shared preferences that device added to Firebase, if not device cannot
                // work properly
                SharedPrefManager.getInstance(context).write(Constant.DEVICE_ADDED_TO_FIREBASE, task.isSuccessful());
            }
        });
    }

    public void updateDeviceStatus(final String deviceId, final boolean isStarted) {
        /*// update isRunning
        devicesRef.child(deviceId).child(Device.IS_RUNNING).setValue(isStarted);
        if (!isStarted) {
            // update lastObservingTimeInMillis only when the service stops to observe
            devicesRef.child(deviceId).child(Device.LAST_OBSERVING_TIME_IN_MILLIS).setValue(new Date().getTime());
        }*/

        devicesRef.child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Device device = dataSnapshot.getValue(Device.class);
                device.setRunning(isStarted);
                device.setLastObservingTimeInMillis(new Date().getTime());
                devicesRef.child(deviceId).setValue(device);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private class MyValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }
}
