package com.del.delcontainer.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.del.delcontainer.DelContainerActivity;
import com.del.delcontainer.R;
import com.del.delcontainer.managers.DelAppManager;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.DELUtils;
import com.del.delcontainer.utils.DelAppWebViewClient;

import java.util.UUID;

public class DelAppContainerFragment extends Fragment {

    private static final String TAG = "DelAppContainerFragment";

    private String appId;
    private String appName;
    private WebView appView;
    private WebViewClient webViewClient;

    public DelAppContainerFragment(String appId, String appName) {
        this.appId = appId;
        this.appName = appName;
        webViewClient = new DelAppWebViewClient(); // unique for every new sub-app
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delappcontainer, container, false);
        loadDelApp(view);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.close_app_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close_app:
                Toast.makeText(getContext().getApplicationContext(),
                        "Closing " + appName, Toast.LENGTH_SHORT).show();
                DelAppManager.getInstance().terminateApp(appId, appName);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set the app title to the service name when it attaches to the
     * container activity
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        DelContainerActivity activity;
        if(context instanceof DelContainerActivity) {
            activity = (DelContainerActivity) context;
            activity.setTitle(appName);
        }
    }

    /**
     * Set up the webview for running the DEL app
     * @param view
     */
    private void loadDelApp(View view) {

        DELUtils delUtils = DELUtils.getInstance();
        delUtils.setContext(getContext());

        appView = view.findViewById(R.id.delAppContainerView);
        appView.getSettings().setJavaScriptEnabled(true);
        appView.getSettings().setDatabaseEnabled(true);
        appView.getSettings().setDomStorageEnabled(true);
        appView.getSettings().setAppCacheEnabled(true);
        appView.addJavascriptInterface(delUtils, Constants.DEL_UTILS);

        // Pass messages
        appView.setWebViewClient(webViewClient);
        appView.loadUrl(getAppUrl());
    }

    /**
     * Return the contained WebView object.
     * This can be used for calling a function in the
     * app.
     */
    public WebView getAppView() {
        return appView;
    }

    /**
     * Get application URLs - apps are fetched using the appId
     * eg: http://hostname:port/app/appId
     *
     * @return
     */
    private String getAppUrl() {

        Log.d(TAG, "getAppUrl: Getting application url for " + appName);

        String appUrl = Constants.HTTP_PREFIX + Constants.DEL_SERVICE_IP + ":"
                + Constants.DEL_SERVICE_PORT + "/" + appId;
        return appUrl;
    }
}
