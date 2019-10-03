package com.unipi.lykourgoss.earthquakeobserver.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;
import com.unipi.lykourgoss.earthquakeobserver.client.tools.Util;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 14,July,2019.
 */

public class BootCompletedReceiver extends BroadcastReceiver {

    public static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "onReceive");
            Toast.makeText(context, TAG, Toast.LENGTH_SHORT).show();
            Util.scheduleObserverService(context);
        }
    }
}
