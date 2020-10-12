package com.del.delcontainer.services;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Abstract class used for defining device data processing behavior
 */
public interface PeripheralDataServiceIntf {

    /**
     * Inject Gatt object to data manager
     * @param gatt
     */
    void setGatt(BluetoothGatt gatt);

    /**
     * Override in custom data manager for handling data fetch
     * @param enable
     */
    void manageDataProvider(boolean enable);

    /**
     * Override and pass to gatt utils for collecting data
     * @param characteristic
     */
    void fetchData(BluetoothGattCharacteristic characteristic);

}
