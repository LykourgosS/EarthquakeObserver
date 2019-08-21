package com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.broadcastreceiver.b_DynamicReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 13,July,2019.
 */

public class ChargingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) { // getAction() might be null thus we don't use it to call equals()
            Toast.makeText(context, "Charger Connected", Toast.LENGTH_SHORT).show();
        }

        if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
            Toast.makeText(context, "Charger Disconnected", Toast.LENGTH_SHORT).show();
        }
    }
}
