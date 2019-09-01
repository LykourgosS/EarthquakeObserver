package com.unipi.lykourgoss.earthquakeobserver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.R;
import com.unipi.lykourgoss.earthquakeobserver.models.Device;
import com.unipi.lykourgoss.earthquakeobserver.models.SensorInfo;
import com.unipi.lykourgoss.earthquakeobserver.models.User;
import com.unipi.lykourgoss.earthquakeobserver.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.DatabaseHandler;

public class SignInActivity extends BaseActivity implements View.OnClickListener, DatabaseHandler.DatabaseListener {

    private static final String TAG = "SignInActivity";

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_CONFIGURE_SENSOR = 9002;

    private FirebaseAuth firebaseAuth;

    private GoogleSignInClient googleSignInClient;

    private DatabaseHandler databaseHandler;

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

        databaseHandler = new DatabaseHandler(Util.getUniqueId(this));
        databaseHandler.setDatabaseListener(this);
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

    // on the sign in button clicked methods called accordingly:
    // {@link signIn() -> onActivityResult(...) -> firebaseAuthWithGoogle(...) ->

    /*
     * On the sign in button clicked methods called accordingly:
     *  {@link #signIn()} -> onActivityResult(...) -> firebaseAuthWithGoogle(...) ->
     * */
    private void doI() {

    }

    /**
     * Starts an activity (made by google) for result to select google account as a user for our
     * app, when account is selected successfully the onActivityResult is triggered. On the sign in
     * button clicked methods called accordingly:
     * 1. {@link #signIn()}
     * 2. {@link #onActivityResult(int, int, Intent)}
     * 3. {@link #firebaseAuthWithGoogle(GoogleSignInAccount)}
     * 4. {@link #onUserAdded(boolean)} + {@link #configureSensor()}
     * 5. {@link #onActivityResult(int, int, Intent)}
     * 6. {@link #addDeviceToFirebase(SensorInfo)} + {@link #onDeviceAdded(boolean)}
     * */
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned from launching Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
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
        } else if (requestCode == RC_CONFIGURE_SENSOR && resultCode == RESULT_OK) {
            SensorInfo sensorInfo = data.getParcelableExtra(Constant.EXTRA_SENSOR_INFO);
            addDeviceToFirebase(sensorInfo);
        }
    }

    /**
     * Sign in to Firebase Auth using the selected Google account.
     * */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            // add user to Firebase Database
                            FirebaseUser user = task.getResult().getUser();
                            databaseHandler.addUser(new User(user.getUid(), user.getEmail(), user.getDisplayName()));
                            // Sign in success, open Sensor configuration activity to get balance
                            // sensor value
                            configureSensor();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.button_google_sign_in), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    /**
     * Start SensorConfigurationActivity waiting for a result
     * */
    private void configureSensor() {
        Intent intent = new Intent(this, SensorConfigurationActivity.class);
        startActivityForResult(intent, RC_CONFIGURE_SENSOR);
    }

    private void addDeviceToFirebase(SensorInfo sensorInfo) {
        showProgressDialog();
        Device device = new Device.Builder()
                .setDeviceId(Util.getUniqueId(this))
                .setFirebaseAuthUid(firebaseAuth.getCurrentUser().getUid())
                .setSensorInfo(sensorInfo)
                .build();
        databaseHandler.addDevice(device);
        final SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(SignInActivity.this);
        new CountDownTimer(5 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                boolean deviceAddedToFirebase = sharedPrefManager.read(Constant.DEVICE_ADDED_TO_FIREBASE, false);
                if (deviceAddedToFirebase) {
                    this.cancel();
                    Log.d(TAG, "onTick: device added successfully");
                    // sharedPrefManager.write()
                    hideProgressDialog();
                    finish();
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                }
            }

            @Override
            public void onFinish() {
                hideProgressDialog();
                boolean deviceAddedToFirebase = sharedPrefManager.read(Constant.DEVICE_ADDED_TO_FIREBASE, false);
                if (!deviceAddedToFirebase) {
                    Log.d(TAG, "onFinish: user deleted");
                    firebaseAuth.getCurrentUser().delete();
                    signOut();
                    Toast.makeText(SignInActivity.this, "Error while creating account. \nTry again.", Toast.LENGTH_SHORT).show();
                }

            }
        }.start();
    }

    private void signOut() {

        firebaseAuth.getCurrentUser().delete();
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
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
                signOut();
                break;
        }
    }

    @Override
    public void onUserAdded(boolean userAddedSuccessfully) {
        hideProgressDialog();
    }

    @Override
    public void onDeviceAdded(boolean deviceAddedSuccessfully) {

    }
}
