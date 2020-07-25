package com.creator.qweekdots.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.creator.qweekdots.R;

import java.util.Objects;

public class WebViewActivity extends Activity {

    @SuppressLint("SetJavaScriptEnabled")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Intent intent = getIntent();
        String url = Objects.requireNonNull(intent.getExtras()).getString("url");

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(url);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
        });

    }
}