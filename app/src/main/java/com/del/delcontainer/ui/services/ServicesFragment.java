package com.del.delcontainer.ui.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.activities.AppViewActivity;
import com.del.delcontainer.adapters.AvailableAppListViewAdapter;
import com.del.delcontainer.adapters.InstalledAppListViewAdapter;
import com.del.delcontainer.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

public class ServicesFragment extends Fragment implements InstalledAppListViewAdapter.AppClickListener {

    private static final String TAG = "ServicesFragment";

    InstalledAppListViewAdapter installedAppListViewAdapter;
    AvailableAppListViewAdapter availableAppListViewAdapter;

    private HashMap<String, HashMap<String, String>> availableAppDetails = new HashMap<>();
    private HashMap<String, Integer> appDetails = new HashMap<>();
    private ArrayList<String> installedAppList = new ArrayList<>();
    private ArrayList<String> availableAppList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_services, container, false);

        setupServices(rootView);
        return rootView;
    }

    private void setupServices(View view) {

        getInstalledAppDetails();
        getAvailableAppDetails();
        initRecyclerView(view);
    }

    private void getInstalledAppDetails() {

        appDetails.put("Heart Health", R.drawable.heart_health_icon);
        appDetails.put("Step Counter", R.drawable.step_counter);
        appDetails.put("Oximeter", R.drawable.default_app_icon);
        appDetails.put("My ECG", R.drawable.default_app_icon);

        for(HashMap.Entry<String, Integer> app : appDetails.entrySet()) {
            installedAppList.add(app.getKey());
        }
    }

    private void getAvailableAppDetails() {

        availableAppDetails.put("Spirometer App", getAppDetail(
                "Monitor your respiratory functions", R.drawable.lungs_icon));
        availableAppDetails.put("Oximeter App", getAppDetail(
                "Monitor blood oxygen saturation", R.drawable.spo2_icon));
        availableAppDetails.put("Core Temp", getAppDetail(
                "Monitor your core temperature", R.drawable.temperature_icon));
        availableAppDetails.put("Random App 1", getAppDetail(
                "Random description 1", R.drawable.default_app_icon));
        availableAppDetails.put("Random App 2", getAppDetail(
                "Random description 2", R.drawable.default_app_icon));
        availableAppDetails.put("Random App 3", getAppDetail(
                "Random description 3", R.drawable.default_app_icon));
    }

    private HashMap<String, String> getAppDetail(String appDescription, int resource) {

        HashMap<String, String> appDetail = new HashMap<>();
        appDetail.put(Constants.APP_DESCRIPTION, appDescription);
        appDetail.put(Constants.APP_IMAGE, Integer.toString(resource));

        return appDetail;
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.installedAppListView);
        installedAppListViewAdapter = new InstalledAppListViewAdapter(getContext(), appDetails, this);
        recyclerView.setAdapter(installedAppListViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));

        // Set Available apps
        RecyclerView recyclerViewAvailableApps = view.findViewById(R.id.availableAppListView);
        availableAppListViewAdapter = new AvailableAppListViewAdapter(getContext(), availableAppDetails);
        recyclerViewAvailableApps.setAdapter(availableAppListViewAdapter);
        recyclerViewAvailableApps.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onAppClick(int position) {
        Log.d(TAG, "onAppClick: launching " + installedAppList.get(position));
        Toast.makeText(getContext(), "Launching " + installedAppList.get(position), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getContext(), AppViewActivity.class);
        intent.putExtra(Constants.APP_IDENT, installedAppList.get(position));
        startActivity(intent);
    }
}