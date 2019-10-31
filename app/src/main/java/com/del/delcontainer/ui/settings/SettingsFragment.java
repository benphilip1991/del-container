package com.del.delcontainer.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.del.delcontainer.R;
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
        settingsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        sendBroadcast();
        return root;
    }

    private void sendBroadcast() {
        Log.d(TAG, "sendBroadcast: ");
        Intent intent = new Intent();
        intent.setAction(Constants.EVENT_DEVICE_DATA);
        LocalBroadcastManager lmb = LocalBroadcastManager.getInstance(this.getContext().getApplicationContext());
        getActivity().getApplicationContext().sendBroadcast(intent);
    }
}