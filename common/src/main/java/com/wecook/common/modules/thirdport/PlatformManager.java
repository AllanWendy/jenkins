package com.wecook.common.modules.thirdport;

import android.content.Context;
import android.content.Intent;

import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.modules.thirdport.object.OrderInfo;
import com.wecook.common.modules.thirdport.platform.Alipay;
import com.wecook.common.modules.thirdport.platform.QQFriends;
import com.wecook.common.modules.thirdport.platform.QQZone;
import com.wecook.common.modules.thirdport.platform.RenRen;
import com.wecook.common.modules.thirdport.platform.WeiBlog;
import com.wecook.common.modules.thirdport.platform.WeiChat;
import com.wecook.common.modules.thirdport.platform.base.BasePlatform;
import com.wecook.common.modules.thirdport.platform.base.IPlatform;

import java.util.HashMap;
import java.util.Map;

/**
 * 分享管理
 */
public class PlatformManager {

    public static final int PLATFORM_WEBLOG = 1; /*微博*/
    public static final int PLATFORM_WECHAT = 2; /*微信好友*/
    public static final int PLATFORM_WECHAT_FRIENDS = 3; /*微信朋友圈*/
    public static final int PLATFORM_QQZONE = 4; /*QQ空间*/
    public static final int PLATFORM_QQ = 5; /*QQ好友*/
    public static final int PLATFORM_RENREN = 6; /*人人网*/
    public static final int PLATFORM_ALIPAPA = 7; /*支付宝*/

    private static PlatformManager INSTANCE;
    private PlatformCreator mPlatformCreator;
    private PlatformConfiguration mConfiguration;

    private Map<Integer, BasePlatform> mCreatedPlatforms = new HashMap<Integer, BasePlatform>();

    private PlatformManager() {
        mPlatformCreator = new PlatformCreator();
    }

