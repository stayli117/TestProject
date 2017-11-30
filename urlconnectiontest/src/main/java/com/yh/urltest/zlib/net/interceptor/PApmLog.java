package com.yh.urltest.zlib.net.interceptor;

import android.util.Log;

import net.people.apmv2.agent.global.ApmConfig;

/**
 * Created by stayli on 2016/11/2.
 */
public final class PApmLog {
    private static boolean debug = true;
    public static String TAG = "PApm->";

    public static void cancel(boolean isCancel) {
        debug = isCancel;
    }

    /**
     * Custom Log output style
     *
     * @param type Log type
     * @param tag  TAG
     * @param msg  Log message
     */
    public static void trace(int type, String tag, String msg) {
        if (debug) {
            if (msg == null) return;
            switch (type) {
                case Log.VERBOSE:
                    Log.v(tag, msg);
                    break;
                case Log.DEBUG:
                    Log.d(tag, msg);
                    break;
                case Log.INFO:
                    Log.i(tag, msg);
                    break;
                case Log.WARN:
                    Log.w(tag, msg);
                    break;
                case Log.ERROR:
                    Log.e(tag, msg);
                    break;
            }
        }

    }

    public static void trace(String tag, String msg) {
        TAG = tag;
        trace(msg);
    }


    /**
     * @param msg Log message
     */
    public static void trace(String msg) {
        if (debug) {
            if (msg.contains(ApmConfig.ApmE))
                trace(Log.ERROR, TAG, msg);
            else
                trace(Log.INFO, TAG, msg);
        }

    }

    /**
     * @param msg Log message
     */
    public static void traceTh(String msg) {
        if (debug) {
            if (msg.contains(ApmConfig.ApmE))
                trace(Log.ERROR, Thread.currentThread().getName() + " " + TAG, msg);
            else
                trace(Log.INFO, Thread.currentThread().getName() + " " + TAG, msg);
        }

    }

    /**
     * @param methodName 方法的名字
     */
    public static void traceTime(String methodName) {
        if (debug) {
            Log.e("MethodTime->", Thread.currentThread().getName() + ":" + methodName + System.currentTimeMillis());
        }
    }

    public static void out(String s) {
        if (debug) {
            System.out.println("People --> " + s);
        }
    }
}
