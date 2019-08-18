package com.unipi.lykourgoss.earthquakeobserver.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 06,August,2019.
 */

public class Locator implements LocationListener {

    private static final String TAG = "Locator";

    public static final int UPDATES_PERIOD = 0/*1000 * 30*/; // get updates every 30 seconds

    private Location lastLocation;

    private boolean isMoving = false;

    private LocationManager locationManager;
    private String provider = LocationManager.NETWORK_PROVIDER;

    private long lastUpdateTime;

    private Context context;

    @SuppressLint("MissingPermission")
    public Locator(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, UPDATES_PERIOD, 10, this);
        /**
         * updates every ~20 seconds
         * locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATES_PERIOD, 0, this);
         *
         * updates every ~10 seconds
         * locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, UPDATES_PERIOD, 0, this);
         * */
//        LocationProvider provider = locationManager.getProvider(locationManager.getBestProvider(createCriteria(), true));
        locationManager.requestLocationUpdates(provider, 30000, 0, this);
        lastLocation = locationManager.getLastKnownLocation(provider);
        lastUpdateTime = SystemClock.elapsedRealtime();
        Log.d(TAG, "Locator: " + lastLocation);
    }

    public void removeUpdates() {
        locationManager.removeUpdates(this);
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

    public boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(provider);
    }

    public boolean isMoving() {
//        float speed = lastLocation.getSpeed();
//        return speed == 0;
        return isMoving;
    }

    // todo remove isFixed
    private boolean isFixed = false;

    private boolean isFixed() {
        return isFixed;
    }

    @Override
    public void onLocationChanged(Location location) {
        // when the onLocationChanged triggered the first time means provider is fixed and only the
        // first time isFixed will become true
        if (!isFixed) isFixed = true;

        long timeNow = SystemClock.elapsedRealtime();
        Log.d(TAG, "onLocationChangedPeriod: " + ((timeNow - lastUpdateTime) / 1000.0));
        lastUpdateTime = timeNow;
        Log.d(TAG, "onLocationChanged: " + location);
        /*if (lastLocation.getLatitude() != location.getLatitude() || lastLocation.getLongitude() != location.getLongitude()) {
            isMoving = true;
        }*/
        if (lastLocation != null && location.distanceTo(lastLocation) > 10) {
            isMoving = true;
        }
        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled");
        Log.d(TAG, "onProviderEnabled: " + locationManager.getLastKnownLocation(provider));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderDisabled(String provider) {
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
        Log.d(TAG, "onProviderDisabled");
        Log.d(TAG, "onProviderDisabled: " + locationManager.getLastKnownLocation(provider));
    }
}
