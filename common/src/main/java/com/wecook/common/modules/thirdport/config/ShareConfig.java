package com.wecook.common.modules.thirdport.config;

/**
 * 分享配置
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/2/14
 */
public interface ShareConfig {

    /**
     * app id
     *
     * @param platform
     * @return
     */
    public String getAppID(int platform);

    /**
     * app secret
     *
     * @param platform
     * @return
     */
    public String getAppSecret(int platform);

    /**
     * 授权地址跳转
     *
     * @param platform
     * @return
     */
    public String getRedirectUrl(int platform);

    /**
     * 授权权限范围
     *
     * @param platform
     * @return
     */
    public String getScope(int platform);

    /**
     * 授权地址
     *
     * @param platform
     * @return
     */
    public String getOAuthUrl(int platform);
}
