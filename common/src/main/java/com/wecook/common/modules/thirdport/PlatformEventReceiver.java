package com.wecook.common.modules.thirdport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 平台组件广播监听
 *
 * @author zhoulu
 * @date 13-12-11
 */
public class PlatformEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PlatformManager.getInstance().onHandleNewIntent(context, intent)) {
            return;
        }
    }
}
