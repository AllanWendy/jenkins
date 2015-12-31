package com.wecook.common.modules.property;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.filemaster.FileMaster;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.SecurityUtils;
import com.wecook.common.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

/**
 * 手机属性
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public class PhoneProperties extends BaseModule {

    private static final String DEFAULT_APK_SIGN = "d89932ab3a1fd8c1f523f90b4707c2ce";

    public static final String PACKAGE_NAME = "p_package_name";
    public static final String DEVICE_ID = "p_device_id";
    public static final String DEVICE_OS = "p_device_os";
    public static final String DEVICE_OS_VERSION = "p_device_os_version";
    public static final String IMEI = "p_imei";
    public static final String ANDROID_ID = "p_android_id";
    public static final String MAC_ADDRESS = "p_mac_address";
    public static final String IP_ADDRESS = "p_ip_address";
    public static final String APK_SIGNATURES = "p_signatures";
    public static final String SCREEN_WIDTH = "p_screen_width";
    public static final String SCREEN_HEIGHT = "p_screen_height";
    public static final String VERSION_NAME = "p_version_name";
    public static final String DEBUG_VERSION_NAME = "p_debug_version_name";
    public static final String VERSION_CODE = "p_version_code";
    public static final String CHANNEL = "p_channel";
    public static final String LOCATION_LAT = "p_location_lat";
    public static final String LOCATION_LON = "p_location_lon";
    public static final String LOCATION_CITY = "p_location_city";
    public static final String PUSH_ID = "p_push_id";

    static Properties sProperties;

    @Override
    public void setup(final Context context) {
        if (sProperties == null) {
            sProperties = new Properties();
            setupPackageName(context);
            setupDeviceUUID(context);
            //StrictMode
            AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
                @Override
                public void run() {
                    setupWifiMacAndIP(context);
                    setupChannel(context);
                }
            });

            setupApkSignatures(context);
            setupScreenSize(context);
            setupVersion(context);
        }
    }

    private void setupChannel(Context context) {
        //set channel
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null
                    && applicationInfo.metaData != null
                    && applicationInfo.metaData.containsKey("UMENG_CHANNEL")) {
                //最新app渠道
                String channel = String.valueOf(applicationInfo.metaData.get("UMENG_CHANNEL"));

                //本地渠道号
                String localChannel = FileUtils.readFileString(FileMaster.getInstance().getLongFileDir(), CHANNEL);

                if (StringUtils.isEmpty(localChannel)) {
                    //清除之前版本留下的update渠道
                    localChannel = (String) SharePreferenceProperties.get(CHANNEL, "empty");
                    if ("update".equals(localChannel)) {
                        localChannel = "empty";
                    }
                }

                if (StringUtils.isEmpty(channel)) {
                    channel = "error";
                }

                //渠道号为update的特殊渠道号，是为了替换更新使用
                if ("update".equals(channel)) {
                    //仍然使用本地的渠道号
                    sProperties.setProperty(CHANNEL, localChannel);
                } else {
                    if (!channel.equals(localChannel)) {
                        FileUtils.newFile(channel.getBytes(), FileMaster.getInstance().getLongFileDir(), CHANNEL);
                    }
                    sProperties.setProperty(CHANNEL, channel);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            sProperties.setProperty(VERSION_CODE, packageInfo.versionCode + "");
            sProperties.setProperty(VERSION_NAME, packageInfo.versionName + "");
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null
                    && applicationInfo.metaData != null
                    && applicationInfo.metaData.containsKey("DEBUG_VERSION")) {
                Object debugVersion = applicationInfo.metaData.get("DEBUG_VERSION");
                sProperties.setProperty(DEBUG_VERSION_NAME, String.valueOf(debugVersion));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void setupScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        sProperties.setProperty(SCREEN_WIDTH, width + "");
        sProperties.setProperty(SCREEN_HEIGHT, height + "");
    }

    private void setupApkSignatures(Context context) {
        //Set Apk Signatures
        String accessKey = DEFAULT_APK_SIGN;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = packageInfo.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i]);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            accessKey = SecurityUtils.encodeByMD5((hexString.toString()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sProperties.put(APK_SIGNATURES, accessKey);
    }

    private void setupWifiMacAndIP(Context context) {
        //Set Mac Address
        try {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            sProperties.put(MAC_ADDRESS, info.getMacAddress());

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddress = networkInterface.getInetAddresses(); enumIpAddress.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        sProperties.put(IP_ADDRESS, inetAddress.getHostAddress().toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDeviceUUID(Context context) {
        //Set device uuid , imei and android_id
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String deviceId = StringUtils.getStringWithoutNull(tm.getDeviceId());
            final String simSerialNumber = StringUtils.getStringWithoutNull(tm.getSimSerialNumber());
            final String androidId = StringUtils.getStringWithoutNull(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            sProperties.put(IMEI, deviceId);
            sProperties.put(ANDROID_ID, androidId);
            UUID deviceUUID = new UUID(androidId.hashCode(), ((long) deviceId.hashCode() << 32) | simSerialNumber.hashCode());
            sProperties.put(DEVICE_ID, SecurityUtils.encodeByMD5(deviceUUID.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        sProperties.put(DEVICE_OS, "android");
        sProperties.put(DEVICE_OS_VERSION, Build.VERSION.RELEASE);
    }

    private void setupPackageName(Context context) {
        //Set package name
        sProperties.put(PACKAGE_NAME, context.getPackageName());
    }

    /**
     * 获得版本名
     *
     * @return
     */
    public static String getVersionName() {
        return sProperties.getProperty(VERSION_NAME);
    }

    /**
     * 获得内部版本名
     *
     * @return
     */
    public static String getDebugVersionName() {
        return sProperties.getProperty(DEBUG_VERSION_NAME);
    }

    /**
     * 获得版本名
     *
     * @return
     */
    public static String getVersionCode() {
        return sProperties.getProperty(VERSION_CODE);
    }

    /**
     * 获得包名
     *
     * @return
     */
    public static String getPackageName() {
        return sProperties.getProperty(PACKAGE_NAME);
    }

    /**
     * 获得设备ID
     *
     * @return
     */
    public static String getDeviceId() {
        return sProperties.getProperty(DEVICE_ID);
    }

    /**
     * 获得设备系统信息
     *
     * @return
     */
    public static String getDeviceOs() {
        return sProperties.getProperty(DEVICE_OS);
    }

    /**
     * 获得设备系统版本
     *
     * @return
     */
    public static String getDeviceOsVersion() {
        return sProperties.getProperty(DEVICE_OS_VERSION);
    }

    /**
     * 获得设备AndroidID
     *
     * @return
     */
    public static String getAndroidId() {
        return sProperties.getProperty(ANDROID_ID);
    }

    /**
     * 获得Mac地址
     *
     * @return
     */
    public static String getMacAddress() {
        return sProperties.getProperty(MAC_ADDRESS);
    }

    /**
     * 获得IMEI
     *
     * @return
     */
    public static String getImei() {
        return sProperties.getProperty(IMEI);
    }

    /**
     * 获得APK签名
     *
     * @return
     */
    public static String getApkSignatures() {
        return sProperties.getProperty(APK_SIGNATURES);
    }

    /**
     * 获得渠道号
     *
     * @return
     */
    public static String getChannel() {
        return sProperties.getProperty(CHANNEL);
    }

    public static String getScreenWidth() {
        return sProperties.getProperty(SCREEN_WIDTH);
    }

    public static int getScreenWidthInt() {
        try {
            return StringUtils.parseInt(sProperties.getProperty(SCREEN_WIDTH));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static String getScreenHeight() {
        return sProperties.getProperty(SCREEN_HEIGHT);
    }

    public static int getScreenHeightInt() {
        try {
            return StringUtils.parseInt(sProperties.getProperty(SCREEN_HEIGHT));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void setLat(double lat) {
        SharePreferenceProperties.set(LOCATION_LAT, "" + lat);
    }

    public static double getLat() {
        return Double.parseDouble((String) SharePreferenceProperties.get(LOCATION_LAT, "0.0"));
    }

    public static void setLon(double lon) {
        SharePreferenceProperties.set(LOCATION_LON, "" + lon);
    }

    public static double getLon() {
        return Double.parseDouble((String) SharePreferenceProperties.get(LOCATION_LON, "0.0"));
    }

    public static void setXMPushRegisterId(String regId) {
        try {
            if (!StringUtils.isEmpty(regId)) {
                String encodePushId = URLEncoder.encode(regId, "utf-8");
                SharePreferenceProperties.set(PUSH_ID, encodePushId);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String getXMPushRegisterId() {
        return (String) SharePreferenceProperties.get(PUSH_ID, "");
    }

    public static String toJson() {

        OutJson outJson = new OutJson();

        if (sProperties != null) {
            outJson.status = 1;
            outJson.info = "success";
            Result result = new Result();
            outJson.result = result;
            result.app = "wecook";
            result.channel = getChannel();
            result.version = getVersionName();
            result.wid = getDeviceId();
        } else {
            outJson.status = 0;
            outJson.info = "module init error";
        }

        Gson gson = new Gson();
        return gson.toJson(outJson);
    }

    private static class OutJson {
        @SerializedName("status")
        private int status;

        @SerializedName("info")
        private String info;

        @SerializedName("result")
        private Result result;

    }

    private static class Result {
        @SerializedName("app")
        private String app;

        @SerializedName("channel")
        private String channel;

        @SerializedName("version")
        private String version;

        @SerializedName("wid")
        private String wid;
    }
}
