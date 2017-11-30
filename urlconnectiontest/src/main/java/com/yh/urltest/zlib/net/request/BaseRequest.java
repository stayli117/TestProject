package com.yh.urltest.zlib.net.request;

import android.content.Context;

import com.yh.urltest.zlib.net.PeoHttp;
import com.yh.urltest.zlib.net.callback.RequestCallback;
import com.yh.urltest.zlib.net.config.ThreadPool;
import com.yh.urltest.zlib.net.manager.RequestManager;
import com.yh.urltest.zlib.net.model.HttpParams;
import com.yh.urltest.zlib.net.model.HttpRequest;
import com.yh.urltest.zlib.net.util.NetHelp;
import com.yh.urltest.zlib.net.util.StreamUtils;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.tag;


/**
 * ================================================
 * 描    述：所有请求的基类，其中泛型 R 主要用于属性设置方法后，返回对应的子类型，以便于实现链式调用
 * 修订历史：
 * ================================================
 */
public abstract class BaseRequest<R extends BaseRequest> {

    protected String url;
    protected String method;
    protected String baseUrl;
    protected Object mTag;
    protected long readTimeOut = 3000;
    protected long writeTimeOut = 3000;
    protected long connectTimeout = 3000;
    protected int retryCount = 3;   //超时重试次数 应该由外部设置
    protected String cacheKey;
    protected Context mContext;

    //添加的param
    protected List<HttpParams> parameters;
    // 添加的头信息


    public BaseRequest(String url) {
        parameters = new ArrayList<>();
        this.url = url;
        baseUrl = url;
    }

    @SuppressWarnings("unchecked")
    public R url(String url) {
        this.url = url;
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R params(HttpParams params) {
        parameters.add(params);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R params(String key, String value) {
        parameters.add(new HttpParams(key, value));
        return (R) this;
    }

    public R params(String key, String value, boolean isGzip) {
        if (isGzip) {
            value = StreamUtils.comGzip(value);
            value.replaceAll("\\+", "%2B");
        }
        parameters.add(new HttpParams(key, value));
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R tag(Object tag) {
        this.mTag = tag;
        return (R) this;
    }
    // 暂未实现
//    @SuppressWarnings("unchecked")
//    public R headers(HttpHeaders headers) {
//        this.headers.put(headers);
//        return (R) this;
//    }
//    @SuppressWarnings("unchecked")
//    public R headers(String key, String value) {
//        mHeaders.add(new HttpHeaders(key, value));
//        return (R) this;
//    }

    public String getUrl() {
        return url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public List<HttpParams> getParameters() {
        return parameters;
    }

    public Object getTag() {
        return tag;
    }


    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }


    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 返回当前的请求方法
     * GET,POST,HEAD,PUT,DELETE,OPTIONS
     */
    public String getMethod() {
        return method;
    }


    /**
     * 非阻塞方法，异步请求，但是回调在主线程中执行
     */
    @SuppressWarnings("unchecked")
    public <T> void execute(RequestCallback callback) {
        // 执行任务
        ThreadPool.execute(invokeRequest(true, NetHelp.isNullOrEmpty(mTag) ? mContext : mTag, callback));
    }

    /**
     * 同步执行 回调到当前线程
     *
     * @param callback
     * @param <T>
     */
    public <T> void enqueue(RequestCallback callback) {
        // 执行任务
        ThreadPool.enqueue(invokeRequest(false, NetHelp.isNullOrEmpty(mTag) ? mContext : mTag, callback));
    }


    /**
     * 构建请求对象 by 请求参数)
     *
     * @param tag      发起HTTP请求的Context
     * @param callBack HTTP请求执行完毕后的回调接口
     */
    public HttpRequest invokeRequest(boolean isCallMain, Object tag, RequestCallback callBack) {
        // 由客户端查找当前TAG对应的manager，
        RequestManager manager = PeoHttp.checkRequestManager(tag, true);
        manager.setIsCallbackMain(isCallMain);
        //该TAG对应的RequestManager对象，创建HttpRequest对象
        return manager.createRequest(this, callBack);
    }


}