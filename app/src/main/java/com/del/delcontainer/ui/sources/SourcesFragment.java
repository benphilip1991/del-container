package com.del.delcontainer.ui.sources;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.adapters.SourcesListViewAdapter;
import com.del.delcontainer.services.BLEDataManagerService;
import com.del.delcontainer.ui.dialogs.ConnectDeviceDialogFragment;
import com.del.delcontainer.utils.Constants;

import java.util.ArrayList;

public class SourcesFragment extends Fragment
        implements SourcesListViewAdapter.DeviceClickListener,
        ConnectDeviceDialogFragment.DialogClickListener {

    private static final String TAG = "SourcesFragment";

    // BLE Constraints
    private BluetoothAdapter bluetoothAdapter;

    SourcesListViewAdapter adapter;
    private ArrayList<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sources, container, false);

        /**
         * Setup an onClick listener for the sources fragment
         */
        TextView rescan = rootView.findViewById(R.id.rescan_devices_button);
        rescan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "rescanBluetooth: rescanning for bluetooth devices");
                Toast.makeText(getActivity(), "Rescanning for devices", Toast.LENGTH_SHORT)
                        .show();
                setupBluetooth();
            }
        });

        /**
         * Start searching for bluetooth devices and when the user selects a device,
         * connect (and add to device list) and stay in the view till the user moves
         * to another tab.
         *
         * The device data can be fetched by services that store the readings in the
         * database till an 'app' requests it
         */
        setupBluetooth();

        // Pass in the created root view object to ensure
        // inflated views can be found.
        initRecyclerView(rootView);

        return rootView;
    }

    // Set up recycler view
    private void initRecyclerView(View view) {

        RecyclerView recyclerView = view.findViewById(R.id.ble_recycler_view);
        adapter = new SourcesListViewAdapter(getContext(), bluetoothDeviceList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Override the onPause callback in this fragment to
     * ensure scanning stops as soon as the user leaves
     * the sources tab.
     */
    @Override
    public void onPause() {
        super.onPause();
        scanBLEDevices(false);
    }

    /**
     * Setup bluetooth and fetch available BLE devices
     */
    public void setupBluetooth() {
        Log.d(TAG, "Setting up Bluetooth scan.");

        final BluetoothManager bluetoothManager = (BluetoothManager) getContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (null == bluetoothAdapter || !bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, Constants.REQUEST_ENABLE_BT);
        }

        scanBLEDevices(true);
    }

    /**
     * Start device scan
     */
    private void scanBLEDevices(final boolean enable) {

        Log.d(TAG, "Scanning for bluetooth peripherals");
        if (enable) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, Constants.SCAN_PERIOD);

            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (null != bluetoothDevice && bluetoothDevice.getName() != null &&
                            !bluetoothDeviceList.contains((bluetoothDevice))) {
                        Log.d(TAG, "Adding new Device : " + bluetoothDevice.getName()
                                + " | " + bluetoothDevice.getAddress());
                        bluetoothDeviceList.add(bluetoothDevice); // Scan devices and add to list

                        // Notifications should be enabled if more devices are added
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    /**
     * Implement interface from Adapter to hold selected device
     *
     * @param position
     */
    @Override
    public void onDeviceClick(int position) {

        Log.d(TAG, "Device selected");
        scanBLEDevices(false);

        ConnectDeviceDialogFragment connectDialog = new ConnectDeviceDialogFragment(
                bluetoothDeviceList.get(position), this);
        connectDialog.show(getFragmentManager(), "ble_connect");
    }

    /**
     * Implemented handler to manage user button presses - Connect or Disconnect device
     *
     * @param device
     * @param operation
     */
    @Override
    public void onDialogButtonPressed(BluetoothDevice device, String operation) {

        Log.d(TAG, "onDialogButtonPressed: " + device.getName() + " " + operation);

        if (operation.equals(Constants.CONNECT)) {
            Toast.makeText(getContext(), "Connecting to "
                    + device.getName(), Toast.LENGTH_SHORT).show();
        } else if (operation.equals(Constants.DISCONNECT)) {
            Toast.makeText(getContext(), "Disconnecting from "
                    + device.getName(), Toast.LENGTH_SHORT).show();
        }
        // FIX: Adapter updates recycler view before BLEDataManagerService connection/disconnection
        adapter.notifyDataSetChanged();

        Intent deviceSelectedIntent = new Intent();
        deviceSelectedIntent.setAction(Constants.EVENT_APP_REGISTERED);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .sendBroadcast(deviceSelectedIntent);

        // Start BLEDataManagerService to handle BLE device operations
        Intent intent = new Intent(getContext(), BLEDataManagerService.class);
        intent.putExtra(Constants.BLE_DEVICE, device);
        intent.putExtra(Constants.OPERATION, operation);

        getActivity().startService(intent);
    }
}