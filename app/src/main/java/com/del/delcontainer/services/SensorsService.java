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
    private Sensor sensor;

    private float stepCount = 0;

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

        enableStepCounter();
    }

    public void enableAccelerometerData() {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void disableAccelerometerData() {
        sensorManager.unregisterListener(this);
    }

    public void enableStepCounter() {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (null != sensor) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d(TAG, "enableStepCounter: Sensor not found");
        }
    }

    /**
     * Methods implemented from the SensorEventListener interface
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getRawAccelerometerData(sensorEvent);
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.d(TAG, "onSensorChanged: Step counter : " + sensorEvent.values[0]);
            stepCount = sensorEvent.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void getRawAccelerometerData(SensorEvent sensorEvent) {
        float[] values = sensorEvent.values;

        // Accelerometer movement in cartesian coordinates
        float x = values[0];
        float y = values[1];
        float z = values[2];

        Log.d(TAG, "getNormalizedAccelerometerData: (X, Y, Z) : (" + x + ", " + y + ", " + z + ")");
    }

    public float getStepCount() {
        return stepCount;
    }
}
