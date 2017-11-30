package com.yh.urltest.zlib.net.callback;

/**
 * 请求回调接口
 */
public interface RequestCallback {
    void onSuccess(String content);

    void onFail(Throwable errorMessage);
}
