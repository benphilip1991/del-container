package com.del.delcontainer;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.del.delcontainer.database.DelDatabase;
import com.del.delcontainer.database.entities.UserProfile;
import com.del.delcontainer.receivers.DelBroadcastReceiver;
import com.del.delcontainer.services.LocationService;
import com.del.delcontainer.ui.services.ServicesFragment;
import com.del.delcontainer.ui.settings.SettingsFragment;
import com.del.delcontainer.ui.sources.SourcesFragment;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.DbHelper;
import com.del.delcontainer.utils.DelAppManager;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.del.delcontainer.R.id.nav_host_fragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // May have to move to another global fragment manager
    private HashMap<Integer, Fragment> containerViewMap = new HashMap<>();

    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerBroadcastReceiver();

        BottomNavigationView navView = findViewById(R.id.nav_view); // BottomNavigationView object in activity_main xml file.
        navView.setOnNavigationItemSelectedListener(navigationListener); // attach the custom listener

        // Fix to ake sure the app doesn't crash the container initially
        BottomNavigationItemView item = findViewById(R.id.navigation_services);
        item.performClick();

        verifyAndGetPermissions();

        LocationService locationService = LocationService.getInstance();
        locationService.initLocationService(this);
    }

    /**
     * Create a custom navigation handler instead of using AppBarConfiguration
     * This approach gives more flexibility with 'app' lifecycle management
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navigationListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()) {

                        case R.id.navigation_services:
                            Log.d(TAG, "onNavigationItemSelected: Selected Services");
                            if (containerViewMap.get(R.id.navigation_services) == null) {
                                containerViewMap.put(R.id.navigation_services, new ServicesFragment());
                            }
                            selectedFragment = containerViewMap.get(R.id.navigation_services);
                            break;
                        case R.id.navigation_sources:
                            Log.d(TAG, "onNavigationItemSelected: Selected Sources");
                            if (containerViewMap.get(R.id.navigation_sources) == null) {
                                containerViewMap.put(R.id.navigation_sources, new SourcesFragment());
                            }
                            selectedFragment = containerViewMap.get(R.id.navigation_sources);
                            break;
                        case R.id.navigation_settings:
                            Log.d(TAG, "onNavigationItemSelected: Selected Settings");
                            if (containerViewMap.get(R.id.navigation_settings) == null) {
                                containerViewMap.put(R.id.navigation_settings, new SettingsFragment());
                            }
                            selectedFragment = containerViewMap.get(R.id.navigation_settings);
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
                        transaction.add(nav_host_fragment, selectedFragment, Constants.HOST_VIEW);
                    }

                    // Make view visible
                    transaction.show(selectedFragment).commit();
                    return true;
                }
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
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.INTERNET
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

        // if permission is not already granted, get it here,
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{permission}, Constants.PERMISSION_REQUEST_CODE);
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
