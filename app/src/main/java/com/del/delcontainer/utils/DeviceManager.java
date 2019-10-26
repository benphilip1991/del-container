package com.del.delcontainer.utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import java.util.HashMap;

/**
 * Singleton class to manage bluetooth devices and services.
 * This pattern ensures only one single list of devices is
 * maintained throughout the app.
 */
public class DeviceManager {

    private static HashMap<String, BluetoothDevice> bluetoothDevices;   // Track connected devices
    private static HashMap<String, BluetoothGatt> bluetoothGattObjects; // Track GATT objects
    private static HashMap<String, String> bluetoothServiceMap;         // Map device service to device address

    private static DeviceManager deviceManager = new DeviceManager();

    private DeviceManager() {

        bluetoothDevices = new HashMap<>();
        bluetoothGattObjects = new HashMap<>();
        bluetoothServiceMap = new HashMap<>();
    }

    public static DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public HashMap<String, BluetoothDevice> getBluetoothDevices() {
        return bluetoothDevices;
    }

    public HashMap<String, BluetoothGatt> getBluetoothGattObjects() {
        return bluetoothGattObjects;
    }

    public HashMap<String, String> getBluetoothServiceMap() {
        return bluetoothServiceMap;
    }
}
