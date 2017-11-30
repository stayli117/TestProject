package com.yh.urltest.zlib.net.request;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by stayli on 2017/3/1.
 * 表示当前请求是否具有请求体
 */

public interface RequestBody<R> {

    R params(String key, String value);

    R upString(String string);

    R upJson(String json);

    R upJson(JSONObject jsonObject);

    R upJson(JSONArray jsonArray);

    R upBytes(byte[] bs);
}
