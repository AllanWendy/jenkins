package com.wecook.common.modules.messager;

import android.content.Context;

import com.wecook.common.app.AppLink;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.property.PhoneProperties;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import com.zhuge.analysis.stat.ZhugeSDK;

import java.util.List;
import java.util.Map;

/**
 * 小米推送消息回调
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/3/14
 */
public class XMPushMessageReceiver extends PushMessageReceiver {
    @Override
    public void onReceiveMessage(Context context, MiPushMessage miPushMessage) {
        Logger.i("xm-push", "[onReceiveMessage] " + miPushMessage.toString());

        Map<String, String> extra = miPushMessage.getExtra();
        if (extra != null) {
            boolean showNotify = (miPushMessage.getPassThrough() == 0);
            AppLink.pendingLink(miPushMessage.getMessageId(), showNotify, extra);

            if (BaseApp.getApplication() != null) {
                Logger.i("xm-push", "app is running !! onLaunchApp..");
                BaseApp.getApplication().getAppLink().onLaunchApp(BaseApp.getApplication());
            }
        }
        ZhugeSDK.getInstance().onMsgReaded(ZhugeSDK.PushChannel.XIAOMI,extra);

    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        Logger.i("xm-push", "[onCommandResult] " + miPushCommandMessage.toString());
        String command = miPushCommandMessage.getCommand();
        List<String> arguments = miPushCommandMessage.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {
                PhoneProperties.setXMPushRegisterId(cmdArg1);
                try {
                    ZhugeSDK.getInstance().setThirdPartyPushUserId(ZhugeSDK.PushChannel.XIAOMI, cmdArg1);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
        }
        String reason = miPushCommandMessage.getReason();
        Logger.i("xm-push", "reason:" + reason + " cmdArg1:" + cmdArg1 + " cmdArg2:" + cmdArg2);
    }
}
