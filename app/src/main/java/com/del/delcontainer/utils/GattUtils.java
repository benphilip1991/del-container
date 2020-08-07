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

import com.del.delcontainer.managers.DeviceManager;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

/**
 * Manage BLE Gatt Callbacks here. At present, only an HR device
 * is being managed
 */
public class GattUtils {

    private static final String TAG = "GattUtils";
    private static DeviceManager deviceManager = DeviceManager.getDeviceManager();
    private static Context context;

    // Buffer for storing BLE data
    byte[] dataBuffer = new byte[4096];

    public GattUtils(Context context) {
        this.context = context;
    }

    /**
     * Map devices to provided services. Called when devices are
     * connected to and before BLE services are used
     *
     * @param gatt
     */
    private void manageDeviceServices(BluetoothGatt gatt) {

        Log.d(TAG, "manageDeviceServices: Managing services");
        for (BluetoothGattService service : gatt.getServices()) {
            Log.d(TAG, "manageDeviceServices: Available Service : " + service.getUuid().toString());

            // Devices may have more than one service. Need to manage them while making sure
            // it doesn't break the following services
            // Hashmap doesn't support multiple values for the same key.
            // We're in a way overwriting existing values - causing trouble later.
            if (service.getUuid().equals(Constants.HEART_RATE_SERVICE)) {

                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), Constants.HR_PROVIDER);

            } else if (service.getUuid().equals(Constants.UART_SERVICE)) {

                Log.d(TAG, "manageDeviceServices: Found UART provider.");
                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), Constants.UART_PROVIDER);

            } else if (service.getUuid().equals(Constants.ISSC_PROP_SERVICE)) {
                Log.d(TAG, "manageDeviceServices: Found ISSC proprietary service");
                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), Constants.ISSC_PROVIDER);
            } else if (service.getUuid().equals(UUID.fromString("38ef3bd5-a6ef-46db-924b-ed5c71b30699"))) {

                Log.d(TAG, "manageDeviceServices: Rezas device");
                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), "SORD");
            } else {

                // [GENERAL]
                //deviceManager.getBluetoothServiceMap().put(gatt.getDevice().getAddress(), "GENERAL");
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

        if (null != rCharacteristic) {

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
     * Handle ISSC proprietary devices. In this instance, MySignals
     * sensors including their smart scale
     *
     * @param gatt
     */
    private void manageISSCProvider(BluetoothGatt gatt) {

        Log.d(TAG, "manageISSCProvider: Connecting to ISSC provider : " + gatt.getDevice().getName());

        for (BluetoothGattCharacteristic chars : gatt.getService(Constants.ISSC_PROP_SERVICE).getCharacteristics()) {
            Log.d(TAG, "manageISSCProvider: Characteristics -> " + chars.getUuid());

            if (null != chars.getDescriptor(Constants.CLIENT_CHARACTERISTIC_CONFIG)) {
                Log.d(TAG, "manageISSCProvider: Enabling notifications");
                gatt.setCharacteristicNotification(chars, true);
                BluetoothGattDescriptor descriptor = chars.getDescriptor(Constants.CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
    }

    private void manageSordDevice(BluetoothGatt gatt) {

        BluetoothGattCharacteristic characteristic = null;

        Log.d(TAG, "manageSordDevice: Getting sord chars");
        characteristic = gatt.getService(UUID.fromString("38ef3bd5-a6ef-46db-924b-ed5c71b30699"))
                .getCharacteristic(UUID.fromString("940778f8-8cff-4cd7-a5dc-e337edd72ec9"));

        if (null != characteristic) {

            Log.d(TAG, "manageHRProvider - Enabling Characteristics " +
                    "Notifications for: " + characteristic.getUuid());
            gatt.setCharacteristicNotification(characteristic, true);

        }
    }

    /**
     * Handle general BLE devices
     *
     * @param gatt
     */
    private void manageGeneralDevice(BluetoothGatt gatt) {
        Log.d(TAG, "manageGeneralDevice: Connecting to general BLE device : " + gatt.getDevice().getName());

        for (BluetoothGattService service : gatt.getServices()) {

            Log.d(TAG, "manageGeneralDevice: Service ----> " + service.getUuid());
            // Each service may have more than one characteristic
            for (BluetoothGattCharacteristic chars : service.getCharacteristics()) {

                Log.d(TAG, "manageGeneralDevice: \t\tCharacteristics ----> " + chars.getUuid());
            }
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

    /**
     * Function to read UART received values.
     *
     * @param characteristic
     */
    private void fetchSordData(BluetoothGattCharacteristic characteristic) {

        Log.d(TAG, "fetchSordData: Read Value     : " + characteristic.getValue());
        byte[] readChars = characteristic.getValue();

        StringBuilder sb = new StringBuilder();
        for (byte value : readChars) {
            sb.append(value + " ");
        }
        Log.d(TAG, "fetchSordData: Byte array in string : " + sb.toString());

        byte[] data = characteristic.getValue();
        String hex = bytesToHex(data);
        Log.d(TAG, "fetchSordData: Converted data : " + hex);

        // Need to keep converting to float till we get 300 or 993 rounded value
        float[] values = toFloatArray(readChars);
        for (float value : values) {
            Log.d(TAG, "fetchSordData: Float Value : " + value + "    |     Rounded : " + Math.round(value));

            if (300 == Math.round(value) || 993 == Math.round(value)) {
                Log.d(TAG, "fetchSordData: START_BIT\n\n\n\n");
            }
        }
    }

    public float[] toFloatArray(byte[] byteArray) {
        int nos = Float.SIZE / Byte.SIZE;
        float[] values = new float[byteArray.length / nos];
        for (int i = 0; i < values.length; i++) {
            values[i] = ByteBuffer.wrap(byteArray, i * nos, nos)
                    .order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }
        return values;
    }

    /**
     * Convert byte values to hexadecimal
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (sb.length() > 0) {
                sb.append(':');
            }
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
            if (state == GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //Connected - discover services if there is no bonding issue

                    Log.d(TAG, "OnConnectionStateChange - Discovering services");
                    gatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "onConnectionStateChange: Closing interface");
                    //gatt.close();
                }
            } else {
                // error
                gatt.close();
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

            // TODO: right now, this is based on an assumption that there's only one device.
            // TODO: the block fails on having multiple entries in the servicemap
            // Is it an HR device?
            if (null != deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())) {
                if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
                        .equals(Constants.HR_PROVIDER)) {

                    manageHRProvider(gatt);

                } else if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
                        .equals(Constants.UART_PROVIDER)) {

                    manageUARTProvider(gatt);
                } else if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
                        .equals(Constants.ISSC_PROVIDER)) {

                    manageISSCProvider(gatt);
                } else if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
                        .equals("SORD")) {
                    manageSordDevice(gatt);
                } else {

                    //[GENERAL]
                    //manageGeneralDevice(gatt);
                }
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

            } else if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
                    .equals("SORD")) {

                fetchSordData(characteristic);
            } else {

                fetchUARTData(characteristic);
            }
        }
    };
}
