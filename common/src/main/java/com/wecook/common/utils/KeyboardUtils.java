package com.wecook.common.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.wecook.common.modules.asynchandler.UIHandler;

/**
 * 键盘辅助类
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/21/14
 */
public class KeyboardUtils {

    /**
     * 打开键盘
     *
     * @param context
     * @param view
     */
    public static void openKeyboard(final Context context, final View view) {
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (context != null && view != null) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(view, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        }, 300);

    }

    /**
     * 关闭键盘
     *
     * @param context
     * @param view
     */
    public static boolean closeKeyboard(Context context, View view) {
        if (view == null) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 显示键盘
     * @param context
     */
    public static void openKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭键盘
     * @param context
     */
    public static void closeKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

}
