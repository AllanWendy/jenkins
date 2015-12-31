package com.wecook.uikit.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.uikit.activity.IActivity;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.IFragment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 界面异步更新任务实现
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/5/14
 */
public class UIAsyncTaskerImpl implements IUIAsyncTasker {

    private Set<UILoader> mLoaderQueue = Collections.synchronizedSet(new HashSet<UILoader>());

    private LoaderManager mSupportLoaderManager;
    private IFragment mFragment;
    private IActivity mFragmentActivity;

    public UIAsyncTaskerImpl(BaseFragment fragment) {
        mFragment = fragment;
        mSupportLoaderManager = fragment.getLoaderManager();
    }

    public UIAsyncTaskerImpl(IActivity activity) {
        mFragmentActivity = activity;
        mSupportLoaderManager = activity.getSupportLoaderManager();
    }

    private boolean checkAllLoadersSuccess() {
        for (UILoader loader : mLoaderQueue) {
            if (!loader.mSuccessed) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAllLoadersFinished() {
        for (UILoader loader : mLoaderQueue) {
            if (!loader.mFinished) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAllLoadersCalled() {
        for (UILoader loader : mLoaderQueue) {
            if (!loader.mCalled) {
                return false;
            }
        }
        return true;
    }

    /**
     * 添加
     *
     * @param loader
     */
    public boolean addUILoader(UILoader loader) {
        if (mLoaderQueue.contains(loader)) {
            return true;
        }
        return mLoaderQueue.add(loader);
    }

    /**
     * 移除UILoader线程
     *
     * @param loader
     */
    public void removeUILoader(UILoader loader) {
        if (loader != null) {
            removeUILoader(loader.getId());
        }
    }

    public void stopAllUILoaders() {
        for (UILoader loader : mLoaderQueue) {
            removeUILoader(loader);
        }
        mLoaderQueue.clear();
    }

    /**
     * 移除ID UILoader线程
     *
     * @param id
     */
    public void removeUILoader(int id) {
        if (mSupportLoaderManager != null) {
            mSupportLoaderManager.destroyLoader(id);
        }
    }

    /**
     * 开始所有的异步加载
     */
    public void restartUILoaders() {
        for (UILoader loader : mLoaderQueue) {
            loader.reset();
        }
        startAllUILoaders();
    }


    /**
     * 初始化所有异步Loader
     */
    public void startAllUILoaders() {
        if (mSupportLoaderManager != null && mSupportLoaderManager.hasRunningLoaders()) {
            return;
        }
        for (UILoader loader : mLoaderQueue) {
            if (!loader.mCalled) {
                if (mSupportLoaderManager != null) {
                    mSupportLoaderManager.initLoader(loader.getId(), null, loader.getSupportLoaderCallback());
                }
            }
        }
    }


    @Override
    public final void startUILoader(final UILoader loader, final Bundle args) {
        /*TODO 是否要保持当前运行线程只有一个，不共存？ && mSupportLoaderManager.hasRunningLoaders()*/
        if (mSupportLoaderManager == null) {
            return;
        }
        if (AndroidUtils.isUIThread()) {
            startUILoaderOnUIThread(loader, args);
        } else {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    startUILoaderOnUIThread(loader, args);
                }
            });
        }
    }

    private void startUILoaderOnUIThread(UILoader loader, Bundle args) {
        if (addUILoader(loader)) {
            if (!loader.mCalled || loader.isRepeatCalled()) {
                if (loader.isRepeatCalled()) {
                    mSupportLoaderManager.restartLoader(loader.getId(), args, loader.getSupportLoaderCallback());
                } else {
                    mSupportLoaderManager.initLoader(loader.getId(), args, loader.getSupportLoaderCallback());
                }
            }
        }
    }

    @Override
    public void deliveryFinish() {
        if (checkAllLoadersFinished()) {
            if (mFragment != null) {
                mFragment.onFinishedToUpdateUI(checkAllLoadersSuccess());
            } else if (mFragmentActivity != null) {
                mFragmentActivity.onFinishedToUpdateUI(checkAllLoadersSuccess());
            }
        }
    }

    @Override
    public Context getContext() {
        if (mFragment != null) {
            return mFragment.getContext().getApplicationContext();
        } else if (mFragmentActivity != null) {
            return mFragmentActivity.getContext().getApplicationContext();
        }
        return null;
    }

    @Override
    public void onStartUILoad() {
        //nothing
    }

    @Override
    public void onStopUILoad() {
        //nothing
    }

    @Override
    public void onFinishedToUpdateUI(boolean success) {
        //nothing
    }

    @Override
    public String dumpUITree() {
        return null;
    }

}
