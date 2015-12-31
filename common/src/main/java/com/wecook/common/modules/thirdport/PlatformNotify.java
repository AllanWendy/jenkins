package com.wecook.common.modules.thirdport;

import android.content.Context;
import android.widget.Toast;

/**
 * 通知
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/3/14
 */
public class PlatformNotify {

    public static void showShortToast(Context context, int msgId) {
        Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
    }
}
