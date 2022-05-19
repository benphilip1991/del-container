package com.del.delcontainer.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.del.delcontainer.managers.DataManager;
import com.del.delcontainer.managers.DelNotificationManager;
import com.del.delcontainer.ui.login.LoginStateRepo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * App APIs exposed to the DEL web applications
 * Just a toast API for now - expand to include
 * app registration and data transfer,
 */
public class DELUtils {

    private Context context;
    private static DELUtils delUtils = null;
    private static final String TAG = "DELUtils";

    private DELUtils() {
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized DELUtils getInstance() {
        if (null == delUtils) {
            delUtils = new DELUtils();
        }

        return delUtils;
    }

    /**
     * Build a function call formatted for the appView as a string
     * Eg: "function_name('param1', 'param2')"
     *
     * @param targetFunction Function to be called in the mini-apps
     * @param params Params to be injected
     * @return
     */
    public String getTargetFunctionString(final String targetFunction, final Object[] params) {

        // Build a function call with parameters as a string
        StringBuilder sb = new StringBuilder(targetFunction).append('(');
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof String) {
                sb.append("\'").append(params[i]).append("\'");
            }

            if (i != params.length - 1) {
                sb.append(",");
            }
        }

        sb.append(')');
        return sb.toString();
    }

    /**
     * Function to execute DelApp function
     *
     * @param appView
     * @param targetFunction
     */
    public void callDelAppFunction(final WebView appView, final String targetFunction) {

        if (null == appView) {
            Log.d(TAG, "callDelAppFunction: Invalid service reference. App does not exist");
            return;
        }

        // Pass in a thread instance to perform action
        appView.post(() -> {
            // Versions above API level 19 (KitKat) support evaluateJavascript
            // the ones below support loadUrl
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                appView.loadUrl("javascript:" + targetFunction);
            } else {
                // result -> string returned to onReceiveValue
                appView.evaluateJavascript(targetFunction, (result) -> {

                    Log.d(TAG, "callDelAppFunction: Returned : " + result);
                });
            }
        });
    }


    /**
     * Function to set requests for data callbacks from container
     *
     * @param requestDefinition
     */
    @JavascriptInterface
    public void setCallbackRequest(String requestDefinition) {

        DataManager dataManager = DataManager.getInstance();
        try {
            JSONObject reqObject = new JSONObject(requestDefinition);
            String appId = reqObject.getString(Constants.APP_ID);
            JSONArray requests = reqObject.getJSONArray(Constants.APP_REQUESTS);

            ArrayList<JSONObject> requestList = new ArrayList<>();
            for (int i = 0; i < requests.length(); requestList.add(requests.getJSONObject(i++))) ;

            dataManager.setCallBackRequests(appId, requestList);

        } catch (Exception e) {
            Log.d(TAG, "setCallbackRequest : " + e.getMessage());
        }
    }

    /**
     * Function to set request to log sensor readings
     *
     * @param requestDefinition
     */
    @JavascriptInterface
    public void setSensorLoggerRequest(String requestDefinition) {

        try {
            JSONObject reqObject = new JSONObject(requestDefinition);
            String appId = reqObject.getString(Constants.APP_ID);
            JSONArray requests = reqObject.getJSONArray(Constants.APP_REQUESTS);

            ArrayList<JSONObject> requestList = new ArrayList<>();
            for (int i = 0; i < requests.length(); requestList.add(requests.getJSONObject(i++))) ;

            DataManager.setSensorLoggerRequests(appId, requestList);

        } catch (Exception e) {
            Log.d(TAG, "setLoggerRequest : " + e.getMessage());
        }
    }

    /**
     * Get app-specific storage
     *
     * @param AppId
     * @return
     */
    @JavascriptInterface
    public String getAppData(String AppId) {
        return DataManager.getAppData(AppId);
    }

    /**
     * Set app-specific storage
     *
     * @param AppId
     * @param content
     * @return
     */
    @JavascriptInterface
    public void setAppData(String AppId, String content) {
        DataManager.setAppData(AppId, content);
    }

    /**
     * Get logged sensor data
     *
     * @param AppId
     * @param sensor
     * @return
     */
    @JavascriptInterface
    public String getSensorData(String AppId, String sensor) {
        return DataManager.getSensorLogs(AppId, sensor);
    }

    /**
     * Get logged sensor data filtered by date
     *
     * @param appId
     * @param sensor
     * @param start
     * @param end
     * @return
     */
    @JavascriptInterface
    public String getSensorData(String appId, String sensor, String start, String end) {
        return DataManager.getSensorLogs(appId, sensor, start, end);
    }

    /**
     * Create single app notification
     *
     * @param appId
     * @param notificationMessage
     */
    @JavascriptInterface
    public void createNotification(String appId, String notificationMessage) {
        DelNotificationManager.getInstance().createAppNotification(appId, notificationMessage);
    }

    @JavascriptInterface
    public String getUserName() {
        return LoginStateRepo.getInstance().getFirstName();
    }
}