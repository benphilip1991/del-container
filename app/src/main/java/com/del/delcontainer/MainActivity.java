package com.del.delcontainer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.del.delcontainer.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static com.del.delcontainer.R.id.nav_host_fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view); // BottomNavigationView object in activity_main xml file.

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_services, R.id.navigation_sources, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        verifyAndGetPermissions();
    }

    /**
     * Check if permissions have been granted and obtain if not
     * provided. Add permissions to the list here as needed.
     *
     */
    private void verifyAndGetPermissions() {

        String[] permissionList = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET
        };

        for(String permission : permissionList) {
            getPermissions(permission);
        }
    }

    /**
     * Fetch permissions
     * @param permission
     */
    private void getPermissions(String permission) {

        // if permission is not already granted, get it here,
        if(ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {permission}, Constants.PERMISSION_REQUEST_CODE);
        }
    }
}
