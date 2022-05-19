package com.del.delcontainer.handlers;

import android.util.Log;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;

import com.del.delcontainer.managers.DataManager;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.services.SensorsService;
import com.del.delcontainer.ui.fragments.DelAppContainerFragment;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.DELUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AccelerometerDataHandler {

    private static final String TAG = "AccelerometerDataHandler";
    private static final long DELAY = 250;
    private static final long INTERVAL = 250;
    private static boolean isRunning = false;
    private static AccelerometerDataHandler instance = null;

    private AccelerometerDataHandler() {
    }

    // Task executors
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> taskHandler = null;

    private final SensorsService sensorsService = SensorsService.getInstance();

    public static synchronized AccelerometerDataHandler getInstance() {
        if (null == instance) {
            instance = new AccelerometerDataHandler();
        }
        return instance;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Start providing accelerometer data
     */
    public void startAccelerometerDataProviderTask() {

        Log.d(TAG, "startAccelerometerDataProviderTask: starting accelerometer updates");
        isRunning = true;

        sensorsService.enableAccelerometerData();
        taskHandler = scheduler.scheduleWithFixedDelay(
                accelerometerDataProviderTask, DELAY, INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Disable accelerometer updates
     */
    public void stopAccelerometerDataProviderTask() {
        Log.d(TAG, "stopAccelerometerDataProviderTask: No more requests. Stopping updates");
        taskHandler.cancel(true);
        isRunning = false;
        sensorsService.disableAccelerometerData();
    }

    /**
     * Runnable instance to inject accelerometer data to the requesting app
     */
    private final Runnable accelerometerDataProviderTask = () -> {

        Log.d(TAG, "accelerometerDataProviderTask: Running accelerometer data request.");

        // Disable accelerometer data requests if no app is requesting data
        if(0 == DataManager.getInstance().getCallBackRequests(Constants.ACCESS_ACCELEROMETER).size()) {
            stopAccelerometerDataProviderTask();
        } else {

            // Get accelerometer data - Float array containing data from all 3 axes
            // Push ahead as a JSON object
            float[] accData = sensorsService.getAccelerometerData();
            JSONObject data = new JSONObject();
            try {
                data.put("X", accData[0]);
                data.put("Y", accData[1]);
                data.put("Z", accData[2]);

            } catch (JSONException e) {
                Log.e(TAG, "accelerometerDataProviderTask: Exception : " + e.getMessage());
            }

            if(DataManager.getLoggerRequestFlag(Constants.ACCESS_ACCELEROMETER)) {
                DataManager.logSensorRecord(Constants.ACCESS_ACCELEROMETER, data.toString());
            }

            for(Map.Entry<String, String> request :
                    DataManager.getInstance().getCallBackRequests(Constants.ACCESS_ACCELEROMETER).entrySet()) {

                // AppId, Callback name
                provideAccelerometerData(request.getKey(), request.getValue(), data);
            }
        }
    };

    /**
     * Provide accelerometer data to the requesting app's registered callback
     *
     * @param appId micro-app id
     * @param callback callback reference for the micro app
     * @param accelerometerData data to send
     */
    private void provideAccelerometerData(String appId, String callback, JSONObject accelerometerData) {

        Log.d(TAG, "provideAccelerometerData: Injecting accelerometer data to app : " + appId);
        HashMap<String, Fragment> appCache = DelAppManager.getInstance().getAppCache();
        DelAppContainerFragment targetFrag = (DelAppContainerFragment) appCache.get(appId);

        if(null == targetFrag) {
            // App doesn't exist - remove data request for the app
            DataManager.getInstance().getCallBackRequests(Constants.ACCESS_ACCELEROMETER).remove(appId);
            return;
        }

        String[] params = new String[]{"accelerometer", accelerometerData.toString()};
        WebView appView = targetFrag.getAppView();
        String functionCall = DELUtils.getInstance().getTargetFunctionString(callback, params);
        DELUtils.getInstance().callDelAppFunction(appView, functionCall);
    }
}
