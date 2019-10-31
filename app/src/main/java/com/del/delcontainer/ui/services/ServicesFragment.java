package com.del.delcontainer.ui.services;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.adapters.AvailableAppListViewAdapter;
import com.del.delcontainer.adapters.InstalledAppListViewAdapter;
import com.del.delcontainer.ui.fragments.DelAppContainerFragment;
import com.del.delcontainer.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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

    // TODO: Need to somehow make sure that apps being added does not result in the
    // TODO: previous ones getting destroyed
    /**
     * Check if the app already exists in the fragment stack and bring it to the front.
     * Use the FragmentTraction show and hide methods for existing fragments
     * Else, create a new fragment object with the required app and launch.
     *
     * @param position
     */
    @Override
    public void onAppClick(int position) {
        Log.d(TAG, "onAppClick: launching " + installedAppList.get(position));
        Toast.makeText(getContext(), "Launching " + installedAppList.get(position), Toast.LENGTH_SHORT).show();

        // instead of launching a new activity, hide the current one and load a new fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        DelAppContainerFragment delAppContainerFragment = new DelAppContainerFragment(UUID.fromString("23666d29-7254-48b9-8104-862de11bdd75"), installedAppList.get(position));
        transaction.add(R.id.nav_host_fragment, delAppContainerFragment, installedAppList.get(position));
        transaction.addToBackStack(installedAppList.get(position));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }
}