package com.unipi.lykourgoss.earthquakeobserver.client.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 18,August,2019.
 */

public class SharedPrefManager {

    private static SharedPrefManager manager;

    private SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // todo ? this.sharedPreferences = context.getSharedPreferences(Constant.PREF_UNIQUE_ID, Context.MODE_PRIVATE);
    }
    
    public static SharedPrefManager getInstance(Context context) {
        if (manager == null) {
            manager = new SharedPrefManager(context);
        }
        return manager;
    }

    /**
     * Read value from SharedPreferences for String
     */
    public String read(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    /**
     * Write value to SharedPreferences for String
     */
    public boolean write(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * Read value from SharedPreferences for boolean
     */
    public boolean read(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    /**
     * Write value to SharedPreferences for boolean
     */
    public boolean write(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * Read value from SharedPreferences for int
     */
    public int read(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    /**
     * Write value to SharedPreferences for int
     */
    public boolean write(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * Read value from SharedPreferences for long
     */
    public long read(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    /**
     * Write value to SharedPreferences for long
     */
    public boolean write(String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * Read value from SharedPreferences for float
     */
    public float read(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    /**
     * Write value to SharedPreferences for float
     */
    public boolean write(String key, float value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }
}
