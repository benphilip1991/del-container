package com.del.delcontainer.ui.sources;

import android.bluetooth.BluetoothDevice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class SourcesModel extends ViewModel {

    private MutableLiveData<ArrayList<BluetoothDevice>> bluetoothDeviceList;

    public LiveData<ArrayList<BluetoothDevice>> getDevices() {

        if(bluetoothDeviceList == null) {
            bluetoothDeviceList = new MutableLiveData<>();
            getBluetoothDevices();
        }

        return bluetoothDeviceList;
    }

    /**
     * Function to get a list of devices that can be connected.
     *
     */
    private void getBluetoothDevices() {

    }
}