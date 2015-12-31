package com.wecook.common.modules;

import android.content.Context;

/**
 * 抽象模块
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public abstract class BaseModule implements IModule {
    private Context mContext;

    /**
     * {@inheritDoc}
     * @param context {@inheritDoc}
     */
    @Override
    public void setup(Context context) {
        mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override public void release() {
        //nothing
    }

    public Context getContext() {
        return mContext;
    }
}
