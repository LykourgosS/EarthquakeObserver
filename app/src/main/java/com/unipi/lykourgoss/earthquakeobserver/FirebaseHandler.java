package com.unipi.lykourgoss.earthquakeobserver;

import android.hardware.SensorEvent;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 03,August,2019.
 */

public class FirebaseHandler {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Constant.EVENT_FIREBASE_REF);
    private Query query;
    private ValueEventListener listener = new MyValueEventListener();

    public FirebaseHandler(/*DatabaseReference databaseReference, Query query*/) {
//        this.databaseReference
//        this.query = query;
    }

    private void addListener() {
        query.addValueEventListener(listener);
    }

    private void removeListener() {
        query.removeEventListener(listener);
    }

    public void addEvent(EarthquakeEvent event) {
        String id = databaseReference.push().getKey();
        event.setId(id);
        databaseReference.child(id).setValue(event);
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
