package com.del.delcontainer.utils;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

public class DelAppWebViewClient extends WebViewClient {

    private static final String TAG = "DelAppWebViewClient";
    private static Fragment fragment = null;
    private static String appId = null;

    public DelAppWebViewClient(Fragment fragment, String appId) {
        this.fragment = fragment;
        this.appId = appId;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d(TAG, "onPageFinished: Pushing data to app");
        String methodUrl = "javascript:setAppId('" + appId + "')";
        view.loadUrl(methodUrl);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        view.loadUrl("about:blank");
        view.loadUrl("file:///android_asset/webview_error.html");
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        view.loadUrl("about:blank");
        view.loadUrl("file:///android_asset/webview_error.html");
    }

    /**
     * Override to only launch zoom meetings on zoom app
     * @param webView
     * @param url
     * @return
     */
    @Override
    public boolean shouldOverrideUrlLoading(final WebView webView, final String url) {
        if(url.contains("zoom.us")) {
            // launch zoom and return true
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if(intent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
                fragment.getActivity().startActivity(intent);
            }
            return true;
        }
        return false;
    }

    /**
     * TODO: Dangerous!!! This block overrides the default certificate validation mechanism
     * TODO: Not to be used in Prod!
     * @param view
     * @param handler
     * @param error
     */
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        //handler.proceed();
    }

    public void pushDataToApp(WebView view, String data) {
        Log.d(TAG, "pushDataToApp: Pushing user data to app");
        String methodUrl = "javascript:sensorDataPush(" + data + ")";
        view.loadUrl(methodUrl);
    }
}
