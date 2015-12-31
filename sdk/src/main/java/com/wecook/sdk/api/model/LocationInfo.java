package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 坐标
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/20/14
 */
public class LocationInfo extends ApiModel {

    /**
     * 是否在服务范围内
     */
    private String name;
    private String address;
    private Location location;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            name = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "name");
            address = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "address");
            location = JsonUtils.getModelItemAsObject(element.getAsJsonObject(), "location", new Location());
        }

    }

}
