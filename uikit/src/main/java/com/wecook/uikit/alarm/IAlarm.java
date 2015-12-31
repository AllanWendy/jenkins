// Copy right WeCook Inc.
package com.wecook.uikit.alarm;

import android.content.Context;

/**
 * 通知类接口
 *
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public interface IAlarm {

    public Context getContext();

    /**
     * 显示
     */
    public void show();

    /**
     * 显示事件
     *
     * @param keepTime
     */
    public void show(long keepTime);

    /**
     * 消失
     */
    public void dismiss();

    /**
     * 取消显示
     */
    public void cancel();

    /**
     * 设置监听
     *
     * @param listener
     */
    public void setAlarmListener(AlarmListener listener);

    /**
     * 监听
     */
    public interface AlarmListener {

        /**
         * 当显示成功的回调
         *
         * @param alarm
         */
        public void onShow(IAlarm alarm);

        /**
         * 当消失完成的回调
         *
         * @param alarm
         */
        public void onDismiss(IAlarm alarm);

        /**
         * 取消
         * @param alarm
         */
        public void onCancel(IAlarm alarm);

    }
}
