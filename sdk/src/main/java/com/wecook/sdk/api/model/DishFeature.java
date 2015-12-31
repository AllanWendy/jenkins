package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 菜品特色
 */
public class DishFeature extends ApiModel {
    /**
     * 文本
     */
    private String text;

    /**
     * 图片
     */
    private String image;

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null && element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            if (jsonObject.has("text")) {
                JsonElement item = jsonObject.get("text");
                if (!item.isJsonNull()) {
                    text = item.getAsString();
                }
            }

            if (jsonObject.has("img")) {
                JsonElement item = jsonObject.get("img");
                if (!item.isJsonNull()) {
                    image = item.getAsString();
                }
            }
        }
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
