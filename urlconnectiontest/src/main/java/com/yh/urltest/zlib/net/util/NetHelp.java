package com.yh.urltest.zlib.net.util;

import java.util.Collection;
import java.util.Map;

/**
 * Created by stayli on 2017/3/3.
 */
 public class NetHelp {
    /**
     * @param obj 待检测对象
     * @return true 代表对象为空
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) return true;
        if (obj instanceof CharSequence) return ((CharSequence) obj).length() == 0;
        if (obj instanceof Collection) return ((Collection) obj).isEmpty();
        if (obj instanceof Map) return ((Map) obj).isEmpty();

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

}
