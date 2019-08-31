package com.unipi.lykourgoss.earthquakeobserver.entities;

import java.util.List;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 31,August,2019.
 */

public class User {

    private String uid;

    private String email;

    private String displayName;

    private List<String> deviceList;

    public User() {
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

    public List<String> getDeviceList() {
        return deviceList;
    }
}
