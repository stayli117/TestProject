package com.llf.testproject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private WebView webView;

    private static final String TAG = "MainActivity";
    private TextView mTvContent;
    private Button mBtnNet;
    private Handler mHandler;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        mTvContent = (TextView) findViewById(R.id.tv_content);
        mBtnNet = (Button) findViewById(R.id.btn_net);
        mBtnNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvContent.setText("网络请求中");
                OkHttpClient client = new OkHttpClient.Builder().build();
                client.newCall(new Request.Builder().url("http://www.baidu.com").build())
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTvContent.setText("fail");
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTvContent.setText("ok");
                                    }
                                });
                            }
                        });
            }
        });
    }

}
