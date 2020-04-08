package com.del.delcontainer.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.del.delcontainer.R;
import com.del.delcontainer.repositories.AuthRepository;
import com.del.delcontainer.ui.dialogs.LogoutDialogFragment;
import com.del.delcontainer.ui.login.LoginActivity;
import com.del.delcontainer.ui.login.LoginStateRepo;
import com.del.delcontainer.utils.Constants;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private SettingsViewModel settingsViewModel;
    private AuthRepository authRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authRepository = AuthRepository.getInstance(getContext());
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView textView = root.findViewById(R.id.text_settings);
        final TextView userName = root.findViewById(R.id.user_name);
        final Button logoutButton = root.findViewById(R.id.logout_button);
        final LinearLayout profileButton = (LinearLayout) root.findViewById(R.id.profile_button);
        final LinearLayout emergencyButton = (LinearLayout) root.findViewById(R.id.emergency_button);
        final LinearLayout privacyButton = (LinearLayout) root.findViewById(R.id.privacy_button);
        final LinearLayout dataButton = (LinearLayout) root.findViewById(R.id.data_button);
        final LinearLayout notificationButton = (LinearLayout) root.findViewById(R.id.notification_button);
        final LinearLayout helpButton = (LinearLayout) root.findViewById(R.id.help_button);

        getFirstName();

        // Attach observer to the viewmodel
        settingsViewModel.getFirstName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                userName.setText(s);
            }
        });

        logoutButton.setOnClickListener((v) -> {
            LogoutDialogFragment logoutDialog = new LogoutDialogFragment();
            logoutDialog.show(getFragmentManager(), "logout");
        });

        profileButton.setOnClickListener((v) -> {
            Toast.makeText(this.getContext(), "Launching Profile", Toast.LENGTH_SHORT).show();
        });

        emergencyButton.setOnClickListener((v) -> {
            Toast.makeText(this.getContext(), "Launching Emergency Contact", Toast.LENGTH_SHORT).show();
        });

        privacyButton.setOnClickListener((v) -> {
            Toast.makeText(this.getContext(), "Launching Privacy", Toast.LENGTH_SHORT).show();
        });

        dataButton.setOnClickListener((v) -> {
            Toast.makeText(this.getContext(), "Launching Data & Storage", Toast.LENGTH_SHORT).show();
        });

        notificationButton.setOnClickListener((v) -> {
            Toast.makeText(this.getContext(), "Launching Notifications", Toast.LENGTH_SHORT).show();
        });

        helpButton.setOnClickListener((v) -> {
            Toast.makeText(this.getContext(), "Launching Help", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    /**
     * Calls the del-api service to get the first name linked to the current user
     */
    private void getFirstName(){
        //settingsViewModel.getUserFirstName(authRepository.getAccessToken(), authRepository.getUserId());
        settingsViewModel.getUserFirstName(LoginStateRepo.getInstance().getToken(), LoginStateRepo.getInstance().getUserId());
    }
}