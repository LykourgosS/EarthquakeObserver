package com.unipi.lykourgoss.earthquakeobserver.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.client.services.ObserverService;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 11,September,2019.
 */

public class InternetDisconnectedReceiver extends BroadcastReceiver { // to take action app must be started at least once

    private static final String TAG = "InternetDisconnectedRec";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            Log.d(TAG, "onReceive");
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) { // checking if there is internet connection
                Toast.makeText(context, "Internet Disconnected", Toast.LENGTH_SHORT).show();
                context.stopService(new Intent(context, ObserverService.class));
                // reschedule job (to start again service) when there is internet connection again
                Util.scheduleObserverService(context);
            }
        }
    }
}
