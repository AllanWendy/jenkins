package com.wecook.uikit.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * 换页卡
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/29/15
 */
public abstract class SwapCard extends Fragment implements Swappable {

    protected boolean isFixed;

    public Fragment getFragment() {
        return this;
    }

    public void onCardIn(Bundle data) {

    }

    public void onCardOut() {

    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean isFixed) {
        this.isFixed = isFixed;
    }
}
