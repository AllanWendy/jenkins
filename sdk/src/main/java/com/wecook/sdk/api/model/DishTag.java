package com.wecook.sdk.api.model;

import android.graphics.Color;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 菜品标签
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/14
 */
public class DishTag extends ApiModel {

    public static final int TYPE_RED = 1;
    public static final int TYPE_GREEN = 2;
    public static final int TYPE_ORANGE = 3;

    @SerializedName("type")
    private String type;

    @SerializedName("description")
    private String desc;

    @SerializedName("text")
    private String name;

    @SerializedName("bg_color")
    private String bgColor;

    @SerializedName("color")
    private String color;

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                type = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "type");
                desc = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "description");
                name = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "text");
                color = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "color");
                bgColor = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "bg_color");
            } else if (element.isJsonPrimitive()) {
                name = json;
            }
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        try {
            return Color.parseColor(color);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getBgColor() {
        try {
            return Color.parseColor(bgColor);
        } catch (Exception e) {
            return Color.RED;
        }
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    @Override
    public String toString() {
        return "DishTag{" +
                "type='" + type + '\'' +
                ", desc='" + desc + '\'' +
                ", name='" + name + '\'' +
                ", bgColor='" + bgColor + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
