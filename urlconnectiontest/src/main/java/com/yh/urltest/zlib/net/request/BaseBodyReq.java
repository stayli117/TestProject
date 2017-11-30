package com.yh.urltest.zlib.net.request;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by stayli on 2017/3/1.
 */

public abstract class BaseBodyReq<R extends BaseBodyReq> extends BaseRequest<R> implements RequestBody<R> {
    protected BaseBodyReq(String url) {
        super(url);
    }

    @Override
    public R upString(String string) {
        return null;
    }

    @Override
    public R upJson(String json) {
        return null;
    }

    @Override
    public R upJson(JSONObject jsonObject) {
        return null;
    }

    @Override
    public R upJson(JSONArray jsonArray) {
        return null;
    }

    @Override
    public R upBytes(byte[] bs) {
        return null;
    }
}
