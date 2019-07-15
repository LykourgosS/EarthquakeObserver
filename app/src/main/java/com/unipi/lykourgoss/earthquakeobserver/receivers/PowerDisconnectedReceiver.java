package com.unipi.lykourgoss.earthquakeobserver.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.services.ObserverService;
import com.unipi.lykourgoss.earthquakeobserver.Util;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 15,July,2019.
 */

public class PowerDisconnectedReceiver extends BroadcastReceiver { // to take action app must be started at least once

    public static final String TAG = "PWRDisconnectedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // todo remove custom action (added for debugging purposes)
        if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())
                || Constant.FAKE_POWER_DISCONNECTED.equals(intent.getAction())) { // getAction() might be null thus we don't use it to call equals()
            Log.d(TAG, "onReceive");
            Toast.makeText(context, "Power Disconnected", Toast.LENGTH_SHORT).show();

            context.stopService(new Intent(context, ObserverService.class));
            Util.scheduleStartJob(context); // reschedule job (to start again service) when the power is connected again
        }
    }
}
