package com.zryf.sotp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by abc on 2016/12/9.
 */

public class SharedPreferencesUtils {
    private static SharedPreferences sharedPreferences = null;

    public static int getProperty(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        int result = sharedPreferences.getInt(key, -1);
        return result;
    }

    public static void setProperty(Context context, String key, int value) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }


    /**
     * 注意：如果为获取到值，返回""空字符串
     *
     * @param context
     * @param key
     * @return
     */
    public static String getPropertyStr(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), 0);
        String result = sharedPreferences.getString(key, "");
        return result;
    }

    public static void setPropertyStr(Context context, String key, String value) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), 0);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
