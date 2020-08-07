package com.del.delcontainer.managers;

import com.del.delcontainer.handlers.LocationDataHandler;
import com.del.delcontainer.handlers.PedometerDataHandler;
import com.del.delcontainer.services.LocationService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Data handler to manage data storage/retrieval from
 * the database.
 * <p>
 * This file interfaces the mini apps to the stored data
 * and allows services to store data in the db.
 */
public class DataManager {

    // TODO: Need a scheduler to go through the mapped requests and deliver -> DataProviderService
    // Map of appId -> List of JSONObjects,
    private static HashMap<String, ArrayList<JSONObject>> dataRequestMap;

    // Map of appId -> callback name
    private static HashMap<String, String> locationRequests;
    private static HashMap<String, String> heartRateRequests;
    private static HashMap<String, String> pedometerRequests;

    private static DataManager dataManager = new DataManager();

    private DataManager() {
        dataRequestMap = new HashMap<>();
        locationRequests = new HashMap<>();
        heartRateRequests = new HashMap<>();
        pedometerRequests = new HashMap<>();
    }

    /**
     * Method to delete all requests associated to an app
     *
     * @param appId
     */
    public void removeDataRequests(String appId) {
        locationRequests.remove(appId);
        heartRateRequests.remove(appId);
        pedometerRequests.remove(appId);
    }

    public static DataManager getInstance() {
        return dataManager;
    }

    public HashMap<String, ArrayList<JSONObject>> getDataRequestMap() {
        return dataRequestMap;
    }

    public HashMap<String, String> getLocationRequests() {
        return locationRequests;
    }

    public HashMap<String, String> getHeartRateRequests() {
        return heartRateRequests;
    }

    public HashMap<String, String> getPedometerRequests() {
        return pedometerRequests;
    }


    // Data provider operations
    /**
     * Start location provider.
     */
    public static void startLocationProviderTask() {

        if (!LocationDataHandler.getInstance().isRunning()) {
            LocationDataHandler.getInstance().startLocationProviderTask();
        }
    }

    /**
     * Start step data provider
     */
    public static void startStepCountProviderTask() {
        if(!PedometerDataHandler.getInstance().isRunning()) {
            PedometerDataHandler.getInstance().startStepDataProviderTask();
        }
    }
}
