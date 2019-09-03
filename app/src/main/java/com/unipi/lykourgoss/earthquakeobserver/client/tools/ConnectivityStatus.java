package com.unipi.lykourgoss.earthquakeobserver.client.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 27,August,2019.
 */

public class ConnectivityStatus {

    private static ConnectivityStatus instance = new ConnectivityStatus();
    static Context context;
    ConnectivityManager connectivityManager;
    boolean connected = false;

    public static ConnectivityStatus getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            return connected;

        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }
}
