package com.del.delcontainer.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.del.delcontainer.database.entities.Heart;
import com.del.delcontainer.repositories.HeartRateRepository;
import com.del.delcontainer.services.LocationService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

/**
 * App APIs exposed to the DEL web applications
 * Just a toast API for now - expand to include
 * app registration and data transfer,
 */
public class DELUtils {

    private Context context;
    private static final String TAG = "DELUtils";

    public DELUtils(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void makeToast(String message) {
        Log.d(TAG, "makeToast: Called MakeToast");
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void registerApp(String appDetails) {

        UUID appId = null;
        String appName = null;

        try {
            JSONObject appInfo = new JSONObject(appDetails);
            appId = UUID.fromString(appInfo.getString(Constants.APP_ID));
            appName = appInfo.getString(Constants.APP_NAME);

            Log.d(TAG, "registerApp: " + appName + "; ID : " + appId);

        } catch(JSONException e) {
            Log.d(TAG, "registerApp: ");
        }

        // Broadcast app registration
        Intent intent = new Intent(Constants.EVENT_APP_REGISTERED);
        intent.putExtra(Constants.APP_ID, appId);
        intent.putExtra(Constants.APP_NAME, appName);

        Log.d(TAG, "registerApp: sending broadcast");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @JavascriptInterface
    public String getLatestHeartData() {

        JSONObject hrData = new JSONObject();

        Heart heartData = HeartRateRepository.getInstance(context.getApplicationContext()).getLatestHeartData();
        if(null == heartData) {
            return "No Data";
        }

        try {
            hrData.put("timestamp", heartData.getDate());
            hrData.put("heartRateValue", heartData.getHeartRate());
        } catch(JSONException e) {
            e.printStackTrace();
        }

        return hrData.toString();
    }

    @JavascriptInterface
    public String getAllHeartRate() {

        JSONArray hrDataArray = new JSONArray();
        JSONObject hrBlock = new JSONObject();

        List<Heart> heartRateData = HeartRateRepository.getInstance(context.getApplicationContext()).getHeartData();

        if(null == heartRateData) {
            return "No Data";
        }

        try {
            for(Heart heart : heartRateData) {
                JSONObject heartRateJsonData = new JSONObject();
                heartRateJsonData.put("timestamp", heart.getDate());
                heartRateJsonData.put("heartRateValue", heart.getHeartRate());
                hrDataArray.put(heartRateJsonData);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        try {
            hrBlock.put("hrData", hrDataArray);
        } catch(Exception e) {
            ;
        }

        return hrBlock.toString();
    }

    @JavascriptInterface
    public String getCurrentLocation() {

        JSONObject locationObject = new JSONObject();
        LocationService locationService = LocationService.getInstance();

        Location location = locationService.getLastLocation();

        if(null != location) {
            try {
                locationObject.put("latitude", location.getLatitude());
                locationObject.put("longitude", location.getLongitude());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return locationObject.toString();
    }

    @JavascriptInterface
    public void terminateApp(String appDetails) {
        UUID appId = null;
        String appName = null;

        try {
            JSONObject appInfo = new JSONObject(appDetails);
            appId = UUID.fromString(appInfo.getString(Constants.APP_ID));
            appName = appInfo.getString(Constants.APP_NAME);

        } catch(JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "terminateApp: Terminating : " + appName);
        DelAppManager.getInstance().terminateApp(appId, appName);
    }
}
