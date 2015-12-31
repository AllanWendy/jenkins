package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.utils.JsonUtils;
import com.wecook.sdk.api.model.base.Selector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 选择地域
 * Created by shan on 2015/8/31.
 */
public class SelectCity extends Selector {
    @SerializedName("city")
    private City city;

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = JsonUtils.getJSONObject(json);
        if (jsonObject.has("city")) {
            city = new City();
            city.parseJson(jsonObject.get("city").toString());
        }

    }

    @Override
    public String toString() {
        return "SelectCity{" +
                "city=" + city +
                '}';
    }
}
