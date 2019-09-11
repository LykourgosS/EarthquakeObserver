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

    private static final float MAX_DISTANCE_CONNECTED__TO_AC = 10; // 10 meters

    private Location lastLocation;

    private LocationManager locationManager;

    private String PROVIDER = LocationManager.NETWORK_PROVIDER;

    private LocatorUpdatesListener listener;

    @SuppressLint("MissingPermission")
    public Locator(Context context, LocatorUpdatesListener listener) {
        this.listener = listener;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(PROVIDER, Constant.LOCATION_REQUEST_INTERVAL, MAX_DISTANCE_CONNECTED__TO_AC, this);
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
        Log.d(TAG, "onLocationChanged: " + location);
        if (lastLocation == null) {
            lastLocation = location;
            listener.onLocatorStatusChanged(true);
        } else if (location.distanceTo(lastLocation) > MAX_DISTANCE_CONNECTED__TO_AC) {
            listener.onLocatorStatusChanged(false);
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
        listener.onLocatorStatusChanged(true);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderDisabled(String provider) {
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
        Log.d(TAG, "onProviderDisabled: " + locationManager.getLastKnownLocation(provider));
        listener.onLocatorStatusChanged(false);
    }

    public interface LocatorUpdatesListener {

        /**
         * Called when the locator is initialized with a valid location with
         * @param #isFixed shows if locator is ready, when the device is moving it will become false
         * */
        void onLocatorStatusChanged(boolean isFixed);
    }

}
