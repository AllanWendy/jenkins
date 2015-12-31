package com.wecook.common.modules.thirdport.platform.base;

import com.wecook.common.modules.thirdport.object.IShareObject;

/**
 * 可分享
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/2/14
 */
public interface Shareable {

    /**
     * 获得APPID
     *
     * @return
     */
    String getShareAppId();

    /**
     * 获得APP Secret
     *
     * @return
     */
    String getShareAppSecret();

    /**
     * 授权
     *
     * @return
     */
    String getShareScope();

    /**
     * 重定向
     *
     * @return
     */
    String getShareRedirectUrl();

    /**
     * 授权网址
     *
     * @return
     */
    String getShareOAuthUrl();

    /**
     * 获得最大输入字数
     *
     * @param shareObject
     * @return
     */
    int getShareMaxLength(IShareObject shareObject);

    /**
     * 是否支持以SSO进行分享
     *
     * @return
     */
    boolean isSupportSSOShare();

    /**
     * 是否支持以Web形式进行分享
     *
     * @return
     */
    boolean isSupportWebShare();

    /**
     * 开始执行分享过程
     */
    void onShare(IShareObject... shareObject);

    /**
     * 检查数据有效性
     *
     * @return
     */
    boolean shareValidateCheck(IShareObject... shareObject);
}
