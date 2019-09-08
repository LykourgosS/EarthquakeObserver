package com.unipi.lykourgoss.earthquakeobserver.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.R;
import com.unipi.lykourgoss.earthquakeobserver.client.models.User;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.dbhandlers.UserHandler;

public class SignInActivity extends BaseActivity implements View.OnClickListener, UserHandler.OnUserAddListener {

    private static final String TAG = "SignInActivity";

    private static final int RC_SIGN_IN = 9001;
    // todo remove private static final int RC_CONFIGURE_SENSOR = 9002;

    private FirebaseAuth firebaseAuth;

    private GoogleSignInClient googleSignInClient;

    private UserHandler userHandler;

    /**
     * On the sign in button clicked methods called accordingly:
     * 1. {@link #signIn()}
     * 2. {@link #onActivityResult(int, int, Intent)}
     * 3. {@link #firebaseAuthWithGoogle(GoogleSignInAccount)}
     * 4. {@link #onUserAdded(boolean)}
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        findViewById(R.id.button_google_sign_in).setOnClickListener(this);
        findViewById(R.id.button_sign_out).setOnClickListener(this);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        firebaseAuth = FirebaseAuth.getInstance();

        userHandler = new UserHandler(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        updateUi(firebaseAuth.getCurrentUser());
    }

    private void updateUi(FirebaseUser currentUser) {
        if (currentUser == null) {
            findViewById(R.id.button_google_sign_in).setVisibility(View.VISIBLE);
            findViewById(R.id.button_sign_out).setVisibility(View.GONE);
        } else {
            findViewById(R.id.button_google_sign_in).setVisibility(View.GONE);
            findViewById(R.id.button_sign_out).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Starts an activity (made by google) for result to select google account as a user for our
     * app, when account is selected successfully the onActivityResult is triggered.
     */
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned from launching Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.d(TAG, "Google sign in failed", e);
                updateUi(null);
            }
        }
    }

    /**
     * Sign in to Firebase Auth using the selected Google account.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    // add user to Firebase Database
                    FirebaseUser user = task.getResult().getUser();
                    userHandler.addUser(new User(user.getUid(), user.getEmail()));
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d(TAG, "signInWithCredential:failure", task.getException());
                    hideProgressDialog();
                    Snackbar.make(findViewById(R.id.button_google_sign_in), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteAndSignOut() {
        showProgressDialog();
        // delete user just created because it didn't added on Firebase Database too
        firebaseAuth.getCurrentUser().delete();
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressDialog();
                        updateUi(null);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_google_sign_in:
                signIn();
                break;
            case R.id.button_sign_out:
                deleteAndSignOut();
                break;
        }
    }

    @Override
    public void onUserAdded(boolean userAddedSuccessfully) {
        if (userAddedSuccessfully) {
            userHandler.checkIfUserIsAdmin(firebaseAuth.getCurrentUser().getEmail());
        } else {
            hideProgressDialog();
            // Sign In or Saving User object to Firebase Database got wrong, request signing in again
            deleteAndSignOut();
            Toast.makeText(this, "Something got wrong. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckIfAdmin(boolean isAdmin) {
        hideProgressDialog();
        SharedPrefManager.getInstance(this).write(Constant.USER_IS_ADMIN, isAdmin);
        // Sign in success, go to ConfigDeviceActivity to set up device to firebase
        startActivity(new Intent(SignInActivity.this, ConfigDeviceActivity.class));
        finish();
    }
}
