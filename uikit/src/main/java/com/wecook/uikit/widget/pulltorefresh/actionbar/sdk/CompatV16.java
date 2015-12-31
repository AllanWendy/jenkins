package com.wecook.uikit.widget.pulltorefresh.actionbar.sdk;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

class CompatV16 {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static void postOnAnimation(View view, Runnable runnable) {
        view.postOnAnimation(runnable);
    }

}
