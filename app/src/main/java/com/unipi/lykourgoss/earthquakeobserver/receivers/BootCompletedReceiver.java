package com.unipi.lykourgoss.earthquakeobserver.receivers;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import com.unipi.lykourgoss.earthquakeobserver.Constant;
import com.unipi.lykourgoss.earthquakeobserver.Util;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 14,July,2019.
 */

public class BootCompletedReceiver extends BroadcastReceiver {

    public static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // todo remove custom action (added for debugging purposes)
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || Constant.FAKE_BOOT.equals(intent.getAction())) {
            Log.d(TAG, "onReceive");
            Toast.makeText(context, TAG, Toast.LENGTH_SHORT).show();
            Util.scheduleStartJob(context);
        }

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        String batteryStatusString = String.format("isCharging = %s\nusbCharge = %s\nacCharge = %s", isCharging, usbCharge, acCharge);
        new AlertDialog.Builder(context).setMessage(batteryStatusString).create().show();
    }
}
