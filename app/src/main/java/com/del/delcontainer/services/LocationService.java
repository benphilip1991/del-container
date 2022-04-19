package com.del.delcontainer.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.del.delcontainer.managers.DataManager;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.ui.fragments.DelAppContainerFragment;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.DELUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Handle location requests from applications.
 * Only fetch the info when required.
 */
public class LocationService {

    private static final String TAG = "LocationService";

    private Context context;
    private Activity activity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean gettingLocationUpdates = false;
    private boolean isLiveLocationEnabled = false;
    private static LocationService instance = null;
    private Location lastLocation = null;

    private LocationService() {
        ;
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

    public void setActivity(Activity activity) {
        if(null != activity) {
            this.activity = activity;
        }
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

            // Self-check for permissions. Request if not already granted
            if (ActivityCompat.checkSelfPermission(
                    this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                            this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Permissions not already granted - check now
                ActivityCompat.requestPermissions(this.activity, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, Constants.LOCATION_REQUEST_CODE);

            } else {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                        locationCallback,
                        Looper.getMainLooper());
            }
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
