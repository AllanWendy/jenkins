package cn.wecook.app.features.thirdport;

import com.wecook.common.modules.thirdport.PlatformConfiguration;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.modules.thirdport.config.PayConfig;
import com.wecook.common.modules.thirdport.config.ShareConfig;

import cn.wecook.app.R;

/**
 * 多平台资源配置
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/3/14
 */
class PlatformResourceConfig extends PlatformConfiguration {

    private AppShareConfig mShareConfig;
    private AppPayConfig mPayConfig;

    @Override
    public boolean enablePlatformSSO(int platform) {
        return true;
    }

    @Override
    public String getPlatformName(int platform) {
        String name = "";
        switch (platform) {
            case PlatformManager.PLATFORM_WECHAT:
                name = "微信";
                break;
            case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                name = "朋友圈";
                break;
            case PlatformManager.PLATFORM_WEBLOG:
                name = "微博";
                break;
            case PlatformManager.PLATFORM_QQ:
                name = "QQ";
                break;
            case PlatformManager.PLATFORM_QQZONE:
                name = "QQ空间";
                break;
            case PlatformManager.PLATFORM_ALIPAPA:
                name = "支付宝";
                break;
        }
        return name;
    }

    @Override
    public int getPlatformIcon(int platform) {
        int iconId = 0;
        switch (platform) {
            case PlatformManager.PLATFORM_WECHAT:
                iconId = R.drawable.app_bt_share_wexin;
                break;
            case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                iconId = R.drawable.app_bt_share_wexin_friends;
                break;
            case PlatformManager.PLATFORM_WEBLOG:
                iconId = R.drawable.app_bt_share_wexin;
                break;
        }
        return iconId;
    }


    @Override
    public PayConfig getPayConfig() {
        if (mPayConfig == null) {
            mPayConfig = new AppPayConfig();
        }
        return mPayConfig;
    }

    @Override
    public ShareConfig getShareConfig() {
        if (mShareConfig == null) {
            mShareConfig = new AppShareConfig();
        }
        return mShareConfig;
    }

    private class AppPayConfig implements PayConfig {

    }

    private class AppShareConfig implements ShareConfig {
        @Override
        public String getAppID(int platform) {
            String appId = "";
            switch (platform) {
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                    appId = "wx8cb2d827342ddcd8";
                    break;
                case PlatformManager.PLATFORM_WEBLOG:
                    appId = "835199507";
                    break;
                case PlatformManager.PLATFORM_QQ:
                case PlatformManager.PLATFORM_QQZONE:
                    appId = "1101253430";
                    break;
            }
            return appId;
        }

        @Override
        public String getAppSecret(int platform) {
            String appId = "";
            switch (platform) {
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                    appId = "7d1246fc992821e4ccd969e2ad0417d1";
                    break;
                case PlatformManager.PLATFORM_WEBLOG:
                    appId = "03e37b5f5b4b0186625118172a678092";
                    break;
                case PlatformManager.PLATFORM_QQ:
                case PlatformManager.PLATFORM_QQZONE:
                    appId = "QIQBTtSIrKkNrRoO";
                    break;
            }
            return appId;
        }

        @Override
        public String getRedirectUrl(int platform) {
            String redirectUrl = "";
            switch (platform) {
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                    redirectUrl = "http://wecook.cn";
                    break;
                case PlatformManager.PLATFORM_WEBLOG:
                    redirectUrl = "http://wecook.cn";
                    break;
                case PlatformManager.PLATFORM_QQ:
                case PlatformManager.PLATFORM_QQZONE:
                    redirectUrl = "http://wecook.cn";
                    break;
            }
            return redirectUrl;
        }

        @Override
        public String getScope(int platform) {
            String scope = "";
            switch (platform) {
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                    scope = "all";
                    break;
                case PlatformManager.PLATFORM_WEBLOG:
                    scope = "all";
                    break;
                case PlatformManager.PLATFORM_QQ:
                case PlatformManager.PLATFORM_QQZONE:
                    scope = "all";
                    break;
            }
            return scope;
        }

        @Override
        public String getOAuthUrl(int platform) {
            String oauthUrl = "";
            switch (platform) {
                case PlatformManager.PLATFORM_WECHAT:
                case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                    oauthUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";
                    break;
                case PlatformManager.PLATFORM_WEBLOG:
                    oauthUrl = "https://open.weibo.cn/oauth2/authorize";
                    break;
                case PlatformManager.PLATFORM_QQ:
                case PlatformManager.PLATFORM_QQZONE:
                    oauthUrl = "http://wecook.cn";
                    break;
            }
            return oauthUrl;
        }
    }
}
