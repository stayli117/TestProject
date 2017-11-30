package com.zryf.sotp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.zryf.sotp.global.DeviceInfo;
import com.zryf.sotp.global.PluginConstants;
import com.zryf.sotp.global.SotpException;
import com.zryf.sotp.utils.SharedPreferencesUtils;
import com.zryf.sotp.utils.TurnOn;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static com.zryf.sotp.global.PluginConstants.AuthMessageType.CREATE_SESSION_KEY;
import static com.zryf.sotp.global.PluginConstants.AuthMessageType.TRADE_AUTH_MESSAGE;
import static com.zryf.sotp.global.PluginConstants.PluginFileHeader.BINDTYPE;
import static com.zryf.sotp.global.PluginConstants.PluginFileHeader.CONFVERSION;
import static com.zryf.sotp.global.PluginConstants.PluginFileHeader.DEVICE_ID;
import static com.zryf.sotp.global.PluginConstants.PluginFileHeader.DFPSERVER_RULE;
import static com.zryf.sotp.global.PluginConstants.PluginFileHeader.PLUGIN_ID;
import static com.zryf.sotp.global.PluginConstants.ProtectionsPram.DOWNLOAD_FILE;
import static com.zryf.sotp.global.PluginConstants.ProtectionsPram.MD5;
import static com.zryf.sotp.global.PluginConstants.ProtectionsPram.META_DATA_APP_ID;
import static com.zryf.sotp.global.PluginConstants.ProtectionsPram.META_DATA_SDK_VERSION;
import static com.zryf.sotp.global.PluginConstants.ProtectionsPram.PLUGIN_COUNTER;
import static com.zryf.sotp.global.PluginConstants.ProtectionsPram.TIME_OFFSET;
import static com.zryf.sotp.global.PluginConstants.SotpType.OTP_CHALLENGE;
import static com.zryf.sotp.global.PluginConstants.SotpType.OTP_CHALLENGE_SCAN_LOGIN;
import static com.zryf.sotp.global.PluginConstants.SotpType.OTP_PIN_CHALLENGE;
import static com.zryf.sotp.global.PluginConstants.SotpType.OTP_TIME;
import static com.zryf.sotp.global.PluginConstants.SotpType.OTP_TIME_CODE;
import static com.zryf.sotp.global.PluginConstants.SotpType.OTP_TIME_LOGIN;
import static com.zryf.sotp.global.PluginConstants.SotpType.OTP_TIME_PIN;
import static com.zryf.sotp.global.PluginConstants.SotpType.OTP_TIME_PIN_LOGIN;
import static com.zryf.sotp.global.PluginConstants.pluginName;
import static com.zryf.sotp.global.PluginConstants.tmpPluginName;


public class SotpClient {
    private Context mContext = null;
    private static SotpClient sotpClientInstance;
    private static DeviceInfo mDeviceInfo;
    private static String mAppId = null;// 要求用户在AndroidMinifest文件中配置的APPID值
    private static String mSdkVersion = null;

    static {
        System.loadLibrary("sotpcomm");
        System.loadLibrary("local");
    }


    /**
     * 获取单例的构造方法，接口的入口
     *
     * @param context 上下文
     */
    private SotpClient(Context context) {
        this.mContext = context;
    }


    private void PARAMETER_ERROR_CHECK(String data) throws SotpException {
        if (data == null || data.trim().isEmpty()) {
            throw new SotpException(SotpException.PARAMETER_ERROR);
        }
    }

    private static void PARAMETER_ERROR_CHECK(String checkPram, int ExceptionCode) throws SotpException {
        if (checkPram == null || checkPram.trim().isEmpty()) {
            throw new SotpException(ExceptionCode);
        }
    }


    /**
     * 提供给用户接口的入口，获取单例
     *
     * @param context
     * @return
     * @throws SotpException
     */
    public static SotpClient getInstance(Context context) throws SotpException {
        if (context == null) {
            throw new SotpException(SotpException.PARAMETER_ERROR);
        }
        if (sotpClientInstance == null) {
            sotpClientInstance = new SotpClient(context);
            ApplicationInfo applicationInfo;
            try {
                applicationInfo = context.getPackageManager().getApplicationInfo(
                        context.getPackageName(), PackageManager.GET_META_DATA);

                mAppId = applicationInfo.metaData.getString(META_DATA_APP_ID);
                mSdkVersion = applicationInfo.metaData.getFloat(META_DATA_SDK_VERSION) + "";
                PARAMETER_ERROR_CHECK(mAppId, SotpException.GET_APP_ID_FAILED);
                PARAMETER_ERROR_CHECK(mSdkVersion, SotpException.GET_VERSION_FAILED);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                throw new SotpException(SotpException.PACKAGE_MANAGER_NAME_NOT_FOUND_EXCEPTION);
            }
            mDeviceInfo = new DeviceInfo(context);
        }
        return sotpClientInstance;
    }

