package com.unipi.lykourgoss.earthquakeobserver.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 06,August,2019.
 */

public class Locator implements LocationListener {

    private static final String TAG = "Locator";

    public static final int UPDATES_PERIOD = 0/*1000 * 10*/; // get updates every 10 seconds

    private Location lastLocation;

    private LocationManager locationManager;

    private long lastUpdateTime;

    @SuppressLint("MissingPermission")
    public Locator(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, UPDATES_PERIOD, 5, this);
        /**
         * updates every ~20 seconds
         * locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATES_PERIOD, 0, this);
         *
         * updates every ~10 seconds
         * locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, UPDATES_PERIOD, 0, this);
         * */
//        LocationProvider provider = locationManager.getProvider(locationManager.getBestProvider(createCriteria(), true));
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
        lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    }

    /*private static Criteria createCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACC);
        criteria.setSpeedRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }*/

    public Location getLastLocation() {
        return lastLocation;
    }

    public boolean isMoving() {
        float speed = lastLocation.getSpeed();
        return speed == 0;
    }

    @Override
    public void onLocationChanged(Location location) {
        long timeNow = SystemClock.elapsedRealtime();
        Log.d(TAG, "onLocationChangedPeriod: " + ((timeNow - lastUpdateTime) / 1000.0));
        lastUpdateTime = timeNow;
        Log.d(TAG, "onLocationChanged: " + location);
        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled");
        Log.d(TAG, "onProviderDisabled: " + locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
    }
}
