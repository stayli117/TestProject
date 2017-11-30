package com.yh.urltest.zlib.net.request;

/**
 * ================================================

 * ================================================
 */
public class PostRequest extends BaseBodyRequest<PostRequest> {

    public PostRequest(String url) {
        super(url);
        method = "POST";
    }

}