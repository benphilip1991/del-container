package com.del.delcontainer.handlers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AppSensorDataHandler extends Thread {

    private static final String TAG = "AppSensorDataHandler";

    private static AppSensorDataHandler appSensorDataHandler = null;
    public Handler handler;

    private AppSensorDataHandler() {

        if(null == appSensorDataHandler) {
            Log.d(TAG, "AppSensorDataHandler: Creating new instance");
            appSensorDataHandler = new AppSensorDataHandler();   
        }
    }

    public static AppSensorDataHandler getInstance() {
        if (null == appSensorDataHandler) {
            Log.d(TAG, "getInstance: No available handler instance. Creating new object");
            new AppSensorDataHandler();
        }

        return appSensorDataHandler;
    }

    /**
     * Handle data management and pass data to appropriate apps
     */
    @Override
    public void run() {
        Looper.prepare();   // call before instantiating a handler
        handler = new Handler();    // Has to be before the loop method

        // To stop, call quit on the looper object -> Looper.myLooper().quit();
        Looper.loop();  // Starts the infinite for loop.
    }
}
