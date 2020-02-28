package com.del.delcontainer.services;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Handle location requests from applications.
 * Only fetch the info when required.
 */
public class LocationService {

    private static final String TAG = "LocationService";

    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean gettingLocationUpdates = false;
    private boolean isLiveLocationEnabled = false;
    private static LocationService instance = null;
    private Location lastLocation = null;

    private LocationService() {
    }

    public static synchronized LocationService getInstance() {
        if (null == instance) {
            instance = new LocationService();
        }
        return instance;
    }

    public void initLocationService(Context context) {
        if (null != context) {
            this.context = context;
        }
        fusedLocationProviderClient = LocationServices.
                getFusedLocationProviderClient(this.context);
    }

    public Location getLastLocation() {
        startLocationUpdates();
        return lastLocation;
    }

    /**
     * If even one app is requesting live location, this remains true.
     * Else, switch to false.
     * Can maintain a list of apps requesting location. -> do later
     *
     * @param flag true to enable location
     */
    public void setLocationServiceEnabled(boolean flag) {
        isLiveLocationEnabled = flag;
    }

    /**
     * Start location updates
     */
    public void startLocationUpdates() {

        // If location is already being fetched, ignore
        if (!gettingLocationUpdates && isLiveLocationEnabled) {
            gettingLocationUpdates = true;
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
    }

    /**
     * Stop location updates. Apps can use the last update.
     */
    public void stopLocationUpdates() {
        if (gettingLocationUpdates) {
            gettingLocationUpdates = false;
            isLiveLocationEnabled = false;
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Update the last location object
     */
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (null != locationResult) {
                lastLocation = locationResult.getLastLocation();
                Log.d(TAG, "onLocationResult: Latitude : " + lastLocation.getLatitude()
                + " | Longitude : " + lastLocation.getLongitude()
                + " | Accuracy : " + lastLocation.getAccuracy());
            }
        }
    };
}
