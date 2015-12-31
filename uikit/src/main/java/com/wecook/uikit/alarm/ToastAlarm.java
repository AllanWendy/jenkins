// Copy right WeCook Inc.
package com.wecook.uikit.alarm;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wecook.common.app.BaseApp;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.R;

/**
 * 会话通知
 *
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public class ToastAlarm extends BaseAlarm {

    private static final ToastAlarm sToastAlarm = new ToastAlarm();

    private Toast mToast;

    public static ToastAlarm getInstance() {
        return sToastAlarm;
    }

    /**
     * 创建会话通知
     *
     * @param context  上下文
     * @param resId    文字id
     * @param duration 显示时长
     * @return
     */
    public static ToastAlarm makeToastAlarm(Context context, int resId, int duration) {
        if (context != null) {
            String contentText = context.getString(resId);
            if (null != contentText) {
                return makeToastAlarm(context, contentText, duration);
            }
        }
        return null;
    }

    /**
     * 创建会话通知
     *
     * @param context  上下文
     * @param text     文字
     * @param duration 显示时长
     * @return
     */
    public static ToastAlarm makeToastAlarm(Context context, CharSequence text, int duration) {
        View v = LayoutInflater.from(context).inflate(R.layout.uikit_toast, null);
        return makeToastAlarm(context, text, v, R.id.uikit_toast_content, duration);
    }

    /**
     * 创建会话通知
     *
     * @param context  上下文
     * @param text     文字
     * @param view     Toast界面
     * @param textId   界面中文本视图ID
     * @param duration 时长
     * @return
     */
    public static ToastAlarm makeToastAlarm(Context context, CharSequence text, View view, int textId, int duration) {
        ToastAlarm alarm = ToastAlarm.getInstance();
        if (alarm.mToast != null) {
            alarm.mToast.cancel();
        }
        alarm.mToast = new Toast(context);
        alarm.mToast.setGravity(Gravity.CENTER, 0, 0);
        if (view != null) {
            TextView tv = (TextView) view.findViewById(textId);
            if (tv != null) {
                tv.setText(text);
            }
            alarm.mToast.setView(view);
        }
        alarm.mToast.setDuration(duration);
        return alarm;
    }

    @Override
    public void show() {
        super.show();
        if (mToast != null) {
            mToast.show();
        }
    }

    @Override
    public void show(long keepTime) {
        super.show(keepTime);
        if (mToast != null) {
            mToast.setDuration((int) keepTime);
            mToast.show();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mToast != null) {
            mToast.cancel();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if (mToast != null) {
            mToast.cancel();
        }
    }

    public static ToastAlarm show(String text) {
        return show(text, Toast.LENGTH_SHORT);
    }

    public static ToastAlarm show(String text, int time) {
        if (StringUtils.isEmpty(text)) {
            return ToastAlarm.getInstance();
        }
        ToastAlarm.makeToastAlarm(BaseApp.getApplication(), text, time).show();
        return ToastAlarm.getInstance();
    }

    public static ToastAlarm show(int text) {
        return show(text, Toast.LENGTH_SHORT);
    }

    public static ToastAlarm show(int text, int time) {
        ToastAlarm.makeToastAlarm(BaseApp.getApplication(), text, time).show();
        return ToastAlarm.getInstance();
    }

    /**
     * 显示之后
     *
     * @param then
     */
    public void then(Runnable then) {
        UIHandler.postDelayed(then, ToastAlarm.getInstance().getDuration());
    }

    private static long mAmongStartTime;

    /**
     * 显示期间
     *
     * @param inside
     */
    public void among(Runnable inside) {

        if (mAmongStartTime == 0) {
            mAmongStartTime = System.currentTimeMillis();
            then(new Runnable() {
                @Override
                public void run() {
                    mAmongStartTime = 0;
                }
            });
            return;
        }

        if (System.currentTimeMillis() - mAmongStartTime < ToastAlarm.getInstance().getDuration()) {
            UIHandler.post(inside);
        }

    }

    private long getDuration() {
        switch (ToastAlarm.getInstance().mToast.getDuration()) {
            case Toast.LENGTH_SHORT:
                return 800;
            case Toast.LENGTH_LONG:
                return 1500;
        }
        return ToastAlarm.getInstance().mToast.getDuration();
    }
}
