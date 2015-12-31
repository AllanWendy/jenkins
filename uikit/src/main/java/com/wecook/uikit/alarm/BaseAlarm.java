// Copy right WeCook Inc.
package com.wecook.uikit.alarm;

import android.content.Context;

import com.wecook.common.core.debug.Logger;

/**
 * 基础抽象层通知器
 *
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public abstract class BaseAlarm implements IAlarm {

    private AlarmListener mAlarmListener;

    protected boolean mIsShowing;

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void show() {
        Logger.d("show..");
        mIsShowing = true;
    }

    @Override
    public void show(long keepTime) {
        Logger.d("show..keepTime[" + keepTime + "]");
        mIsShowing = true;
    }

    @Override
    public void dismiss() {
        Logger.d("dismiss..");
        mIsShowing = false;
    }

    @Override
    public void cancel() {
        Logger.d("cancel..");
        mIsShowing = false;
    }

    @Override
    public void setAlarmListener(AlarmListener listener) {
        mAlarmListener = listener;
    }

    /**
     * 返回监听器
     *
     * @return
     */
    public AlarmListener getAlarmListener() {
        return mAlarmListener;
    }

    /**
     * 是否正在显示中
     *
     * @return
     */
    public boolean isShowing() {
        return mIsShowing;
    }
}
