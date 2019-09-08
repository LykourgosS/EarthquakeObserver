package com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Device;
import com.unipi.lykourgoss.earthquakeobserver.client.models.User;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 08,September,2019.
 */

public class DeviceHandler {

    private static final String TAG = "DeviceHandler";

    private static final String DEVICES_REF = "devices";
    private static final String USERS_REF = "users";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private static DatabaseReference devicesRef = databaseReference.child(DEVICES_REF);
    private static DatabaseReference usersRef = databaseReference.child(USERS_REF);

    private OnDeviceAddListener listener;

    public DeviceHandler(OnDeviceAddListener onDeviceAddListener) {
        this.listener = onDeviceAddListener;
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
                            listener.onDeviceAdded(task.isSuccessful());
                        }
                    });
                } else {
                    // something went wrong while subscribing to FCM topic
                    listener.onDeviceAdded(task.isSuccessful());
                }
            }
        });
    }

    public static void updateDeviceStatus(final String deviceId, final boolean isStarted) {
        Log.d(TAG, "updateDeviceStatus");
        Map<String, Object> deviceUpdates = new HashMap<>();

        deviceUpdates.put(Device.IS_RUNNING, isStarted);

        long millis = new Date().getTime();
        deviceUpdates.put(Device.LAST_OBSERVING_TIME_IN_MILLIS, millis);

        deviceUpdates.put(Device.LAST_OBSERVING_DATE_TIME, Util.millisToDateTime(millis));

        devicesRef.child(deviceId).updateChildren(deviceUpdates);
    }

    public interface OnDeviceAddListener {
        /**
         * Triggered when the {@link #addDevice(Device)} )} is completed.
         *
         * @param deviceAddedSuccessfully shows if the device added successfully or not.
         */
        void onDeviceAdded(boolean deviceAddedSuccessfully);
    }
}
