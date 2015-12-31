package com.wecook.sdk.api.model.base;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;

/**
 * 收藏数据
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/28/14
 */
public abstract class Favorite extends ApiModel implements Favorable {

    @SerializedName("is_favourite")
    public String isFavourite;

    public boolean isFav() {
        if ("0".equals(isFavourite)) {
            return false;
        } else if ("1".equals(isFavourite)) {
            return true;
        }
        return false;
    }

    public void setFav(boolean isFavourite) {
        this.isFavourite = (isFavourite ? "1" : "0");
    }

    public void copyFavorite(Favorite from, Favorite to) {
        to.isFavourite = from.isFavourite;
    }
}
