package com.del.delcontainer.handlers;

import android.location.Location;
import android.util.Log;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;

import com.del.delcontainer.managers.DataManager;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.services.LocationService;
import com.del.delcontainer.ui.fragments.DelAppContainerFragment;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.DELUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LocationDataHandler {

    private static final String TAG = "LocationDataHandler";
    private static final long DELAY = 1;
    private static final long INTERVAL = 5;
    private static boolean isRunning = false;
    private static LocationDataHandler instance = null;

    // Executors for periodic provider task
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> taskHandler = null;

    private LocationService locationService = LocationService.getInstance();

    private LocationDataHandler() {
    }

    /**
     * Get class instance
     *
     * @return
     */
    public static synchronized LocationDataHandler getInstance() {
        if (null == instance) {
            instance = new LocationDataHandler();
        }
        return instance;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Start location provider - the task goes through registered apps
     * and injects the required data.
     */
    public void startLocationProviderTask() {

        Log.d(TAG, "startLocationProviderTask: Starting location updates.");
        isRunning = true;

        // Start location updates
        locationService.setLocationServiceEnabled(true);
        locationService.startLocationUpdates();

        taskHandler = scheduler.scheduleWithFixedDelay(
                locationProviderTask, DELAY, INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Stop providing location updates. Ideally, this method is called
     * when no apps are registered for the updates.
     */
    public void stopLocationProviderTask() {

        Log.d(TAG, "LocationProviderTask: No more requests. Stopping updates.");
        taskHandler.cancel(true);
        isRunning = false;

        // Stop location requests
        locationService.setLocationServiceEnabled(false);
        locationService.stopLocationUpdates();
    }

    /**
     * Inject location data to services in a given interval
     */
    private Runnable locationProviderTask = () -> {

        Log.d(TAG, "locationProviderTask : Running location request.");
        if (0 == DataManager.getInstance().getCallBackRequests(Constants.ACCESS_LOCATION).size()) {
            // If running, stop location updates
            LocationService.getInstance().stopLocationUpdates();
            stopLocationProviderTask();
        } else {

            Location location = LocationService.getInstance().getLastLocation();
            JSONObject data = new JSONObject();
            try {
                data.put("latitude", location.getLatitude());
                data.put("longitude", location.getLongitude());
                data.put("accuracy", location.getAccuracy());
            } catch (Exception e) {
                Log.e(TAG, "provideLocationData: Exception : " + e.getMessage());
            }
            //Log data if required
            if(DataManager.getLoggerRequestFlag(Constants.ACCESS_LOCATION)) {
                DataManager.LogSensorRecord(Constants.ACCESS_LOCATION, data.toString());
            }

            for (Map.Entry<String, String> request :
                    DataManager.getInstance().getCallBackRequests(Constants.ACCESS_LOCATION).entrySet()) {

                // AppId, Callback function name
                provideLocationData(request.getKey(), request.getValue(), data);
            }
        }
    };

    /**
     * Provide requested data to the app -> callback
     * @param appId
     * @param callback
     */
    private void provideLocationData(String appId, String callback, JSONObject data) {

        Log.d(TAG, "provideLocationData: injecting location data to app : " + appId);
        HashMap<String, Fragment> appCache = DelAppManager.getInstance().getAppCache();
        DelAppContainerFragment targetFrag = (DelAppContainerFragment) appCache.get(appId);

        if (null == targetFrag) {
            // Fragment doesn't exist. Clear location data request for the app.
            DataManager.getInstance().getCallBackRequests(Constants.ACCESS_LOCATION).remove(appId);
            return;
        }

        // Create data object and send to registered callback
        String[] params = new String[]{"location", data.toString()};
        WebView appView = targetFrag.getAppView();
        String functionCall = DELUtils.getInstance().getTargetFunctionString(callback, params);
        DELUtils.getInstance().callDelAppFunction(appView, functionCall);
    }
}
