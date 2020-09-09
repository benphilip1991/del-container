package com.del.delcontainer.managers;

import android.util.Log;

import com.del.delcontainer.handlers.LocationDataHandler;
import com.del.delcontainer.handlers.PedometerDataHandler;
import com.del.delcontainer.repositories.UserServicesRepository;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.CustomMutableLiveData;
import com.del.delcontainer.utils.apiUtils.pojo.LinkedApplicationDetails;

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
    private static final String TAG = "DataManager";

    // TODO: Need a scheduler to go through the mapped requests and deliver -> DataProviderService
    // Map of request resource -> dictionary of (appId -> callback),
    static HashMap<String, HashMap<String, String>> dataRequestMap;

    //Object to store installed apps and permissions
    private static CustomMutableLiveData<UserServicesRepository> userServicesRepository =
            new CustomMutableLiveData<>();

    private static DataManager dataManager = new DataManager();

    private DataManager() {
        dataRequestMap = new HashMap<>();
        userServicesRepository.postValue(UserServicesRepository.getInstance());
        //TODO: Observe and update app list when changes are triggered
    }

    public static DataManager getInstance() {
        return dataManager;
    }

    /**
     * Method to delete resource requests associated to an app
     * @param resource
     * @param appId
     */
    public void removeResourceRequests(String resource, String appId) {
        dataRequestMap.get(resource).remove(appId);
    }

    /**
     * Method to get all requests for a resource
     * @param resource
     */
    public HashMap<String, String> getRequests(String resource) {
        return dataRequestMap.get(resource);
    }

    /**
     * Method to set application requests for a resource
     * @param appId
     * @param requests
     */
    public static void setRequests(String appId, ArrayList<JSONObject> requests) {
        ArrayList<LinkedApplicationDetails> linkedApps = userServicesRepository
                .getValue().getUserServicesList();
        try{
            for (JSONObject request:requests) {
                String resource = request.getString(Constants.RESOURCE);
                String callback = request.getString(Constants.CALLBACK);
                if(validatePermissions(linkedApps, appId, resource)){
                    if(null == dataRequestMap.get(resource)) {
                        dataRequestMap.put(resource, new HashMap<String, String>());
                    }
                    Log.d(TAG, "addRequest: Setting"
                            + resource + " request for service : " + appId);
                    dataRequestMap.get(resource).put(appId, callback);
                    startProviderTask(resource);

                } else {
                    Log.d(TAG, "addRequest: No permission to set "
                            + resource + " request for service : " + appId);
                }
            }
        } catch (Exception e) {
            Log.d(TAG,"Error while parsing requests");
        }
    }

    /**
     * Method to validate application permissions for a resource
     * @param linkedApps
     * @param appId
     * @param resource
     */
    private static boolean validatePermissions(ArrayList<LinkedApplicationDetails> linkedApps,
                                               String appId, String resource) {
        for(LinkedApplicationDetails app : linkedApps) {
            if (app.getApplicationId().equals(appId) &&
                    app.getApplicationPermissions().contains(resource))
                return true;
        }
        return false;
    }

    /**
     * Method to start resource providers
     * @param resource
     */
    public static void startProviderTask(String resource) {
        switch(resource) {
            case Constants.ACCESS_LOCATION:
                if (!LocationDataHandler.getInstance().isRunning())
                    LocationDataHandler.getInstance().startLocationProviderTask();
                break;
            case Constants.ACCESS_PEDOMETER:
                if(!PedometerDataHandler.getInstance().isRunning())
                    PedometerDataHandler.getInstance().startStepDataProviderTask();
            default:
        }

    }
}