    public static PlatformManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlatformManager();
        }
        return INSTANCE;
    }

    public static void init(Context context, PlatformConfiguration configuration) {
        PlatformManager.getInstance().mConfiguration = configuration;
        PlatformManager.getInstance().initPlatforms(context);
    }

    /**
     * 检测是否支持该option分享
     *
     * @param context
     * @param platform
     * @return
     */
    public boolean isSupport(Context context, int platform) {
        BasePlatform shareOption = mPlatformCreator.createPlatform(context, platform);
        if (shareOption != null) {
            return shareOption.checkSupport();
        }
        return false;
    }

    /**
     * 是否安装软件
     *
     * @param context
     * @param platform
     * @return
     */
    public boolean isInstalled(Context context, int platform) {
        BasePlatform shareOption = mPlatformCreator.createPlatform(context, platform);
        if (shareOption != null) {
            return shareOption.isInstalledApp();
        }
        return false;
    }

    /**
     * 是否开启消息通知
     *
     * @param enable
     */
    public void enableShowNotify(boolean enable) {
        mConfiguration.enableShowNotify(enable);
    }

    /**
     * 判断是否有效
     *
     * @param context
     * @param platform
     * @return
     */
    public boolean isValidSession(Context context, int platform) {
        BasePlatform shareOption = mPlatformCreator.createPlatform(context, platform);
        if (shareOption != null) {
            return shareOption.isValidSession();
        }
        return false;
    }

    /**
     * 进行绑定
     *
     * @param context
     * @param platform
     * @param listener
     */
    public void bind(Context context, int platform, IPlatform.IPlatformEventListener listener) {
        BasePlatform shareOption = mPlatformCreator.createPlatform(context, platform, listener);
        if (shareOption != null) {
            shareOption.doLogin();
        }
    }

    /**
     * 进行解绑
     *
     * @param context
     * @param platform
     * @param listener
     */
    public void unbind(Context context, int platform, IPlatform.IPlatformEventListener listener) {
        BasePlatform shareOption = mPlatformCreator.createPlatform(context, platform, listener);
        if (shareOption != null) {
            shareOption.doLogout();
        }
    }

    /**
     * 进行分享
     *
     * @param platform    {@link #PLATFORM_QQZONE#PLATFORM_WEBLOG#PLATFORM_WECHAT#PLATFORM_WECHAT_FRIENDS}
     * @param shareObject
     * @param listener
     */
    public void share(Context context, int platform, IShareObject shareObject, IPlatform.IPlatformEventListener listener) {
        BasePlatform shareOption = mPlatformCreator.createPlatform(context, platform, shareObject, listener);
        if (shareOption != null) {
            shareOption.doShare();
        }
    }

    /**
     * 调用支付
     *
     * @param context
     * @param platformType
     * @param orderInfo
     * @param listener
     */
    public void pay(Context context, int platformType, OrderInfo orderInfo, IPlatform.IPlatformEventListener listener) {
        BasePlatform platform = mPlatformCreator.createPlatform(context, platformType, listener);
        if (platform != null) {
            platform.doPay(orderInfo);
        }
    }

    /**
     * Handle Activity's onActivityResult() method.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onHandleActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        synchronized (mPlatformCreator) {
            for (BasePlatform shareOption : mCreatedPlatforms.values()) {
                if (data == null) {
                    data = new Intent(shareOption.getCurrentAction());
                }
                data.putExtra(BasePlatform.EXTRA_REQUEST_CODE, requestCode);
                data.putExtra(BasePlatform.EXTRA_RESULT_CODE, resultCode);
                data.putExtra(BasePlatform.EXTRA_TYPE, BasePlatform.TYPE_ACTIVITY_RESULT);
                shareOption.onEvent(context, data);
            }
        }
    }

    /**
     * Handle Activity's onNewIntent(intent) method.
     *
     * @param intent
     */
    public boolean onHandleNewIntent(Context context, Intent intent) {
        synchronized (mPlatformCreator) {
            boolean handled = false;
            for (BasePlatform shareOption : mCreatedPlatforms.values()) {
                if (intent == null) {
                    intent = new Intent(shareOption.getCurrentAction());
                }
                intent.putExtra(BasePlatform.EXTRA_TYPE, BasePlatform.TYPE_NEW_INTENT);
                if (shareOption.onEvent(context, intent)) {
                    handled = true;
                }
            }
            return handled;
        }
    }

    public void initPlatforms(Context context) {
        mPlatformCreator.createPlatform(context, PLATFORM_WEBLOG);
        mPlatformCreator.createPlatform(context, PLATFORM_WECHAT);
        mPlatformCreator.createPlatform(context, PLATFORM_WECHAT_FRIENDS);
        mPlatformCreator.createPlatform(context, PLATFORM_QQZONE);
        mPlatformCreator.createPlatform(context, PLATFORM_QQ);
        mPlatformCreator.createPlatform(context, PLATFORM_RENREN);
        mPlatformCreator.createPlatform(context, PLATFORM_ALIPAPA);
    }

    public IPlatform getPlatform(int option) {
        return mCreatedPlatforms.get(option);
    }

    public PlatformConfiguration getConfig() {
        return mConfiguration;
    }

    /**
     * 分享项工厂
     */
    public class PlatformCreator {

        public synchronized BasePlatform createPlatform(Context context, int platformType) {
            BasePlatform platform = mCreatedPlatforms.get(platformType);
            if (!mConfiguration.enablePlatform(platformType)) {
                return null;
            }
            switch (platformType) {
                case PLATFORM_WEBLOG:
                    if (platform == null) {
                        platform = new WeiBlog(context);
                        mCreatedPlatforms.put(platformType, platform);
                    }
                    break;
                case PLATFORM_WECHAT:
                    if (platform == null) {
                        platform = new WeiChat(context);
                        mCreatedPlatforms.put(platformType, platform);
                    }
                    break;
                case PLATFORM_WECHAT_FRIENDS:
                    if (platform == null) {
                        platform = new WeiChat(context);
                        mCreatedPlatforms.put(platformType, platform);
                    }
                    ((WeiChat) platform).setEnableTimeline(true);
                    break;
                case PLATFORM_QQZONE:
                    if (platform == null) {
                        platform = new QQZone(context);
                        mCreatedPlatforms.put(platformType, platform);
                    }
                    break;
                case PLATFORM_QQ:
                    if (platform == null) {
                        platform = new QQFriends(context);
                        mCreatedPlatforms.put(platformType, platform);
                    }
                    break;
                case PLATFORM_RENREN:
                    if (platform == null) {
                        platform = new RenRen(context);
                        mCreatedPlatforms.put(platformType, platform);
                    }
                    break;
                case PLATFORM_ALIPAPA:
                    if (platform == null) {
                        platform = new Alipay(context);
                        mCreatedPlatforms.put(platformType, platform);
                    }
                    break;
            }
            platform.setType(platformType);
            return platform;
        }

        public synchronized BasePlatform createPlatform(Context context, int platformType, IShareObject shareObject, IPlatform.IPlatformEventListener listener) {
            BasePlatform platform = createPlatform(context, platformType);
            if (platform != null) {
                platform.setContext(context);
                platform.setShareObject(shareObject);
                platform.setEventListener(listener);
            }
            return platform;
        }

        public synchronized BasePlatform createPlatform(Context context, int platformType, IPlatform.IPlatformEventListener listener) {
            BasePlatform platform = createPlatform(context, platformType);
            if (platform != null) {
                platform.setContext(context);
                platform.setEventListener(listener);
            }
            return platform;
        }
    }

}