    /**
     * 获取设备环境信息
     *
     * @param type 类型。0:下载, 更新(传全集)； 1,认证及其他操作(根据配置选传)
     * @return Base64后的Json字符串
     */
    public String getSotpEnvInfo(int type) throws SotpException {
        return mDeviceInfo.getSotpDeviceInfo(type);
    }

    /**
     * 获取应用App信息
     * 获取App的信息。App的签名，App的Hash，SDK的版本号；
     *
     * @return 封装成的报文，Base64编码
     * @throws SotpException
     */
    public String getSotpAppInfo() throws SotpException {
        try {
            JSONObject appInfoObject = new JSONObject();
            appInfoObject.put(PluginConstants.ServiceParam.APP_ID, mAppId);
            appInfoObject.put(PluginConstants.ServiceParam.SIGN, getSignedinfo());
            appInfoObject.put(PluginConstants.ServiceParam.HASH, getAPKMD5(mContext));
            appInfoObject.put(PluginConstants.ServiceParam.SDK_VERSION, mSdkVersion);
            TurnOn.d("getSotpAppInfo method appInfo json=" + appInfoObject.toString());
            return Base64.encodeToString(appInfoObject.toString().getBytes(), Base64.DEFAULT).replace("\n", "");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.JSON_DATA_EXCEPTION);
        }
    }

    /**
     * 获取插件信息
     * 获取插件的信息。插件Id，插件hash，插件使用次数，设备标识
     *
     * @return Base64
     * @throws SotpException
     */
    public String getSotpInfo(String userName) throws SotpException {
        try {
            JSONObject sotpInfoObject = new JSONObject();
            sotpInfoObject.put(PluginConstants.ServiceParam.PLUGIN_ID, getSotpId(userName));
            sotpInfoObject.put(PluginConstants.ServiceParam.HASH, getFileMD5(PluginConstants.realPath));// 插件hash
            sotpInfoObject.put(PluginConstants.ServiceParam.DEV_ID, SharedPreferencesUtils.getPropertyStr(mContext, DEVICE_ID));
            String count = getUseCount();
            sotpInfoObject.put(PluginConstants.ServiceParam.USER_COUNT, count);// 插件hash
            sotpInfoObject.put(PluginConstants.ServiceParam.BINDTYPE, SharedPreferencesUtils.getProperty(mContext, "bindType"));
            TurnOn.d("getSotpAppInfo method appInfo json=" + sotpInfoObject.toString());
            String sotpInfo = Base64.encodeToString(sotpInfoObject.toString().getBytes(), Base64.DEFAULT).replace("\n", "");
            TurnOn.d("getSotpAppInfo method appInfo=" + sotpInfo);
            return sotpInfo;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.JSON_DATA_EXCEPTION);
        }
    }

    /**
     * 获取插件Id
     *
     * @return 插件Id
     * @throws SotpException
     */
    public String getSotpId(String userId) throws SotpException {
        String pluginId = SharedPreferencesUtils.getPropertyStr(mContext, userId);
        if (pluginId.trim().isEmpty()) {
            throw new SotpException(SotpException.GET_PLUGIN_ID_FAILED);
        }
        return pluginId;
    }


    /**
     * 生成公因子
     *
     * @return
     * @throws SotpException
     */
    public String getPublicKey() throws SotpException {
        return getClientPubKey(mContext);
    }

    /**
     * 保存公钥
     *
     * @param publicKey 公钥
     * @return 0 成功
     */
    public void savePublicKey(String publicKey) throws SotpException {
        saveSerPubKey(mContext, publicKey);
    }

    /**
     * 获取客户端签名因子
     *
     * @param data 待签名数据
     * @return 获取签名的业务报文
     * @throws SotpException
     */
    public String genSignature(String data, String userId) throws SotpException {
        return getClientSignInfo(mContext, data, getSotpId(userId));
    }

    /**
     * 获取最终签名
     *
     * @param serverSign 服务端返回的签名因子
     * @return 最终签名
     */
    public String getSignResult(String serverSign) throws SotpException {
        PARAMETER_ERROR_CHECK(serverSign);
        return getClientSignRet(mContext, serverSign);
    }


    /**
     * 保存SOTP插件的接口
     *
     * @param pluginData   服务器传来的插件数据
     * @param confInfoData 服务器传来的配置信息
     * @param userId       用户标识
     * @return 0:		保存成功
     * @throws SotpException
     */
    public int saveSotp(String pluginData, String confInfoData, String userId) throws SotpException {
        TurnOn.d("pluginData" + pluginData + "/confInfoData" + confInfoData + "/userId" + userId);
        PARAMETER_ERROR_CHECK(pluginData);
        PARAMETER_ERROR_CHECK(confInfoData);
        PARAMETER_ERROR_CHECK(userId);
        String attachmentBaseData = new String(Base64.decode(confInfoData, Base64.DEFAULT));
        try {
            JSONObject jsonObject = new JSONObject(attachmentBaseData);
            String deviceId = jsonObject.optString(DEVICE_ID);
            String pluginId = jsonObject.optString(PLUGIN_ID);
            int fingerprintPolicy = jsonObject.optInt(DFPSERVER_RULE, -1);
            String conVersion = jsonObject.optString(CONFVERSION);
            int bindType = jsonObject.optInt(BINDTYPE);

//            SharedPreferencesUtils.setPluginId(mContext, pluginId);
            SharedPreferencesUtils.setPropertyStr(mContext, userId, pluginId);
            SharedPreferencesUtils.setPropertyStr(mContext, DEVICE_ID, deviceId);
            SharedPreferencesUtils.setProperty(mContext, "depServerRule", fingerprintPolicy);
            SharedPreferencesUtils.setPropertyStr(mContext, "confVersion", conVersion);
            SharedPreferencesUtils.setProperty(mContext, "bindType", bindType);

        } catch (JSONException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.JSON_DATA_EXCEPTION);
        }
        //创建双文件  分别存放插件数据，配置文件
        PluginConstants.realPath = mContext.getFilesDir().getPath() + DOWNLOAD_FILE + File.separator +
                userId + File.separator + pluginName;

        File pluginFile = new File(PluginConstants.realPath);
        File confInfoFile = new File(PluginConstants.realPath
                + PluginConstants.FileLastName.lastName4hwFile);
        File useCountFile = new File(PluginConstants.realPath
                + PluginConstants.FileLastName.lastName4cfgFile);
        //创建两者的父目录
        File parentFile4plugin = new File(pluginFile.getParentFile().getAbsolutePath());
        File parentFile4hw = new File(confInfoFile.getParentFile().getAbsolutePath());
        if (!parentFile4plugin.exists()) {
            parentFile4plugin.mkdirs();
        }
        if (!parentFile4hw.exists()) {
            parentFile4hw.mkdirs();
        }

        JSONObject cfgJson = new JSONObject();
        try {
            cfgJson.put(PLUGIN_COUNTER, 0);
            cfgJson.put(TIME_OFFSET, 0);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.JSON_DATA_EXCEPTION);
        }
        //开始解码
        byte[] pluginDataFile = Base64.decode(pluginData, Base64.DEFAULT);
        byte[] confInfoDateFile = Base64.decode(confInfoData, Base64.DEFAULT);
        byte[] by = cfgJson.toString().getBytes();

        FileOutputStream osPlugin = null;
        FileOutputStream osCnfInfo = null;
        FileOutputStream osUseCount = null;
        //创建文件流，保存解码数据
        try {
            osPlugin = new FileOutputStream(pluginFile);
            osPlugin.write(pluginDataFile);
            osCnfInfo = new FileOutputStream(confInfoFile);
            osCnfInfo.write(confInfoDateFile);
            osUseCount = new FileOutputStream(useCountFile);
            osUseCount.write(by);
            //删除公私钥
            File secretFile = new File(mContext.getFilesDir().getPath() + File.separator + ".random1");
            File publicFile = new File(mContext.getFilesDir().getPath() + File.separator + ".pubKey");
            if (secretFile.exists()) {
                secretFile.delete();
            }
            if (publicFile.exists()) {
                publicFile.delete();
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.WRITE_FILE_ERROR);
        } finally {
            try {
                if (osPlugin != null) {
                    osPlugin.close();
                }
                if (osCnfInfo != null) {
                    osCnfInfo.close();
                }
                if (osUseCount != null) {
                    osUseCount.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载预置插件
     *
     * @throws SotpException
     */
    public void loadSotpLocal() throws SotpException {
        unLoadSOTPLocal();
        loadSOTPLocal(mContext.getFilesDir().getParent() + "/lib/libplugin.so");
    }


    /**
     * 加载SOTP安全插件。保存插件后，调用本接口可以将安全插件加载。
     *
     * @param userId 用户标识
     * @return
     */
    public int initSotp(String userId) throws SotpException {
        TurnOn.d("initSotp userId" + userId);
        PARAMETER_ERROR_CHECK(userId);
        PluginConstants.realPath = mContext.getFilesDir().getPath() + DOWNLOAD_FILE + File.separator + userId + File.separator + pluginName;
        String tmpPath = mContext.getFilesDir().getPath() + DOWNLOAD_FILE + File.separator + userId + File.separator + tmpPluginName;
        //判断插件文件存在
        File pluginFile = new File(PluginConstants.realPath);
        File tmpFile = new File(tmpPath);
        if (!pluginFile.exists()) {
            throw new SotpException(SotpException.READ_FILE_FAILED);
        }

        try {
            FileInputStream inputStream = new FileInputStream(pluginFile);
            FileOutputStream outputStream = new FileOutputStream(tmpFile);
            byte[] bytes = new byte[(int) pluginFile.length()];
            inputStream.read(bytes);
            inputStream.close();
            //解压
            byte[] plugin = inflate(bytes);
            outputStream.write(plugin);
            outputStream.close();

            //判断硬件文件是否存在，存在则为新版本库，不存在则为旧版本库
            File hdFile = new File(PluginConstants.realPath + PluginConstants.FileLastName.lastName4hwFile);
            if (hdFile.exists()) {
                //新版本库
                String[] deviceStr = mDeviceInfo.getJniDevice(PluginConstants.FINGERPRINT_POLICY);
                unLoadSOTP();
                sotpInit(mContext, tmpPath, PluginConstants.realPath + PluginConstants.FileLastName.lastName4hwFile, deviceStr);
                tmpFile.delete();
                return 0;
            } else {
                throw new SotpException(SotpException.READ_FILE_FAILED);
            }
        } catch (FileNotFoundException e) {
            TurnOn.d(e.getMessage() + "\n" + Log.getStackTraceString(e));
            throw new SotpException(SotpException.READ_FILE_FAILED);
        } catch (IOException e) {
            TurnOn.d(e.getMessage() + "\n" + Log.getStackTraceString(e));
            throw new SotpException(SotpException.WRITE_FILE_ERROR);
        } catch (DataFormatException e) {
            TurnOn.d(e.getMessage() + "\n" + Log.getStackTraceString(e));
            throw new SotpException(SotpException.INFLATE_EXCEPTION);
        }
    }

    /**
     * 卸载插件
     *
     * @throws SotpException
     */
    public void unloadSotp() throws SotpException {
        unLoadSOTP();
    }

    public void unLoadSotpLocal() throws SotpException {
        unLoadSOTPLocal();
    }

    /**
     * 生成动态口令
     *
     * @param type      类型。1,时间型；2,时间pin码型；3,挑战型；4,挑战pin码型
     * @param time      时间。
     * @param pin       pin码。
     * @param challenge 挑战码。
     * @return 一次性口令码
     * @throws SotpException
     */
    public String getSotpCode(int type, String time, String pin, String challenge) throws SotpException {
        String sotpCode = null;
        switch (type) {
            case OTP_TIME:
                String[] pramsTime = {time};
                sotpCode = sotpGenOtp(mContext, OTP_TIME, pramsTime);
//                sotpCode = PluginConstants.Confounding.TEN + sotpCode;
                TurnOn.e("pram1=" + pramsTime[0] + " /TYPE=" + OTP_TIME + "  /sotpCode=" + sotpCode);
                break;
            case OTP_TIME_LOGIN:
                String[] pramsTimeLogin = {time};
                sotpCode = sotpGenOtp(mContext, OTP_TIME, pramsTimeLogin);
//                sotpCode = PluginConstants.Confounding.TEN + sotpCode + "|" + getUseCount();
                sotpCode = sotpCode + "|" + getUseCount();
                TurnOn.e("pram1=" + pramsTimeLogin[0] + " /TYPE=" + OTP_TIME + "  /sotpCode=" + sotpCode);
                break;
            case OTP_TIME_CODE:
                //时间型获取otp   web页面输入otp，登录
                String[] pramsTimeWeb = {time};
                sotpCode = sotpGenOtp(mContext, OTP_TIME, pramsTimeWeb);
                TurnOn.e("pram1=" + pramsTimeWeb[0] + " /TYPE=" + OTP_TIME_CODE + "sotpCode" + sotpCode);
                break;
            case OTP_TIME_PIN:
                String[] pramsTimePin = {time, pin};
                sotpCode = sotpGenOtp(mContext, OTP_TIME_PIN, pramsTimePin);
//                sotpCode = PluginConstants.Confounding.TWENTY + sotpCode;
                TurnOn.e("pram1=" + pramsTimePin[0] + "/" + pramsTimePin[1] + " /TYPE=" + OTP_TIME_PIN + "  /sotpCode=" + sotpCode);
                break;
            case OTP_TIME_PIN_LOGIN:
                String[] pramsTimePinLogin = {time, pin};
                sotpCode = sotpGenOtp(mContext, OTP_TIME_PIN, pramsTimePinLogin);
                TurnOn.e("pram1=" + pramsTimePinLogin[0] + "/" + pramsTimePinLogin[1] + " /TYPE=" + OTP_TIME_PIN + "  /sotpCode=" + sotpCode);
                break;

            case OTP_CHALLENGE:
                String[] pramsChallenge = {challenge};
                sotpCode = sotpGenOtp(mContext, OTP_CHALLENGE, pramsChallenge);
//                sotpCode = PluginConstants.Confounding.THIRTY + sotpCode;
                TurnOn.e("pram=" + challenge + " /TYPE=" + OTP_CHALLENGE + "  /sotpCode=" + sotpCode);
                break;
            case OTP_CHALLENGE_SCAN_LOGIN:
                String[] pramsCS = {challenge};
                sotpCode = sotpGenOtp(mContext, OTP_CHALLENGE, pramsCS);
//                sotpCode = PluginConstants.Confounding.THIRTY + sotpCode + "|" + getUseCount();
                sotpCode = sotpCode + "|" + getUseCount();
                TurnOn.e("pram=" + challenge + " /TYPE=" + OTP_CHALLENGE + "  /sotpCode=" + sotpCode);
                break;
            case OTP_PIN_CHALLENGE:
                String[] pramsPC = {pin, challenge};
                sotpCode = sotpGenOtp(mContext, OTP_PIN_CHALLENGE, pramsPC);
                sotpCode = sotpAuthEncrypt(PluginConstants.Confounding.FORTY + sotpCode);
                TurnOn.e("pram=" + challenge + " /TYPE=" + OTP_CHALLENGE + "  /sotpCode=" + sotpCode);
                break;
        }
        return sotpCode;
    }


    /**
     * Sbox加密
     * sotp算法生成对称密钥，SM4加密。（短信加密）
     *
     * @param data 待加密数据
     * @return 加密后的数据
     */
    public String sBoxEncrypt(String data) throws SotpException {
        PARAMETER_ERROR_CHECK(data);
        return encMsg(data);
    }

    /**
     * Sbox加密(设备信息+随机数)
     * sotp算法生成对称密钥，SM4加密。（短信加密）
     *
     * @param data   待加密数据
     * @param random 随机数
     * @return 加密后的数据
     * @throws SotpException
     */
    public String sBoxEncrypt(String data, String random) throws SotpException {
        PARAMETER_ERROR_CHECK(data);
        PARAMETER_ERROR_CHECK(random);

        return enc_msg(random, data);

    }


    /**
     * 预置算法加密短信(appid+随机数) 无插件
     *
     * @param data   待加密数据
     * @param random 随机数
     * @return 加密后的数据
     * @throws SotpException
     */
    public String sBoxLocaltEncrypt(String data, String random) throws SotpException {
        PARAMETER_ERROR_CHECK(data);
        PARAMETER_ERROR_CHECK(random);

//        return localEncMsg(mAppId, random, data);
        return encMsgLocal(mAppId, random, data);

    }

    /**
     * 短信FP1解密加密
     *
     * @param type    type = 0， 没有插件   type = 1， 有插件
     * @param userId  用户标识
     * @param msgText 加密文本
     * @return
     */
    public String getMsgDecrypts(int type, String userId, String msgText) {
        String userInfo = 2 + "|" + userId;
        return msgFPEDec(type, userInfo, msgText);
    }

    /**
     * 生成客户端认证消息
     *
     * @param time      客户端传给服务端用于生成message的随机数。【由app生成传给服务端】
     * @param challenge 服务器返回的挑战值。
     * @param message   服务器返回的协商消息
     * @return 协商消息
     */
    public String genSotpMessage(String time, String challenge, String message) throws SotpException {
        PARAMETER_ERROR_CHECK(message);

        String[] tradeAuthMsg = {time};
        String localTradeAuthMsg = null;
        try {
            localTradeAuthMsg = authMsg(TRADE_AUTH_MESSAGE, tradeAuthMsg);
        } catch (SotpException e) {
            throw new SotpException(e.getTechCode());
        }
        if (localTradeAuthMsg.equals(message)) {
            String[] sessionKeyParam = {challenge, time};
            try {
                authMsg(CREATE_SESSION_KEY, sessionKeyParam);
            } catch (SotpException e) {
                throw new SotpException(e.getTechCode());
            }
            String[] authMsgPer = {challenge};
            return authMsg(TRADE_AUTH_MESSAGE, authMsgPer);
        } else {
            throw new SotpException(SotpException.CONTRAST_AUTH_MSG_FAILED);
        }
    }

    /**
     * 会话密钥加密
     * 会话密钥加密原文，并返回十六进制密文。【对称算法】
     *
     * @param plainData 待加密的明文数据。
     * @return String:	 十六进制密文字符串
     */
    public String sessionkeyEncrypt(String plainData) throws SotpException {
        PARAMETER_ERROR_CHECK(plainData);
        return sotpSessionEncrypt(0, 0, plainData);
    }

    /**
     * 会话密钥解密
     * 会话密钥解密，并返回十六进制或base64格式原文。【对称算法】
     *
     * @param cipherData 待解密的密文数据。
     * @param dataType   返回的原文数据类型
     * @return 解密后的原文
     * @throws SotpException
     */
    public String sessionkeyDecrypt(String cipherData, String dataType) throws SotpException {
        PARAMETER_ERROR_CHECK(cipherData);
        PARAMETER_ERROR_CHECK(dataType);
        return sotpSessionDecrypt(0, 0, cipherData);
    }


    /**
     * 检查插件是否存在
     *
     * @param context
     * @param userId  用户名
     * @return false:	不存在        true:	 存在
     * @throws SotpException
     */
    public boolean isPluginExists(Context context, String userId) throws SotpException {
        PARAMETER_ERROR_CHECK(userId);

        String filePath = context.getFilesDir().getPath() + DOWNLOAD_FILE + File.separator + userId + File.separator + pluginName;
        File pluginFile = new File(filePath);
        return pluginFile.exists();
    }

    /**
     * 使用插件进行加密。下载插件第一步成功获取挑战码，对该挑战码进行加密，
     * 作为请求下载插件第二步返回给后端challengeAns的value
     *
     * @param data 待加密数据
     * @return 加密结果
     */
    public String sotpEncrypt(String data) throws SotpException {
        PARAMETER_ERROR_CHECK(data);
        return sotpAuthEncrypt(data);
    }

    /**
     * 删除插件
     *
     * @param userId 用户信息
     * @return false:	删除失败        true:删除成功
     */
    public boolean isDelPlugin(Context context, String userId) throws SotpException {
        PARAMETER_ERROR_CHECK(userId);
        String filePath = context.getFilesDir().getPath() + DOWNLOAD_FILE + File.separator + userId + File.separator + pluginName;
        File pluginFile = new File(filePath);
        return delete(pluginFile);
    }

    /**
     * 删除文件
     *
     * @param pluginFile 文件路径
     * @return
     */
    private static boolean delete(File pluginFile) {
        if (pluginFile.exists() && pluginFile.isFile()) {
            return pluginFile.delete();
        }
        if (pluginFile.isDirectory()) {
            File[] childFiles = pluginFile.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return pluginFile.delete();
            }
            if (childFiles.length > 0) {
                for (File childFile : childFiles) {
                    delete(childFile);
                }
            }
        }

        return pluginFile.delete();
    }

    /**
     * 获得签名信息
     *
     * @return
     * @throws SotpException
     */
    private String getSignedinfo() throws SotpException {
        PackageInfo packageInfo = null;
        try {
            PackageManager packageManager = mContext.getPackageManager();
            String name = mContext.getPackageName();
            if (packageManager == null || name == null)
                throw new SotpException(SotpException.GET_SIGNATURE_FAILED);
            packageInfo = packageManager.getPackageInfo(name,
                    PackageManager.GET_SIGNATURES);

        } catch (NameNotFoundException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.GET_SIGNATURE_FAILED);
        }
        Signature[] signatures = packageInfo.signatures;
        Signature sign = signatures[0];
        String digest = new Integer(sign.hashCode()).toString();
        return digest;
    }

    /**
     * 获取App的Hash
     *
     * @param mContext
     * @return
     */
    private String getAPKMD5(Context mContext) {
        InputStream is = null;
        int len;
        MessageDigest msgDigest = null;
        String path = mContext.getPackageCodePath();
        File file = new File(path);
        try {
            is = (InputStream) new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        try {
            msgDigest = MessageDigest.getInstance(MD5);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] content = new byte[1024 * 2];
        try {
            while ((len = is.read(content)) != -1)
                msgDigest.update(content, 0, len);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] digest = msgDigest.digest();
        return byteArrayToHex(digest);
    }

    private String byteArrayToHex(byte[] byteArray) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < byteArray.length; i++) {
            tmp = (Integer.toHexString(byteArray[i] & 0xff));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }

        return des;

    }

    /**
     * 读取插件使用次数
     *
     * @return 使用次数
     */
    public String getUseCount() throws SotpException {
        int userCountInt = 0;
        FileInputStream inputStream = null;
        FileOutputStream outputStrem = null;
        try {
            /*找到调用次数配置文件*/
            File cfgfile = new File(PluginConstants.realPath + PluginConstants.FileLastName.lastName4cfgFile);
            /*初始化文件的输入输出流*/
            inputStream = new FileInputStream(cfgfile);
            /*读出文件数据*/
            byte[] bytes = new byte[(int) cfgfile.length()];
            int result = inputStream.read(bytes);
            if (result < 0) {
                throw new SotpException(SotpException.READ_FILE_FAILED);
            }
            /*将数据转换为JSON格式*/
            JSONObject jsonObject = new JSONObject(new String(bytes));
            /*从JSON中解析出pluginCounter项，存到变量里*/

            userCountInt = jsonObject.getInt(PLUGIN_COUNTER);
            /*将pluginCounter加一再保存回去*/
            ++userCountInt;
            jsonObject.put(PLUGIN_COUNTER, userCountInt);
            outputStrem = new FileOutputStream(cfgfile);
            outputStrem.write(jsonObject.toString().getBytes());

        } catch (IOException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.WRITE_FILE_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.JSON_DATA_EXCEPTION);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStrem != null) {
                    outputStrem.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return userCountInt + "";
    }

    private static byte[] inflate(byte[] source) throws DataFormatException, IOException {
        Inflater inflater = new Inflater();
        ByteArrayOutputStream stream = null;
        byte[] result = null;
        try {
            inflater.setInput(source);
            stream = new ByteArrayOutputStream(source.length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int decompressed = inflater.inflate(buffer);
                stream.write(buffer, 0, decompressed);
            }
            stream.close();
            result = stream.toByteArray();
            stream = null;
        } finally {
            inflater.end();
            if (stream != null) {
                stream.close();
            }
        }
        return result;
    }

    /**
     * app 文件 hash
     *
     * @param filePath
     * @return
     * @throws SotpException
     */
    private String getFileMD5(String filePath) throws SotpException {
        FileInputStream inputStream = null;
        int len = 0;
        MessageDigest digest = null;
        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            return "";
        }
        try {

            digest = MessageDigest.getInstance(MD5);
            inputStream = new FileInputStream(inputFile);
            byte[] bytes = new byte[1024 * 4];
            while (((len = inputStream.read(bytes)) != -1))
                digest.update(bytes, 0, len);
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.GET_HASH_FAILED);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.READ_FILE_FAILED);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.WRITE_FILE_ERROR);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 各种Native方法，用于和插件中的JNI方法呼应

    /**
     * 加载插件
     *
     * @param pluginPath 插件路径
     * @return
     * @throws SotpException
     */
    private native int loadSOTP(String pluginPath) throws SotpException;

    /**
     * 加载预置插件
     *
     * @param pluginPath
     * @return
     * @throws SotpException
     */
    private native int loadSOTPLocal(String pluginPath) throws SotpException;


    /**
     * @param context    上下文
     * @param pluginPath 插件路径
     * @param hwFilePath 配置文件路径
     * @param hwInfo     手机硬件配置信息数组
     * @throws SotpException
     */
    private native void sotpInit(Context context, String pluginPath, String hwFilePath, String[] hwInfo) throws SotpException;  //插件初始化

    /**
     * 卸载插件
     *
     * @throws SotpException
     */
    private native void unLoadSOTP() throws SotpException;


    /**
     * 卸载预置插件
     *
     * @throws SotpException
     */
    private native void unLoadSOTPLocal() throws SotpException;

    /**
     * 生成otp
     * time : 时间字符串。
     * Pin : 长度小于128的字符串
     * Challenge : 长度小于128的字符串
     *
     * @param context 上下文
     * @param type    1，        2，          3，          4
     * @param info    [time]  [time , pin]  [challenge]  [challenge, pin]
     * @return otp
     * @throws SotpException
     */
    private native static String sotpGenOtp(Context context, int type, String[] info) throws SotpException;  //生成otp码

    /**
     * 客户端加密接口
     *
     * @param plain 需要加密的明文字符串。
     * @return 加密结果base64的字符串
     * @throws SotpException
     */
    private native static String sotpAuthEncrypt(String plain) throws SotpException;  //客户端加密

    private native static String sotpAuthDecrypt(String cipher) throws SotpException;  //客户端解密

