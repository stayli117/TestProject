package com.yh.urltest.zlib.net.util;

import android.util.SparseArray;

import org.json.JSONArray;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by stayli on 2017/2/24.
 */

public class PaUtil {
    /**
     * @param obj 待检测对象
     * @return true 代表对象为空
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) return true;
        if (obj instanceof CharSequence) return ((CharSequence) obj).length() == 0;
        if (obj instanceof Collection) return ((Collection) obj).isEmpty();
        if (obj instanceof Map) return ((Map) obj).isEmpty();
        if (obj instanceof JSONArray) return ((JSONArray) obj).length() < 1;
        if (obj instanceof SparseArray) return ((SparseArray) obj).size() < 1;

        if (obj instanceof Object[]) {
            Object[] object = (Object[]) obj;
            if (object.length == 0) {
                return true;
            }
            for (Object anObject : object) {
                if (isNullOrEmpty(anObject)) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * 利用MD5
     *
     * @param str 待加密的字符串
     * @return 加密后的字符串
     */
    public static String encoderByMd5(String str) {
        String newstr = "md5Fail";
        try {
            //确定计算方法
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //加密后的字符串 需要进行 hash 转换
            byte[] bs = md5.digest(str.getBytes());
            StringBuilder sb = new StringBuilder(40);
            for (byte x : bs) {
                if ((x & 0xff) >> 4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }
            return sb.toString();

        } catch (Exception e) {
            e.getMessage();
        }
        return newstr;
    }

    // date类型转换为String类型
    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String getFormattingTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }




    public static JSONArray list2JsonArray(ArrayList<String> list) {
        JSONArray array = new JSONArray();
        for (String s : list) {
            array.put(s);
        }
        return array;
    }

}
