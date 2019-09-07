package com.unipi.lykourgoss.earthquakeobserver.client.tools;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.ClientSettings;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Device;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Earthquake;
import com.unipi.lykourgoss.earthquakeobserver.client.models.EarthquakeEvent;
import com.unipi.lykourgoss.earthquakeobserver.client.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 03,August,2019.
 */

public class DatabaseHandler {

    private static final String TAG = "DatabaseHandler";

    private static final String MINOR_ACTIVE_EVENTS_REF = "minor-active-events";
    private static final String MAJOR_ACTIVE_EVENTS_REF = "major-active-events";

    private static final String SAVED_EVENTS_REF = "saved-events";

    private static final String EARTHQUAKES_REF = "earthquakes";

    private static final String DEVICES_REF = "devices";
    private static final String USERS_REF = "users";

    private static final String ADMINS_REF = "admins";

    private static final String CLIENT_SETTINGS_REF = "client-settings";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private DatabaseReference minorActiveEventRef;
    private DatabaseReference majorActiveEventRef;

    private DatabaseReference savedEventsRef;

    private DatabaseReference earthquakesRef;

    private DatabaseReference devicesRef;
    private DatabaseReference usersRef;

    private DatabaseReference adminsRef;

    private DatabaseReference clientSettingsRef;

    private OnUserAddListener onUserAddListener;
    private OnDeviceAddListener onDeviceAddListener;
    private OnEarthquakeFetchListener onEarthquakeFetchListener;
    private OnUpdateSettingsListener onUpdateSettingsListener;

    /*private ValueEventListener listener = new MyValueEventListener();
    private Query query;*/

    public DatabaseHandler(String deviceId) {
        minorActiveEventRef = databaseReference.child(MINOR_ACTIVE_EVENTS_REF).child(deviceId);
        majorActiveEventRef = databaseReference.child(MAJOR_ACTIVE_EVENTS_REF).child(deviceId);
        earthquakesRef = databaseReference.child(EARTHQUAKES_REF);
        savedEventsRef = databaseReference.child(SAVED_EVENTS_REF).child(deviceId);
        devicesRef = databaseReference.child(DEVICES_REF);
        usersRef = databaseReference.child(USERS_REF);
        adminsRef = databaseReference.child(ADMINS_REF);
        clientSettingsRef = databaseReference.child(CLIENT_SETTINGS_REF);
    }

    public void addOnUserAddListener(OnUserAddListener onUserAddListener) {
        this.onUserAddListener = onUserAddListener;
    }

    public void addOnDeviceAddListener(OnDeviceAddListener onDeviceAddListener) {
        this.onDeviceAddListener = onDeviceAddListener;
    }

    public void addOnEarthquakeFetchListener(OnEarthquakeFetchListener onEarthquakeFetchListener) {
        this.onEarthquakeFetchListener = onEarthquakeFetchListener;
    }

