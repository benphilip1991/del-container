package com.del.delcontainer;

import static com.del.delcontainer.R.id.host_fragment;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.del.delcontainer.managers.DataManager;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.managers.DelNotificationManager;
import com.del.delcontainer.receivers.DelBroadcastReceiver;
import com.del.delcontainer.services.LocationService;
import com.del.delcontainer.services.SensorsService;
import com.del.delcontainer.ui.chatbot.ChatbotButtonHandler;
import com.del.delcontainer.ui.dialogs.ChatBotDialogFragment;
import com.del.delcontainer.ui.services.ServicesFragment;
import com.del.delcontainer.ui.settings.SettingsFragment;
import com.del.delcontainer.ui.sources.SourcesFragment;
import com.del.delcontainer.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

public class DelContainerActivity extends AppCompatActivity {
    private static final String TAG = "DelContainerActivity";

    IntentFilter intentFilter;

    // May have to move to another global fragment manager
    private HashMap<Integer, Fragment> containerViewMap = new HashMap<>();
    ChatBotDialogFragment chatBotDialogFragment;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == savedInstanceState) {
            Log.d(TAG, "onCreate: Activity never existed before");
            registerBroadcastReceiver();
            BottomNavigationView navView = findViewById(R.id.nav_view); // BottomNavigationView object in activity_main xml file.
            navView.setOnNavigationItemSelectedListener(navigationListener); // attach the custom listener
            chatBotDialogFragment = ChatBotDialogFragment.newInstance();

            /**
             * Explicitly setting the default view to the services on first run
             * This ensures the containerViewMap tracks the first view as well.
             * TODO Issue - launches two fragment instances
             */
            navView.setSelectedItemId(R.id.navigation_services);

            FloatingActionButton chatButton = findViewById(R.id.chat_button);
            chatButton.setOnClickListener(v -> showChatBot());

            // Experimental for now
            verifyAndGetPermissions();
            initServices();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Stopping location updates");
        super.onPause();
        LocationService locationService = LocationService.getInstance();
        locationService.stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: Resuming location service");
        LocationService locationService = LocationService.getInstance();
        locationService.setActivity(this);
        locationService.startLocationUpdates();

        Intent intent = getIntent();
        if(null != intent) {
            String appId = intent.getStringExtra(Constants.INTENT_APP_ID);
            Log.d(TAG, "onCreate: Notification App Id : " + appId);
        }
    }

    /**
     * To show the close action bar menu button
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_options, menu);

        return true;
    }

    /**
     * Handle close button clicks
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_running_apps:
                Toast.makeText(this, "Showing Running Apps", Toast.LENGTH_SHORT)
                        .show();
                DelAppManager.getInstance().showRunningApps();
                return true;
            case R.id.close_all_apps:
                Toast.makeText(this, "Closing All Apps", Toast.LENGTH_SHORT).show();
                DelAppManager.getInstance().terminateAllApps();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialize container services
     */
    private void initServices() {

        DelAppManager.getInstance().setFragmentManager(this.getSupportFragmentManager());
        DelNotificationManager.getInstance().initNotificationManager(this);
        DataManager.initDataManager(this);

        LocationService locationService = LocationService.getInstance();
        locationService.initLocationService(this);

        SensorsService sensorsService = SensorsService.getInstance();
        sensorsService.initSensorService(this);
    }

    /**
     * Click handler for the chat bot pop-up button
     */
    public void showChatBot() {
        FragmentManager fm = getSupportFragmentManager();
        chatBotDialogFragment.show(fm, "chatBotDialogFragment");
    }

    /**
     * Create a custom navigation handler instead of using AppBarConfiguration
     * This approach gives more flexibility with 'app' lifecycle management
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navigationListener =
            (menuItem) -> {

                this.setTitle(R.string.app_name);

                Fragment selectedFragment = null;
                switch (menuItem.getItemId()) {
                    case R.id.navigation_services:
                        Log.d(TAG, "onNavigationItemSelected: Selected Services");
                        if (containerViewMap.get(R.id.navigation_services) == null) {
                            containerViewMap.put(R.id.navigation_services, new ServicesFragment());
                        }
                        selectedFragment = containerViewMap.get(R.id.navigation_services);
                        this.setTitle(R.string.title_services);
                        ChatbotButtonHandler.getInstance().toggleChatButtonVisibility(this, true);
                        break;
                    case R.id.navigation_sources:
                        Log.d(TAG, "onNavigationItemSelected: Selected Sources");
                        if (containerViewMap.get(R.id.navigation_sources) == null) {
                            containerViewMap.put(R.id.navigation_sources, new SourcesFragment());
                        }
                        selectedFragment = containerViewMap.get(R.id.navigation_sources);
                        this.setTitle(R.string.title_sources);
                        break;
                    case R.id.navigation_settings:
                        Log.d(TAG, "onNavigationItemSelected: Selected Settings");
                        if (containerViewMap.get(R.id.navigation_settings) == null) {
                            containerViewMap.put(R.id.navigation_settings, new SettingsFragment());
                        }
                        selectedFragment = containerViewMap.get(R.id.navigation_settings);
                        this.setTitle(R.string.title_settings);
                        break;
                }
                DelAppManager.getInstance().hideAllApps();

                // Get the fragment manager and begin transaction. But only hide/show them
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                for (Map.Entry<Integer, Fragment> appView : containerViewMap.entrySet()) {
                    if (appView.getValue().isAdded() && appView.getValue().isVisible()) {
                        transaction.hide(appView.getValue());
                    }
                }

                if (!selectedFragment.isAdded()) {
                    transaction.add(host_fragment, selectedFragment, Constants.HOST_VIEW);
                }

                // Make view visible
                transaction.show(selectedFragment).commit();
                return true;
            };


    /**
     * Register broadcast receivers
     */
    private void registerBroadcastReceiver() {

        DelBroadcastReceiver delBroadcastReceiver = new DelBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.EVENT_DEVICE_DATA);
        intentFilter.addAction(Constants.EVENT_APP_REGISTERED);

        // Register the receiver in the localbroadcastmanager
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(delBroadcastReceiver, intentFilter);
    }

    /**
     * Check if permissions have been granted and obtain if not
     * provided. Add permissions to the list here as needed.
     */
    private void verifyAndGetPermissions() {

        String[] permissionList = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
        };

        for (String permission : permissionList) {
            getPermissions(permission);
        }
    }

    /**
     * Fetch permissions
     *
     * @param permission
     */
    private void getPermissions(String permission) {

        Log.d(TAG, "getPermissions: Fetching permission : " + permission);
        // if permission is not already granted, get it here,
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{permission},
                    Constants.PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Handle the back button - this method makes sure the app runs in the background
     * when the user presses back - this saves the container state.
     */
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
