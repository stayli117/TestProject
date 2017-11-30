package com.zryf.sotp.global;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.zryf.sotp.utils.SharedPreferencesUtils;
import com.zryf.sotp.utils.TurnOn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Author: DengXiaojia
 * Date: 2017/2/13
 * Email: xj_deng@people2000.net
 * LastUpdateTime: 2017/2/13
 * LastUpdateBy: DengXiaojia
 */
public class DeviceInfo {
    private Context mContext = null;
    private static long totalMemory = 0;
    private LocationManager manager;
    private String loc;
    private String provider;

    private static String[] policyFingerprint = {"imei", "equipmentModel", "device", "serialNumber", "displayMetrics", "board",
            "hardware", "radioVersion", "fingerprint", "host", "mac", "imsi", "user", "cpuAbi", "cpuCount", "totalMemory",
            "baseBandVersion", "version", "buildVersion", "androidVersion", "ipAddress",  "providerName", "location",
            "wifiInfo", "uuid", "phoneScreenSize", "mobileOperators", "language"};


    public DeviceInfo(Context context) {
        this.mContext = context;
    }

    /**
     * 获取设备硬件信息
     *
     * @param type 0:下载\更新; 1:其他操作
     * @return 设备imei imsi 手机号码，手机android sdk版本，手机系统版本，手机型号,手机厂商，
     * 手机蓝牙mac地址，手机wifi mac地址
     * @throws SotpException
     */
    public String getSotpDeviceInfo(int type) throws SotpException {
        if (type == PluginConstants.FINGERPRINT_TYPE_ZERO) {
            return getSotpDevice(PluginConstants.FINGERPRINT_POLICY);
        } else {
            //后端认证项
            int fingerprintPolicy = SharedPreferencesUtils.getProperty(mContext, "depServerRule");
            TurnOn.e("fingerprintPolicy=" + fingerprintPolicy);
            if (fingerprintPolicy < 0 || fingerprintPolicy == 0) {
                return getSotpDevice(PluginConstants.FINGERPRINT_POLICY);
            } else {
                return getSotpDevice(fingerprintPolicy);

            }
        }
    }

    public String[] getJniDevice(int num) throws SotpException {
        TurnOn.d("getJniDevice--->" + num);
        String[] str = getFingerprintJniPolicy(num);
        return str;

    }

    /**
     * 设备信息给jni使用
     *
     * @param collectionFiled
     * @return
     */
    public String[] getFingerprintJniPolicy(int collectionFiled) throws SotpException {
        String collectionStr = getPolicyFingerprint(collectionFiled);
        String[] ary = collectionStr.split("\\|");
        String[] st = new String[ary.length];
        for (int i = 0; i < ary.length; i++) {
            Object deviceInfo = getFingerprintInfo(ary[i]);
            String deviceInfoStr = deviceInfo.toString();
            st[i] = deviceInfoStr;
        }
        return st;
    }

