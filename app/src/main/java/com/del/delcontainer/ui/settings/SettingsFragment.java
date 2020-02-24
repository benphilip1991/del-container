package com.del.delcontainer.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.del.delcontainer.R;
import com.del.delcontainer.ui.dialogs.LogoutDialogFragment;
import com.del.delcontainer.ui.login.LoginActivity;
import com.del.delcontainer.ui.login.LoginStateRepo;
import com.del.delcontainer.utils.Constants;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private SettingsViewModel settingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView textView = root.findViewById(R.id.text_settings);
        final Button logoutButton = root.findViewById(R.id.logout_button);

        // Attach observer to the viewmodel
        settingsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        logoutButton.setOnClickListener((v) -> {
            LogoutDialogFragment logoutDialog = new LogoutDialogFragment();
            logoutDialog.show(getFragmentManager(), "logout");
        });

        return root;
    }
}