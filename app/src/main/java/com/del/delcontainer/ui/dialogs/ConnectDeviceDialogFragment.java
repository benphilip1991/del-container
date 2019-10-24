package com.del.delcontainer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.del.delcontainer.R;
import com.del.delcontainer.services.BLEDataManagerService;

public class ConnectDeviceDialogFragment extends DialogFragment {

    private static final String DISCONNECT  = "DISCONNECT";
    private static final String CONNECT     = "CONNECT";

    DialogClickListener dialogClickListener;
    BluetoothDevice device = null;

    /**
     * Dialog constructor. Accepts the selected bluetooth device along with
     * an instance of the interface implementation.
     *
     * @param device
     * @param dialogClickListener
     */
    public ConnectDeviceDialogFragment(BluetoothDevice device, DialogClickListener dialogClickListener) {
        this.device = device;
        this.dialogClickListener = dialogClickListener; // Instance of class implementing this interface
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.connect_bluetooth_devices_dialog_title);
        builder.setMessage(R.string.connect_bluetooth_devices);
        builder.setCancelable(true);

        String mainButtonText = "Connect";
        if(BLEDataManagerService
                .checkBluetoothGattObjectExists(device.getAddress())) {
            mainButtonText = "Disconnect";
        }

        // The main button is set as Connect or Disconnect depending on the device status
        // If the device is already connected, the dialog prompts for disconnection.
        builder.setPositiveButton(mainButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(BLEDataManagerService.checkBluetoothGattObjectExists
                                (device.getAddress())) {

                            dialogClickListener.onDialogButtonPressed(device, DISCONNECT);
                        } else {
                            dialogClickListener.onDialogButtonPressed(device, CONNECT);
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();

                    }
                });

        return builder.create();
    }

    /**
     * Interface to implement event callbacks that the host can use for
     * performing follow-up actions
     */
    public interface DialogClickListener {
        void onDialogButtonPressed(BluetoothDevice device, String operation);
    }
}
