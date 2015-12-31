package com.wecook.common.modules.messager;

import android.content.Context;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.AndroidUtils;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * 小米推送
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/3/14
 */
public class XMPushMessager extends BaseModule {

    private static final String APP_ID = "2882303761517186257";
    private static final String APP_KEY = "5571718697257";
    private static final String APP_SECRET = "U/WHreKB11+0u5QSCi49yg==";
    private static boolean sIsRegistered;

    @Override
    public void setup(final Context context) {
        super.setup(context);
        if (AndroidUtils.isMainProcess(context)) {
            Logger.i("xm-push", "init...");

            register(context);
        }

        LoggerInterface log = new LoggerInterface() {
            @Override
            public void setTag(String s) {

            }

            @Override
            public void log(String s) {
                Logger.d("xm-push", s);
            }

            @Override
            public void log(String s, Throwable throwable) {
                Logger.d("xm-push", s, throwable);
            }
        };
        com.xiaomi.mipush.sdk.Logger.setLogger(context, log);

        subscribe(context, "all");
        subscribe(context, PhoneProperties.getVersionName());
        subscribe(context, PhoneProperties.getDeviceId());

    }

    public static void register(Context context) {
        MiPushClient.registerPush(context, APP_ID, APP_KEY);
        sIsRegistered = true;
    }

    public static void unregister(Context context) {
        MiPushClient.unregisterPush(context);
        sIsRegistered = false;
    }

    public static boolean isRegister() {
        return sIsRegistered;
    }

    /**
     * 上报消息点击率
     *
     * @param context
     * @param msgId
     */
    public static void reportMessageClicked(Context context, String msgId) {
        MiPushClient.reportMessageClicked(context, msgId);
    }

    /**
     * 清理通知栏
     *
     * @param context
     */
    public static void clearNotification(Context context) {
        MiPushClient.clearNotification(context);
    }

    /**
     * 暂停推送
     *
     * @param context
     */
    public static void pausePush(Context context) {
        Logger.i("xm-push", "pausePush");
        MiPushClient.pausePush(context, null);
    }

    /**
     * 恢复推送
     *
     * @param context
     */
    public static void resumePush(Context context) {
        Logger.i("xm-push", "resumePush");
        MiPushClient.resumePush(context, null);
    }

    /**
     * 设置别名
     *
     * @param context
     * @param alias
     */
    public static void setAlias(Context context, String alias) {
        MiPushClient.setAlias(context, alias, null);
    }

    /**
     * 设置标签
     *
     * @param context
     * @param topic
     */
    public static void subscribe(Context context, String topic) {
        MiPushClient.subscribe(context, topic, null);
    }

    /**
     * 取消设置标签
     *
     * @param context
     * @param topic
     */
    public static void unsubscribe(Context context, String topic) {
        MiPushClient.unsubscribe(context, topic, null);
    }

    /**
     * 设置接受时间
     *
     * @param context
     * @param startHour
     * @param startMin
     * @param endHour
     * @param endMin
     */
    public static void setAcceptTime(Context context, int startHour, int startMin, int endHour, int endMin) {
        MiPushClient.setAcceptTime(context, startHour, startMin, endHour, endMin, null);
    }


}
