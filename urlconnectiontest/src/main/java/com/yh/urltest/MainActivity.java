package com.yh.urltest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yh.urltest.zlib.net.PeoHttp;
import com.yh.urltest.zlib.net.callback.RequestCallback;

/**
 * urlConnection 连接复用测试
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        PeoHttp.get("").execute(new RequestCallback() {
            @Override
            public void onSuccess(String content) {

            }

            @Override
            public void onFail(Throwable errorMessage) {

            }
        });
    }
}
