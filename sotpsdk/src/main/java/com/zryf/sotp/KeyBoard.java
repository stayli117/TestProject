package com.zryf.sotp;

/**
 * Author  Llf
 * Time    2017/8/22
 */

public class KeyBoard {

    public static native void FreshKeyMap();

    public static native void inputNum(int num);

    public static native void deletNum();

    public static native String getEncPassword(int count);

    //预置插件
    public static native void FreshKeyMapLocal();

    public static native void inputNumLocal(int num);

    public static native void deletNumLocal();

    public static native String getEncPasswordLocal(int count);
}
