package com.del.delcontainer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.del.delcontainer.R;
import com.del.delcontainer.utils.Constants;



public class MessageDialogFragment extends DialogFragment {
    private static final String TAG = "InformationDialogFragment";
    private String message;
    private int dialogLayoutResource;
    TextView dialogText;

    public MessageDialogFragment(String type, String message) {
        super();
        this.message = message;
        switch (type){
            case Constants.DIALOG_ERROR:
                dialogLayoutResource = R.layout.error_dialog;
                break;
            case Constants.DIALOG_WARNING:
                dialogLayoutResource = R.layout.warning_dialog;
                break;
            default:
                dialogLayoutResource = R.layout.message_dialog;
                break;
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(dialogLayoutResource, null);
        dialogText = dialogView.findViewById(R.id.info_dialog_message);
        dialogText.setText(message);
        // Inflate and set the layout for the dialog
        builder.setView(dialogView);
        Button cancel = (Button) dialogView.findViewById(R.id.dismiss_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        // Create the AlertDialog object and return it
        return dialog;
    }
}
