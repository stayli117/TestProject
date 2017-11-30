package com.zryf.sotp.global;


/**
 * Author: DengXiaojia
 * Date: 2017/5/15
 * Email: xj_deng@people2000.net
 * LastUpdateTime: 2017/5/15
 * LastUpdateBy: DengXiaojia
 */
public class SotpException extends Exception {
    private int techCode = 0;
    public static final int PARAMETER_ERROR = 9401; //传入参数为空错误
    public static final int PACKAGE_MANAGER_NAME_NOT_FOUND_EXCEPTION = 9402;//用户信息APP_ID不匹配

    public static final int GET_APP_ID_FAILED = 9403; //获取appId失败
    public static final int GET_VERSION_FAILED = 9404; //获取sdkVersion失败
    public static final int GET_HASH_FAILED = 9405; //获取Hash失败
    public static final int GET_SIGNATURE_FAILED = 9406; //获取签名失败
    public static final int GET_PLUGIN_ID_FAILED = 9407; //获取插件Id失败
    public static final int CONTRAST_AUTH_MSG_FAILED = 9408;//对比认证消息异常[新加]

    public static final int READ_FILE_FAILED = 9501; //读取文件失败
    public static final int FILE_DELETE_ERROR = 9502; //删除文件失败
    public static final int WRITE_FILE_ERROR = 9503;  //写入文件失败

    public static final int JSON_DATA_EXCEPTION = 9901; //JSON异常
    public static final int INDEX_OUT_OF_BOUNDS_EXCEPTION = 9902; //数组越界
    public static final int INTERRUPTED_EXCEPTION = 9903; // 中断故障(异常)
    public static final int CLASS_NOT_FOUND_EXCEPTION = 9904; //类找不到异常
    public static final int METHOD_NOT_FOUND_EXCEPTION = 9905; //方法找不到异常
    public static final int INSTANTIATION_EXCEPTION = 9906; //实例化异常
    public static final int ILLEGAL_ACCESS_EXCEPTION = 9907; //安全权限异常
    public static final int INVOCATION_TARGET_EXCEPTION = 9908; //方法调用内部异常
    public static final int SOCKET_EXCEPTION = 9909;  //socket异常
    public static final int INFLATE_EXCEPTION = 9910; //填充异常

    //参数错误
    public static final int PARAM_NULL = 9001;
    public static final int PARAM_LEN_ERROR = 9002;
    public static final int ARRAY_LENGTH_ERROR = 9003;
    //文件操作错误
    public static final int OPEN_FILE_FAILED = 9011;
    public static final int FILE_READ_FAILED = 9012;
    public static final int STAT_FILE_FAILED = 9013;
    public static final int WRITE_FILE_FAILED = 9014;
    //分配内存失败
    public static final int MALLOC_FAILED = 9021;
    //编码、解码
    public static final int BASE64_CODE_FAILED = 9031;
    public static final int BASE64_DECODE_FAILED = 9032;
    //校验信息不匹配
    public static final int CHECK_HARDWARE_ERROR = 9041;
    public static final int CHECK_APPSIGN_ERROR = 9042;
    public static final int CHECK_ALG_ERROR = 9043;
    public static final int PERMISSION_DENY = 9044;
    //jni函数调用错误
    public static final int REGISTER_NATIVE_METHODS_FAILED = 9051;
    public static final int UNREGISTER_NATIVE_METHODS_FAILED = 9052;
    public static final int GETENV_FAILED = 9053;
    public static final int CALL_JNI_FAILED = 9054;
    public static final int GET_HW_ERR = 9055;
    public static final int GET_FILEPATH_FAILED = 9056;
    //初始化加解密接口失败
    public static final int INIT_INTERFACE_FAILED = 9061;
    //json解析
    public static final int JSON_FORMAT_ERROR = 9071;
    public static final int PARSE_CHECKRULE_ERROR = 9072;
    public static final int PARSE_DEVICEDED_ERROR = 9073;
    public static final int PARSE_HWINFO_ERROR = 9074;
    public static final int PARSE_SIGNTURE_ERROR = 9075;
    public static final int PARSE_AUTHMASK_ERROR = 9076;
    //OTP
    public static final int OTP_TYPE_ERROR = 9081;
    public static final int HW_TYPE_ERR = 9082;
    //算法入参及填充相关错误
    public static final int SM4DENC_PARAM_LEN_ERROR = 9091;
    public static final int SM4_PADDING_ERROR = 9092;
    public static final int DENC_FAILED = 9093;
    public static final int FPE_INITKEY_FAILED = 9094;
    public static final int FPE_DEC_FAILED = 9095;
    //dl 操作错误码。
    public static final int DLOPEN_ERR = 9100;
    public static final int DLSYM_ENC_ERR = 9101;
    public static final int DLSYM_DEC_ERR = 9102;
    //签名相关错误码
    public static final int SM2_GEN_SIGN_ERR = 9110;
    public static final int SM2_GEN_SIGN1_ERR = 9111;
    public static final int SM2_GEN_RANDOM_ERR = 9112;
    public static final int SM2_GEN_P1_ERR = 9113;
    //库加载相关错误
    public static final int UNLOAD_SO = 9120;
    public static final int UNLOAD_SO_FAILED = 9121;


    public SotpException() {
        super();
    }

    public SotpException(String msg) {
        super(msg);
        this.techCode = Integer.parseInt(msg);
    }

    public SotpException(int errorCode) {
        this.techCode = errorCode;
    }


    public int getTechCode() {
        return techCode;
    }
}
