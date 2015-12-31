package com.wecook.uikit.loader;

import android.content.Context;
import android.os.Bundle;

/**
 * TODO
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/5/14
 */
public interface IUIAsyncTasker {

    public boolean addUILoader(UILoader loader);
    public void restartUILoaders();
    public void removeUILoader(UILoader loader);
    public void removeUILoader(int id);
    public void stopAllUILoaders();
    public void startAllUILoaders();
    public void startUILoader(UILoader loader, Bundle args);
    public void deliveryFinish();

    public Context getContext();

    /**
     * 开始加载界面数据
     */
    public void onStartUILoad();

    /**
     * 停止加载界面数据
     */
    public void onStopUILoad();

    /**
     * 完成数据加载
     * @param success
     */
    public void onFinishedToUpdateUI(boolean success);

    /**
     * 打印界面树结构
     * @return
     */
    public String dumpUITree();

}
