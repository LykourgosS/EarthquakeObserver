package com.unipi.lykourgoss.earthquakeobserver.filestoremove.codinginflow.broadcastreceiver.e_OrderedBroadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 14,July,2019.
 */

public class OrderedReceiver1 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "OrderedReceiver1 triggered", Toast.LENGTH_SHORT).show();
    }
}
