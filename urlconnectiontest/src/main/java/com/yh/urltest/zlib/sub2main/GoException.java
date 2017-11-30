package com.yh.urltest.zlib.sub2main;

/**
 * Created by stayli on 2017/2/8.
 */

public class GoException extends RuntimeException {
    public GoException() {
    }
    public GoException(Throwable cause) {
        super(cause);
    }

    public GoException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoException(String message) {
        super(message);
    }
}
