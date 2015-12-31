package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 食材资源营养元素
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/16/14
 */
public class FoodResourceElements extends ApiModel {
    @SerializedName("name")
    private String name;

    @SerializedName("val")
    private String value;

    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonObject(json)) {
            JSONObject jsonObject = JsonUtils.getJSONObject(json);
            name = jsonObject.optString("name");
            value = jsonObject.optString("val");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
