package com.del.delcontainer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.del.delcontainer.utils.Constants;

public class DelBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "DelBroadcastReceiver";

    public DelBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.d(TAG, "onReceive: Received event : " + intent.getAction());

        if(action.equals(Constants.EVENT_APP_REGISTERED)) {
            Log.d(TAG, "onReceive: App registration event received : " + action);

            // Can do something here

        }
    }
}
