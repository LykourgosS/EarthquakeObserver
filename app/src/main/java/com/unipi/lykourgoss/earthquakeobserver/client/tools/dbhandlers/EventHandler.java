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

    private static final String MINOR_ACTIVE_EVENTS_REF = "minor-active-events";
    private static final String MAJOR_ACTIVE_EVENTS_REF = "major-active-events";

    private static final String SAVED_EVENTS_REF = "saved-events";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private DatabaseReference minorActiveEventRef;
    private DatabaseReference majorActiveEventRef;

    private DatabaseReference savedEventsRef;

    public EventHandler(String deviceId) {
        minorActiveEventRef = databaseReference.child(MINOR_ACTIVE_EVENTS_REF).child(deviceId);
        majorActiveEventRef = databaseReference.child(MAJOR_ACTIVE_EVENTS_REF).child(deviceId);
        savedEventsRef = databaseReference.child(SAVED_EVENTS_REF).child(deviceId);
    }

    public void addEventToMinors(EarthquakeEvent newEvent) {
        Log.d(TAG, "addEvent: minor");
        String eventId = minorActiveEventRef.push().getKey();
        newEvent.setEventId(eventId);
        // todo remove listener
        minorActiveEventRef.setValue(newEvent).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Completed = " + task.isSuccessful());
            }
        });
    }

    public void addEventToMajors() {
        Log.d(TAG, "addEventToMajors: event is now major");
        minorActiveEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    EarthquakeEvent event = dataSnapshot.getValue(EarthquakeEvent.class);
                    // add the event from minor-events to major-events
                    majorActiveEventRef.setValue(event);
                    // delete the event that moved from minor-events to major-events
                    minorActiveEventRef.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void updateEvent(boolean isMajor, int valueIndex, float sensorValue, long endTime) {
        Log.d(TAG, "updateEvent: isMajor = " + isMajor);
        Map<String, Object> eventUpdates = new HashMap<>();

        String valuePath = EarthquakeEvent.SENSOR_VALUES + "/" + valueIndex;
        eventUpdates.put(valuePath, sensorValue);

        eventUpdates.put(EarthquakeEvent.END_TIME, endTime);

        eventUpdates.put(EarthquakeEvent.END_DATE_TIME, Util.millisToDateTime(endTime));

        if (isMajor) {
            majorActiveEventRef.updateChildren(eventUpdates);
        } else {
            minorActiveEventRef.updateChildren(eventUpdates);
        }
    }

    public void terminateEvent(boolean isMajor) {
        Log.d(TAG, "terminateEvent: isMajor = " + isMajor);
        if (isMajor) {
            majorActiveEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        EarthquakeEvent event = dataSnapshot.getValue(EarthquakeEvent.class);
                        Log.d(TAG, "terminateEvent: onDataChange: event saved");
                        savedEventsRef.child(event.getEventId()).setValue(event);
                        majorActiveEventRef.setValue(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            minorActiveEventRef.setValue(null);
        }
    }

    public void deleteSavedEvents() {
        savedEventsRef.setValue(null);
    }
}
