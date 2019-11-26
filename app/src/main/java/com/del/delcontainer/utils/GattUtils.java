package com.del.delcontainer.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Manage BLE Gatt Callbacks here. At present, only an HR device
 * is being managed
 */
public class GattUtils {

    private static final String TAG = "GattUtils";
    private static DeviceManager deviceManager = DeviceManager.getDeviceManager();
    private static Context context;

    public GattUtils(Context context) {
        this.context = context;
        Log.d(TAG, "GattUtils: CONTEXT " + context.toString());
    }

    /**
     * Map devices to provided services
     *
     * @param gatt
     */
    private void manageDeviceServices(BluetoothGatt gatt) {

        for (BluetoothGattService service : gatt.getServices()) {
            Log.d(TAG, "manageDeviceServices: Available Service : " + service.getUuid().toString());

            if (service.getUuid().equals(Constants.HEART_RATE_SERVICE)) {

                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), Constants.HR_PROVIDER);

            } else if (service.getUuid().equals(Constants.UART_SERVICE)) {

                Log.d(TAG, "manageDeviceServices: Found UART provider.");
                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), Constants.UART_PROVIDER);

            }

            // Add more as devices are added
        }
    }

    /**
     * Manage heart rate provider
     *
     * @param gatt
     */
    private void manageHRProvider(BluetoothGatt gatt) {

        BluetoothGattCharacteristic characteristic = null;

        Log.d(TAG, "manageHRProvider - Getting Characteristics of HR Provider");
        characteristic = gatt.getService(Constants.HEART_RATE_SERVICE)
                .getCharacteristic(Constants.HEART_RATE_MEASUREMENT_CHAR);

        if (null != characteristic) {

            Log.d(TAG, "manageHRProvider - Enabling Characteristics " +
                    "Notifications for: " + characteristic.getUuid());
            gatt.setCharacteristicNotification(characteristic, true);


            // Write descriptor to enable Notifications
            BluetoothGattDescriptor descriptor = characteristic.
                    getDescriptor(Constants.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Handle UART communication
     *
     * @param gatt
     */
    private void manageUARTProvider(BluetoothGatt gatt) {

        BluetoothGattCharacteristic rCharacteristic;
        BluetoothGattCharacteristic tCharacteristic = null;

        Log.d(TAG, "manageUARTProvider: Got UART device : " + gatt.getDevice().getName());
        rCharacteristic = gatt.getService(Constants.UART_SERVICE).getCharacteristic(Constants.UART_RX);

        if(null != rCharacteristic) {

            Log.d(TAG, "manageUARTProvider: Got UART Characteristics");
            gatt.setCharacteristicNotification(rCharacteristic, true);

            // Write descriptor to enable notification
            BluetoothGattDescriptor descriptor = rCharacteristic.
                    getDescriptor(Constants.CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Function to fetch BLE HR characteristics from the connected device
     *
     * @param characteristic
     */
    private void fetchHRValues(BluetoothGattCharacteristic characteristic) {
        // Get the flags and determine if the value is UINT8 or UINT16
        int flag = characteristic.getProperties();
        int format = -1;
        Integer hr = 0;

        // Broadcast on HR read? Might not be efficient
        if ((flag & 0x01) != 0) {
            Log.d(TAG, "onCharacteristicChanged: Heart Rate in UINT16 format");
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
        } else {
            Log.d(TAG, "onCharacteristicChanged: Heart Rate in UINT8 format");
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
        }

        hr = characteristic.getIntValue(format, 1);
        Log.d(TAG, "Heart Rate : " + hr.toString());

        // Broadcast now?
        broadcastData(hr);
    }

    /**
     * Function to read UART received values.
     *
     * @param characteristic
     */
    private void fetchUARTData(BluetoothGattCharacteristic characteristic) {

        Log.d(TAG, "fetchUARTData: Read Value     : " + characteristic.getValue());
        byte[] data = characteristic.getValue();
        String hex = bytesToHex(data);
        Log.d(TAG, "fetchUARTData: Converted data : " + hex);
    }

    public static String bytesToHex(byte[] bytes) {

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Broadcast sensor data to the registered listeners.
     *
     * @param data
     */
    private void broadcastData(int data) {

        Intent intent = new Intent();
        intent.setAction(Constants.EVENT_DEVICE_DATA);
        intent.putExtra(Constants.DATA_TYPE, Constants.HR_DATA);
        intent.putExtra(Constants.DATA_VALUE, data);

        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
    }

    // -------------------------------------------------------------------
    // Begin gatt callbacks - multiple if necessary
    // -------------------------------------------------------------------
    /**
     * Define a GATT callback for use in gatt connections.
     * The callback provides methods for managing connected devices.
     */
    public BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        /**
         * Listen for device state change
         *
         * @param gatt
         * @param state
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int state, int newState) {

            Log.d(TAG, "OnConnectionStateChange : " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Connected - discover services
                Log.d(TAG, "OnConnectionStateChange - Discovering services");
                gatt.discoverServices();
            }
        }

        /**
         * Called by discoverServices internally when new services
         * are discovered
         *
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            manageDeviceServices(gatt);

            // Is it an HR device?
            if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
                    .equals(Constants.HR_PROVIDER)) {

                manageHRProvider(gatt);

            } else if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
                    .equals(Constants.UART_PROVIDER)) {

                manageUARTProvider(gatt);
            }
        }

        /**
         * Triggered by change in registered characteristic. Fetch data
         * from the connected devices
         *
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            Log.d(TAG, "OnCharacteristicChanged");

            // If the device is a heart rate provider, fetch the characteristic values
            if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
                    .equals(Constants.HR_PROVIDER)) {

                fetchHRValues(characteristic);

            } else {

                fetchUARTData(characteristic);
            }
        }
    };
}
