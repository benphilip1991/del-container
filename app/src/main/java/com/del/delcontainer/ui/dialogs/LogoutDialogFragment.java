package com.del.delcontainer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.repositories.AuthRepository;
import com.del.delcontainer.ui.login.LoginStateRepo;

public class LogoutDialogFragment extends DialogFragment {

    private static final String TAG = "LogoutDialogFragment";
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sign Out");
        builder.setMessage("Are you sure you want to sign out?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {

            Log.d(TAG, "onCreateDialog: Signing out");
            LoginStateRepo.getInstance().setUserId(null);
            LoginStateRepo.getInstance().setToken(null);

            // Clear auth info
            AuthRepository.getInstance(getContext().getApplicationContext()).clearAuthInfo();

            // Kill all running apps
            DelAppManager.getInstance().terminateAllApps();
            DelAppManager.getInstance().clearFragmentManager();

            // Clear the stack and return to login activity
            getActivity().finish();

        }).setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });

        return builder.create();
    }
}
