// Copy right WeCook Inc.
package com.wecook.uikit.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.android.debug.hv.ViewServer;
import com.umeng.analytics.MobclickAgent;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.debug.DebugCenter;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.logstatistics.LogEvent;
import com.wecook.common.modules.logstatistics.LogTracker;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.uikit.loader.UILoader;

import java.util.HashSet;


/**
 * 抽象Activity
 * 
 * @author 	kevin
 * @since 	2014-Sep 17, 2014
 * @version	v1.0
 */
public abstract class BaseActivity extends Activity implements IActivity {

    private HashSet<ActivityLifecycleListener> activityLifecycleListeners = new HashSet<ActivityLifecycleListener>();

    private LogEvent mLogEvent = LogTracker.getLogEvent(this);

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void registerActivityLifecycleListener(ActivityLifecycleListener lifecycleListener) {
        if (lifecycleListener != null) {
            activityLifecycleListeners.add(lifecycleListener);
        }
    }

    @Override
    public void unregisterActivityLifecycleListener(ActivityLifecycleListener lifecycleListener) {
        if (lifecycleListener != null) {
            activityLifecycleListeners.remove(lifecycleListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerActivityLifecycleListener(this);
        dispatchActivityLifecycleListener(LF_onCreate);
        if (DebugCenter.isDebugable()) {
            ViewServer.get(this).addWindow(this);
        }
    }

    public boolean openDeliver() {
        return true;
    }

    @Override
    protected void onStart() {
        onStartUILoad();
        startAllUILoaders();
        super.onStart();
        dispatchActivityLifecycleListener(LF_onStart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dispatchActivityLifecycleListener(LF_onResume);
        if (DebugCenter.isDebugable()) {
            ViewServer.get(this).setFocusedWindow(this);
        }
        if (openDeliver()) {
            BaseApp.getApplication().getAppLink().onUpdateLink(this);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        dispatchActivityLifecycleListener(LF_onAttachedToWindow);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        dispatchActivityLifecycleListener(LF_onNewIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dispatchActivityLifecycleListener(LF_onPause);
    }

    @Override
    protected void onStop() {
        onStopUILoad();
        stopAllUILoaders();
        super.onStop();
        dispatchActivityLifecycleListener(LF_onStop);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dispatchActivityLifecycleListener(LF_onDestroy);
        unregisterActivityLifecycleListener(this);
        if (DebugCenter.isDebugable()) {
            ViewServer.get(this).removeWindow(this);
        }
        if (!AndroidUtils.isMainProcess(this)) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dispatchActivityLifecycleListener(LF_onDetachedFromWindow);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        dispatchActivityLifecycleListener(LF_onLowMemory);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        dispatchActivityLifecycleListener(LF_onPostResume);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        dispatchActivityLifecycleListener(LF_onRestart);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        dispatchActivityLifecycleListener(LF_onRestoreInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        dispatchActivityLifecycleListener(LF_onSaveInstanceState);
    }

    @Override
    public void onLifecycleChanged(IActivity activity, int lifeState) {
        //nothing
        if (lifeState >= 0 && lifeState < sLifecycleNames.length) {
            Logger.i("[" + ((Object)activity).getClass().getSimpleName() + "] " + sLifecycleNames[lifeState]);
        }

        switch (lifeState) {
            case LF_onStart:
                LogTracker.startTracking(mLogEvent);
                break;
            case LF_onStop:
                LogTracker.stopTracking(mLogEvent);
                break;
            case LF_onResume:
                MobclickAgent.onResume(getContext());
                break;
            case LF_onPause:
                MobclickAgent.onPause(getContext());
                break;
        }
    }

    private void dispatchActivityLifecycleListener(int lifecycleState) {
        for (ActivityLifecycleListener listener : activityLifecycleListeners) {
            if (listener != null) {
                listener.onLifecycleChanged(this, lifecycleState);
            }
        }
    }


    @Override
    public LoaderManager getSupportLoaderManager() {
        return null;
    }

    @Override
    public boolean addUILoader(UILoader loader) {
        return false;
    }

    @Override
    public void restartUILoaders() {

    }

    @Override
    public void removeUILoader(UILoader loader) {

    }

    @Override
    public void removeUILoader(int id) {

    }

    @Override
    public void stopAllUILoaders() {

    }

    @Override
    public void startAllUILoaders() {

    }

    @Override
    public final void startUILoader(UILoader loader, Bundle args) {

    }

    @Override
    public void deliveryFinish() {

    }

    @Override
    public void onStartUILoad() {

    }

    @Override
    public void onStopUILoad() {

    }

    @Override
    public void onFinishedToUpdateUI(boolean success) {

    }

    @Override
    public String dumpUITree() {
        return null;
    }
}
