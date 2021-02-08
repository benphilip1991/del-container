package com.del.delcontainer.services.interfaces;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Interface for defining device data processing behavior
 * Implement for all services handling data from peripherals
 */
public interface PeripheralDataServiceIntf {

    /**
     * Inject Gatt object for implemented service - pass in address and fetch
     * object from DeviceManager
     * @param deviceAddress
     */
    void setDevice(String deviceAddress);

    /**
     * Implement in custom data manager for handling data fetch
     * @param enable
     */
    void manageDataProvider(boolean enable);

    /**
     * Implement and pass to gatt utils for collecting data
     * @param characteristic
     */
    void fetchData(BluetoothGattCharacteristic characteristic);
}
