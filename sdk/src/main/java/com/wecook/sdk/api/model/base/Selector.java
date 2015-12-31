package com.wecook.sdk.api.model.base;

import com.wecook.common.core.internet.ApiModel;

/**
 * TODO
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public abstract class Selector extends ApiModel implements Selectable {

    private boolean isSelected;

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
