package com.del.delcontainer;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.del.delcontainer.receivers.DelBroadcastReceiver;
import com.del.delcontainer.services.LocationService;
import com.del.delcontainer.services.SensorsService;
import com.del.delcontainer.ui.chatbot.ChatAdapter;
import com.del.delcontainer.ui.chatbot.ChatType;
import com.del.delcontainer.ui.services.ServicesFragment;
import com.del.delcontainer.ui.settings.SettingsFragment;
import com.del.delcontainer.ui.sources.SourcesFragment;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.managers.DelAppManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.del.delcontainer.R.id.nav_host_fragment;

public class DelContainerActivity extends AppCompatActivity {
    private static final String TAG = "DelContainerActivity";
    Dialog myDialog;
    EditText inputText;
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;
    List<ChatType> chatTypeList;

    // May have to move to another global fragment manager
    private HashMap<Integer, Fragment> containerViewMap = new HashMap<>();

    FloatingActionButton chatButton;
    IntentFilter intentFilter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDialog = new Dialog(this);

        registerBroadcastReceiver();
        BottomNavigationView navView = findViewById(R.id.nav_view); // BottomNavigationView object in activity_main xml file.
        navView.setOnNavigationItemSelectedListener(navigationListener); // attach the custom listener

        /**
         * Explicitly setting the default view to the services on first run
         * This ensures the containerViewMap tracks the first view as well.
         * Issue - launches two fragment instances
         */
        if (null == savedInstanceState) {
            navView.setSelectedItemId(R.id.navigation_services); // remove. Need to find a proper fix
            List<Fragment> fragList = getSupportFragmentManager().getFragments();
            for(Fragment frag : fragList) {

                if(frag instanceof NavHostFragment) {
                    Log.d(TAG, "[FRAG_ID] onCreate: Instance of NavHostFragment" );
                }
                Log.d(TAG, "[FRAG_ID] onCreate: Fragment ID : " + frag);
            }
        }

        // Experimental for now
        //initChatbot();
        verifyAndGetPermissions();
        initServices();
        scheduleProviderJobs();
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
        locationService.startLocationUpdates();
    }

    /**
     * To show the close action bar menu button
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
        LocationService locationService = LocationService.getInstance();
        locationService.initLocationService(this);

        SensorsService sensorsService = SensorsService.getInstance();
        sensorsService.initSensorService(this);
    }

    /**
     * Initialize chat button and add event listener
     * TODO: move to conversation manager and handle everything from there
     */
    public void initChatbot(View v) {

        myDialog.setContentView(R.layout.popup);

        chatButton = findViewById(R.id.chatButton);
        inputText = myDialog.findViewById(R.id.inputText);
        recyclerView = myDialog.findViewById(R.id.chat);
        chatTypeList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatTypeList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(chatAdapter);

        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    inputText.setHint("");
                else
                    inputText.setHint("Ask Something");
            }
        });

        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    ChatType responseMessage = new ChatType(inputText.getText().toString(), true);
                    chatTypeList.add(responseMessage);
                    ChatType responseMessage2 = new ChatType(inputText.getText().toString(), false);
                    chatTypeList.add(responseMessage2);
                    inputText.setText("");
                    chatAdapter.notifyDataSetChanged();
                    if (!isLastVisible())
                        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                }
                return false;
            }
        });
        myDialog.getWindow().setLayout(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    boolean isLastVisible() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int pos = layoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = recyclerView.getAdapter().getItemCount();
        return (pos >= numItems);
    }

    /**
     * Initialize data provider jobs
     */
    protected void scheduleProviderJobs() {
        ;
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
                    transaction.add(nav_host_fragment, selectedFragment, Constants.HOST_VIEW);
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
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACTIVITY_RECOGNITION
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
