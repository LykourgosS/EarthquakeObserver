package com.unipi.lykourgoss.earthquakeobserver.models;

import java.util.Map;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 31,August,2019.
 */

public class User {

    public static final String DEVICES = "devices";

    private String uid;

    private String email;

    private String fcmToken;

    private Map<String, Boolean> devices;

    public User() {
    }

    public User(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Map<String, Boolean> getDevices() {
        return devices;
    }
}
