package com.yh.urltest.zlib.net.manager;


import com.yh.urltest.zlib.net.callback.RequestCallback;
import com.yh.urltest.zlib.net.config.ThreadPool;
import com.yh.urltest.zlib.net.model.HttpRequest;
import com.yh.urltest.zlib.net.request.BaseRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class RequestManager {

    public ArrayList<HttpRequest> requests; //请求的集合
    private int TIME = 1000;
    private HashMap<HttpRequest, Long> requestLongHashMap = new HashMap<>(); //保存每个请求对象

    public RequestManager() {
        requests = new ArrayList<>();
    }

    private boolean mIsCallbackMain = true;

    public void setIsCallbackMain(boolean isCallbackMain) {
        mIsCallbackMain = isCallbackMain;
    }

    public boolean getIsCallbackMain() {
        return mIsCallbackMain;
    }

    /**
     * 有参数调用
     */
    public HttpRequest createRequest(BaseRequest baseRequest, RequestCallback requestCallback) {
        HttpRequest request = new HttpRequest(this, baseRequest, requestCallback);
        addRequest(request);
        return request;
    }

    /**
     * 添加Request到列表
     */
    public void addRequest(final HttpRequest request) {
        requests.add(request);
    }


    //检查请求的超时时间
    public void checkExpiresOfRequest() {
        Set<Map.Entry<HttpRequest, Long>> entries = requestLongHashMap.entrySet();
        for (Map.Entry<HttpRequest, Long> entry : entries) {
            Long value = entry.getValue();
            if (value <= System.currentTimeMillis()) {
                requests.remove(entry.getKey());
                //检查是否还未执行 如果未执行 直接在此方法中 移除
                checklBlockingRequest(entry.getKey());

            }
        }

    }

    /**
     * 取消所有的网络请求(包括正在执行的)
     */
    public void cancelAllRequest() {
        BlockingQueue queue = ThreadPool.getQuene();
        for (int i = requests.size() - 1; i >= 0; i--) {
            HttpRequest request = requests.get(i);
            if (queue.contains(request)) {
                queue.remove(request);
            } else {
                request.disconnect();
            }
        }
        requests.clear();
    }

    /**
     * 取消未执行的网络请求
     */
    public void cancelBlockingRequest() {
        // 取交集(即取出那些在线程池的阻塞队列中等待执行的请求)
        List<HttpRequest> intersection = (List<HttpRequest>) requests.clone();
        intersection.retainAll(ThreadPool.getQuene());
        // 分别删除
        ThreadPool.getQuene().removeAll(intersection);
        requests.removeAll(intersection);
    }

    /**
     * 查询未执行的网络请求
     *
     * @param key
     */
    public void checklBlockingRequest(HttpRequest key) {
        // 取交集(即取出那些在线程池的阻塞队列中等待执行的请求)
        List<HttpRequest> intersection = (List<HttpRequest>) requests.clone();
        intersection.retainAll(ThreadPool.getQuene());

        cancelDesignatedRequest(intersection.contains(key) ? key : null);
    }


    /**
     * 取消指定的网络请求
     */
    public void cancelDesignatedRequest(HttpRequest request) {
        if (request == null) {
            return;
        }
        if (!ThreadPool.removeTaskFromQueue(request)) {
            request.disconnect();
        }
    }


}
