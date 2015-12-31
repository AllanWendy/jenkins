package com.wecook.sdk.api.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;

/**
 * 坐标
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/20/14
 */
public class Location extends ApiModel {

    /**
     * 是否在服务范围内
     */
    private boolean enableDish;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {

            if (element.isJsonArray()) {
                JsonArray jsonArray = element.getAsJsonArray();
                if (jsonArray != null) {
                    longitude = "" + jsonArray.get(0).getAsString();
                    latitude = "" + jsonArray.get(1).getAsString();
                }
            } else if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.has("is_dishes")) {
                    int dishCode = jsonObject.get("is_dishes").getAsInt();
                    enableDish = (dishCode == 1);
                }

                if (jsonObject.has("lat")) {
                    latitude = jsonObject.get("lat").getAsString();
                }

                if (jsonObject.has("lng")) {
                    longitude = jsonObject.get("lng").getAsString();
                } else if (jsonObject.has("lon")) {
                    longitude = jsonObject.get("lon").getAsString();
                }
            }
        }

    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public boolean isEnableDish() {
        return enableDish;
    }

    public void setEnableDish(boolean enableDish) {
        this.enableDish = enableDish;
    }

    public boolean effective() {
        return !StringUtils.isEmpty(longitude)
                && !StringUtils.isEmpty(latitude)
                && Double.compare(Double.parseDouble(longitude), 0) != 0
                && Double.compare(Double.parseDouble(latitude), 0) != 0;
    }
}
