package com.del.delcontainer.utils;

import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Random;

public class DelAppWebViewClient extends WebViewClient {

    private static final String TAG = "DelAppWebViewClient";

    @Override
    public void onPageFinished(WebView view, String url) {

        Log.d(TAG, "onPageFinished: Pushing data to app");
        //String methodUrl = "javascript:displayAppMessage('Heart Rate : " + new Random().nextInt(500) + "')";
        String methodUrl = "javascript:displayAppMessage('Demo App')";
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
}
