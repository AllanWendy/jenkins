package com.wecook.common.modules.thirdport;

import com.wecook.common.modules.thirdport.config.PayConfig;
import com.wecook.common.modules.thirdport.config.ShareConfig;

/**
 * 分享配置
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/3/14
 */
public abstract class PlatformConfiguration {

    private boolean showNotify = true;

    /**
     * 是否显示通知
     *
     * @param enable
     */
    public void enableShowNotify(boolean enable) {
        showNotify = enable;
    }

    /**
     * 是否显示通知
     *
     * @param enable
     */
    public boolean enableShowNotify() {
        return showNotify;
    }

    /**
     * 是否开启
     *
     * @return
     */
    public boolean enablePlatform(int platform) {
        return true;
    }

    /**
     * 是否开启SSO功能
     *
     * @param platform
     * @return
     */
    public boolean enablePlatformSSO(int platform) {
        return false;
    }

    /**
     * 名字
     *
     * @param platform
     * @return
     */
    public abstract String getPlatformName(int platform);

    /**
     * 标题
     *
     * @param platform
     * @return
     */
    public abstract int getPlatformIcon(int platform);

    /**
     * 获得支付配置
     * @return
     */
    public abstract PayConfig getPayConfig();

    /**
     * 获得分享配置
     * @return
     */
    public abstract ShareConfig getShareConfig();
}
