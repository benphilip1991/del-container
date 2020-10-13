package com.del.delcontainer.services;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.del.delcontainer.managers.DeviceManager;
import com.del.delcontainer.services.interfaces.PeripheralDataServiceIntf;
import com.del.delcontainer.utils.Constants;

import java.util.Vector;

/**
 * Handle heart rate requests from applications
 * Only enable data fetch when required.
 */
public class HeartRateService implements PeripheralDataServiceIntf {

    private static final String TAG = "HeartRateService";
    private static HeartRateService instance = null;
    private String deviceAddress = null;
    private DeviceManager deviceManager = DeviceManager.getDeviceManager();

    // Buffer for storing HR values data
    Vector<Integer> dataBuffer = new Vector<>();

    // Needs to be singleton as the same instance will be used in
    // both HeartRateDataHandler and GattUtils
    private HeartRateService() {
        ;
    }

    public static synchronized HeartRateService getInstance() {
        if (null == instance) {
            instance = new HeartRateService();
        }
        return instance;
    }

    /**
     * Get latest HR average value.
     *
     * @return
     */
    public int getLatestHRAverage() {
        int avg = 0;
        for (Integer val : dataBuffer) {
            avg += val;
        }
        if(dataBuffer.size() > 0) {
            avg /= dataBuffer.size();
            dataBuffer.clear();
        }

        return avg;
    }

    /**
     * Start HR update - returns true if a device is connected and if
     * updates can proceed. Else return false
     *
     * @return boolean
     */
    public boolean startHRUpdate() {
        Log.d(TAG, "startHRUpdate: Starting updates");
        if (null != deviceManager.getBluetoothGattObjects().get(deviceAddress)) {
            manageDataProvider(true);
            return true;
        }
        return false;
    }

    /**
     * Stop HR update
     */
    public void stopHRUpdate() {
        Log.d(TAG, "stopHRUpdate: Stopping updates");
        manageDataProvider(false);
    }

    /**
     * Check if the device is active (users may disconnect while data is being transferred)
     * @return
     */
    public boolean isDeviceActive() {
        if(null != deviceManager.getBluetoothGattObjects().get(deviceAddress)) {
            return true;
        } else {
            return false;
        }
    }

    //---------------------------------------------------------
    // PeripheralDataServiceIntf implementation
    //---------------------------------------------------------

    /**
     * Set the BluetoothGatt object to be used in the service
     *
     * @param deviceAddress
     */
    @Override
    public void setDevice(String deviceAddress) {
        this.deviceAddress = deviceAddress;
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
        BluetoothGatt gatt = deviceManager.getBluetoothGattObjects().get(deviceAddress);

        if (null == gatt) {
            //throw new RuntimeException(TAG + "manageDataProvider - Invalid Gatt object");
            Log.d(TAG, "manageDataProvider: GATT Object : NULL");
            return;
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
                Log.d(TAG, "manageDataProvider: Enabling HR sensor notifications");
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                Log.d(TAG, "manageDataProvider: Disabling HR sensor notifications");
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            gatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Fetch data from given characteristic.
     * This method will be called from the Gatt Callback -> onCharacteristicChanged
     *
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
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
        }

        hr = characteristic.getIntValue(format, 1);
        Log.d(TAG, "Heart Rate : " + hr.toString());

        dataBuffer.add(hr);
    }
}
