package com.zryf.sotp.utils;

import android.util.Log;

import com.zryf.sotp.global.PluginConstants;

/**
 * Author: DengXiaojia
 * Date: 2017/3/20
 * Email: xj_deng@people2000.net
 * LastUpdateTime: 2017/3/20
 * LastUpdateBy: DengXiaojia
 */
public class TurnOn {
    public static void e(String errMsg) {
        if (PluginConstants.isDebug)
            Log.e(PluginConstants.TAG, errMsg);
    }

    public static void d(String content) {
        if (PluginConstants.isDebug) {
            Log.d(PluginConstants.TAG, content);
        }
    }

}
