package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 评论数量概览
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/13
 */
public class CommentCount extends ApiModel {

    @SerializedName("type_0")
    private String all;

    @SerializedName("type_1")
    private String good;

    @SerializedName("type_2")
    private String middle;

    @SerializedName("type_3")
    private String bad;

    @SerializedName("type_avg")
    private String average;
    /**
     * 菜品质量评分
     */
    private String type_dishes;
    /**
     * 物流服务评分
     */
    private String type_delivery;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type_0")) {
                all = object.get("type_0").getAsString();
            }
            if (object.has("type_1")) {
                good = object.get("type_1").getAsString();
            }
            if (object.has("type_2")) {
                middle = object.get("type_2").getAsString();
            }
            if (object.has("type_3")) {
                bad = object.get("type_3").getAsString();
            }
            if (object.has("type_avg")) {
                average = object.get("type_avg").getAsString();
            }
            if (object.has("type_dishes")) {
                type_dishes = object.get("type_dishes").getAsString();
            }
            if (object.has("type_delivery")) {
                type_delivery = object.get("type_delivery").getAsString();
            }

        }
    }

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }

    public String getBad() {
        return bad;
    }

    public void setBad(String bad) {
        this.bad = bad;
    }

    public String getGood() {
        return good;
    }

    public void setGood(String good) {
        this.good = good;
    }

    public String getMiddle() {
        return middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

    public String getType_dishes() {
        return type_dishes;
    }

    public void setType_dishes(String type_dishes) {
        this.type_dishes = type_dishes;
    }

    public String getType_delivery() {
        return type_delivery;
    }

    public void setType_delivery(String type_delivery) {
        this.type_delivery = type_delivery;
    }
}
