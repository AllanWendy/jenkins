package com.wecook.sdk.api.model.base;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 基础数据
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/30
 */
public class DataModel extends ApiModel {

    private JsonElement element;

    @Override
    public void parseJson(String json) throws JSONException {
        element = JsonUtils.getJsonElement(json);
    }

    public JsonElement getElement() {
        return element;
    }

    public JsonElement findValue(String key) {
        return findValue(element, key);
    }

    private JsonElement findValue(JsonElement element, String key) {
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if(object.has(key)){
                    return object.get(key);
                }
            } else if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    return findValue(array.get(i), key);
                }
            }
        }
        return null;
    }
}
