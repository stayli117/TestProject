package com.yh.urltest.zlib.net.request;

import android.content.Context;

/**
 * ================================================
 * ================================================
 */
public class GetRequest extends BaseBodyRequest<GetRequest> {

    public GetRequest(Context context, String url) {
        super(url);
        mContext = context;
        method = "GET";

    }

}