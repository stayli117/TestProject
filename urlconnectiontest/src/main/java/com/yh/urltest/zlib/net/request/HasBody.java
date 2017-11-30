package com.yh.urltest.zlib.net.request;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ================================================
 * ================================================
 */
public interface HasBody<R> {

    R isMultipart(boolean isMultipart);

    R upString(String string);

    R upJson(String json);

    R upJson(JSONObject jsonObject);

    R upJson(JSONArray jsonArray);

    R upBytes(byte[] bs);
}