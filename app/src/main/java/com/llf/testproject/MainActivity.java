package com.llf.testproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.zryf.sotp.KeyBoardDialog;
import com.zryf.sotp.global.KeyBoardInputCallback;

public class MainActivity extends AppCompatActivity implements KeyBoardInputCallback {

    private WebView webView;
    private KeyBoardDialog keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        webView = (WebView) findViewById(R.id.webview);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPwd("123456");
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);// 设置支持javascript脚本
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.e("People", "onJsAlert:" + message);
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                Log.e("People", "onJsConfirm:" + message);
                return super.onJsConfirm(view, url, message, result);
            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webView.addJavascriptInterface(this, "my");
        webView.loadUrl("file:///android_asset/index.html");
    }

    @JavascriptInterface
    public void showKeyboard() {
//        keyboard = new KeyBoardDialog(this, this, "", 0);
//        keyboard.show();
        Toast.makeText(this, "show", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void getPwd(String pwd) {
        if (!TextUtils.isEmpty(pwd)) {
            webView.loadUrl("javascript:native2JsData" + "('" + pwd + "')");
        }
    }

    @Override
    public void getCharNum(int num) {

    }
}
