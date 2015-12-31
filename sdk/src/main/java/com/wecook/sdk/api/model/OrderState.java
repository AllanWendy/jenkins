package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 订单状态
 * Created by simon on 15/9/19.
 */
public class OrderState extends ApiModel {

    private String title;
    private String icon;
    private String desc;
    private String time;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("title")) {
                JsonElement item = object.get("title");
                if (!item.isJsonNull()) {
                    title = item.getAsString();
                }
            }
            if (object.has("icon")) {
                JsonElement item = object.get("icon");
                if (!item.isJsonNull()) {
                    icon = item.getAsString();
                }
            }
            if (object.has("desc")) {
                JsonElement item = object.get("desc");
                if (!item.isJsonNull()) {
                    desc = item.getAsString();
                }
            }
            if (object.has("time")) {
                JsonElement item = object.get("time");
                if (!item.isJsonNull()) {
                    time = item.getAsString();
                }
            }
        }
    }
}