//    private native static int sotpSetSessionKey(String challenge1, String challenge2) throws SotpException;   //生成会话密钥

    /**
     * 会话密钥加密接口
     *
     * @param type  加密解密 0
     * @param alg   加密方式 0
     * @param plain 加密数据String
     * @return
     * @throws SotpException
     */
    private native static String sotpSessionEncrypt(int type, int alg, String plain) throws SotpException;  //会话密钥加密

    /**
     * 会话密钥解密接口
     *
     * @param type   0
     * @param alg    0
     * @param cipher 会话密钥加密密文
     * @return
     * @throws SotpException
     */
    private native static String sotpSessionDecrypt(int type, int alg, String cipher) throws SotpException;  //会话密钥解密

    /**
     * 生成认证消息
     *
     * @param type 0，生成认证消息          1, 生成会话密钥        2，交易认证
     * @param info info{random, pin}        info{random, time}     Info:{random}
     * @return 认证消息                      null                     认证消息
     * @throws SotpException
     */
    private native static String authMsg(int type, String[] info) throws SotpException;

    private native static String getLibcomVer();

    /**
     * 短信加密接口
     *
     * @param msg 需要加密的明文
     * @return 密文base64的字符串
     */
    private native static String encMsg(String msg) throws SotpException;

    /**
     * 短信加密接口,appID传值是预置插件加密，appID传null是个性化插件加密
     *
     * @param random 随机数
     * @param msg    需要加密的明文
     * @return
     */
    private native static String enc_msg(String random, String msg) throws SotpException;

    /**
     * 本地短信加密接口(无插件)
     *
     * @param appID  标识应用版本
     * @param random 随机谁
     * @param msg    需要加密的明文
     * @return
     */
    private native static String encMsgLocal(String appID, String random, String msg) throws SotpException;

    /**
     * 短信FP1解密接口
     *
     * @param type   type = 0， 没有插件
     * @param info   userName
     * @param encMsg
     * @return
     */
    public native String msgFPEDec(int type, String info, String encMsg);

    /**
     * 获取客户端公钥
     *
     * @param context
     * @return
     * @throws SotpException
     */
    private native String getClientPubKey(Context context) throws SotpException;

    /**
     * 保存客户端公钥
     *
     * @param context
     * @param pubKey
     * @return
     * @throws SotpException
     */
    private native void saveSerPubKey(Context context, String pubKey) throws SotpException;

    /**
     * 获取客户端签名因子
     *
     * @param context
     * @param data
     * @param UID
     * @return
     * @throws SotpException
     */
    private native String getClientSignInfo(Context context, String data, String UID) throws SotpException;

    /**
     * 获取客户端签名
     *
     * @param context
     * @param serSignInfo
     * @return
     * @throws SotpException
     */
    private native String getClientSignRet(Context context, String serSignInfo) throws SotpException;


}

