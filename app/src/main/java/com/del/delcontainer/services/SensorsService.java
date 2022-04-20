package com.del.delcontainer.services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

public class SensorsService implements SensorEventListener {

    private static final String TAG = "SensorsService";
    private static SensorsService instance = null;
    private Context context = null;
    private SensorManager sensorManager;

    private float stepCount = 0;
    private float accelerometerData[];

    private SensorsService() {
    }

    public static synchronized SensorsService getInstance() {
        if (null == instance) {
            instance = new SensorsService();
        }
        return instance;
    }

    public void initSensorService(Context context) {
        if (null != context) {
            this.context = context;
        }

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorsList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensors : sensorsList) {
            Log.d(TAG, "initSensorService: Sensor : " + sensors.getName());
        }
    }

    /**
     * Enable accelerometer data when required
     */
    public void enableAccelerometerData() {

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(null != sensor) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "enableAccelerometerData: Accelerometer enabled");
        } else {
            Log.d(TAG, "enableAccelerometerData: Accelerometer not found");
        }
    }

    /**
     * Disable accelerometer data when done
     */
    public void disableAccelerometerData() {
        sensorManager.unregisterListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        Log.d(TAG, "disableAccelerometerData: Accelerometer disabled");
    }

    /**
     * Enable gyroscope data when required
     */
    public void enableGyroscopeData() {

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(null != sensor) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "enableGyroscopeData: Gyroscope enabled");
        } else {
            Log.d(TAG, "enableGyroscopeData: Gyroscope not found");
        }
    }

    /**
     * Disable gyroscope data when done
     */
    public void disableGyroscopeData() {
        sensorManager.unregisterListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        Log.d(TAG, "disableGyroscopeData: Gyroscope disabled");
    }


    /**
     * Enable step counter when required
     */
    public void enableStepCounter() {

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (null != sensor) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "enableStepCounter: Step counter enabled");
        } else {
            Log.d(TAG, "enableStepCounter: Step counter not found");
        }
    }

    /**
     * Disable step counter when done
     */
    public void disableStepCounter() {
        sensorManager.unregisterListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER));
        Log.d(TAG, "disableStepCounter: Step counter disabled");
    }

    /**
     * Methods implemented from the SensorEventListener interface
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerData = sensorEvent.values;
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.d(TAG, "onSensorChanged: Step counter : " + sensorEvent.values[0]);
            stepCount = sensorEvent.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public float getStepCount() {
        return stepCount;
    }

    public float[] getAccelerometerData() {
        return accelerometerData;
    }
}
