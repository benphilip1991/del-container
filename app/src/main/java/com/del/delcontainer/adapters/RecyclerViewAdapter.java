package com.del.delcontainer.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<BluetoothDevice> devices;
    private Context mContext;

    private DeviceClickListener deviceClickListener;

    public RecyclerViewAdapter(Context mContext, ArrayList<BluetoothDevice> devices,
                               DeviceClickListener deviceClickListener) {
        this.devices = devices;
        this.mContext = mContext;
        this.deviceClickListener = deviceClickListener;
    }

    /**
     * Function responsible for inflating the view
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_listitem, parent, false);

        ViewHolder viewHolder = new ViewHolder(view, deviceClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");

        holder.deviceName.setText(devices.get(position).getName()
                + " " + devices.get(position).getAddress()); // Fetch names from the list of devices
        holder.device = devices.get(position);
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
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView deviceName;
        BluetoothDevice device;
        RelativeLayout bleLayout;

        DeviceClickListener deviceClickListener;

        public ViewHolder(@NonNull View itemView, DeviceClickListener deviceClickListener) {
            super(itemView);

            deviceName = itemView.findViewById(R.id.device_name);
            bleLayout = itemView.findViewById(R.id.BLE_layout);
            this.deviceClickListener = deviceClickListener;

            itemView.setOnClickListener(this);
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
     *
     */
    public interface DeviceClickListener {
        void onDeviceClick(int position);
    }
}
