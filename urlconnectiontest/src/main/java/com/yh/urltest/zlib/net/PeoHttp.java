package com.yh.urltest.zlib.net;

import android.content.Context;

import com.yh.urltest.zlib.net.config.ThreadPool;
import com.yh.urltest.zlib.net.config.ThreadPoolConfig;
import com.yh.urltest.zlib.net.manager.RequestManager;
import com.yh.urltest.zlib.net.model.HttpRequest;
import com.yh.urltest.zlib.net.request.GetRequest;
import com.yh.urltest.zlib.net.request.PostRequest;

import java.util.HashMap;
import java.util.Map;

public final class PeoHttp {
    // 配置信息
    // 存放每个TAG对应的RequestManager 如果没有定义TAG 直接使用初始化的Context对象
    static Map<Object, RequestManager> managerMap;
    public static Context mContext;

    /**
     * 初始化 交由用户配置线程池
     *
     * @param config 全局配置信息
     */
    public static void init(ThreadPoolConfig config) {
        managerMap = new HashMap<>();
        mContext = config.context;
        // 初始化线程池
        ThreadPool.init(config);
    }

    /**
     * 初始化 使用默认配置
     *
     * @param context 全局配置信息
     */
    public static void init(Context context) {
        managerMap = new HashMap<>();
        mContext = context;
        // 初始化线程池
        ThreadPool.init(new ThreadPoolConfig(context));
    }


    public static GetRequest get(String url) {
        return new GetRequest(mContext, url);
    }

    public static PostRequest post(String url) {
        return new PostRequest(url);
    }

    /**
     * 取消指定TAG中发起的所有HTTP请求
     *
     * @param tag
     */
    public static void cancelAllRequest(Object tag) {
        checkRequestManager(tag, false).cancelAllRequest();
    }

    /**
     * 取消线程池中整个阻塞队列所有HTTP请求
     */
    public static void cancelAllRequest() {
        ThreadPool.removeAllTask();
    }

    /**
     * 取消指定TAG中未执行的请求
     *
     * @param tag
     */
    public static void cancelBlockingRequest(Object tag) {
        checkRequestManager(tag, false).cancelBlockingRequest();
    }

    /**
     * 取消指定请求
     *
     * @param tag
     * @param request
     */
    public static void cancelDesignatedRequest(Object tag, HttpRequest request) {
        checkRequestManager(tag, false).cancelDesignatedRequest(request);
    }

    /**
     * 访问TAG对应的RequestManager对象
     *
     * @param tag 请求标记
     * @param createNew 当RequestManager对象为null时是否创建新的RequestManager对象
     * @return 请求管理者
     */
    public static RequestManager checkRequestManager(Object tag, boolean createNew) {
        RequestManager manager;
        if ((manager = managerMap.get(tag)) == null) {
            if (createNew) {
                manager = new RequestManager();
                managerMap.put(tag, manager);
            } else {
                throw new NullPointerException(tag.getClass().getSimpleName() + "'s RequestManager is null!");
            }
        }
        return manager;
    }

    /**
     * 关闭线程池，并等待任务执行完成，不接受新任务
     */
    public static void shutdown() {
        ThreadPool.shutdown();
    }

    /**
     * 关闭，立即关闭，并挂起所有正在执行的线程，不接受新任务
     */
    public static void shutdownRightnow() {

        ThreadPool.shutdownRightnow();
    }
}
