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
import com.unipi.lykourgoss.earthquakeobserver.client.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 08,September,2019.
 */

public class UserHandler {

    private static final String TAG = "UserHandler";

    private static final String USERS_REF = "users";

    private static final String ADMINS_REF = "admins";

    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    private static DatabaseReference usersRef = databaseReference.child(USERS_REF);

    private static DatabaseReference adminsRef = databaseReference.child(ADMINS_REF);

    private OnUserAddListener listener;

    public UserHandler(OnUserAddListener onUserAddListener) {
        this.listener = onUserAddListener;
    }

    public void addUser(final User user) {
        Log.d(TAG, "addUser");
        Map<String, Object> userUpdates = new HashMap<>();

        userUpdates.put(User.UID, user.getUid());

        userUpdates.put(User.EMAIL, user.getEmail());

        usersRef.child(user.getUid()).updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                listener.onUserAdded(task.isSuccessful());
            }
        });
    }

    public void checkIfUserIsAdmin(final String email) {
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> admins = (ArrayList) dataSnapshot.getValue();
                    listener.onCheckIfAdmin(admins.contains(email));
                } else {
                    listener.onCheckIfAdmin(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onCheckIfAdmin(false);
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
}
