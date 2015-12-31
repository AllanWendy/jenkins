package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 最近购买菜品（属性）
 * Created by simon on 15/9/21.
 */
public class RecentlyDish extends ApiModel {
    /**
     * 订单号
     */
    private String id;
    /**
     * 菜品名称
     */
    private String title;
    /**
     * 购买时间
     */
    private String buy_time;
    /**
     * 图片
     */
    private String image;
    /**
     * 商店
     */
    private Restaurant restaurant;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();

            if (object.has("id")) {
                JsonElement item = object.get("id");
                if (!item.isJsonNull()) {
                    id = item.getAsString();
                }
            }
            if (object.has("title")) {
                JsonElement item = object.get("title");
                if (!item.isJsonNull()) {
                    title = item.getAsString();
                }
            }
            if (object.has("buy_time")) {
                JsonElement item = object.get("buy_time");
                if (!item.isJsonNull()) {
                    buy_time = item.getAsString();
                }
            }
            if (object.has("image")) {
                JsonElement item = object.get("image");
                if (!item.isJsonNull()) {
                    image = item.getAsString();
                }
            }

            if (object.has("restaurant")) {
                restaurant = new Restaurant();
                restaurant.parseJson(object.get("restaurant").toString());
            }

        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBuy_time() {
        return buy_time;
    }

    public void setBuy_time(String buy_time) {
        this.buy_time = buy_time;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
