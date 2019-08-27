package com.unipi.lykourgoss.earthquakeobserver.tools.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 27,August,2019.
 */

public class AuthHandler {

    private static AuthHandler authHandler;

    private FirebaseAuth firebaseAuth;

    private AuthHandler() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public static AuthHandler getInstance() {
        if (authHandler == null) {
            authHandler = new AuthHandler();
        }
        return authHandler;
    }

    public FirebaseUser getCurrentUser() {
        return this.firebaseAuth.getCurrentUser();
    }
}
