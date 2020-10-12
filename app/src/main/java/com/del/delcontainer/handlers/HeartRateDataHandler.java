package com.del.delcontainer.handlers;

import android.util.Log;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;

import com.del.delcontainer.managers.DataManager;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.services.HeartRateService;
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

/**
 * Handle heart rate readings and pass on to requesting apps.
 * Readings are made when an HR sensor is connected to the phone - these are then
 * broadcast locally and then stored by the DelBroadcastReceiver.
 * Need to make sure the HR data is handled here instead of anywhere else.
 */
public class HeartRateDataHandler {

    private static final String TAG = "HeartRateDataHandler";
    private static final long DELAY = 1;
    private static final long INTERVAL = 5;
    public static boolean isRunning = false;
    private static HeartRateDataHandler instance = null;

    // Executors for periodic provider task
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> taskHandler = null;

    private HeartRateService heartRateService = HeartRateService.getInstance();

    private HeartRateDataHandler() {

    }

    /**
     * Get instance of HeartRateDataHandler
     *
     * @return
     */
    public static synchronized HeartRateDataHandler getInstance() {
        if (null == instance) {
            instance = new HeartRateDataHandler();
        }
        return instance;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Push heart rate readings to requesting apps at a fixed intervals
     */
    public void startHRProviderTask() {
        Log.d(TAG, "startHRProviderTask: Starting Heart Rate provider task");
        isRunning = true;

        // Start Heart Rate Data updates
        boolean res = heartRateService.startHRUpdate();

        if (res) {
            taskHandler = scheduler.scheduleWithFixedDelay(
                    hrProviderTask, DELAY, INTERVAL, TimeUnit.SECONDS);
        } else {
            Log.d(TAG, "startHRProviderTask: Cannot start HR service!");
        }
    }

    /**
     * Stop providing HR reading updates. Ideally, this method is called
     * when no apps are registered for the updates.
     */
    public void stopHRProviderTask() {
        Log.d(TAG, "stopHRProviderTask: No more requests. Stopping updates.");
        taskHandler.cancel(true);
        isRunning = false;

        // Stop HR update request
        heartRateService.stopHRUpdate();
    }

    /**
     * Fetch latest HR value buffer, parse and push to requesting app.
     */
    private Runnable hrProviderTask = () -> {

        Log.d(TAG, "hrProviderTask: Running HR provider");
        if (0 == DataManager.getInstance().getCallBackRequests(Constants.ACCESS_HEART_RATE).size()) {
            // If running, stop updates as no requests exist
            stopHRProviderTask();
        } else {

            int hr_avg = HeartRateService.getInstance().getLatestHRAverage();
            JSONObject data = new JSONObject();
            try {
                data.put("heart_rate", hr_avg);
            } catch (Exception e) {
                Log.e(TAG, "hrProviderTask : Exception : " + e.getMessage());
            }
            // Log data if required
            if (DataManager.getLoggerRequestFlag(Constants.ACCESS_HEART_RATE)) {
                DataManager.LogSensorRecord(Constants.ACCESS_HEART_RATE, data.toString());
            }

            for (Map.Entry<String, String> request :
                    DataManager.getInstance().getCallBackRequests(Constants.ACCESS_HEART_RATE).entrySet()) {

                // AppId, Callback function name
                provideHRData(request.getKey(), request.getValue(), data);
            }
        }
    };

    /**
     * Inject heart rate data to app -> callback
     *
     * @param appId
     * @param callback
     * @param data
     */
    private void provideHRData(String appId, String callback, JSONObject data) {

        Log.d(TAG, "provideHRData: injecting Heart Rate data to app : " + appId);
        HashMap<String, Fragment> appCache = DelAppManager.getInstance().getAppCache();
        DelAppContainerFragment targetFrag = (DelAppContainerFragment) appCache.get(appId);

        if (null == targetFrag) {
            // Fragment doesn't exist. Clear location data request for the app.
            DataManager.getInstance().getCallBackRequests(Constants.ACCESS_HEART_RATE).remove(appId);
            return;
        }

        // create data object and send to registered callback
        String[] params = new String[]{"heart_rate", data.toString()};
        WebView appView = targetFrag.getAppView();
        String functionCall = DELUtils.getInstance().getTargetFunctionString(callback, params);
        DELUtils.getInstance().callDelAppFunction(appView, functionCall);
    }
}
