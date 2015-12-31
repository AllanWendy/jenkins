package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 *   最近购买菜品
 * Created by simon on 15/9/21.
 */
public class RecentlyDishes extends ApiModel {
    /**
     * 最近购买总数
     */
    private String count;

    private ApiModelList<RecentlyDish> recentlyDishList;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();

            if (object.has("count")) {
                JsonElement item = object.get("count");
                if (!item.isJsonNull()) {
                    count = item.getAsString();
                }
            }
            if (object.has("list")) {
                recentlyDishList = new ApiModelList<RecentlyDish>(new RecentlyDish());
                recentlyDishList.parseJson(object.get("list").toString());
            }

        }
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public ApiModelList<RecentlyDish> getRecentlyDishList() {
        return recentlyDishList;
    }

    public void setRecentlyDishList(ApiModelList<RecentlyDish> recentlyDishList) {
        this.recentlyDishList = recentlyDishList;
    }
}
