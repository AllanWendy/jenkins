package com.wecook.common.modules.thirdport;

import com.wecook.common.modules.thirdport.platform.base.IPlatform;

/**
 * 简单实现平台事件监听
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/3/14
 */
public class SimplePlatformEventListener implements IPlatform.IPlatformEventListener {
    @Override
    public void onResponseShare(IPlatform platform, boolean success, String message) {

    }

    @Override
    public void onResponseLogin(IPlatform platform, boolean success, String message) {

    }

    @Override
    public void onResponseLogout(IPlatform platform, boolean success, String message) {

    }

    @Override
    public void onResponsePay(IPlatform platform, boolean success, String message) {

    }
}
