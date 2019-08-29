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
import com.unipi.lykourgoss.earthquakeobserver.entities.SensorInfo;
import com.unipi.lykourgoss.earthquakeobserver.entities.Device;
import com.unipi.lykourgoss.earthquakeobserver.tools.SharedPrefManager;
import com.unipi.lykourgoss.earthquakeobserver.tools.Util;
import com.unipi.lykourgoss.earthquakeobserver.tools.firebase.DatabaseHandler;

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_CONFIGURE_SENSOR = 9002;

    private FirebaseAuth firebaseAuth;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // todo remove
        startActivity(new Intent(this, MainActivity.class));

        findViewById(R.id.button_google_sign_in).setOnClickListener(this);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
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
                // updateUI(null);
            }
        } else if (requestCode == RC_CONFIGURE_SENSOR && resultCode == RESULT_OK) {
            SensorInfo sensorInfo = data.getParcelableExtra(Constant.EXTRA_SENSOR_INFO);
            addDeviceToFirebase(sensorInfo);
        }
    }

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
                            // Sign in success, open Sensor configuration activity to get
                            // balance sensor value
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

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

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
        DatabaseHandler databaseHandler = new DatabaseHandler(this, device.getDeviceId());
        databaseHandler.addDevice(device);
        final SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(SignInActivity.this);
        new CountDownTimer(5 * 1000, 1000){
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
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // updateUI(null);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_google_sign_in:
                signIn();
                break;
        }
    }
}
