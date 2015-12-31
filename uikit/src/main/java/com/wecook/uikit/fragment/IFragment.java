// Copy right WeCook Inc.
package com.wecook.uikit.fragment;

import com.wecook.uikit.loader.IUIAsyncTasker;

/**
 * 
 * @author 	kevin
 * @since 	2014-Sep 17, 2014
 * @version	v1.0
 */
public interface IFragment extends FragmentLifecycleListener, IUIAsyncTasker{

    /**
     * 注册生命周期监听
     * @param listener
     */
    public void registerFragmentLifecycleListener(FragmentLifecycleListener listener);

    /**
     * 取消生命周期监听
     * @param listener
     */
    public void unregisterFragmentLifecycleListener(FragmentLifecycleListener listener);

    /**
     * 判断是否完成数据加载
     * @return
     */
    public boolean isDataLoaded();
}
