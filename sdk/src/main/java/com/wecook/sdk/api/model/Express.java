package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * 配送类型
 * Created by LK on 2015/9/22.
 */
public class Express extends ApiModel {
    private String title;

    private String text;

    private String icon;

    private String value;

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("express_type_list")) {
                    return new JSONArray(object.get("express_type_list").toString());
                }
            }
        }
        return super.findJSONArray(json);
    }

    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonObject(json)) {
            JsonElement element = JsonUtils.getJsonElement(json);
            if (element != null && element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                title = JsonUtils.getModelItemAsString(jsonObject, "title");
                text = JsonUtils.getModelItemAsString(jsonObject, "text");
                icon = JsonUtils.getModelItemAsString(jsonObject, "icon");
                value = JsonUtils.getModelItemAsString(jsonObject, "value");
            }
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Express{" +
                "title='" + title + '\'' +
                ", icon='" + icon + '\'' +
                ", text='" + text + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
