package com.wecook.uikit.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.wecook.common.modules.asynchandler.SimpleSync;
import com.wecook.common.modules.asynchandler.SyncHandler;

/**
 * 界面加载器
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/5/14
 */
public abstract class UILoader<T> extends SimpleSync<T>{

    Context mContext;
    AsyncTaskLoader mSupportLoader;
    boolean mCalled;
    boolean mFinished;
    boolean mSuccessed;
    boolean mRepeat = true;
    int mId = -99;
    IUIAsyncTasker mAsyncTasker;

    public UILoader(IUIAsyncTasker tasker) {
        this(tasker, -99);
    }

    public UILoader(IUIAsyncTasker tasker, int id) {
        mContext = tasker.getContext();
        mAsyncTasker = tasker;
        mId = id;
    }

    public void setRepeatCalled(boolean repeat) {
        mRepeat = repeat;
    }

    public boolean isRepeatCalled() {
        return mRepeat;
    }

    void reset() {
        mCalled = false;
        mFinished = false;
        mSuccessed = false;
    }

    /**
     * 后台耗时操作
     * @return
     */
    public T runBackground(){
        return SyncHandler.sync(this);
    }

    /**
     * 更新界面
     * @param data
     */
    public void postUI(T data) {
        if (mListener != null) {
            mListener.onLoaded(data);
        }
    }

    /**
     *
     * @return
     */
    public Loader<T> getSupportLoader() {
        mSupportLoader = new AsyncTaskLoader<T>(mContext) {
            @Override
            public T loadInBackground() {
                return runBackground();
            }
        };
        mSupportLoader.setUpdateThrottle(500);
        return mSupportLoader;
    }

    LoaderManager.LoaderCallbacks<T> getSupportLoaderCallback() {
        return new LoaderManager.LoaderCallbacks<T>() {
            @Override
            public Loader<T> onCreateLoader(int id, Bundle args) {
                mCalled = true;
                Loader<T> loader = getSupportLoader();
                loader.forceLoad();
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<T> loader, final T data) {
                postUI(data);
                mFinished = true;
                mSuccessed = (data != null);
                loader.stopLoading();
                if (mAsyncTasker != null) {
                    mAsyncTasker.removeUILoader(getId());
                    mAsyncTasker.deliveryFinish();
                }
            }

            @Override
            public void onLoaderReset(Loader<T> loader) {

            }
        };
    }

    public int getId() {
        if (mId == -99) {
            return mId = hashCode();
        }
        return mId;
    }

    public void start(Bundle bundle) {
        if (mAsyncTasker != null) {
            mAsyncTasker.startUILoader(this, bundle);
        }
    }

    @Override
    public void sync(Callback<T> callback) {

    }

    private UILoaderListener<T> mListener;

    void setUILoaderListener(UILoaderListener<T> listener) {
        mListener = listener;
    }

    class RepeatAsyncTaskLoader extends AsyncTaskLoader<T> {

        public RepeatAsyncTaskLoader(Context context) {
            super(context);
        }

        @Override
        public T loadInBackground() {
            return null;
        }
    }

    interface UILoaderListener<T> {
        public void onLoaded(T o);
    }
}
