package com.wecook.common.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * App链接
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/18/14
 */
public abstract class AppLink {

    public final static int SEARCH = 0xff01;
    public final static String SCHEME_HTTP = "http";
    public final static String SCHEME_APP = "wecook";
    protected static List<LinkMessage> sMessageList = new ArrayList<LinkMessage>();
    protected static SparseArray<LinkCallback> sCallbackMap = new SparseArray<LinkCallback>();
    private static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static int sLinkCode;

    static {
        sMatcher.addURI("wecook", "search/result", SEARCH);
    }

    /**
     * 注册递送功能
     *
     * @param authority
     * @param path
     * @param callback
     */
    public static void registerLink(String authority, String path, LinkCallback callback) {
        sCallbackMap.put(sLinkCode, callback);
        sMatcher.addURI(authority, path, sLinkCode++);
    }

    /**
     * 注册递送功能
     *
     * @param authorities
     * @param path
     * @param callback
     */
    public static void registerLink(List<String> authorities, String path, LinkCallback callback) {
        sCallbackMap.put(sLinkCode, callback);
        for (String authority : authorities) {
            sMatcher.addURI(authority, path, sLinkCode);
        }
        sLinkCode++;
    }

    /**
     * 添加递送消息
     *
     * @param messageId
     * @param showNotify
     * @param extra
     */
    public static void pendingLink(String messageId, boolean showNotify, Map<String, String> extra) {
        LinkMessage message = new LinkMessage();
        message.messageId = messageId;
        message.showNotify = showNotify;
        message.extra = extra;
        sMessageList.add(0, message);
    }

    /**
     * 添加递送消息
     *
     * @param url
     */
    public static void pendingLink(String url) {
        LinkMessage message = new LinkMessage();
        message.extra = new HashMap<>();
        message.extra.put("url", url);
        sMessageList.add(0, message);
    }

    /**
     * 递送Url
     *
     * @param uriString
     * @return
     */
    public static int sendLink(String uriString) {
        Uri uri = null;
        try {
            uri = Uri.parse(uriString);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return sendLink(uri);
    }

    /**
     * 递送Uri
     *
     * @param uri
     * @return
     */
    public static int sendLink(final Uri uri) {
        if (uri != null && !uri.isRelative()) {
            Logger.d("sendLink", "uri = " + uri.toString());
            final int deliverCode = sMatcher.match(uri);
            Logger.d("sendLink", "sendLink code = " + deliverCode);
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    LinkCallback callback = sCallbackMap.get(deliverCode);
                    if (callback != null) {
                        callback.doLink(uri, getParams(uri));
                    }
                }
            });

            return deliverCode;
        }
        return UriMatcher.NO_MATCH;
    }

    /**
     * 递送Uri
     *
     * @param message
     * @return
     */
    public static int sendLink(LinkMessage message) {
        if (message != null && message.extra != null) {

            String uri = message.extra.get("url");
            String wkMsgId = message.extra.get("id");
            if (!StringUtils.isEmpty(uri)) {
                if (!StringUtils.isEmpty(wkMsgId)) {
                    uri += "&msgId=" + wkMsgId;
                }

                Logger.d("sendLink", "uri = " + uri);
                return sendLink(uri);
            }
        }
        return UriMatcher.NO_MATCH;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static Bundle getParams(Uri uri) {
        Bundle bundle = new Bundle();
        if (uri != null) {
            for (String paramName : uri.getQueryParameterNames()) {
                String paramValue = uri.getQueryParameter(paramName);
                bundle.putString(paramName, paramValue);
            }
        }
        return bundle;
    }

    public static boolean isAppUri(String uriString) {
        if (!StringUtils.isEmpty(uriString)) {
            Uri uri = Uri.parse(uriString);
            return isAppUri(uri);
        }
        return false;
    }

    public static boolean isAppUri(Uri uri) {
        if (uri != null && uri.isAbsolute()) {
            String scheme = uri.getScheme();
            return SCHEME_APP.equals(scheme);
        }
        return false;
    }

    public static boolean isHttpUri(String uriString) {
        if (!StringUtils.isEmpty(uriString)) {
            Uri uri = Uri.parse(uriString);
            return isHttpUri(uri);
        }
        return false;
    }

    public static boolean isHttpUri(Uri uri) {
        if (uri != null && uri.isAbsolute()) {
            String scheme = uri.getScheme();
            return SCHEME_HTTP.equals(scheme);
        }
        return false;
    }

    /**
     * 更新快捷递送
     *
     * @param context
     */
    public abstract void onUpdateLink(Context context);

    /**
     * 启动软件
     *
     * @param context
     */
    public abstract void onLaunchApp(Context context);

    public static interface LinkCallback {

        /**
         * 快捷递送
         *
         * @param uri
         * @param params
         */
        public void doLink(Uri uri, Bundle params);
    }

    public static class LinkMessage {
        public String messageId;
        public boolean showNotify;
        public Map<String, String> extra;
    }
}
