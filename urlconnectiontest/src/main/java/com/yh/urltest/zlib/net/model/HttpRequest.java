package com.yh.urltest.zlib.net.model;

import android.accounts.NetworkErrorException;
import android.os.Handler;

import com.yh.urltest.zlib.net.callback.RequestCallback;
import com.yh.urltest.zlib.net.interceptor.PApmLog;
import com.yh.urltest.zlib.net.manager.RequestManager;
import com.yh.urltest.zlib.net.request.BaseRequest;
import com.yh.urltest.zlib.net.util.NetHelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HttpRequest implements Runnable {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private String mUrl;
    private String method;
    // 宿主Manager
    private RequestManager hostManager;

    // 请求参数
    private List params;

    // 请求回调
    private RequestCallback callback;
    // Handler对象，用于在回调时切换回主线程进行相应操作
    private Handler handler;
    // URL及HttpURLConnection对象
    private URL mURL;
    private HttpURLConnection mConnection;
    // 请求中断标志位
    private Boolean interrupted = false;
    private boolean mIsCallbackMain = true;

    public HttpRequest() {
    }

    /**
     * 构造器
     *
     * @param hostManager     请求主管理者
     * @param baseRequest     基础请求对象
     * @param requestCallback 请求回调对象
     */
    public HttpRequest(RequestManager hostManager, BaseRequest baseRequest, RequestCallback requestCallback) {
        this.hostManager = hostManager;
        this.mUrl = baseRequest.getUrl();
        this.params = baseRequest.getParameters();
        this.method = baseRequest.getMethod();
        this.callback = requestCallback;
        mIsCallbackMain = hostManager.getIsCallbackMain();
        if (mIsCallbackMain) {
            handler = new Handler();
        }
    }


    @Override
    public void run() {
        // 判断请求类型
        switch (method) {
            case GET:
                // 类型为HTTP-GET时，将请求参数组装到URL链接字符串上
                String trulyURL = getTrulyURL(mUrl);
                PApmLog.traceTh("get->" + trulyURL);
                // 正式发送GET请求到服务器
                sendHttpGetToServer(trulyURL);
                break;
            case POST:
                // 发送POST请求到服务器
                sendHttpPostToServer(mUrl);
                break;
            default:
                break;
        }
    }

    /**
     * @param url 请求地址
     * @return 真实地址
     */
    private String getTrulyURL(String url) {
        return NetHelp.isNullOrEmpty(params) ? url : url + "?" + convertParam2String();
    }

    /**
     * 发起GET请求
     *
     * @param url
     */
    public void sendHttpGetToServer(String url) {
        try {
            mURL = new URL(url);
            mConnection = (HttpURLConnection) mURL.openConnection();

            // 连接服务器的超时时长
            mConnection.setConnectTimeout(2000);
            // 从服务器读取数据的超时时长
            mConnection.setReadTimeout(2000);

            mConnection.setUseCaches(false); // 禁止缓存
            mConnection.setInstanceFollowRedirects(true); // 自动执行HTTP重定向
            setHeader();
            if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 如果未设置请求中断，则进行读取数据的工作
                if (!interrupted) {
                    // read content from response..
                    final String result = readFromResponse(mConnection.getInputStream());
                    // call back
                    callbackWhoThread(result);
                } else { // 中断请求
                    return;
                }
            }
        } catch (Exception e) {
            handleNetworkError(e);
        } finally {
            hostManager.requests.remove(this);
        }
    }

    /**
     * 发起POST请求
     *
     * @param url 请求地址
     */
    public void sendHttpPostToServer(String url) {
        try {
            mURL = new URL(url);
            mConnection = (HttpURLConnection) mURL.openConnection();
            // 连接服务器的超时时长
            mConnection.setConnectTimeout(8000);
            // 从服务器读取数据的超时时长
            mConnection.setReadTimeout(8000);

            setHeader();

            // 允许输入输出 调用这个两个方法，会默认走Post
            mConnection.setDoOutput(true);
            mConnection.setDoInput(true);
            // 向请求体中写入参数
            if (params != null && !params.isEmpty()) {
                String paramString = convertParam2String();
                BufferedWriter br = new BufferedWriter(new OutputStreamWriter(mConnection.getOutputStream()));
                br.write(paramString);
                br.flush();
                br.close();
                PApmLog.traceTh("post--> " + url + "\r\n" + paramString);
            }

            if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if (!interrupted) {
                    String result = readFromResponse(mConnection.getInputStream());
                    callbackWhoThread(result);
                } else {
                    return;
                }
            } else {
                handleNetworkError(new NetworkErrorException("status --> " + mConnection.getResponseCode()));
            }
        } catch (Exception e) {
            handleNetworkError(e);
        } finally {
            hostManager.requests.remove(this);
        }
    }


    /**
     * 具体判断是否回调到主线程，默认回调到主线程
     *
     * @param result 回调结果对象
     */
    private void callbackWhoThread(final Object result) {
        String s = mIsCallbackMain ? "结果回调到主线程" : "结果回调到子线程";
        PApmLog.traceTh(s + "-->" + result);
        if (callback != null) {
            if (mIsCallbackMain) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (result instanceof String) {
                            callback.onSuccess(result.toString());
                        } else if (result instanceof Throwable) {
                            callback.onFail((Throwable) result);
                        }
                    }
                });
            } else {
                if (result instanceof String) {
                    callback.onSuccess(result.toString());
                } else if (result instanceof Throwable) {
                    callback.onFail((Throwable) result);
                }
            }

        }
    }

    /**
     * 设置头信息
     */
    private void setHeader() {
        mConnection.setRequestProperty("Accept-Charset", "utf-8");
        mConnection.setRequestProperty("contentType", "utf-8");
        mConnection.setRequestProperty("accept", "*/*");
        mConnection.setRequestProperty("connection", "Keep-Alive");
        mConnection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
    }

    /**
     * 将请求参数转换为String
     */
    private String convertParam2String() {
        StringBuilder paramsBuilder = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            HttpParams param = (HttpParams) params.get(i);
            paramsBuilder.append(param.getName()).append("=").append(param.getValue());
            if (i < params.size() - 1)
                paramsBuilder.append("&");
        }
        return paramsBuilder.toString();
    }


    /**
     * 从http response中读取响应数据
     *
     * @param inputStream 响应流
     * @return 响应数据
     * @throws IOException
     */
    private String readFromResponse(InputStream inputStream) throws IOException {
        String line;
        StringBuilder builder = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = br.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    /**
     * 异常回调
     *
     * @param errorMsg
     */
    private void handleNetworkError(final Throwable errorMsg) {
        callbackWhoThread(errorMsg);
    }

    /**
     * 中断请求
     */
    public void disconnect() {
        // 设置标志位
        interrupted = true;
        // 如果当前请求正处于与服务器连接状态下，则断开连接
        if (mConnection != null)
            mConnection.disconnect();
    }
}
