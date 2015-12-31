// Copy right WeCook Inc.
package com.wecook.uikit.activity;

/**
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public interface ActivityLifecycleListener {

    public static final int LF_onCreate = 0;
    public static final int LF_onStart = 1;
    public static final int LF_onResume = 2;
    public static final int LF_onAttachedToWindow = 3;
    public static final int LF_onNewIntent = 4;
    public static final int LF_onPause = 5;
    public static final int LF_onStop = 6;
    public static final int LF_onDestroy = 7;
    public static final int LF_onDetachedFromWindow = 8;
    public static final int LF_onLowMemory = 9;
    public static final int LF_onPostResume = 10;
    public static final int LF_onRestart = 11;
    public static final int LF_onRestoreInstanceState = 12;
    public static final int LF_onSaveInstanceState = 13;

    public static String[] sLifecycleNames = {
            "onCreate",
            "onStart",
            "onResume",
            "onAttachedToWindow",
            "onNewIntent",
            "onPause",
            "onStop",
            "onDestroy",
            "onDetachedFromWindow",
            "onLowMemory",
            "onPostResume",
            "onRestart",
            "onRestoreInstanceState",
            "onSaveInstanceState",
    };

    /**
     * 生命周期变化
     *
     * @param activity
     * @param lifeState
     */
    public void onLifecycleChanged(IActivity activity, int lifeState);
}
