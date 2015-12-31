// Copy right WeCook Inc.
package com.wecook.uikit.activity;

import android.support.v4.app.LoaderManager;

import com.wecook.uikit.loader.IUIAsyncTasker;

/**
 * Activity接口
 * 
 * @author 	kevin
 * @since 	2014-Sep 17, 2014
 * @version	v1.0
 */
public interface IActivity extends ActivityLifecycleListener, IUIAsyncTasker{


    /**
     * 注册生命周期监听
     * @param lifecycleListener
     */
    public void registerActivityLifecycleListener(ActivityLifecycleListener lifecycleListener);

    /**
     * 取消注册生命周期监听
     * @param lifecycleListener
     */
    public void unregisterActivityLifecycleListener(ActivityLifecycleListener lifecycleListener);

    LoaderManager getSupportLoaderManager();

    android.app.LoaderManager getLoaderManager();

    boolean openDeliver();
}
