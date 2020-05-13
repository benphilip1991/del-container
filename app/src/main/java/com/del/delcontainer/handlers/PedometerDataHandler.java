package com.del.delcontainer.handlers;

import android.util.Log;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;

import com.del.delcontainer.managers.DataManager;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.services.SensorsService;
import com.del.delcontainer.ui.fragments.DelAppContainerFragment;
import com.del.delcontainer.utils.DELUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PedometerDataHandler {

    private static final String TAG = "PedometerDataHandler";
    private static final long DELAY = 1;
    private static final long INTERVAL = 3;
    private static boolean isRunning = false;

    private static PedometerDataHandler instance = null;

    private PedometerDataHandler() {
    }

    // Executors for periodic provider task
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> taskHandler = null;

    private SensorsService sensorsService = SensorsService.getInstance();

    public static synchronized PedometerDataHandler getInstance() {
        if (null == instance) {
            instance = new PedometerDataHandler();
        }
        return instance;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Start providing step data from the pedometer
     */
    public void startStepDataProviderTask() {

        Log.d(TAG, "startStepDataProviderTask: starting step count updates");
        isRunning = true;

        taskHandler = scheduler.scheduleWithFixedDelay(
                stepCountProviderTask, DELAY, INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Stop step count data updates
     */
    public void stopStepDataProviderTask() {
        Log.d(TAG, "stopStepDataProviderTask: No more requests. Stopping updates.");
        taskHandler.cancel(true);
        isRunning = false;
    }

    /**
     * Runnable instance to inject step data to requesting app
     */
    private Runnable stepCountProviderTask = () -> {

        Log.d(TAG, "stepCountProviderTask: Running step count request.");
        if (0 == DataManager.getInstance().getPedometerRequests().size()) {
            stopStepDataProviderTask();
        } else {
            for (Map.Entry<String, String> request :
                    DataManager.getInstance().getPedometerRequests().entrySet()) {

                provideStepData(request.getKey(), request.getValue());
            }
        }
    };

    /**
     * Provide requested data to app -> callback
     * @param appId
     * @param callback
     */
    private void provideStepData(String appId, String callback) {

        Log.d(TAG, "provideStepData: injecting step data to app : " + appId);
        HashMap<String, Fragment> appCache = DelAppManager.getInstance().getAppCache();
        DelAppContainerFragment targetFrag = (DelAppContainerFragment) appCache.get(appId);
        String stepCount = String.valueOf(sensorsService.getStepCount());

        if(null == targetFrag) {
            // App doesn't exist - remove step count data request for app.
            DataManager.getInstance().getPedometerRequests().remove(appId);
            return;
        }

        String[] params = new String[]{"step_count", stepCount};
        WebView appView = targetFrag.getAppView();
        String functionCall = DELUtils.getInstance().getTargetFunctionString(callback, params);
        DELUtils.getInstance().callDelAppFunction(appView, functionCall);
    }
}
