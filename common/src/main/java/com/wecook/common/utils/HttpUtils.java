package com.wecook.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import org.apache.http.HttpHost;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Http辅助类
 *
 * @author kevin created at 9/23/14
 * @version 1.0
 */
public class HttpUtils {

    /**
     * TODO 获得代理Host
     * @param context
     * @return
     */
    public static HttpHost getProxy(Context context) {
        return null;
    }

    public static String getUrlEncodeString(String text) {
        String encodeString = "";
        try {
            encodeString = URLEncoder.encode(text, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeString;
    }

    @SuppressLint("DefaultLocale")
    public static boolean isValidHttpUri(Uri uri) {
        String uriString = uri.toString();
        return isValidHttpUri(uriString);
    }

    public static boolean isValidHttpUri(String uriString) {
        if (StringUtils.isEmpty(uriString)) {
            return false;
        }
        String loweCaseString = uriString.toLowerCase();
        if (loweCaseString.startsWith("http") || loweCaseString.startsWith("https")) {
            return true;
        }

        return false;
    }

    public static boolean isContentUri(String uriString) {
        if (StringUtils.isEmpty(uriString)) {
            return false;
        }
        String loweCaseString = uriString.toLowerCase();
        if (loweCaseString.startsWith("content")) {
            return true;
        }
        return false;
    }

    public static boolean isContentUri(Uri uri) {
        String uriString = uri.toString();
        return isContentUri(uriString);
    }

    public static boolean isFileUri(String uriString) {
        if (StringUtils.isEmpty(uriString)) {
            return false;
        }
        String loweCaseString = uriString.toLowerCase();
        if (loweCaseString.startsWith("file")) {
            return true;
        }
        return false;
    }

    public static boolean isFileUri(Uri uri) {
        String uriString = uri.toString();
        return isFileUri(uriString);
    }

    public static String getPath(String path) {
        if (isContentUri(path) || isFileUri(path)) {
            Uri uri = Uri.parse(path);
            return uri.getPath();
        }
        return path;
    }

    public static boolean isValidSdcardUri(String uri) {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (uri.startsWith(dir)) {
            return true;
        }
        return false;
    }

    public static Map<String, String> getParams(String url) {
        Map<String, String> params = new HashMap<String, String>();
        try {

            URL resultUrl = new URL(url);
            String query = resultUrl.getQuery();
            String[] paramList = query.split("&");

            for (String param : paramList) {
                String[] keyvalue = param.split("=");
                params.put(keyvalue[0], keyvalue[1]);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return params;
    }
}
