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
    private boolean getLocationUpdates = true;

    private static LocationService instance;

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

        requestLocationUpdates();

        Log.d(TAG, "getLastLocation: called");
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
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
                    }
                });

        return locationHistory.get(locationHistory.size() - 1);
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

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                ;
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Live location updates
     */
    public void requestLocationUpdates() {

        // Check if the user has enabled location updates.
        if(getLocationUpdates) {
            startLocationUpdates();
        }
    }


    /**
     * Start location updates
     */
    private void startLocationUpdates() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
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
                        + " | Longitude : " + location.getLongitude());
            }
        }
    };

}
