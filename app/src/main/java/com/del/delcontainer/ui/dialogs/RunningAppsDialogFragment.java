package com.del.delcontainer.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.adapters.RunningAppsListViewAdapter;
import com.del.delcontainer.managers.DelAppManager;

import java.util.HashMap;

public class RunningAppsDialogFragment extends AppCompatDialogFragment {

    private static final String TAG = "RunningAppsDialogFragme";
    private RunningAppsListViewAdapter runningAppsListViewAdapter;
    private HashMap<String, String> appNameMap;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.runningapps_dialog, null);
        builder.setView(view).setTitle("Running Apps");

        RecyclerView recyclerView = view.findViewById(R.id.running_apps_view);
        runningAppsListViewAdapter = new RunningAppsListViewAdapter(
                DelAppManager.getInstance().getAppNameMap(),
                (appId) -> {
                    DelAppManager.getInstance().terminateApp(appId);
                    runningAppsListViewAdapter.createAppNameList();
                    runningAppsListViewAdapter.notifyDataSetChanged();
                });
        recyclerView.setAdapter(runningAppsListViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        return builder.create();
    }
}
