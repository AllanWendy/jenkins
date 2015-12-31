package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 保障
 * Created by simon on 15/9/20.
 */
public class Security extends ApiModel {
    /**
     * 保障的名称
     */
    private String name;
    /**
     * 描述
     */
    private String desc;
    /**
     * icon
     */
    private String icon;
    /**
     * 内容
     */
    private String content;


    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("text")) {
                JsonElement item = object.get("text");
                if (!item.isJsonNull()) {
                    name = item.getAsString();
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
            if (object.has("content")) {
                JsonElement item = object.get("content");
                if (!item.isJsonNull()) {
                    content = item.getAsString();
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
