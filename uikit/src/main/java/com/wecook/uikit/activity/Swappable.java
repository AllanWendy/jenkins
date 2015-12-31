package com.wecook.uikit.activity;

import android.os.Bundle;

/**
 * 可换页
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public interface Swappable {

    public void next(SwapCard currentCard, SwapCard card, Bundle data);

    public boolean back(Bundle data);

    public void setFixed(boolean isFixed);

    public boolean isFixed();

    public void finishAll();
}
