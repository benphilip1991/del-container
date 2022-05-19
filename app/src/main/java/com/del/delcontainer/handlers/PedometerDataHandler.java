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
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> taskHandler = null;

    private final SensorsService sensorsService = SensorsService.getInstance();

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

        sensorsService.enableStepCounter();
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
        sensorsService.disableStepCounter();
    }

    /**
     * Runnable instance to inject step data to requesting app
     */
    private final Runnable stepCountProviderTask = () -> {

        Map<String, String> callbacks = DataManager.getInstance().getCallBackRequests(Constants.ACCESS_PEDOMETER);

        Log.d(TAG, "stepCountProviderTask: Running step count request.");
        if (0 == callbacks.size()) {
            Log.d(TAG, "No more pedometer requests: ");
            stopStepDataProviderTask();
        } else {

            String stepCount = String.valueOf(sensorsService.getStepCount());
            if (DataManager.getLoggerRequestFlag(Constants.ACCESS_PEDOMETER)) {
                DataManager.logSensorRecord(Constants.ACCESS_PEDOMETER, stepCount);
            }

            for (Map.Entry<String, String> request : callbacks.entrySet()) {
                provideStepData(request.getKey(), request.getValue(), stepCount);
            }
        }
    };

    /**
     * Provide requested data to app -> callback
     *
     * @param appId    micro-app id
     * @param callback callback reference for the micro app
     */
    private void provideStepData(String appId, String callback, String stepCount) {

        Log.d(TAG, "provideStepData: injecting step data to app : " + appId);
        HashMap<String, Fragment> appCache = DelAppManager.getInstance().getAppCache();
        DelAppContainerFragment targetFrag = (DelAppContainerFragment) appCache.get(appId);

        if (null == targetFrag) {
            // App doesn't exist - remove step count data request for app.
            DataManager.getInstance().removeCallbackRequests(Constants.ACCESS_PEDOMETER, appId);
            return;
        }

        String[] params = new String[]{"step_count", stepCount};
        WebView appView = targetFrag.getAppView();
        String functionCall = DELUtils.getInstance().getTargetFunctionString(callback, params);
        DELUtils.getInstance().callDelAppFunction(appView, functionCall);

    }
}
