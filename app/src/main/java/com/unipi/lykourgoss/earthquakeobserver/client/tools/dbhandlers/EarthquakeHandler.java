package com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unipi.lykourgoss.earthquakeobserver.client.models.Earthquake;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 08,September,2019.
 */

public class EarthquakeHandler {

    private static final String TAG = "EarthquakeHandler";

    private static final String EARTHQUAKES_REF = "earthquakes";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private static DatabaseReference earthquakesRef = databaseReference.child(EARTHQUAKES_REF);

    private OnEarthquakeFetchListener listener;

    public EarthquakeHandler(OnEarthquakeFetchListener onEarthquakeFetchListener) {
        this.listener = onEarthquakeFetchListener;
    }

    public void getEarthquake(String id) {
        earthquakesRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listener.onEarthquakeFetched(dataSnapshot.getValue(Earthquake.class));
                } else {
                    listener.onEarthquakeFetched(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onEarthquakeFetched(null);
            }
        });
    }

    public interface OnEarthquakeFetchListener {
        /**
         * Triggered when the {@link #getEarthquake(String)} is completed.
         *
         * @param earthquake the Earthquake object fetched from Firebase.
         */
        void onEarthquakeFetched(Earthquake earthquake);
    }
}
