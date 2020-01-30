package com.del.delcontainer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.del.delcontainer.database.entities.Heart;
import com.del.delcontainer.repositories.HeartRateRepository;
import com.del.delcontainer.utils.Constants;

import java.util.Calendar;
import java.util.Date;

public class DelBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "DelBroadcastReceiver";
    HeartRateRepository hrRepository;

    public DelBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        hrRepository = HeartRateRepository.getInstance(context);
        Log.d(TAG, "onReceive: Received event : " + intent.getAction());

        // Store fetched app ids in the database or store device data
        if(action.equals(Constants.EVENT_APP_REGISTERED)) {
            Log.d(TAG, "onReceive: App registration event received : " + action);

            // Can do something here

        } else if(action.equals(Constants.EVENT_DEVICE_DATA)) {

            // TODO: pass this data to registered apps / store in local database
            Log.d(TAG, "onReceive: Sensor data received : " +
                    intent.getStringExtra(Constants.DATA_TYPE) + " : " +
                    intent.getIntExtra(Constants.DATA_VALUE, 0));

            if(intent.getStringExtra(Constants.DATA_TYPE).equals(Constants.HR_DATA)) {
                storeHeartRateData(intent.getIntExtra(Constants.DATA_VALUE, 0));
            }
        }
    }

    /**
     * Method to store incoming data values to the database
     * Dirty approach - stores values every second
     *
     * TODO: fix storage time
     * TODO: use data manager to store data. Do NOT call repository directory
     * @param value
     */
    private void storeHeartRateData(int value) {

        Date date = Calendar.getInstance().getTime();
        Heart heart = new Heart(date.toString(), value);

        Log.d(TAG, "storeHeartRateData: Storing HR info " + date.toString() + " | " + value);
        hrRepository.addHeartRateInfo(heart);
    }
}
