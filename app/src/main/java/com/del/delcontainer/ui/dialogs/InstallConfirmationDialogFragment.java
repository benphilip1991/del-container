package com.del.delcontainer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.adapters.PermissionListViewAdapter;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.apiUtils.pojo.ApplicationDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.LinearLayout.HORIZONTAL;


public class InstallConfirmationDialogFragment extends DialogFragment {
    private static final String TAG = "InstallConfirmationDialogFragment";
    private List<String> permissions;
    TextView dialogText;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    PermissionListViewAdapter permissionListViewAdapter;
    DialogClickListener dialogClickListener;

    public InstallConfirmationDialogFragment(List<String> permissions, DialogClickListener dialogClickListener) {
        super();
        this.permissions = permissions;
        this.dialogClickListener =dialogClickListener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        List<String> permissionsDescriptions = new ArrayList<>();

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.install_confirmation_dialog, null);

        if(permissions.size()>0){
            //Collating a list of descriptions for every permission
            for (String permission: permissions) {
                String test = Constants.PERM_DESCRIPTION.get(permission);
                permissionsDescriptions.add(test);
            }

            //Setting the recycler view adapter
            recyclerView = (RecyclerView) dialogView.findViewById(R.id.permission_recycler_view);
            linearLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            permissionListViewAdapter = new PermissionListViewAdapter(getContext(), permissionsDescriptions);
            recyclerView.setAdapter(permissionListViewAdapter);
            DividerItemDecoration itemDecor = new DividerItemDecoration(recyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(itemDecor);
        } else {
            dialogText = dialogView.findViewById(R.id.permission_dialog_message);
            dialogText.setText("This app does not require any permissions.");
        }



        // Inflate and set the layout for the dialog
        builder.setView(dialogView);
        Button okButton = (Button) dialogView.findViewById(R.id.ok_button);

        //Set button click listeners
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickListener.onPositiveButtonPressed();
                dismiss();
            }
        });
        Button cancelButton = (Button) dialogView.findViewById(R.id.dismiss_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    /**
     * Interface to implement event callbacks that the host can use for
     * performing follow-up actions
     */
    public interface DialogClickListener {
        void onPositiveButtonPressed();
    }
}
