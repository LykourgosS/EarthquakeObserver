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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.models.Device;
import com.unipi.lykourgoss.earthquakeobserver.models.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.models.User;
import com.unipi.lykourgoss.earthquakeobserver.tools.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 03,August,2019.
 */

public class DatabaseHandler {

    private static final String TAG = "DatabaseHandler";

    private static final String ACTIVE_EVENTS_REF = "active-events";
    private static final String SAVED_EVENTS_REF = "saved-events";
    private static final String DEVICES_REF = "devices";
    private static final String USER_REF = "users";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private DatabaseReference activeEventsRef;
    private DatabaseReference savedEventsRef;
    private DatabaseReference devicesRef;
    private DatabaseReference usersRef;

    private DatabaseListener databaseListener;

    /*private ValueEventListener listener = new MyValueEventListener();
    private Query query;*/

    public DatabaseHandler(String deviceId) {
        activeEventsRef = databaseReference.child(ACTIVE_EVENTS_REF).child(deviceId);
        savedEventsRef = databaseReference.child(SAVED_EVENTS_REF).child(deviceId);
        devicesRef = databaseReference.child(DEVICES_REF);
        usersRef = databaseReference.child(USER_REF);
    }

    public void setDatabaseListener(DatabaseListener databaseListener) {
        this.databaseListener = databaseListener;
    }

    /*private void addListener() {
        query.addValueEventListener(listener);
    }

    private void removeListener() {
        query.removeEventListener(listener);
    }*/

    public void addEvent(EarthquakeEvent newEvent) {
        Log.d(TAG, "addEvent: value = " + newEvent.getSensorValues().get(0));
        String eventId = activeEventsRef.push().getKey();
        newEvent.setEventId(eventId);
        activeEventsRef.setValue(newEvent).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Completed = " + task.isSuccessful());
            }
        });
    }

    public void updateEvent(int valueIndex, float sensorValue, long endTime) {
        Log.d(TAG, "updateEvent: value = " + sensorValue);
        Map<String, Object> eventUpdates = new HashMap<>();

        String valuePath = "/" + EarthquakeEvent.SENSOR_VALUES + "/" + valueIndex;
        eventUpdates.put(valuePath, sensorValue);

        String endTimePath = "/" + EarthquakeEvent.END_TIME;
        eventUpdates.put(endTimePath, endTime);

        String endDateTimePath = "/" + EarthquakeEvent.END_DATE_TIME;
        eventUpdates.put(endDateTimePath, Util.millisToDateTime(endTime));

        activeEventsRef.updateChildren(eventUpdates);

        /*activeEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        });*/
    }

    public void terminateEvent() {
        Log.d(TAG, "terminateEvent");
        activeEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EarthquakeEvent event = dataSnapshot.getValue(EarthquakeEvent.class);
                if (event.getDuration() > Constant.MIN_EVENT_DURATION) {
                    Log.d(TAG, "terminateEvent: onDataChange: event saved");
                    savedEventsRef.child(event.getEventId()).setValue(event);
                }
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

    public void addUser(final User user) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    user.setFcmToken(task.getResult().getToken());
                    usersRef.child(user.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            databaseListener.onUserAdded(task.isSuccessful());
                        }
                    });
                } else {
                    // something went wrong while acquiring FCM Token
                    databaseListener.onUserAdded(task.isSuccessful());
                }
            }
        });
    }

    public interface DatabaseListener {
        /**
         * Triggered when the {@link #addUser(User)} is completed.
         *
         * @param userAddedSuccessfully shows if the used added successfully.
         * */
        void onUserAdded(boolean userAddedSuccessfully);

        /**
         * Triggered when the {@link #addDevice(Device)} )} is completed.
         *
         * @param deviceAddedSuccessfully shows if the device added successfully.
         * */
        void onDeviceAdded(boolean deviceAddedSuccessfully);
    }


    public void addDevice(Device device) {
        devicesRef.child(device.getDeviceId()).setValue(device).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseListener.onDeviceAdded(task.isSuccessful());
                // save to shared preferences that device added to Firebase, if not device cannot
                // work properly
                // SharedPrefManager.getInstance(context).write(Constant.DEVICE_ADDED_TO_FIREBASE, task.isSuccessful());
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

        Log.d(TAG, "updateDeviceStatus");
        Map<String, Object> deviceUpdates = new HashMap<>();

        String isRunningPath = "/" + Device.IS_RUNNING;
        deviceUpdates.put(isRunningPath, isStarted);

        long millis = new Date().getTime();

        String millisPath = "/" + Device.LAST_OBSERVING_TIME_IN_MILLIS;
        deviceUpdates.put(millisPath, millis);

        String dateTimePath = "/" + Device.LAST_OBSERVING_DATE_TIME;
        deviceUpdates.put(dateTimePath, Util.millisToDateTime(millis));

        devicesRef.child(deviceId).updateChildren(deviceUpdates);

        /*devicesRef.child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        });*/
    }

    /*private class MyValueEventListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }*/
}
