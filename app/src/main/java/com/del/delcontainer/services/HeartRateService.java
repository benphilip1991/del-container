package com.del.delcontainer.services;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.del.delcontainer.utils.Constants;

import java.util.Vector;

/**
 * Handle heart rate requests from applications
 * Only enable data fetch when required.
 */
public class HeartRateService implements PeripheralDataServiceIntf {

    private static final String TAG = "HeartRateService";
    private static HeartRateService instance = null;
    private BluetoothGatt gatt = null;

    // Buffer for storing HR values data
    Vector<Integer> dataBuffer = new Vector<>();

    // Needs to be singleton as the same instance will be used in
    // both HeartRateDataHandler and GattUtils
    private HeartRateService() { ; }

    public static synchronized HeartRateService getInstance() {
        if(null == instance) {
            instance = new HeartRateService();
        }
        return instance;
    }

    /**
     * Set the BluetoothGatt object to be used in the service
     * @param gatt
     */
    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    /**
     * Get latest HR average value.
     * @return
     */
    public int getLatestHRAverage() {
        int avg = 0;
        for(Integer val : dataBuffer) {
            avg += val;
        }
        avg /= dataBuffer.size();
        dataBuffer.clear();

        return avg;
    }


    /**
     * Start HR update - returns true if a device is connected and if
     * updates can proceed. Else return false
     * @return boolean
     */
    public boolean startHRUpdate() {
        boolean res = false;
        if(null != gatt) {
            manageDataProvider(true);
            res = true;
        }

        return res;
    }

    /**
     * Stop HR update
     */
    public void stopHRUpdate() {
        manageDataProvider(false);
    }

    /**
     * Control device data fetch - set enable to true for enabling notifications
     * which allows peripherals to notify the app of data updates
     *
     * @param enable
     */
    @Override
    public void manageDataProvider(boolean enable) {
        BluetoothGattCharacteristic characteristic = null;

        if(null == gatt) {
            throw new RuntimeException(TAG + "manageDataProvider - Invalid Gatt object");
        }

        Log.d(TAG, "manageDataProvider - Getting Characteristics of HR Provider");
        characteristic = gatt.getService(Constants.HEART_RATE_SERVICE)
                .getCharacteristic(Constants.HEART_RATE_MEASUREMENT_CHAR);

        if (null != characteristic) {

            Log.d(TAG, "manageDataProvider - Enabling Characteristics " +
                    "Notifications for: " + characteristic.getUuid());
            gatt.setCharacteristicNotification(characteristic, enable);

            // Write descriptor to enable Notifications
            BluetoothGattDescriptor descriptor = characteristic.
                    getDescriptor(Constants.CLIENT_CHARACTERISTIC_CONFIG);

            if (enable) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            gatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Fetch data from given characteristic.
     * This method will be called from the Gatt Callback -> onCharacteristicChanged
     * @param characteristic
     */
    @Override
    public void fetchData(BluetoothGattCharacteristic characteristic) {

        // Get the flags and determine if the value is UINT8 or UINT16
        int flag = characteristic.getProperties();
        int format = -1;
        Integer hr = 0;

        // Get value and push into buffer
        if ((flag & 0x01) != 0) {
            Log.d(TAG, "onCharacteristicChanged: Heart Rate in UINT16 format");
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
        } else {
            Log.d(TAG, "onCharacteristicChanged: Heart Rate in UINT8 format");
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
        }

        hr = characteristic.getIntValue(format, 1);
        Log.d(TAG, "Heart Rate : " + hr.toString());

        dataBuffer.add(hr);
    }
}
