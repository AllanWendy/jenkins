// Copy right WeCook Inc.
package com.wecook.common.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.IModule;
import com.wecook.common.modules.ModuleManager;
import com.wecook.common.modules.ModuleManager.ModuleRegisterObserver;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.AndroidUtils;

import java.util.List;

/**
 * App抽象类
 *
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public abstract class BaseApp extends Application {

    private static BaseApp sApp;

    private static boolean mResume;
    private static boolean mPendingKill;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        ModuleManager.asInstance().registerAllModules(this, new ModuleRegisterObserver() {

            @Override
            public void onPrepare(Context context, IModule module) {
                onPrepareModule(context, module);
            }
        });
    }

    public abstract void onPrepareModule(Context context, IModule module);

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageFetcher.asInstance().onLowMemory(-1);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ImageFetcher.asInstance().onLowMemory(level);
    }

    /**
     * 获得Application对象
     *
     * @return
     */
    public static BaseApp getApplication() {
        return sApp;
    }

    /**
     * 退出软件
     */
    public static void quitApp() {
        quitApp(true);
    }

    /**
     * 强制退出软件
     */
    public static void quitApp(boolean waitLog) {
        ModuleManager.asInstance().unRegisterAllModules();
        if (AndroidUtils.isMainProcess(sApp)) {

            if (!waitLog) {
                killAll();
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mResume = false;
                    mPendingKill = true;
                    MobclickAgent.onKillProcess(sApp);

                    if (!mResume) {
                        Logger.d("app", "kill..");
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mPendingKill = false;
                                killAll();
                            }
                        });
                    }
                }
            }).start();
        } else {
            killAll();
        }

    }

    private static void killAll() {
        sApp.onDestroy();

        List<Integer> pIds = AndroidUtils.getAllProcessId(sApp,
                new String[]{sApp.getPackageName() + ":push"});
        for (int pid : pIds) {
            Process.killProcess(pid);
        }
    }

    /**
     * 销毁
     */
    protected void onDestroy() {

    }

    /**
     * 隐藏软件
     */
    public static void hideApp() {
        //模拟HOME键
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
        intent.addCategory(Intent.CATEGORY_HOME);
        sApp.startActivity(intent);
    }

    public static void resumeApp() {
        mResume = true;
        Logger.d("app", "resume...");
    }

    public static boolean isPendingKillApp() {
        return mPendingKill;
    }

    public AppLink getAppLink() {
        return new AppLink() {
            @Override
            public void onUpdateLink(Context context) {

            }

            @Override
            public void onLaunchApp(Context context) {

            }
        };
    }
}
