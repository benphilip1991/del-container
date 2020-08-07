package com.del.delcontainer.managers;


import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.del.delcontainer.R;
import com.del.delcontainer.ui.dialogs.RunningAppsDialogFragment;
import com.del.delcontainer.ui.fragments.DelAppContainerFragment;
import com.del.delcontainer.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DEL application manager responsible for app fragments
 * running inside the container
 *
 * Keep track of the following:
 *      What apps are running
 *      Which ones are hidden and which is visible
 *      Fragment to webview (app) mapping
 */
public class DelAppManager {

    private static final String TAG = "DelAppManager";

    // Singleton app manager object
    private static DelAppManager delAppManager = null;
    private FragmentManager fragmentManager = null;
    private HashMap<String, Fragment> appCache = new HashMap<>();
    private HashMap<String, String> appNameMap = new HashMap<>();

    private DelAppManager() {
        ;
    }

    public static DelAppManager getInstance() {
        if(null == delAppManager) {
            delAppManager = new DelAppManager();
        }

        return delAppManager;
    }

    /**
     * Check if the fragment manager already exists and if not,
     * set it.
     *
     * @param fragmentManager
     */
    public void setFragmentManager(FragmentManager fragmentManager) {
        // Another potential point of crash - clear on logout
        if(null == this.fragmentManager) {
            this.fragmentManager = fragmentManager;
            Log.d(TAG, "setFragmentManager: set new FragmentManager in app manager");
        }
    }

    /**
     * Clear the fragment manager
     */
    public void clearFragmentManager() {
        if(null != this.fragmentManager) {
            this.fragmentManager = null;
        }
    }

    /**
     * Get app cache
     */
    public HashMap<String, Fragment> getAppCache() {
        return appCache;
    }

    /**
     * Get app name map
     */
    public HashMap<String, String> getAppNameMap() {
        return appNameMap;
    }

    /**
     * Housekeeping methods for managing app states.
     * Launch app can be used for bringing running apps to the foreground
     * or launching a new instance of an app.
     */
    public void launchApp(String appId, String appName) {

        Log.d(TAG, "launchApp: Launching : " + appName);
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Create new fragment instances only if the app is being launched for the first time
        // Check in the app manager cache
        if(null == appCache.get(appId)) {

            Log.d(TAG, "launchApp: Creating new app instance : " + appName);
            DelAppContainerFragment delAppContainerFragment =
                    new DelAppContainerFragment(appId, appName);
            appCache.put(appId, delAppContainerFragment);
            appNameMap.put(appId, appName);

            Log.d(TAG, "launchApp: Adding to transaction");
            Log.d("MainActivity", "launchApp: Del APP fragment ID : "
                    + delAppContainerFragment.getId());

            // last parameter is the app tag
            transaction.add(R.id.host_fragment, appCache.get(appId), appId);
        }

        DelAppContainerFragment app = (DelAppContainerFragment) appCache.get(appId);
        if(app.isHidden()) {
            Log.d(TAG, "launchApp: Showing app : " + appName);
            transaction.show(appCache.get(appId));
            app.setAppTitle();
        } else if(app.isVisible()) {
            Log.d(TAG, "launchApp: App visible : " + appName);
            app.setAppTitle();
        }

        transaction.hide(fragmentManager.findFragmentByTag(Constants.HOST_VIEW));
        transaction.commit();
    }

    /**
     * Display running apps inside a dialog
     */
    public void showRunningApps() {
        if(null != fragmentManager && null != appNameMap) {
            RunningAppsDialogFragment runningApps = new RunningAppsDialogFragment();
            runningApps.show(fragmentManager, "RUNNING_APPS");    
        } else {
            Log.d(TAG, "showRunningApps: Empty app list");
        }
    }

    /**
     * Terminate application
     *
     * @param appId
     */
    public void terminateApp(String appId) {

        if(null == appCache.get(appId)) {
            Log.d(TAG, "terminateApp: Invalid termination request. App doesn't exist.");
            return;
        }

        // App exists - hide, detach and remove from map
        // Has the potential to show a blank screen -> Maybe have an observer to check on open apps
        // and if closed, show the services view
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if(appCache.get(appId).isVisible()) {
            transaction.hide(appCache.get(appId));
            transaction.detach(appCache.get(appId));
        }

        transaction.remove(appCache.get(appId));
        appCache.remove(appId);
        appNameMap.remove(appId);
        transaction.commit();
    }

    /**
     * Kill all apps - usually called when logging out
     */
    public void terminateAllApps() {

        ArrayList<String> appIds = new ArrayList<>();
        // Need to push into another list, else this will throw
        // a concurrentmodificationexception
        for(String appId : appCache.keySet()) {
            Log.d(TAG, "terminateAllApps: Terminating  ---> " + appId);
            appIds.add(appId);
        }

        for(String appId : appIds) {
            terminateApp(appId);
        }
    }

    /**
     * Hide any apps that may be visible on the interface
     *
     */
    public void hideAllApps() {

        // Check passes only when no app has been launched
        if(null == fragmentManager) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        for(Map.Entry<String, Fragment> app : appCache.entrySet()) {
            Log.d(TAG, "hideAllApps: Checking : " + app.getKey());
            if(app.getValue().isVisible()) {
                Log.d(TAG, "hideAllApps: Hiding : " + app.getKey());
                transaction.hide(app.getValue());
            }
        }

        Log.d(TAG, "hideAllApps: Hiding all running apps.");
        transaction.commit();
    }
}
