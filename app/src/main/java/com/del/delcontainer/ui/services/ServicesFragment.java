package com.del.delcontainer.ui.services;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.adapters.AvailableAppListViewAdapter;
import com.del.delcontainer.adapters.InstalledAppListViewAdapter;
import com.del.delcontainer.ui.dialogs.MessageDialogFragment;
import com.del.delcontainer.ui.login.LoginStateRepo;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.managers.DelAppManager;

public class ServicesFragment extends Fragment {

    private static final String TAG = "ServicesFragment";

    InstalledAppListViewAdapter installedAppListViewAdapter;
    AvailableAppListViewAdapter availableAppListViewAdapter;

    ServicesViewModel servicesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: Service Fragment created");
        servicesViewModel = ViewModelProviders.of(this).get(ServicesViewModel.class);
        View rootView = inflater.inflate(R.layout.fragment_services, container, false);
        setupServices(rootView);

        return rootView;
    }

    private void setupServices(View view) {

        final TextView userName = view.findViewById(R.id.header_profile_text);
        getFirstName();

        // Attach observer to the viewmodel for username
        servicesViewModel.getFirstName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                userName.setText(s);
            }
        });

        servicesViewModel.getStatusObserver().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String type) {
                //Call dialog
                MessageDialogFragment infoDialog = new MessageDialogFragment(type,
                        servicesViewModel.getStatusMessage());
                infoDialog.show(getFragmentManager(), "information");
            }
        });

        getAppsList();
        initRecyclerView(view);
    }

    /**
     * Calls the del-api service to get the list of all linked services
     * and all available services
     */
    private void getAppsList() {

        servicesViewModel.getAllUserServices(LoginStateRepo.getInstance().getToken(),
                LoginStateRepo.getInstance().getUserId());
        servicesViewModel.getAllAvailableServices(LoginStateRepo.getInstance().getToken());
    }

    /**
     * Initialize recycler view and setup
     *
     * @param view
     */
    private void initRecyclerView(View view) {

        // Set available services fetched from del-api
        servicesViewModel.getServicesList().observe(this, (servicesList) -> {
            if (null != servicesList) {

                RecyclerView recyclerViewAvailableApps = view.
                        findViewById(R.id.available_app_list_view);
                availableAppListViewAdapter = new AvailableAppListViewAdapter(getContext(),
                        servicesList,
                        (position) -> {
                            /**
                             * Handle click events when the user taps the GET button on
                             * the available apps card. Add app to user's linked services.
                             */
                            Log.d(TAG, "initRecyclerView: Fetching app : " +
                                    servicesList.get(position).getApplicationName());
                            Toast.makeText(getContext(), "Getting App : " + servicesList
                                    .get(position).getApplicationName(), Toast.LENGTH_LONG).show();

                            try {
                                servicesViewModel.updateUserApplicationsList(
                                        LoginStateRepo.getInstance().getToken(),
                                        LoginStateRepo.getInstance().getUserId(),
                                        servicesList.get(position)
                                                .get_id(), Constants.APP_ADD);
                                installedAppListViewAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                recyclerViewAvailableApps.setAdapter(availableAppListViewAdapter);
                recyclerViewAvailableApps.setLayoutManager(new GridLayoutManager(getContext(), 1,
                        GridLayoutManager.VERTICAL, false));
            }
        });

        // Set linked services fetched from del-api
        servicesViewModel.getUserServicesList().observe(this, (userServicesList) -> {
            if (null != userServicesList) {

                RecyclerView recyclerView = view.
                        findViewById(R.id.installed_app_list_view);
                installedAppListViewAdapter = new InstalledAppListViewAdapter(getContext(),
                        userServicesList,
                        (position) -> {
                            /**
                             * Handle clicks events on each service card
                             * Check if the service already exists in the fragment stack and bring
                             * it to the foreground. If not, create a new fragment object.
                             */
                            Log.d(TAG, "onAppClick: launching " + userServicesList
                                    .get(position).getApplicationName());
                            Toast.makeText(getContext(), "Launching " + userServicesList
                                    .get(position).getApplicationName(), Toast.LENGTH_SHORT).show();

                            // Get fragment manager instance and launch app
                            FragmentManager fragmentManager = getActivity()
                                    .getSupportFragmentManager();
                            DelAppManager delAppManager = DelAppManager.getInstance();
                            delAppManager.setFragmentManager(fragmentManager);

                            // Launch app.
                            delAppManager.launchApp(
                                    userServicesList.get(position).getApplicationId(),
                                    userServicesList.get(position).getApplicationName(),
                                    userServicesList.get(position).getApplicationUrl());
                        },
                        (position, cardView) -> {
                            /**
                             * Listen for long click and provide option to delete app
                             * This function shows a chat_dialog menu with a delete option.
                             */
                            PopupMenu deletePopup = new PopupMenu(getContext(), cardView);
                            deletePopup.getMenuInflater().inflate(R.menu.delete_app_menu,
                                    deletePopup.getMenu());

                            deletePopup.setOnMenuItemClickListener((menuItem) -> {
                                if (menuItem.getItemId() == R.id.delete_app) {
                                    Toast.makeText(getContext(), "Deleting : " +
                                                    userServicesList.get(position)
                                                            .getApplicationName(),
                                            Toast.LENGTH_SHORT).show();
                                    try {
                                        servicesViewModel.updateUserApplicationsList(
                                                LoginStateRepo.getInstance().getToken(),
                                                LoginStateRepo.getInstance().getUserId(),
                                                userServicesList.get(position).getApplicationId(),
                                                Constants.APP_DELETE);
                                        installedAppListViewAdapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(),
                                                e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                return true;
                            });
                            // Menu set up. Show on long click
                            deletePopup.show();
                        });

                recyclerView.setAdapter(installedAppListViewAdapter);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2,
                        GridLayoutManager.HORIZONTAL, false));
            }
        });
    }
    /**
     * Calls the del-api service to get the first name linked to the current user
     */
    private void getFirstName(){
        servicesViewModel.getUserFirstName(LoginStateRepo.getInstance().getToken(),
                LoginStateRepo.getInstance().getUserId());
    }
    /**
     * Calls the del-api service to get the status of the view model processing
     */
    private void getStatusObserver(){
        servicesViewModel.getStatusObserver();
    }
}