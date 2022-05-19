package com.del.delcontainer.managers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.del.delcontainer.database.DelDatabase;
import com.del.delcontainer.database.dao.SensorRecordDao;
import com.del.delcontainer.database.entities.SensorRecord;
import com.del.delcontainer.handlers.AccelerometerDataHandler;
import com.del.delcontainer.handlers.HeartRateDataHandler;
import com.del.delcontainer.handlers.LocationDataHandler;
import com.del.delcontainer.handlers.PedometerDataHandler;
import com.del.delcontainer.repositories.UserServicesRepository;
import com.del.delcontainer.storage.FileStorage;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.CustomMutableLiveData;
import com.del.delcontainer.utils.apiUtils.pojo.LinkedApplicationDetails;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Data handler to manage data storage/retrieval from
 * the database.
 * <p>
 * This file interfaces the mini apps to the stored data
 * and allows services to store data in the db.
 */
public class DataManager {
    private static final String TAG = "DataManager";

    // Map of callback requests resource -> dictionary of (appId -> callback)
    static HashMap<String, HashMap<String, String>> dataRequestMap;

    // Map of logger requests resource -> number of apps requiring data
    static HashMap<String, Integer> sensorLoggerFlags;

    //Object to store installed apps and permissions
    private static CustomMutableLiveData<UserServicesRepository> userServicesRepository =
            new CustomMutableLiveData<>();

    //Singleton instance of the DEL DataManager
    private static DataManager dataManager;

    //Object for app-specific storage
    private static FileStorage fileStorage;

    //Object for sensor log retrieval
    private static SensorRecordDao sensorRecordDao;

    //List of linked applications including permissions
    private static ArrayList<LinkedApplicationDetails> linkedApps;

    private DataManager(Context context) {
        dataRequestMap = new HashMap<>();
        sensorLoggerFlags = new HashMap<>();
        userServicesRepository.postValue(UserServicesRepository.getInstance());
        fileStorage = new FileStorage(context);
        DelDatabase database = DelDatabase.getInstance(context);
        sensorRecordDao = database.sensorRecordDao();

        new Handler(Looper.getMainLooper()).post(() -> {
            userServicesRepository.observeForever((userServicesRepository) -> {
                Log.d(TAG, "Observing for changes");
                //Running commands that are triggered when the application list changes
                linkedApps = userServicesRepository.getUserServicesList();
            });
        });
    }

    public static void initDataManager(Context context) {
        if (null == dataManager) {
            dataManager = new DataManager(context);
        }
    }

    public static DataManager getInstance() {
        return dataManager;
    }

    /**
     * Method to return the linked
     * @return linkedApps
     */
    public static ArrayList<LinkedApplicationDetails> getUserAppsList() {
        return linkedApps;
    }

    /**
     * Method to delete callback resource requests associated to an app
     *
     * @param resource
     * @param appId
     */
    public void removeCallbackRequests(String resource, String appId) {
        dataRequestMap.get(resource).remove(appId);
    }

    /**
     * Method to get all callback requests for a resource
     *
     * @param resource
     */
    public HashMap<String, String> getCallBackRequests(String resource) {
        return dataRequestMap.get(resource);
    }

    /**
     * Method to check if data from a resource needs to be logged
     *
     * @param resource
     */
    public static boolean getLoggerRequestFlag(String resource) {
        return sensorLoggerFlags.get(resource) != null && sensorLoggerFlags.get(resource) > 0;
    }

    /**
     * Method to get app-specific storage JSON strings
     *
     * @param appId
     */
    public static String getAppData(String appId) {
        return fileStorage.readFile(appId);
    }

    /**
     * Method to save app-specific storage JSON strings
     *
     * @param appId
     */
    public static void setAppData(String appId, String content) {
        fileStorage.writeFile(appId, content);
    }

