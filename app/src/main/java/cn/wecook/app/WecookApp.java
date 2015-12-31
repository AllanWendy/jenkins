package cn.wecook.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.wecook.common.app.AppLink;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.debug.DebugCenter;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.IModule;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.database.DataLibraryManager;
import com.wecook.common.modules.logstatistics.LogConfiger;
import com.wecook.common.modules.logstatistics.LogTracker;
import com.wecook.common.modules.messager.XMPushMessager;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.sdk.api.legacy.ConfigApi;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.dbprovider.AppDataLibrary;
import com.wecook.sdk.dbprovider.InspireSearchDataLibrary;
import com.wecook.sdk.policy.LogConstant;
import com.zhuge.analysis.stat.ZhugeSDK;

import cn.wecook.app.features.deliver.WecookLink;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.server.AppLogServer;

/**
 * 味库Android客户端
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class WecookApp extends BaseApp {
    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
            StrictMode.noteSlowCall("slow-call");
        }

        super.onCreate();
        
        DebugCenter.openAppDebug(BuildConfig.DEBUG || WecookConfig.getInstance().isTest());
        DebugCenter.catchGlobalException(this);
        //第三方组件
        ThirdPortDelivery.init(this);

        //UI刷新
        UIHandler.initInApp(this);

        //API初始化
        Api.init(WecookConfig.getInstance());

        try {
            //统计配置
            MobclickAgent.setDebugMode(DebugCenter.isDebugable());
            MobclickAgent.updateOnlineConfig(this);
            MobclickAgent.setCatchUncaughtExceptions(true);
            MobclickAgent.openActivityDurationTrack(false);
            //统计启动日志
            MobclickAgent.onEvent(this, LogConstant.UBS_APP_START_COUNT);
        } catch (Throwable e) {
            Logger.d("init umeng ", e);
        }

        try {
            ZhugeSDK.getInstance().init(this);
        } catch (Throwable e) {
            Logger.d("init zhuge-io ", e);
        }

        try {
            //如果是友盟自动更新过来的渠道[update]，则保留为原渠道
            Object currentChannel = AndroidUtils.getMetaDataFromApplication(this, "UMENG_CHANNEL");
            if ("update".equals(currentChannel)) {
                AnalyticsConfig.setChannel(PhoneProperties.getChannel());
                ZhugeSDK.getInstance().getConfig().setChannel(PhoneProperties.getChannel());
            }
        } catch (Throwable e) {
            Logger.d("init", e);
        }

        //快捷递送功能
        WecookLink.getInstance().onUpdateLink(this);

        Logger.d("app", "start...");
    }

    @Override
    public void onPrepareModule(Context context, IModule module) {
        if (module instanceof DataLibraryManager) {
            //注册数据库
            DataLibraryManager.addLibrary(new AppDataLibrary(this));
            DataLibraryManager.addLibrary(new InspireSearchDataLibrary(this));
        } else if (module instanceof LogTracker) {
            //设置日志发送服务
            LogConfiger configer = new LogConfiger();
            configer.setLogServerCls(AppLogServer.class);
            LogTracker.asInstance().setLogConfiger(context, configer);
        } else if (module instanceof XMPushMessager) {
            SharePreferenceProperties.register(new SharePreferenceProperties.OnSharedPreferenceSetListener() {
                @Override
                public void onSharedPreferenceSet(SharedPreferences sharedPreferences, String key, Object value, Object old) {

                    if (PhoneProperties.PUSH_ID.equals(key)) {
                        if (!value.equals(old)) {
                            //上报推送信息
                            ConfigApi.reportPush(new ApiCallback<State>() {
                                @Override
                                public void onResult(State result) {
                                    if (!result.available()) {
                                        SharePreferenceProperties.set(PhoneProperties.PUSH_ID, "");
                                    }
                                }
                            });
                        }
                        SharePreferenceProperties.unregister(this);
                    }
                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZhugeSDK.getInstance().flush(this);
    }

    @Override
    public AppLink getAppLink() {
        return WecookLink.getInstance();
    }

}
