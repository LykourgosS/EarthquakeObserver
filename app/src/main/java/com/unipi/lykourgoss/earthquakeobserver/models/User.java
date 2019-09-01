package com.unipi.lykourgoss.earthquakeobserver.models;

import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 31,August,2019.
 */

public class User {

    private String uid;

    private String email;

    private String displayName;

    private String fcmToken;

    private List<String> devices;

    public User() {
    }

    public User(String uid, String email, String displayName) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public List<String> getDevices() {
        return devices;
    }
}