    public void addOnUpdateSettingsListener(OnUpdateSettingsListener onUpdateSettingsListener) {
        this.onUpdateSettingsListener = onUpdateSettingsListener;
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

        /*minorActiveEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EarthquakeEvent event = dataSnapshot.getValue(EarthquakeEvent.class);
                event.addSensorValue(sensorValue);
                event.setEndTime(endTime);
                minorActiveEventRef.setValue(event);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
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

    public void getEarthquake(String id) {
        earthquakesRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    onEarthquakeFetchListener.onEarthquakeFetched(dataSnapshot.getValue(Earthquake.class));
                } else {
                    onEarthquakeFetchListener.onEarthquakeFetched(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onEarthquakeFetchListener.onEarthquakeFetched(null);
            }
        });
    }

    public void deleteSavedEvents() {
        savedEventsRef.setValue(null);
    }

    public void checkIfUserIsAdmin(final String email) {
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> admins = (ArrayList) dataSnapshot.getValue();
                    onUserAddListener.onCheckIfAdmin(admins.contains(email));
                } else {
                    onUserAddListener.onCheckIfAdmin(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onUserAddListener.onCheckIfAdmin(false);
            }
        });
    }

    // TODO: 09/06/2019 remove comments from previous methods states

    public void addUser(final User user) {

        // todo is it ok???
        // Subscribe to topic in FCM
        /*FirebaseMessaging.getInstance().subscribeToTopic(Constant.EARTHQUAKES_FEED_TOPIC).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // user.setFcmToken(task.getResult().getToken());
                    usersRef.child(user.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            databaseListener.onUserAdded(task.isSuccessful());
                        }
                    });
                } else {
                    // something went wrong while subscribing to FCM topic
                    databaseListener.onUserAdded(task.isSuccessful());
                }
            }
        });*/

        // first get FCM token and then add user to Firebase Database
        /*FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
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
        });*/

        Log.d(TAG, "addUser");
        Map<String, Object> userUpdates = new HashMap<>();

        userUpdates.put(User.UID, user.getUid());

        userUpdates.put(User.EMAIL, user.getEmail());

        usersRef.child(user.getUid()).updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onUserAddListener.onUserAdded(task.isSuccessful());
            }
        });

        /*usersRef.child(user.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseListener.onUserAdded(task.isSuccessful());
            }
        });*/
    }

    /**
     * Adds device to Firebase Database in two paths, 1st path is under: /devices. 2nd one is
     * under: /users/{uid}/devices.
     */
    public void addDevice(final Device device) {
        Log.d(TAG, "addDevice");
        final Map<String, Object> deviceAddition = new HashMap<>();

        // put the value true on path: /users/{firebaseAuthUid()}/devices/{deviceId()}
        String userPath = usersRef.child(device.getFirebaseAuthUid()).child(User.DEVICES).child(device.getDeviceId()).getPath().toString();
        deviceAddition.put(userPath, true);

        // put the Device object device on path: /devices/{deviceId}
        String devicesPath = devicesRef.child(device.getDeviceId()).getPath().toString();
        deviceAddition.put(devicesPath, device);

        FirebaseMessaging.getInstance().subscribeToTopic(Constant.EARTHQUAKES_FEED_TOPIC).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: subscribeToTopic = " + task.isSuccessful());
                if (task.isSuccessful()) {
                    databaseReference.updateChildren(deviceAddition).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            onDeviceAddListener.onDeviceAdded(task.isSuccessful());
                        }
                    });
                } else {
                    // something went wrong while subscribing to FCM topic
                    onDeviceAddListener.onDeviceAdded(task.isSuccessful());
                }
            }
        });
    }

    public void updateDeviceStatus(final String deviceId, final boolean isStarted) {
        Log.d(TAG, "updateDeviceStatus");
        Map<String, Object> deviceUpdates = new HashMap<>();

        // todo see if ok with following comment
        //String isRunningPath = "/" + Device.IS_RUNNING;
        deviceUpdates.put(Device.IS_RUNNING, isStarted);

        long millis = new Date().getTime();

        //String millisPath = "/" + Device.LAST_OBSERVING_TIME_IN_MILLIS;
        deviceUpdates.put(Device.LAST_OBSERVING_TIME_IN_MILLIS, millis);

        //String dateTimePath = "/" + Device.LAST_OBSERVING_DATE_TIME;
        deviceUpdates.put(Device.LAST_OBSERVING_DATE_TIME, Util.millisToDateTime(millis));

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

    public void getSettings() {
        clientSettingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ClientSettings settings = dataSnapshot.getValue(ClientSettings.class);
                    onUpdateSettingsListener.onSettingsUpdate(settings);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public interface OnUserAddListener {
        /**
         * Triggered when the {@link #addUser(User)} is completed.
         *
         * @param userAddedSuccessfully shows if the user added successfully or not.
         */
        void onUserAdded(boolean userAddedSuccessfully);

        /**
         * Triggered when the {@link #checkIfUserIsAdmin(String)} is completed.
         *
         * @param isAdmin shows if the user added is an admin.
         */
        void onCheckIfAdmin(boolean isAdmin);
    }

    public interface OnDeviceAddListener {
        /**
         * Triggered when the {@link #addDevice(Device)} )} is completed.
         *
         * @param deviceAddedSuccessfully shows if the device added successfully or not.
         */
        void onDeviceAdded(boolean deviceAddedSuccessfully);
    }

    public interface OnEarthquakeFetchListener {
        /**
         * Triggered when the {@link #getEarthquake(String)} is completed.
         *
         * @param earthquake the Earthquake object fetched from Firebase.
         */
        void onEarthquakeFetched(Earthquake earthquake);
    }

    public interface OnUpdateSettingsListener {
        /**
         * Triggered when the {@link #getSettings()} is completed.
         *
         * @param settings ClientSettings object fetched from Firebase.
         */
        void onSettingsUpdate(ClientSettings settings);
    }
}
