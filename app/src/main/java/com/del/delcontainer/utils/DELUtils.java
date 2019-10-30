package com.del.delcontainer.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

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
}
