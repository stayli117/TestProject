package com.yh.urltest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.Scanner;

/**
 * urlConnection 连接复用测试
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText mETInput = (EditText) findViewById(R.id.et_input);

        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
            Log.e(TAG, "onCreate: " + scanner.next());
        } else {
            Log.e(TAG, "onCreate: 等待录入.....");

        }

//        PeoHttp.get("").execute(new RequestCallback() {
//            @Override
//            public void onSuccess(String content) {
//
//            }
//
//            @Override
//            public void onFail(Throwable errorMessage) {
//
//            }
//        });
    }
}
