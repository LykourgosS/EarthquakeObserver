package com.unipi.lykourgoss.earthquakeobserver.activities;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.unipi.lykourgoss.earthquakeobserver.tools.Util;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 27,August,2019.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String LOADING = "Loading";

    // todo maybe remove it
    protected String deviceId;

    protected ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceId = Util.getUniqueId(this);
    }

    protected void showProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(LOADING);
            dialog.setIndeterminate(true);
        }
        dialog.show();
    }

    protected void hideProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}
