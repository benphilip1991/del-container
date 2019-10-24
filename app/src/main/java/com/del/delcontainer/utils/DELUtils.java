package com.del.delcontainer.utils;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * App apis exposed to the DEL web applications
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
}
