package com.wecook.common.modules.thirdport.platform.base;

import android.content.Context;
import android.content.Intent;


/**
 * 分享选项接口
 */
public interface IPlatform extends Shareable, Payable{

    /**
     * 注册监听
     *
     * @param eventListener
     */
    void setEventListener(IPlatformEventListener eventListener);

    /**
     * 类型
     *
     * @param type
     */
    void setType(int type);

    /**
     * 是否安装软件
     *
     * @return
     */
    boolean isInstalledApp();

    /**
     * 是否需要进行更新
     *
     * @return
     */
    boolean isNeedUpdate();

    /**
     * 登录状态是否有效
     *
     * @return
     */
    boolean isValidSession();

    /**
     * 获得ICON资源ID
     *
     * @return
     */
    int getIcon();

    /**
     * 获得功能名字
     *
     * @return
     */
    String getName();

    /**
     * 检查API-SDK是否有效
     *
     * @return
     */
    boolean onCreate();

    /**
     * 帐号登录
     */
    void onLogin();

    /**
     * 帐号注销
     */
    void onLogout();

    /**
     * 消息响应监听
     *
     * @param context
     * @param intent
     * @return
     */
    boolean onEvent(Context context, Intent intent);

    /**
     * 获得用户信息
     *
     * @return
     */
    Object getUserInfo();

    interface IPlatformEventListener {

        /**
         * 分享结果反馈
         *
         * @param option
         * @param success
         * @param message
         */
        void onResponseShare(IPlatform option, boolean success, String message);

        /**
         * 登录结果反馈
         *
         * @param option
         * @param success
         * @param message
         */
        void onResponseLogin(IPlatform option, boolean success, String message);

        /**
         * 登出结果反馈
         *
         * @param option
         * @param success
         * @param message
         */
        void onResponseLogout(IPlatform option, boolean success, String message);

        /**
         * 支付结果反馈
         *
         * @param option
         * @param success
         * @param message
         */
        void onResponsePay(IPlatform option, boolean success, String message);
    }
}
