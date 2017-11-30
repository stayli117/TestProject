package com.yh.urltest.zlib.net.request;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ================================================
 * <p>
 * ================================================
 */
public abstract class BaseBodyRequest<R extends BaseBodyRequest> extends BaseRequest<R> implements HasBody<R> {


    protected String content;           //上传的文本内容
    protected byte[] bs;                //上传的字节数据

    protected boolean isMultipart = false;  //是否强制使用 multipart/form-data 表单上传


    public BaseBodyRequest(String url) {
        super(url);
    }

    @SuppressWarnings("unchecked")
    @Override
    public R isMultipart(boolean isMultipart) {
        this.isMultipart = isMultipart;
        return (R) this;
    }

    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     */
    @SuppressWarnings("unchecked")
    @Override
    public R upString(String string) {
        this.content = string;
        return (R) this;
    }


    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     */
    @SuppressWarnings("unchecked")
    @Override
    public R upJson(String json) {
        this.content = json;

        return (R) this;
    }

    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     */
    @SuppressWarnings("unchecked")
    @Override
    public R upJson(JSONObject jsonObject) {
        this.content = jsonObject.toString();

        return (R) this;
    }

    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     */
    @SuppressWarnings("unchecked")
    @Override
    public R upJson(JSONArray jsonArray) {
        this.content = jsonArray.toString();

        return (R) this;
    }

    /**
     * 注意使用该方法上传字符串会清空实体中其他所有的参数，头信息不清除
     */
    @SuppressWarnings("unchecked")
    @Override
    public R upBytes(byte[] bs) {
        this.bs = bs;
        return (R) this;
    }

}