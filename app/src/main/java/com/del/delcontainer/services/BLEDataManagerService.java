package com.del.delcontainer.services;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.DeviceManager;
import com.del.delcontainer.utils.GattUtils;


public class BLEDataManagerService extends IntentService {

    private static final String TAG = "BLEDataManagerService";
    private static DeviceManager deviceManager = DeviceManager.getDeviceManager();
    private static GattUtils gattUtils = new GattUtils();


    public BLEDataManagerService() {
        super("BLEDataManagerService");
    }

    public static boolean checkBluetoothGattObjectExists(String deviceAddress) {

        return deviceManager.getBluetoothGattObjects().get(deviceAddress) != null ? true : false;
    }

    /**
     * Automatically invoked when this service is started
     * by starting the intent.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        BluetoothDevice device = null;
        if (null != intent) {

            try {
                device = intent.getExtras().getParcelable(Constants.BLE_DEVICE);

            } catch (NullPointerException e) {
                Log.e(TAG, "onHandleIntent: Null Pointer Exception" + e.getMessage());
            }

            // Check if disconnect or connection
            if (intent.getStringExtra(Constants.OPERATION).equals(Constants.DISCONNECT)) {
                disconnectBLEDevice(device.getAddress());

            } else if (intent.getStringExtra(Constants.OPERATION).equals(Constants.CONNECT)) {

                // Connection request
                Log.d(TAG, "onHandleIntent: Device details : "
                        + device.getName() + " "
                        + device.getAddress());

                validateDeviceAndFetchData(device);
            }
        }
    }

    /**
     * Check the type of device and start data fetch operations
     *
     * @param device
     */
    private void validateDeviceAndFetchData(BluetoothDevice device) {

        // Check if the device is not null
        if (null != device) {

            Log.d(TAG, "Getting data from : " + device.getName());

            deviceManager.getBluetoothDevices().put(device.getAddress(), device);
            BluetoothGatt bluetoothGatt = device.connectGatt(
                    this, true, gattUtils.gattCallback);

            // Make the ID a unique key - device MAC address
            deviceManager.getBluetoothGattObjects()
                    .put(bluetoothGatt.getDevice().getAddress(), bluetoothGatt);
        }
    }


    /**
     * Method to terminate connection to a device.
     *
     * @param deviceAddress
     */
    private void disconnectBLEDevice(String deviceAddress) {

        BluetoothGatt gatt = deviceManager.getBluetoothGattObjects().get(deviceAddress);

        if (null == gatt) {
            Log.d(TAG, "disconnectBLEDevice: gatt object not found");
            return;
        }

        Log.d(TAG, "disconnectBLEDevice: Closing connection to : "
                + gatt.getDevice().getName());
        gatt.close();

        deviceManager.getBluetoothGattObjects().remove(deviceAddress);
    }
}
