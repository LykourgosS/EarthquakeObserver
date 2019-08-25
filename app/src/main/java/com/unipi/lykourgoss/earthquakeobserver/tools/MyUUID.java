package com.unipi.lykourgoss.earthquakeobserver.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.unipi.lykourgoss.earthquakeobserver.Constant;

import java.util.UUID;


/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 31,July,2019.
 */

public class MyUUID {

    // todo add the following to Util class
    public synchronized static String getUuid(Context context) {
        SharedPrefManager manager = SharedPrefManager.getInstance(context);
        String uuid = manager.read(Constant.PREF_UNIQUE_ID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();

            manager.write(Constant.PREF_UNIQUE_ID, uuid);
        }
        return uuid;
    }
}
