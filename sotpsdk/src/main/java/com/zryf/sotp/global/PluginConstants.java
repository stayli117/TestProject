package com.zryf.sotp.global;


import java.io.File;

public class PluginConstants {
    public static final String pluginName = "sotpCore";// 插件的名字
    public static final String tmpPluginName = "tmpSotpCore";//加载插件的名字
    public static final boolean isDebug = true;// 是否调试
    public static String realPath = null;  // 用户在调用saveSotp时传入的userName和path会组合成一个真正保存插件的目录在这里持久保存
    public final static String TAG = "SOTP";
    public final static int FINGERPRINT_POLICY = 33554431;//设备指纹位图安卓最大值
    public final static int FINGERPRINT_TYPE_ZERO = 0;

    /**
     * 用于生成SOTP口令码类型的常量值
     */
    public static class SotpType {
        public static final int OTP_TIME = 1;// 时间型
        public static final int OTP_TIME_PIN = 2;// PIN码型
        public static final int OTP_CHALLENGE = 3;// 挑战码型
        public static final int OTP_PIN_CHALLENGE = 4;// PIN码加挑战码型
        public static final int OTP_CHALLENGE_SCAN_LOGIN = 5;// 挑战码型
        public static final int OTP_TIME_PIN_LOGIN = 6;// 挑战码型
        public static final int OTP_TIME_CODE = 7;// 时间型
        public static final int OTP_TIME_LOGIN = 8;// 时间型
    }

    /**
     * this pram is  service need key
     */
    public static class ServiceParam {
        public static final String APP_ID = "appId";
        public static final String SIGN = "sign";
        public static final String HASH = "hash";
        public static final String SDK_VERSION = "sdkVersion";
        public static final String PLUGIN_ID = "pluginId";
        public static final String DEV_ID = "devId";
        public static final String USER_COUNT = "useCount";
        public static final String BINDTYPE = "bindType";
    }

    /**
     * get the otp code encryption obfuscation code class
     */
    public static class Confounding {
        public static final String TEN = "10@";
        public static final String TWENTY = "20@";
        public static final String THIRTY = "30@";
        public static final String FORTY = "40@";
    }

    public static class ProtectionsPram {
        public static final String META_DATA_APP_ID = "cn.peoplenet.sotpv2.APPID";
        public static final String META_DATA_SDK_VERSION = "cn.peoplenet.sotpv2.SDKVERSION";
        public static final String TMP_TAIL_STR = "_people2000_tmp_tmp"; // 重加载库的后缀
        public static final String DOWNLOAD_FILE = File.separator + "download";
        public static final String PLUGIN_COUNTER = "PLUGIN_COUNTER";
        public static final String MD5 = "MD5";
        public static final String TIME_OFFSET = "TIME_OFFSET";
    }

    public static class FileLastName {
        public static final String lastName4hwFile = ".dat";// 硬件绑定文件后缀
        public static final String lastName4cfgFile = ".cfg";// 硬件绑定文件后缀
    }

    public static class AuthMessageType {
        public static final int NEGOTIATION_AUTHENTICATION_MESSAGE = 0;  //协商认证消息
        public static final int CREATE_SESSION_KEY = 1;  //生成serssion key
        public static final int TRADE_AUTH_MESSAGE = 2;   //交易认证
    }

    public static class PluginFileHeader {
        public static final String DEVI_INFO = "0";
        public static final String JOIN_ID = "1";
        public static final String CHECK_RULE = "2";
        public static final String DEVICE_ID = "3";
        public static final String APP_SIGN = "4";
        public static final String PLUG_IN_HASH = "5";
        public static final String PLUGIN_ID = "6";
        public static final String PEOPLESDKVERSION = "7";
        public static final String OTP_AUTH = "8";
        public static final String DFPSERVER_RULE = "9";
        public static final String DFPSDK_RULE = "10";
        public static final String CONFVERSION = "11";
        public static final String BINDTYPE = "13";
    }

    public static class AppInfo {
        public String packageName;
        public String appName;
        public String apkPath;
    }
}



