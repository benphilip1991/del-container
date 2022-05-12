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
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.del.delcontainer.R;
import com.del.delcontainer.adapters.AvailableAppListViewAdapter;
import com.del.delcontainer.adapters.InstalledAppListViewAdapter;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.ui.chatbot.ChatbotButtonHandler;
import com.del.delcontainer.ui.dialogs.InstallConfirmationDialogFragment;
import com.del.delcontainer.ui.dialogs.MessageDialogFragment;
import com.del.delcontainer.ui.login.LoginStateRepo;
import com.del.delcontainer.utils.Constants;

public class ServicesFragment extends Fragment {

    private static final String TAG = "ServicesFragment";

    InstalledAppListViewAdapter installedAppListViewAdapter;
    AvailableAppListViewAdapter availableAppListViewAdapter;

    MotionLayout motionLayout;
    ServicesViewModel servicesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: Service Fragment created");
        servicesViewModel = ViewModelProviders.of(this).get(ServicesViewModel.class);
        View rootView = inflater.inflate(R.layout.fragment_services, container, false);
        setupServices(rootView);

        motionLayout = rootView.findViewById(R.id.services_drawer);
        motionLayout.setTransitionListener(appDrawerTransitionListener);

        return rootView;
    }

    /**
     * New transition listener to hide/show the chat button when the app drawer is opened
     */
    private final MotionLayout.TransitionListener appDrawerTransitionListener = new MotionLayout.TransitionListener() {
        @Override
        public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
        }

        @Override
        public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {
        }

        @Override
        public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {

            if (motionLayout == getView().findViewById(R.id.services_drawer)) {
                if (currentId == R.id.app_drawer_anim_hidden) {
                    ChatbotButtonHandler.getInstance().toggleChatButtonVisibility(getActivity(), true);
                } else if (currentId == R.id.app_drawer_anim_visible) {
                    ChatbotButtonHandler.getInstance().toggleChatButtonVisibility(getActivity(), false);
                }
            }
        }

        @Override
        public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
        }
    };

    /**
     * Setup services - get user and app details and setup
     * views
     *
     * @param view root view of this fragment
     */
    private void setupServices(View view) {

        final TextView userName = view.findViewById(R.id.header_profile_text);
        getFirstName();

        // Attach observer to the viewmodel for username
        servicesViewModel.getFirstName().observe(this, (name) -> {
            userName.setText(name);
        });

        servicesViewModel.getStatusObserver().observe(this, (type) -> {
            //Call dialog
            MessageDialogFragment infoDialog = new MessageDialogFragment(type,
                    servicesViewModel.getStatusMessage());
            infoDialog.show(getFragmentManager(), "information");
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
                            /*
                             * Handle click events when the user taps the GET button on
                             * the available apps card. Prompt the permissions used by the
                             * application and confirms installation
                             */
                            InstallConfirmationDialogFragment installConfirmationDialog =
                                    new InstallConfirmationDialogFragment(
                                            servicesList.get(position).getApplicationPermissions(),
                                            () -> {
                                                /*
                                                 * Handles app installation on positive button click of
                                                 * dialog. Adds app to user's linked services.
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
                            installConfirmationDialog.show(getFragmentManager(), "installConfirmDialog");
                        });
                recyclerViewAvailableApps.setAdapter(availableAppListViewAdapter);
                recyclerViewAvailableApps.setLayoutManager(new GridLayoutManager(getContext(), 1,
                        GridLayoutManager.VERTICAL, false));
            }
        });

        // Set linked services fetched from del-api
        servicesViewModel.getUserServicesRepo().observe(this, (userServicesRepository) -> {
            if (null != userServicesRepository) {

                RecyclerView recyclerView = view.
                        findViewById(R.id.installed_app_list_view);
                installedAppListViewAdapter = new InstalledAppListViewAdapter(getContext(),
                        userServicesRepository.getUserServicesList(),
                        (position) -> {

                            ChatbotButtonHandler.getInstance().toggleChatButtonVisibility(getActivity(), false);
                            /*
                             * Handle clicks events on each service card
                             * Check if the service already exists in the fragment stack and bring
                             * it to the foreground. If not, create a new fragment object.
                             */
                            Log.d(TAG, "onAppClick: launching " + userServicesRepository
                                    .getUserServicesList().get(position).getApplicationName());
                            Toast.makeText(getContext(), "Launching " + userServicesRepository
                                            .getUserServicesList().get(position).getApplicationName(),
                                    Toast.LENGTH_SHORT).show();

                            // Get fragment manager instance and launch app
                            FragmentManager fragmentManager = getActivity()
                                    .getSupportFragmentManager();
                            DelAppManager delAppManager = DelAppManager.getInstance();
                            delAppManager.setFragmentManager(fragmentManager);

                            // Launch app.
                            delAppManager.launchApp(
                                    userServicesRepository.getUserServicesList()
                                            .get(position).getApplicationId(),
                                    userServicesRepository.getUserServicesList()
                                            .get(position).getApplicationName(),
                                    userServicesRepository.getUserServicesList()
                                            .get(position).getApplicationUrl());
                        },
                        (position, cardView) -> {
                            /*
                             * Listen for long click and provide option to delete app
                             * This function shows a chat_dialog menu with a delete option.
                             */
                            PopupMenu deletePopup = new PopupMenu(getContext(), cardView);
                            deletePopup.getMenuInflater().inflate(R.menu.delete_app_menu,
                                    deletePopup.getMenu());

                            deletePopup.setOnMenuItemClickListener((menuItem) -> {
                                if (menuItem.getItemId() == R.id.delete_app) {
                                    Toast.makeText(getContext(), "Deleting : " +
                                                    userServicesRepository.getUserServicesList()
                                                            .get(position).getApplicationName(),
                                            Toast.LENGTH_SHORT).show();
                                    try {
                                        servicesViewModel.updateUserApplicationsList(
                                                LoginStateRepo.getInstance().getToken(),
                                                LoginStateRepo.getInstance().getUserId(),
                                                userServicesRepository.getUserServicesList()
                                                        .get(position).getApplicationId(),
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
    private void getFirstName() {
        servicesViewModel.getUserFirstName(LoginStateRepo.getInstance().getToken(),
                LoginStateRepo.getInstance().getUserId());
    }
}