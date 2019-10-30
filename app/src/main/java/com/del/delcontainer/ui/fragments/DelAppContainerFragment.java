package com.del.delcontainer.ui.fragments;

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

import java.util.UUID;

public class DelAppContainerFragment extends Fragment {

    private UUID appId;
    private String appName;

    public DelAppContainerFragment(UUID appId, String appName) {
        this.appId = appId;
        this.appName = appName;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delappcontainer, container, false);
        DELUtils delUtils = new DELUtils(getContext());

        WebView appView = view.findViewById(R.id.delAppContainerView);
        appView.getSettings().setJavaScriptEnabled(true);
        appView.addJavascriptInterface(delUtils, Constants.DEL_UTILS);

        // Pass messages
        appView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String message) {
                view.loadUrl("javascript:displayAppMessage('Test Message from Android App')");
            }
        });
        appView.loadUrl(getAppUrl());

        return view;
    }


    /**
     * Hardcoded for now
     * @return
     */
    private String getAppUrl() {

        String appIdent = "";

        if(appName.equals("Heart Health"))
            appIdent = "heart_health";
        else if(appName.equals("Step Counter"))
            appIdent = "step_counter";

        // URL would be fixed and only app names (or UUIDs) would identify apps. Stick with
        // one single 'app' for now at the root index
        String appUrl = Constants.HTTP_PREFIX + Constants.DEL_SERVICE_IP + ":" + Constants.DEL_PORT
                + "/" + appIdent;
        return appUrl;
    }
}
