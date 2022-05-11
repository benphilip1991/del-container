package com.del.delcontainer.utils;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.del.delcontainer.managers.DeviceManager;
import com.del.delcontainer.services.HeartRateService;
import com.del.delcontainer.services.interfaces.PeripheralDataServiceIntf;

import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;

/**
 * Manage BLE Gatt Callbacks here. At present, only an HR device
 * is being managed
 */
public class GattUtils {

    private static final String TAG = "GattUtils";
    private static final DeviceManager deviceManager = DeviceManager.getDeviceManager();
    private static Context context;

    private PeripheralDataServiceIntf peripheralDataService;
    private HandleConnectedDevices handleConnectedDevices;

    public GattUtils(Context context, HandleConnectedDevices handleConnectedDevices) {
        this.context = context;
        this.handleConnectedDevices = handleConnectedDevices;
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
            // TODO: Maybe get the users to choose type of device instead of overwriting like this?
            if (service.getUuid().equals(Constants.HEART_RATE_SERVICE)) {

                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), Constants.HR_PROVIDER); // Maybe get the user to select type of device?

            } else if (service.getUuid().equals(Constants.UART_SERVICE)) {

                Log.d(TAG, "manageDeviceServices: Found UART provider.");
                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), Constants.UART_PROVIDER);

            } else if (service.getUuid().equals(Constants.ISSC_PROP_SERVICE)) {
                Log.d(TAG, "manageDeviceServices: Found ISSC proprietary service");
                deviceManager.getBluetoothServiceMap()
                        .put(gatt.getDevice().getAddress(), Constants.ISSC_PROVIDER);
            } else {

                // [GENERAL]
                //deviceManager.getBluetoothServiceMap().put(gatt.getDevice().getAddress(), "GENERAL");
            }

            // Add more as devices are added
        }
    }

    /**
     * Setup appropriate service
     * TODO: Instantiate appropriate services depending on device type
     *
     * @param deviceAddress
     */
    private void setupDataService(String deviceAddress) {

        // What about other devices?
        peripheralDataService = HeartRateService.getInstance();
        peripheralDataService.setDevice(deviceAddress);
    }
    
    /**
     * Handle UART communication
     *
     * @param gatt
     */
    private void manageUARTProvider(BluetoothGatt gatt) {

        BluetoothGattCharacteristic rCharacteristic;
        BluetoothGattCharacteristic tCharacteristic = null;

        if (ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this.context,
                    Constants.REQUEST_BLUETOOTH_PERM, Toast.LENGTH_LONG).show();
            return;
        }

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

        if (ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this.context,
                    Constants.REQUEST_BLUETOOTH_PERM, Toast.LENGTH_LONG).show();
            return;
        }

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

    /**
     * Handle general BLE devices
     *
     * @param gatt
     */
    private void manageGeneralDevice(BluetoothGatt gatt) {
        if (ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this.context,
                    Constants.REQUEST_BLUETOOTH_PERM, Toast.LENGTH_LONG).show();
            return;
        }

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
    // Begin gatt callback
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

            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(context,
                        Constants.REQUEST_BLUETOOTH_PERM, Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(TAG, "OnConnectionStateChange : " + newState);
            if (state == GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //Connected - discover services if there is no bonding issue
                    Log.d(TAG, "OnConnectionStateChange - Discovering services");
                    gatt.discoverServices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "onConnectionStateChange: Closing interface");
                    gatt.close();
                    handleConnectedDevices.clearConnectedDevice(gatt.getDevice().getAddress(),
                            "Connection State Change: closing interface");
                }
            } else {
                // error
                Log.d(TAG, "onConnectionStateChange: Connection error");
                gatt.close();
                handleConnectedDevices.clearConnectedDevice(gatt.getDevice().getAddress(),
                        "Connection State Change: Connection error");
            }
        }

        /**
         * Called by discoverServices internally when new services
         * are discovered
         *
         * @param gatt
         * @param status
         */
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//
//            manageDeviceServices(gatt);
//
//            // TODO: right now, this is based on an assumption that there's only one device.
//            // TODO: the block fails on having multiple entries in the servicemap
//            // Is it an HR device?
//            if (null != deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())) {
//                if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
//                        .equals(Constants.HR_PROVIDER)) {
//
//                    manageHRProvider(gatt);
//
//                } else if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
//                        .equals(Constants.UART_PROVIDER)) {
//
//                    manageUARTProvider(gatt);
//                } else if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
//                        .equals(Constants.ISSC_PROVIDER)) {
//
//                    manageISSCProvider(gatt);
//                } else {
//
//                    //[GENERAL]
//                    //manageGeneralDevice(gatt);
//                }
//            }
//        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            for (BluetoothGattService service : gatt.getServices()) {
                Log.d(TAG, "onServicesDiscovered: Available Service : " +
                        service.getUuid().toString());
            }

            // Assumption for now - Only HR device is available. May ask the user to choose type of device later.
            //manageHRProvider(gatt);

            // Setup appropriate manager objects
            setupDataService(gatt.getDevice().getAddress());

            // Second parameter - set to false to disable data update by default
            peripheralDataService.manageDataProvider(false);
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
            peripheralDataService.fetchData(characteristic);

//            // If the device is a heart rate provider, fetch the characteristic values
//            if (deviceManager.getBluetoothServiceMap().get(gatt.getDevice().getAddress())
//                    .equals(Constants.HR_PROVIDER)) {
//
//                fetchHRValues(characteristic);
//
//            } else {
//
//                fetchUARTData(characteristic);
//            }
        }
    };

    /**
     * Notify implementing class that a device connection has failed
     */
    public interface HandleConnectedDevices {
        void clearConnectedDevice(String deviceAddress, String message);
    }
}
