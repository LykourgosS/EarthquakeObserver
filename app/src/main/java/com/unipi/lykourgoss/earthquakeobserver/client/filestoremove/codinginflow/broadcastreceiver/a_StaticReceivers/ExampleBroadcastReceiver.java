package com.unipi.lykourgoss.earthquakeobserver.client.filestoremove.codinginflow.broadcastreceiver.a_StaticReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 12,July,2019.
 */

public class ExampleBroadcastReceiver extends BroadcastReceiver { // to take action app must be started at least once

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) { // getAction might be null thus we don't use it to call equals()
            Toast.makeText(context, "Boot completed", Toast.LENGTH_SHORT).show();
        }

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            Toast.makeText(context, "Connectivity changed", Toast.LENGTH_SHORT).show();
        }
    }
}
