package com.wecook.sdk.api.model;

import android.graphics.Color;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 运营项
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/17
 */
public class OperatorItem extends ApiModel {


    private String id;
    private String title;
    private String subTitle;
    private String image;
    private String color;
    private String url;

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("id")) {
                id = object.get("id").getAsString();
            }
            if (object.has("name")) {
                title = object.get("name").getAsString();
            }
            if (object.has("sub_name")) {
                subTitle = object.get("sub_name").getAsString();
            }
            if (object.has("color")) {
                color = object.get("color").getAsString();
            }
            if (object.has("icon")) {
                image = object.get("icon").getAsString();
            }
            if (object.has("url")) {
                url = object.get("url").getAsString();
            }
        }
    }

    public int getColor() {
        try {
            return Color.parseColor(color);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
