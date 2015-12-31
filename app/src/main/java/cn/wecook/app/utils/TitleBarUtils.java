package cn.wecook.app.utils;

import android.content.Context;
import android.view.View;

import com.wecook.uikit.widget.TitleBar;

/**
 * TitleBar辅助类
 * Created by LK on 2015/9/17.
 */
public class TitleBarUtils {

    /**
     * 设置TitleBar 提示语
     *
     * @param context
     * @param titleBar
     * @param str          提示语
     * @param isShowRemind 是否显示提示按钮
     * @param listener
     * @return
     */
    public static TitleBar setRemind(Context context, TitleBar titleBar, String str, boolean isShowRemind, View.OnClickListener listener) {
        if (null != titleBar) {
            titleBar.enableRemind(isShowRemind);
            if (isShowRemind) {
                titleBar.setRemindText(str, listener);
            }
        }
        return titleBar;
    }




}