    /**
     * 根据配置项收集硬件信息
     *
     * @param num 后端认证项配置值
     * @return base64后的硬件信息
     * @throws SotpException
     */
    private String getSotpDevice(int num) throws SotpException {
        TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = telManager.getLine1Number();
        String phoneRelease = Build.VERSION.RELEASE;
        String phoneBrand = Build.BRAND;


        JSONObject object = new JSONObject();
        JSONObject riskObj = new JSONObject();
        //sxy 设备指纹
        object = getFingerprintPolicy(num);

        try {
            object.put("dev_type", "Android");
            if (phoneNumber != null)
                object.put("phone_num", phoneNumber);

            if (phoneRelease != null)
                object.put("system_version", phoneRelease);
            if (phoneBrand != null)
                object.put("manufacturer", phoneBrand);

            if (isRootSystem()) {

                JSONArray result = isRooted();
                riskObj.put("root", result);

            } else {
                riskObj.put("root", "0");
            }

            if (checkXp()) {
                JSONArray xpArray = new JSONArray();
                xpArray.put("xposed");
                riskObj.put("hook", xpArray);

            } else {
                riskObj.put("hook", "0");
            }


            object.put("riskInfo", riskObj);

            //配置文件版本号"confVersion":
            String confVersion = SharedPreferencesUtils.getPropertyStr(mContext, "confVersion");
            if (!TextUtils.isEmpty(confVersion)) {
                object.put("confVersion", confVersion);
            }
            //  返回Base64编码的结果
            return Base64.encodeToString(object.toString().getBytes(), Base64.DEFAULT).replace("\n", "");

        } catch (JSONException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.JSON_DATA_EXCEPTION);
        }

    }

    /**
     * 收集设备指纹信息
     *
     * @param collectionFiled 后端认证项配置值
     * @return 硬件信息json
     */
    public JSONObject getFingerprintPolicy(int collectionFiled) throws SotpException {
        String collectionStr = getPolicyFingerprint(collectionFiled);
        String[] ary = collectionStr.split("\\|");
        JSONObject object = new JSONObject();
        for (String anAry : ary) {
            Object deviceInfo = getFingerprintInfo(anAry);
            try {
                if (deviceInfo != null) {
                    object.put(anAry, deviceInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                throw new SotpException(SotpException.JSON_DATA_EXCEPTION);
            }
        }

        return object;
    }


    /**
     * @param key 根据位图切割的项
     * @return 硬件信息任意类型, 获取值为空, 返回""
     */
    public Object getFingerprintInfo(String key) throws SotpException {
        switch (key) {
            // 设备型号
            case "equipmentModel":
                return noNullStr(Build.MODEL);

            // 设备参数
            case "device":
                return noNullStr(Build.DEVICE);

            // 设备序列号
            case "serialNumber":
                return noNullStr(getSerialNumber());

            // 屏幕分辨率
            case "displayMetrics":
                return getScreen();

            // 主板
            case "board":
                return noNullStr(Build.BOARD);

            // 硬件名称
            case "hardware":
                return noNullStr(Build.HARDWARE);

            // 无线电固定版本
            case "radioVersion":
                return noNullStr(Build.getRadioVersion());

            // 唯一识别码
            case "fingerprint":
                return noNullStr(Build.FINGERPRINT);
            // Host
            case "host":
                return noNullStr(Build.HOST);

            // MAC
            case "mac":
                return noNullStr(getMacAddress(mContext));

            // IMEI
            case "imei":
                String imeiDate = noNullStr(getImei());
                return imeiDate;

            // IMSI
            case "imsi":
                TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                String imsiData = noNullStr(telManager.getSubscriberId());
                return imsiData;

            // User
            case "user":
                return noNullStr(Build.USER);

            // CPU_ABI
            case "cpuAbi":
                return noNullStr(Build.CPU_ABI);

            //CPU个数
            case "cpuCount":
                return getNumCores();

            //总内存
            case "totalMemory":
                return getTotalMemorySize(mContext);

            //基带版本
            case "baseBandVersion":
                return noNullStr(getBasebandVer());

            //内核版本
            case "version":
                return noNullStr(getLinuxCoreVer());

            //内部版本
            case "buildVersion":
                return noNullStr(Build.DISPLAY);

            //安卓版本
            case "androidVersion":
                return Build.VERSION.SDK_INT;

            // 联网状态
            case "providerName":
                return getNetworkType();

            // ip地址
            case "ipAddress":
                String ipDate = noNullStr(getPhoneIp(mContext));
                return ipDate;


            //地理位置
            case "location":
                startLocation();
                return noNullStr(loc);

            //WIFI信息
            case "wifiInfo":
                return noNullStr(getWifiInfo(mContext));

            default:
                break;
        }
        return "";
    }


    /**
     * 检测当前系统是否Root
     *
     * @return true 已Root; false 未Root
     */
    public  boolean isRootSystem() throws SotpException {
        if (isExecuted("/system/xbin/su")) {
            return true;
        }
        if (isExecuted("/system/bin/su")) {
            return true;
        }
        if (isExecuted("/system/sbin/su")) {
            return true;
        }
        if (isExecuted("/sbin/su")) {
            return true;
        }
        if (isExecuted("/vendor/bin/su")) {
            return true;
        }
        if (checkRootMethod1()) {
            return true;
        }
        if (checkRootMethod2()) {
            return true;
        }
        return false;
    }

    /**
     * 检测root目录
     * @return root情况
     */
    public JSONArray isRooted() throws SotpException {
        JSONArray rootArray = new JSONArray();
        if (isExecuted("/system/xbin/su")) {
            rootArray.put("\\system\\xbin\\su");
        }
        if (isExecuted("/system/bin/su")) {
            rootArray.put("\\system\\bin\\su");
        }
        if (isExecuted("/system/sbin/su")) {
            rootArray.put("\\system\\sbin\\su");

        }
        if (isExecuted("/sbin/su")) {
            rootArray.put("\\sbin\\su");
        }
        if (isExecuted("/vendor/bin/su")) {
            rootArray.put("\\vendor\\bin\\su");
        }
        if (checkRootMethod1()) {
            rootArray.put("test-keys");
        }
        if (checkRootMethod2()) {
            rootArray.put("\\system\\app\\Superuser.apk");
        }

        return rootArray;
    }

    private String noNullStr(String s) {
        return (TextUtils.isEmpty(s) ? "" : s);
    }

    /**
     * 获取位置
     */
    public void startLocation() {
        manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // 返回当前最好的定位提供者
        provider = manager.getBestProvider(getCriteria(), true);
        if (provider != null && manager.isProviderEnabled(provider)) { // 判断当前的provider是否可用
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // 获取上次位置保存信息 并以当前的提供者进行修正
            getLastLoc(provider);


//            manager.requestLocationUpdates(provider, 1000, 0, locationListener);

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    manager.requestLocationUpdates(provider, 1000, 0, locationListener);
                }
            };

            new Thread() {
                public void run() {
                    Looper.prepare();
                    new Handler().post(runnable);
                    Looper.loop();
                }
            }.start();

        } else { // 最好的定位者为空 判断当前 GPS是否可用
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // 获取位置上次保存信息 并以当前的提供者进行修正
                getLastLoc(LocationManager.GPS_PROVIDER);
                // 绑定监听
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

            }
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
            getLastLoc(provider);
        }

        public void onProviderEnabled(String provider) {
            getLastLoc(provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:

                case LocationProvider.TEMPORARILY_UNAVAILABLE:

                    break;
            }
        }
    };


    private void getLastLoc(String provider) {
        Location location = getLocation(provider);
        if (location != null) {
            updateWithNewLocation(location);
        }
    }

    /**
     * 更新的位置信息
     *
     * @param location 位置
     */
    private void updateWithNewLocation(Location location) {

        loc = "Latitude:" + location.getLatitude() + ",Longitude:" + location.getLongitude();

    }

    /**
     * 获取location
     *
     * @param provider 定位提供者
     * @return location
     */
    private Location getLocation(final String provider) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return manager.getLastKnownLocation(provider);
    }

    /**
     * 返回查询条件
     *
     * @return 当前的定位方式
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        //criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    //获取屏幕宽高
    public String getScreen() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(dm);
        int screen_w = dm.widthPixels;
        int screen_h = dm.heightPixels;
        return String.valueOf(screen_w + "X" + screen_h);
    }

    public String getWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = (null == wifiManager ? null : wifiManager.getConnectionInfo());
        return wifiInfo.toString();


    }

    /**
     * BASEBAND-VER 基带版本 return String
     */
    public String getBasebandVer() throws SotpException {
        String Version = "";
        Class cl = null;
        try {
            cl = Class.forName("android.os.SystemProperties");
            Object invoker = cl.newInstance();
            Method m = cl.getMethod("get", new Class[]{String.class,
                    String.class});
            Object result = m.invoke(invoker, new Object[]{
                    "gsm.version.baseband", "no message"});
            Version = (String) result;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.CLASS_NOT_FOUND_EXCEPTION);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.METHOD_NOT_FOUND_EXCEPTION);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.INSTANTIATION_EXCEPTION);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.ILLEGAL_ACCESS_EXCEPTION);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.INVOCATION_TARGET_EXCEPTION);
        }
        return Version;
    }


    /**
     * 获取设备序列号
     *
     * @return
     */

    public static String getSerialNumber() throws SotpException {
        String serial = "";
        Class<?> c = null;
        try {
            c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.CLASS_NOT_FOUND_EXCEPTION);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.METHOD_NOT_FOUND_EXCEPTION);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.ILLEGAL_ACCESS_EXCEPTION);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.INVOCATION_TARGET_EXCEPTION);
        }

        return serial;
    }

    /**
     * CORE-VER 内核版本 return String
     */
    public static String getLinuxCoreVer() throws SotpException {
        Process process = null;
        String kernelVersion = "";
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream outs = process.getInputStream();
        InputStreamReader isrout = new InputStreamReader(outs);
        BufferedReader brout = new BufferedReader(isrout, 8 * 1024);

        String result = "";
        String line;
        try {
            while ((line = brout.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.READ_FILE_FAILED);
        }
        try {
            if (result != "") {
                String Keyword = "version ";
                int index = result.indexOf(Keyword);
                line = result.substring(index + Keyword.length());
                index = line.indexOf(" ");
                kernelVersion = line.substring(0, index);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.INDEX_OUT_OF_BOUNDS_EXCEPTION);
        }
        return kernelVersion;
    }

    /**
     * @return CPU个数
     */
    private int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        File dir = new File("/sys/devices/system/cpu/");
        File[] files = dir.listFiles(new CpuFilter());
        return files.length;
    }

    /**
     * 获取运行总内存
     *
     * @param context 可传入应用程序上下文。
     * @return 总内存大单位为GB。
     */
    public String getTotalMemorySize(Context context) throws SotpException {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            if (!TextUtils.isEmpty(subMemoryLine)) {
                totalMemory = Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.READ_FILE_FAILED);
        }
        return Formatter.formatFileSize(context, totalMemory);
    }

    /**
     * 获取当前的网络类型
     *
     * @return
     */
    private String getNetworkType() {

        // 获取网络的状态信息，有下面三种方式
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            int type = networkInfo.getType();
            return getNetworkClassByType(type);
        } else if (networkInfo != null && !networkInfo.isConnected()) {
            return "No WiFi Or Cellular";
        } else {
            return "No WiFi Or Cellular";
        }
    }

    /**
     * 获取当前的网络类型
     *
     * @param networkType
     * @return
     */
    private String getNetworkClassByType(int networkType) {

        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return getMobileNetworkType();
            case ConnectivityManager.TYPE_WIFI:
                return "WiFi";
            case ConnectivityManager.TYPE_BLUETOOTH:
                return "BLUETOOTH";
            case ConnectivityManager.TYPE_ETHERNET:
                return "ETHERNET";

            default:
                return "No WiFi Or Cellular";
        }
    }

    /**
     * 获取当前移动网络的类型
     */
    private String getMobileNetworkType() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "No WiFi Or Cellular";
        }
    }


    @SuppressLint("NewApi")
    public String getImei() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imeiSIM = telephonyManager.getDeviceId();
            String imeiSIM1 = telephonyManager.getDeviceId(0);
            String imeiSIM2 = telephonyManager.getDeviceId(1);
            StringBuffer sb = new StringBuffer();
            HashSet<String> set = new HashSet<String>();

            if (imeiSIM != null && !imeiSIM.isEmpty()) {
                set.add(imeiSIM);
            }
            if (imeiSIM1 != null && !imeiSIM1.isEmpty()) {
                set.add(imeiSIM1);
            }

            if (imeiSIM2 != null && !imeiSIM2.isEmpty()) {
                set.add(imeiSIM2);
            }

            Iterator i = set.iterator();
            while (i.hasNext()) {
                String temp = (String) i.next();
                sb.append(temp).append("&");
            }

            if (sb != null && !sb.equals("")) {
                return sb.substring(0, sb.length() - 1);
            }
        } else {
            String imei = telephonyManager.getDeviceId();
            if (imei != null)
                return imei;
        }
        return "";
    }


    public static String getPhoneIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        String ipStr = (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);// 手机ip地址

        return ipStr;
    }

    /**
     * 获取设备MAC地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>}</p>
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET"/>}</p>
     *
     * @return MAC地址
     */
    public String getMacAddress(Context context) throws SotpException {
        String macAddress = getMacAddressByWifiInfo(context);
        if (!"02:00:00:00:00:00".equals(macAddress)) {
            return macAddress;
        }
        macAddress = getMacAddressByNetworkInterface();
        if (!"02:00:00:00:00:00".equals(macAddress)) {
            return macAddress;
        }
        macAddress = getMacAddressByFile();
        if (!"02:00:00:00:00:00".equals(macAddress)) {
            return macAddress;
        }
        macAddress = getMacReadFile();
        if (!"02:00:00:00:00:00".equals(macAddress)) {
            return macAddress;
        }
        return "";
    }

    /**
     * 获取设备MAC地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>}</p>
     *
     * @return MAC地址
     */
    private static String getMacAddressByWifiInfo(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiInfo info = wifi.getConnectionInfo();
            if (info != null) return info.getMacAddress();
        }
        return "02:00:00:00:00:00";
    }

    /**
     * 获取设备MAC地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET"/>}</p>
     *
     * @return MAC地址
     */
    private static String getMacAddressByNetworkInterface() throws SotpException {
        List<NetworkInterface> nis = null;
        try {
            nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : nis) {
                if (!ni.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = ni.getHardwareAddress();
                if (macBytes != null && macBytes.length > 0) {
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02x:", b));
                    }
                    return res1.deleteCharAt(res1.length() - 1).toString();
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.SOCKET_EXCEPTION);

        }

        return "02:00:00:00:00:00";
    }

    /**
     * 获取设备MAC地址
     *
     * @return MAC地址
     */
    private String getMacAddressByFile() throws SotpException {
        CommandResult result = execCmd("getprop wifi.interface", false);
        if (result.result == 0) {
            String name = result.successMsg;
            if (name != null) {
                result = execCmd("cat /sys/class/net/" + name + "/address", false);
                if (result.result == 0) {
                    if (result.successMsg != null) {
                        return result.successMsg;
                    }
                }
            }
        }
        return "02:00:00:00:00:00";
    }

    /**
     * @return
     */
    public String getMacReadFile() throws SotpException {
        CommandResult result = execCmd("getprop wifi.interface", false);
        if (result.result == 0) {
            String name = result.successMsg;
            File file = new File("/sys/class/net/" + name + "/address");
            if (file.exists()) {
                try {
                    return loadFileAsString(file).toUpperCase().substring(0, 17);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }


    private boolean checkXp() {
        List<PluginConstants.AppInfo> appInfos = getAppInfos(mContext);
        for (PluginConstants.AppInfo appInfo : appInfos) {
            if (appInfo.packageName.equals("de.robv.android.xposed.installer")) {
                return true;
            }
        }

        return false;
    }


    private boolean checkRootMethod1() {
        String buildTags = Build.TAGS;
        return (buildTags != null) && (buildTags.contains("test-keys"));
    }

    private boolean checkRootMethod2() {
        File file = new File("/system/app/Superuser.apk");
        return file.exists();
    }

    private boolean isExecuted(String path) throws SotpException {
        boolean ret = false;
        try {
            if (new File(path).exists()) {
                Process exec = Runtime.getRuntime()
                        .exec("ls -l " + path + "\n");

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        exec.getInputStream()));
                String str = null;
                while ((str = br.readLine()) != null) {
                    if (str.length() > 10) {
                        String key = (String) str.subSequence(9, 10);
                        if ((key.equals("x")) || (key.equals("s"))) {
                            ret = true;
                        }
                    }
                }
                int err = exec.waitFor();
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.READ_FILE_FAILED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new SotpException(SotpException.INTERRUPTED_EXCEPTION);
        }
        return ret;
    }

    //xpose检测
    public List<PluginConstants.AppInfo> getAppInfos(Context context) {
        PackageManager pm = context.getPackageManager();

        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        Log.i("TAG", packageInfos.toString());
        List<PluginConstants.AppInfo> appInfos = new ArrayList<PluginConstants.AppInfo>();
        for (PackageInfo packageInfo : packageInfos) {
            PluginConstants.AppInfo appInfo = new PluginConstants.AppInfo();
            appInfo.packageName = packageInfo.packageName;
            appInfo.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
            appInfo.apkPath = packageInfo.applicationInfo.sourceDir;
            appInfos.add(appInfo);
        }
        return appInfos;
    }


    /**
     * 是否是在root下执行命令
     *
     * @param command 命令
     * @param isRoot  是否需要root权限执行
     * @return CommandResult
     */
    private CommandResult execCmd(String command, boolean isRoot) throws SotpException {
        return execCmd(new String[]{command}, isRoot, true);
    }

    /**
     * 是否是在root下执行命令
     *
     * @param commands        命令数组
     * @param isRoot          是否需要root权限执行
     * @param isNeedResultMsg 是否需要结果消息
     * @return CommandResult
     */
    private CommandResult execCmd(String[] commands, boolean isRoot, boolean isNeedResultMsg) throws SotpException {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) continue;
                os.write(command.getBytes());
                os.writeBytes("\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();
            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SotpException(SotpException.READ_FILE_FAILED);
        } finally {
            closeIO(os, successResult, errorResult);
            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(
                result,
                successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString()
        );
    }

    /**
     * 返回的命令结果
     */
    public static class CommandResult {
        /**
         * 结果码
         **/
        public int result;
        /**
         * 成功信息
         **/
        public String successMsg;
        /**
         * 错误信息
         **/
        public String errorMsg;

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }


    public String loadFileAsString(File fileName) throws Exception {
        return streamToStr(new FileInputStream(fileName));
    }

    public String streamToStr(InputStream is) throws IOException {
        return readInputStream(is).toString();
    }


    private ByteArrayOutputStream readInputStream(final InputStream ImStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = ImStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        closeIO(outStream, ImStream);
        return outStream;
    }

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    private void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /*
* getPolicyFingerprint input: policy (int) output: 1. String, authentication
* factors separated by '|'
* eg:imei|equipmentModel|device|serialNumber
* 2. null,if policy is invalid.
*/
    private static String getPolicyFingerprint(int policy) {

        // judge policy,if bit count gt 20, return null.
        if (policy > 0x7fffffff) {
            return null;
        }
        int iPolicy = policy & 0xffffffff;
        List<String> policyContentList = new ArrayList<String>();
        int cursor = 1;
        int compareCount = 1;
        while (compareCount <= 32) {
            if ((iPolicy & cursor) != 0) {
                policyContentList.add(policyFingerprint[compareCount - 1]);
            }
            cursor <<= 1;
            compareCount += 1;

        }
        if (policyContentList.isEmpty())
            return "";

        StringBuilder response = new StringBuilder();
        for (String p : policyContentList) {
            response.append(p);
            response.append("|");
        }

        return response.substring(0, response.length() - 1).toString();
    }


}