    /**
     * Method to set application callback requests for a resource
     *
     * @param appId
     * @param requests
     */
    public void setCallBackRequests(String appId, ArrayList<JSONObject> requests) {
        try {
            for (JSONObject request : requests) {
                String resource = request.getString(Constants.RESOURCE);
                String callback = request.getString(Constants.CALLBACK);
                if (validatePermissions(appId, resource)) {
                    if (null == dataRequestMap.get(resource)) {
                        dataRequestMap.put(resource, new HashMap<String, String>());
                    }
                    Log.d(TAG, "setCallBackRequests: Setting " + resource
                            + " callback request for service : " + appId);
                    dataRequestMap.get(resource).put(appId, callback);
                    startProviderTask(resource);

                } else {
                    Log.d(TAG, "setCallBackRequests: No permission to set " + resource
                            + "callback request for service : " + appId);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "setCallBackRequests: Error while parsing callback requests");
        }
    }

    /**
     * Method to set/remove application data logging requests for a resource
     *
     * @param appId
     * @param requests
     */
    public static void setSensorLoggerRequests(String appId, ArrayList<JSONObject> requests) {
        try {
            for (JSONObject request : requests) {
                String resource = request.getString(Constants.RESOURCE);
                boolean toggle = request.getBoolean(Constants.TOGGLE);
                if (validatePermissions(appId, resource)) {
                    Integer curFlag;
                    if (!sensorLoggerFlags.containsKey(resource))
                        curFlag = 0;
                    else
                        curFlag = sensorLoggerFlags.get(resource);

                    if (!toggle && curFlag > 0) {
                        Log.d(TAG, "setSensorLoggerRequests: Removing " + resource
                                + " logger request for service : " + appId);
                        sensorLoggerFlags.put(resource, curFlag - 1);
                    } else {
                        Log.d(TAG, "setSensorLoggerRequests: Setting " + resource
                                + " logger request for service : " + appId);
                        sensorLoggerFlags.put(resource, curFlag + 1);
                    }
                } else {
                    Log.d(TAG, "setSensorLoggerRequests: No permission to set " + resource
                            + " logger request for service : " + appId);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "setSensorLoggerRequests: Error while parsing logger requests");
        }
    }

    /**
     * Method to get all application data logs for a resource
     *
     * @param appId
     * @param resource
     */
    public static String getSensorLogs(String appId, String resource) {
        if (validatePermissions(appId, resource)) {
            Gson gson = new Gson();
            List<SensorRecord> sensorRecords = sensorRecordDao.getAllSensorRecords(resource);
            Log.d(TAG, "getSensorLogs: Sending " + resource + " logs to app " + appId);
            return gson.toJson(sensorRecords);
        } else {
            Log.d(TAG, "getSensorLogs: No permissions to send " + resource
                    + " logs to app " + appId);
            return null;
        }
    }

    /**
     * Method to get date filtered application data logs for a resource
     *
     * @param appId
     * @param resource
     * @param start
     * @param end
     */
    public static String getSensorLogs(String appId, String resource, String start, String end) {
        if (validatePermissions(appId, resource)) {
            Gson gson = new Gson();
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date startDate = format.parse(start);
                Date endDate = format.parse(end);
                List<SensorRecord> sensorRecords = sensorRecordDao
                        .getSensorRecordsBetweenDates(resource, startDate, endDate);
                Log.d(TAG, "getSensorLogs: Sending " + resource + " logs to app " + appId);
                return gson.toJson(sensorRecords);
            } catch (Exception e) {
                Log.d(TAG, "getSensorLogs: Error while parsing date for " + appId);
            }
        } else {
            Log.d(TAG, "getSensorLogs: No permissions to send " + resource
                    + " logs to app " + appId);
        }
        return null;
    }

    /**
     * Logging utility for sensor handlers to store sensor records
     * in the database
     *
     * @param sensor
     * @param reading
     */
    public static void logSensorRecord(String sensor, String reading) {
        Log.d(TAG, "LogSensorRecord: Logged record for sensor:" + sensor);
        new SensorEventLoggerTask()
                .execute(new SensorRecord(new Date(), sensor, reading));
    }

    /**
     * Method to validate application permissions for a resource
     *
     * @param appId
     * @param resource
     */
    private static boolean validatePermissions(String appId, String resource) {
        for (LinkedApplicationDetails app : linkedApps) {
            if (app.getApplicationId().equals(appId) &&
                    app.getApplicationPermissions().contains(resource))
                return true;
        }
        return false;
    }

    /**
     * Method to start resource providers
     *
     * @param resource
     */
    public static void startProviderTask(String resource) {
        switch (resource) {
            case Constants.ACCESS_LOCATION:
                if (!LocationDataHandler.getInstance().isRunning())
                    LocationDataHandler.getInstance().startLocationProviderTask();
                break;
            case Constants.ACCESS_PEDOMETER:
                if (!PedometerDataHandler.getInstance().isRunning())
                    PedometerDataHandler.getInstance().startStepDataProviderTask();
                break;
            case Constants.ACCESS_HEART_RATE:
                if (!HeartRateDataHandler.getInstance().isRunning())
                    HeartRateDataHandler.getInstance().startHRProviderTask();
                break;
            case Constants.ACCESS_ACCELEROMETER:
                if (!AccelerometerDataHandler.getInstance().isRunning())
                    AccelerometerDataHandler.getInstance().startAccelerometerDataProviderTask();
                break;
            default:
        }
    }

    /**
     * Async storage of sensor logs
     */
    private static class SensorEventLoggerTask extends AsyncTask<SensorRecord, Void, Void> {
        @Override
        protected Void doInBackground(SensorRecord... sensorRecord) {
            // log the value
            sensorRecordDao.insert(sensorRecord[0]);
            return null;
        }
    }
}
