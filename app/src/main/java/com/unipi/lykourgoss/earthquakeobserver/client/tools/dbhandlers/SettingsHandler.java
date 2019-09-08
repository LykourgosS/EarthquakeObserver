package com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers;

import androidx.annotation.NonNull;

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

    private OnUpdateSettingsListener listener;

    public SettingsHandler(OnUpdateSettingsListener onUpdateSettingsListener) {
        this.listener = onUpdateSettingsListener;
    }

    public void getSettings() {
        settingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Settings settings = dataSnapshot.getValue(Settings.class);
                    listener.onSettingsUpdate(settings);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public interface OnUpdateSettingsListener {
        /**
         * Triggered when the {@link #getSettings()} is completed.
         *
         * @param settings Settings object fetched from Firebase.
         */
        void onSettingsUpdate(Settings settings);
    }
}
