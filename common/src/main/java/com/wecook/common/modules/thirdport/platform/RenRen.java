package com.wecook.common.modules.thirdport.platform;

import android.content.Context;
import android.content.Intent;

import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.modules.thirdport.platform.base.BasePlatform;


/**
 * 人人网
 *
 * @author zhoulu
 * @date 13-12-13
 */
public class RenRen extends BasePlatform {
    public RenRen(Context context) {
        super(context);
    }


    @Override
    public boolean isSupportSSOShare() {
        return false;
    }

    @Override
    public boolean isSupportWebShare() {
        return false;
    }

    @Override
    public boolean isInstalledApp() {
        return false;
    }

    @Override
    public boolean onCreate() {
        return false;
    }


    @Override
    public void onLogin() {

    }

    @Override
    public void onLogout() {

    }

    @Override
    public boolean onEvent(Context context, Intent intent) {
        return false;
    }

    @Override
    public void onShare(IShareObject... shareObject) {

    }

    @Override
    public boolean shareValidateCheck(IShareObject... shareObject) {
        return false;
    }

}
