package com.del.delcontainer.services;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/**
 * Handle location requests from applications.
 * Only fetch the info when required.
 */
public class LocationService {

    private static final String TAG = "LocationService";

    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ArrayList<Location> locationHistory = new ArrayList<>();
    private boolean gettingLocationUpdates = false;
    private boolean isLiveLocationEnabled = false;

    private static LocationService instance = null;

    private LocationService() {
        ;
    }

    public static synchronized LocationService getInstance() {
        if (null == instance) {
            instance = new LocationService();
        }

        return instance;
    }

    // Context dependency injection
    public void initLocationService(Context context) {

        if (null == context) {
            this.context = context;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public Location getLastLocation() {

        startLocationUpdates();

        Log.d(TAG, "getLastLocation: called");
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(/*new OnSuccessListener<Location>()*/ (location) -> {
                    //@Override
                    //public void onSuccess(Location location) {
                        if (null != location) {
                            if (0 != locationHistory.size()) {
                                if (locationHistory.get(locationHistory.size() - 1).getLatitude() != location.getLatitude()
                                        && locationHistory.get(locationHistory.size() - 1).getLongitude() != location.getLongitude()) {

                                    Log.d(TAG, "onSuccess: Adding new location : " + location.getLatitude() + " | " + location.getLongitude());
                                    locationHistory.add(location);
                                }
                            } else {
                                locationHistory.add(location);
                            }
                        }
                    //}
                });

        return locationHistory.get(locationHistory.size() - 1);
    }

    /**
     * If even one app is requesting live location, this remains true.
     * Else, switch to false.
     * Can maintain a list of apps requesting location. -> do later
     * @param flag
     */
    public void setLocationServiceEnabled(boolean flag) {
        isLiveLocationEnabled = flag;
    }

    /**
     * Method to get location and push to calling app.
     */
    private void getLocation() {

        // Connect to the google play services and the location services API
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(context.getApplicationContext());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener((locationSettingsResponse) -> {
                ;
        });

        task.addOnFailureListener((e) -> {
                e.printStackTrace();
        });
    }


    /**
     * Start location updates
     */
    public void startLocationUpdates() {

        // If location is already being fetched, ignore
        if(!gettingLocationUpdates && isLiveLocationEnabled) {

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

        if(gettingLocationUpdates) {
            gettingLocationUpdates = false;
            isLiveLocationEnabled = false;
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(null == locationResult) {
                return;
            }

            // Push data to app
            for(Location location : locationResult.getLocations()) {
                Log.d(TAG, "onLocationResult: Latitude : " + location.getLatitude()
                        + " | Longitude : " + location.getLongitude() + " | Accuracy : " + location.getAccuracy());
            }
        }
    };

}
