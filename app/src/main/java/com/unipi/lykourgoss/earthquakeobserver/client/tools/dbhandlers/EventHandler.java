package com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.lykourgoss.earthquakeobserver.client.models.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 08,September,2019.
 */

public class EventHandler {

    private static final String TAG = "EventHandler";

    private static final String ACTIVE_EVENTS_REF = "major-active-events"; // todo remove major keyword

    private static final String SAVED_EVENTS_REF = "saved-events";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private DatabaseReference activeEventRef;

    private DatabaseReference savedEventsRef;

    public EventHandler(String deviceId) {
        activeEventRef = databaseReference.child(ACTIVE_EVENTS_REF).child(deviceId);
        savedEventsRef = databaseReference.child(SAVED_EVENTS_REF).child(deviceId);
    }

    public void addEvent(EarthquakeEvent event) {
        Log.d(TAG, "addEvent");
        String eventId = activeEventRef.push().getKey();
        event.setEventId(eventId);
        activeEventRef.setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "addEvent completed = " + task.isSuccessful());
            }
        });
    }

    public void updateEvent(int valueIndex, float sensorValue, long endTime) {
        Log.d(TAG, "updateEvent");
        Map<String, Object> eventUpdates = new HashMap<>();

        String valuePath = EarthquakeEvent.SENSOR_VALUES + "/" + valueIndex;
        eventUpdates.put(valuePath, sensorValue);

        eventUpdates.put(EarthquakeEvent.END_TIME, endTime);

        eventUpdates.put(EarthquakeEvent.END_DATE_TIME, Util.millisToDateTime(endTime));

        activeEventRef.updateChildren(eventUpdates);
    }

    public void terminateEvent() {
        Log.d(TAG, "terminateEvent");
        activeEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    EarthquakeEvent event = dataSnapshot.getValue(EarthquakeEvent.class);
                    Log.d(TAG, "terminateEvent completed");
                    if (event.getEventId() != null) {
                        // if sampling period is too small or network's connection is slow,
                        // checking that the event has been saved successfully for the first time
                        // (and not just updated, which means will have only endTime, endDatetime,
                        // and a sensor value) in active-events
                        savedEventsRef.child(event.getEventId()).setValue(event);
                    }
                    activeEventRef.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void deleteSavedEvents() {
        savedEventsRef.setValue(null);
    }
}
