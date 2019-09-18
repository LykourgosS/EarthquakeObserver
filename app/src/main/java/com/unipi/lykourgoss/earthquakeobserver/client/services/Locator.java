package com.unipi.lykourgoss.earthquakeobserver.client.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.unipi.lykourgoss.earthquakeobserver.client.Constant;

/**
 * Created by LykourgosS <lpsarantidis@gmail.com>
 * on 06,August,2019.
 */

public class Locator implements LocationListener {

    private static final String TAG = "Locator";

    private static final float MAX_DISTANCE_CONNECTED__TO_AC = 100; // in meters

    private Location lastLocation;

    private LocationManager locationManager;

    private String PROVIDER = LocationManager.NETWORK_PROVIDER;

    private LocatorUpdatesListener listener;

    @SuppressLint("MissingPermission")
    public Locator(Context context, LocatorUpdatesListener listener) {
        this.listener = listener;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(PROVIDER, Constant.LOCATION_REQUEST_INTERVAL, 0, this);
        //lastLocation = locationManager.getLastKnownLocation(PROVIDER);
        //Log.d(TAG, "Locator: " + lastLocation);
        //listener.onLocatorInit(locationManager.isProviderEnabled(PROVIDER));
    }

    public void removeUpdates() {
        locationManager.removeUpdates(this);
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastLocation == null) {
            Log.d(TAG, "onLocationChanged: first location = " + location);
            lastLocation = location;
            listener.onLocatorStatusChanged(true);
        } else if (location.distanceTo(lastLocation) > MAX_DISTANCE_CONNECTED__TO_AC) {
            Log.d(TAG, "onLocationChanged: max distance exceeded, distance = " + location.distanceTo(lastLocation));
            listener.onLocatorStatusChanged(false);
        } else {
            // could be removed last assignment, because distance is not greater from
            // MAX_DISTANCE_CONNECTED__TO_AC (used mainly for debugging to see the location updates)
            Log.d(TAG, "onLocationChanged: distance = " + location.distanceTo(lastLocation));
            lastLocation = location;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged" + locationManager.getLastKnownLocation(provider));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + locationManager.getLastKnownLocation(provider));
        lastLocation = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderDisabled(String provider) {
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
        Log.d(TAG, "onProviderDisabled: " + locationManager.getLastKnownLocation(provider));
        listener.onLocatorStatusChanged(false);
        lastLocation = null;
    }

    public interface LocatorUpdatesListener {

        /**
         * Called when the locator is initialized with a valid location with
         * @param #isFixed shows if locator is ready, when the device is moving it will become false
         * */
        void onLocatorStatusChanged(boolean isFixed);
    }

}
