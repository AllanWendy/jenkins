package com.wecook.common.modules;

import android.content.Context;

/**
 * 模块接口
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public interface IModule {

    /**
     * 设置
     * @param context
     */
    public void setup(Context context);

    /**
     * 释放
     */
    public void release();
}
