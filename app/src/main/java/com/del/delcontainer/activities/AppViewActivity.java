package com.del.delcontainer.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.del.delcontainer.R;
import com.del.delcontainer.utils.Constants;
import com.del.delcontainer.utils.DELUtils;

public class AppViewActivity extends AppCompatActivity {

    private static final String TAG = "AppViewActivity";

    private DELUtils delUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_view);

        delUtils = new DELUtils(this);

        Intent intent = getIntent();
        String appIdent = intent.getStringExtra(Constants.APP_IDENT);

        initAppView(appIdent);
    }

    private void initAppView(String appIdent) {

        WebView appView = findViewById(R.id.appView);
        appView.getSettings().setJavaScriptEnabled(true);
        appView.addJavascriptInterface(delUtils, "DelUtils");

        // Pass initial message
        appView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String message) {
                view.loadUrl("javascript:displayAppMessage('Test Message from Android App')");
            }
        });

        appView.loadUrl(getAppUrl(appIdent));
    }

    private String getAppUrl(String appIdent) {

        // URL would be fixed and only app names (or UUIDs) would identify apps. Stick with
        // one single 'app' for now at the root index
        String appUrl = Constants.HTTP_PREFIX + Constants.DEL_SERVICE_IP + ":" + Constants.DEL_PORT;
        return appUrl;
    }
}
