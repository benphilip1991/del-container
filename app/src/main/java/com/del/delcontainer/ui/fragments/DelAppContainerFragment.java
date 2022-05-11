package com.del.delcontainer.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
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

public class DelAppContainerFragment extends Fragment {

    private static final String TAG = "DelAppContainerFragment";

    private String appId = null;
    private String appName = null;
    private String packageName = null; //same as app description URL
    private WebView appView;
    private WebViewClient webViewClient;
    DelContainerActivity activity;

    public DelAppContainerFragment() {
        ;
    }

    public DelAppContainerFragment(String appId, String appName, String packageName) {
        this.appId = appId;
        this.appName = appName;
        this.packageName = packageName;
        webViewClient = new DelAppWebViewClient(this, appId); // unique for every new sub-app
    }

    /**
     * Set the app title to the service name when it attaches to the
     * container activity
     *
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof DelContainerActivity) {
            activity = (DelContainerActivity) context;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delappcontainer, container, false);
        loadDelApp(view);

        activity.setTitle(appName);
        setHasOptionsMenu(true);
        return view;
    }

    /**
     * Call to set application title on resume
     */
    public void setAppTitle() {
        if (null != appName) {
            activity.setTitle(appName);
        }
    }

    /**
     * Set title back to Services when closing app
     */
    @Override
    public void onPause() {
        super.onPause();
        activity.setTitle(R.string.title_services);
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
                DelAppManager.getInstance().terminateApp(appId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Set up the webview for running the DEL app
     *
     * @param view
     */
    private void loadDelApp(View view) {

        Log.d(TAG, "loadDelApp: Launching app in micro app container");
        DELUtils delUtils = DELUtils.getInstance();
        delUtils.setContext(getContext());

        appView = view.findViewById(R.id.del_app_container_view);
        appView.getSettings().setJavaScriptEnabled(true);
        appView.getSettings().setDatabaseEnabled(true);
        appView.getSettings().setDomStorageEnabled(true);
        appView.getSettings().setAppCacheEnabled(true);
        appView.getSettings().setAllowFileAccessFromFileURLs(true);
        appView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        appView.getSettings().setSupportZoom(true);
        appView.getSettings().setBuiltInZoomControls(true);
        appView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        appView.addJavascriptInterface(delUtils, Constants.DEL_UTILS);

        appView.setWebViewClient(webViewClient);
        appView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest: Fetching webchrome permissions");

                getActivity().runOnUiThread(() -> {
                    final String[] requestedResources = request.getResources();
                    for (String permReq : requestedResources) {
                        if (permReq.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                            request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                            break;
                        }
                    }
                });
            }
        });

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
                + Constants.DEL_PORT + Constants.API_BASE_PATH + Constants.APP_RESOURCE_PATH
                + appId + "/" + packageName + ".html";

        Log.d(TAG, "getAppUrl: App URL : " + appUrl);
        return appUrl;
    }
}
