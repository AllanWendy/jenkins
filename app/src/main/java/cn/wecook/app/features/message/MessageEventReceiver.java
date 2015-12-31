package cn.wecook.app.features.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.wecook.common.core.debug.Logger;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.model.CookShow;

/**
 * 消息事件接收
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/19/15
 */
public class MessageEventReceiver extends BroadcastReceiver {

    public static final String PERMISSION_EVENT = "cn.wecook.app.permission_EVENT_RECEIVER";
    public static final String ACTION_COOK_SHOW = "cn.wecook.app.action_COOK_SHOW";
    public static final String ACTION_PICK_ONE_PIC = "cn.wecook.app.action_PICK_ONE_PIC";
    public static final String ACTION_PICK_MULTI_PIC = "cn.wecook.app.action_PICK_MULTI_PIC";
    public static final String PARAM_ONE_BITMAP = "param_one_bitmap";
    public static final String PARAM_LIST_BITMAP = "param_list_bitmap";

    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.i("message", "MessageEventReceiver .. onReceive.");

        String action = intent.getAction();
        Logger.i("message", "MessageEventReceiver .. action:" + action);
        if (ACTION_COOK_SHOW.equals(action)) {
            Intent data = new Intent(CookShowApi.ACTION_COOK_SHOW);
            Bundle bundle = new Bundle();
            CookShow cookShow = (CookShow) intent.getSerializableExtra(CookShowApi.PARAM_COOK_SHOW);
            bundle.putSerializable(CookShowApi.PARAM_COOK_SHOW, cookShow);
            data.putExtras(bundle);
            LocalBroadcastManager.getInstance(context).sendBroadcast(data);

        } else if (ACTION_PICK_ONE_PIC.equals(action)
                || ACTION_PICK_MULTI_PIC.equals(action)) {

            Intent data = new Intent(intent);
            LocalBroadcastManager.getInstance(context).sendBroadcast(data);
        }


    }
}
