package com.unipi.lykourgoss.earthquakeobserver.tools;

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

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 03,August,2019.
 */

public class FirebaseHandler {

    private static final String TAG = "FirebaseHandler";

    private static final String ACTIVE_EVENTS_REF = "active-events";
    private static final String SAVED_EVENTS_REF = "saved-events";
    
    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    
    private DatabaseReference activeEventsRef;
    private DatabaseReference savedEventsRef;
    
    private ValueEventListener listener = new MyValueEventListener();
    private Query query;

    public FirebaseHandler(String deviceId) {
        activeEventsRef = databaseReference.child(ACTIVE_EVENTS_REF).child(deviceId);
        savedEventsRef = databaseReference.child(SAVED_EVENTS_REF).child(deviceId);
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
                Log.d(TAG, "onComplete");
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
        saveEvent();

    }

    private void saveEvent() {
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

    private class MyValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }
}
