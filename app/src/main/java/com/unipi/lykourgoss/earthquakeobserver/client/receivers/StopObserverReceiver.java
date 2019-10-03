package com.unipi.lykourgoss.earthquakeobserver.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.services.ObserverService;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 15,July,2019.
 */

public class StopObserverReceiver extends BroadcastReceiver { // to take action app must be started at least once

    private static final String TAG = "StopObserverReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent.getAction());
        // getAction() might be null thus we don't use it to call equals()
        if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) { // Check if power disconnected

            Log.d(TAG, "onReceive: Power Disconnected");

            context.stopService(new Intent(context, ObserverService.class));
            // reschedule job (to start again service) when the power is connected again
            Util.scheduleObserverService(context);

        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) { // Check internet connectivity

            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) { // checking if there is internet connection
                Log.d(TAG, "onReceive: Internet Disconnected");

                context.stopService(new Intent(context, ObserverService.class));
                // reschedule job (to start again service) when there is internet connection again
                Util.scheduleObserverService(context);
            }
        }
    }
}
