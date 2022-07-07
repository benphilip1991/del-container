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

public class GyroscopeDataHandler {

    private static final String TAG = "GyroscopeDataHandler";
    private static final long DELAY = 250;
    private static final long INTERVAL = 250;
    private static boolean isRunning = false;
    private static GyroscopeDataHandler instance = null;

    private GyroscopeDataHandler() {
    }

    // Task executors
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> taskHandler = null;

    private final SensorsService sensorsService = SensorsService.getInstance();

    public static synchronized GyroscopeDataHandler getInstance() {
        if (null == instance) {
            instance = new GyroscopeDataHandler();
        }
        return instance;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Start providing gyroscope data
     */
    public void startGyroscopeDataProviderTask() {
        Log.d(TAG, "startGyroscopeDataProviderTask: starting gyroscope updates");
        isRunning = true;

        sensorsService.enableGyroscopeData();
        taskHandler = scheduler.scheduleWithFixedDelay(
                gyroscopeDataProviderTask, DELAY, INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Disable gyroscope updates
     */
    public void stopGyroscopeDataProviderTask() {
        Log.d(TAG, "stopGyroscopeDataProviderTask: No more requests. Stopping updates");
        taskHandler.cancel(true);
        isRunning = false;
        sensorsService.disableGyroscopeData();
    }

    /**
     * Runnable instance to inject gyroscope data to the requesting app
     */
    private final Runnable gyroscopeDataProviderTask = () -> {

        Log.d(TAG, "gyroscopeDataProviderTask: Running gyroscope data request");

        // Disable gyroscope data requests if no app is requesting data
        if (0 == DataManager.getInstance().getCallBackRequests(Constants.ACCESS_GYROSCOPE).size()) {
            stopGyroscopeDataProviderTask();
        } else {

            // Get raw gyroscope data
            float[] gyroData = sensorsService.getGyroscopeData();
            JSONObject data = new JSONObject();
            try {
                data.put("X", gyroData[0]);
                data.put("Y", gyroData[1]);
                data.put("Z", gyroData[2]);
            } catch (JSONException e) {
                Log.e(TAG, "gyroscopeDataProviderTask: Exception : " + e.getMessage());
            }

            if (DataManager.getLoggerRequestFlag(Constants.ACCESS_GYROSCOPE)) {
                DataManager.logSensorRecord(Constants.ACCESS_GYROSCOPE, data.toString());
            }

            for (Map.Entry<String, String> request :
                    DataManager.getInstance().getCallBackRequests(Constants.ACCESS_GYROSCOPE).entrySet()) {

                // AppId, Callback
                provideGyroscopeData(request.getKey(), request.getValue(), data);
            }
        }
    };

    /**
     * Provide gyroscope data to the requesting app's registered callback
     *
     * @param appId         micro-app id
     * @param callback      callback reference for the micro app
     * @param gyroscopeData data to send
     */
    private void provideGyroscopeData(String appId, String callback, JSONObject gyroscopeData) {

        Log.d(TAG, "provideGyroscopeData: Injecting gyroscope data to app : " + appId);
        HashMap<String, Fragment> appCache = DelAppManager.getInstance().getAppCache();
        DelAppContainerFragment targetFrag = (DelAppContainerFragment) appCache.get(appId);

        if (null == targetFrag) {
            // App doesn't exist - remove data request for the app
            DataManager.getInstance().getCallBackRequests(Constants.ACCESS_ACCELEROMETER).remove(appId);
            return;
        }

        String[] params = new String[]{"gyroscope", gyroscopeData.toString()};
        WebView appView = targetFrag.getAppView();
        String functionCall = DELUtils.getInstance().getTargetFunctionString(callback, params);
        DELUtils.getInstance().callDelAppFunction(appView, functionCall);
    }
}
