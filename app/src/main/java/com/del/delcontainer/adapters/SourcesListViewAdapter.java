package com.del.delcontainer.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.services.BLEDataManagerService;

import java.util.ArrayList;

public class SourcesListViewAdapter extends RecyclerView.Adapter<SourcesListViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private final ArrayList<BluetoothDevice> devices;
    private final Context mContext;

    private final DeviceClickListener deviceClickListener;

    public SourcesListViewAdapter(Context mContext, ArrayList<BluetoothDevice> devices,
                                  DeviceClickListener deviceClickListener) {
        this.devices = devices;
        this.mContext = mContext;
        this.deviceClickListener = deviceClickListener;
    }

    /**
     * Function responsible for inflating the view
     *
     * @param parent   view group parent
     * @param viewType view type
     * @return return a view holder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sources_listitem, parent, false);

        return new ViewHolder(view, deviceClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");
        holder.deviceName.setText(devices.get(position).getName()); // Fetch names from the list of devices
        holder.deviceAddress.setText(devices.get(position).getAddress());
        holder.device = devices.get(position);
        //Set button text based on connection status
        if (BLEDataManagerService.checkBluetoothGattObjectExists
                (devices.get(position).getAddress())) {
            holder.connectButton.setText(R.string.disconnect);
            holder.connectButton.setBackgroundResource(R.drawable.custom_warning_rounded_button);
            holder.connectButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorWarningDark));
        } else {
            holder.connectButton.setText(R.string.connect);
            holder.connectButton.setBackgroundResource(R.drawable.custom_primary_rounded_button);
            holder.connectButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
    }

    @Override
    public int getItemCount() {

        return devices.size();
    }

    /**
     * Hold the widgets in the memory - of each individual
     * entry (all BLE devices)
     * Need to list out items in the list
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView deviceName;
        TextView deviceAddress;
        BluetoothDevice device;
        Button connectButton;
        RelativeLayout bleLayout;

        DeviceClickListener deviceClickListener;

        public ViewHolder(@NonNull View itemView, DeviceClickListener deviceClickListener) {
            super(itemView);

            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
            bleLayout = itemView.findViewById(R.id.ble_layout);
            connectButton = itemView.findViewById(R.id.connect_button);
            this.deviceClickListener = deviceClickListener;

            connectButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "Clicked on : " + device.getName() + " "
                    + device.getAddress());
            deviceClickListener.onDeviceClick(getAdapterPosition());
        }
    }

    /**
     * Interface to help detect and interpret an item click
     */
    public interface DeviceClickListener {
        void onDeviceClick(int position);
    }
}
