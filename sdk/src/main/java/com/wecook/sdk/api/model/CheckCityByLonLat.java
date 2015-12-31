package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * Created by LK on 2015/10/13.
 */
public class CheckCityByLonLat extends ApiModel {
    private City resultCity;//状态码


    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonObject(json)) {
            JsonElement element = JsonUtils.getJsonElement(json);
            if (element != null && element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.has("result") && getStatusState() == 1) {
                    resultCity = JsonUtils.getModelItemAsObject(jsonObject.getAsJsonObject("result"), "city", new City());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "CheckCityByLonLat{" +
                ", resultCity=" + resultCity +
                '}';
    }
}
