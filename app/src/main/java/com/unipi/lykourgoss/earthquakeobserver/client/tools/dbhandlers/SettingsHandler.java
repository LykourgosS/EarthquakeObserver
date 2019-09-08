package com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Settings;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 08,September,2019.
 */

public class SettingsHandler {

    private static final String TAG = "SettingsHandler";

    private static final String SETTINGS_REF = "settings";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private static DatabaseReference settingsRef = databaseReference.child(SETTINGS_REF);

    private OnSettingsListener listener;

    public SettingsHandler(OnSettingsListener onSettingsListener) {
        this.listener = onSettingsListener;
    }

    public void fetchSettings() {
        settingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Settings settings = dataSnapshot.getValue(Settings.class);
                    listener.onSettingsFetched(settings);
                } else {
                    listener.onSettingsFetched(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onSettingsFetched(null);
            }
        });
    }

    public void updateSettings(Settings settings) {
        settingsRef.setValue(settings).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onSettingsUpdated(task.isSuccessful());
            }
        });
    }

    public interface OnSettingsListener {

        /**
         * Triggered when the {@link #fetchSettings()} is completed.
         *
         * @param settings Settings object fetched from Firebase.
         */
        void onSettingsFetched(Settings settings);

        /**
         * Triggered when the {@link #updateSettings(Settings)} is completed.
         *
         * @param successfullyUpdated shows if settings updated successfully.
         */
        void onSettingsUpdated(boolean successfullyUpdated);
    }
}
