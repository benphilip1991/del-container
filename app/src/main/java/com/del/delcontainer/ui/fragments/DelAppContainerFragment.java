package com.del.delcontainer.ui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

import com.del.delcontainer.R;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.DELUtils;
import com.del.delcontainer.utils.DelAppWebViewClient;

import java.util.UUID;

public class DelAppContainerFragment extends Fragment {

    private static final String TAG = "DelAppContainerFragment";

    private UUID appId;
    private String appName;
    private WebView appView;
    private WebViewClient webViewClient;

    public DelAppContainerFragment(UUID appId, String appName) {
        this.appId = appId;
        this.appName = appName;
        webViewClient = new DelAppWebViewClient();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delappcontainer, container, false);
        loadDelApp(view);

        return view;
    }

    /**
     * Set up the webview for running the DEL app
     * @param view
     */
    private void loadDelApp(View view) {

        DELUtils delUtils = new DELUtils(getContext());

        appView = view.findViewById(R.id.delAppContainerView);
        appView.getSettings().setJavaScriptEnabled(true);
        appView.getSettings().setDatabaseEnabled(true);
        appView.getSettings().setDomStorageEnabled(true);
        appView.addJavascriptInterface(delUtils, Constants.DEL_UTILS);

        // Pass messages
        appView.setWebViewClient(webViewClient);
        appView.loadUrl(getAppUrl());
    }

    /**
     * Hardcoded for now.
     * TODO: Get apps using the provided UUID.
     *
     * @return
     */
    private String getAppUrl() {

        String appIdent = "";

        if (appName.equals("Heart Health"))
            appIdent = "heart_health";
        else if (appName.equals("Step Counter"))
            appIdent = "step_counter";
        else if(appName.equals("Mood Tracker")) {
            String appUrl = Constants.HTTP_PREFIX + Constants.MYMAPS_SERVICE_IP + ":" + Constants.MYMAPS_PORT;
            return appUrl;
        }

        // URL would be fixed and only app names (or UUIDs) would identify apps. Stick with
        // one single 'app' for now at the root index
        String appUrl = Constants.HTTP_PREFIX + Constants.DEL_SERVICE_IP + ":" + Constants.DEL_PORT
                + "/" + appIdent;
        return appUrl;
    }

}
