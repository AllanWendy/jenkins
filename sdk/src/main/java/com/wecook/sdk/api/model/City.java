package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.utils.JsonUtils;
import com.wecook.sdk.api.model.base.Selector;

import org.json.JSONException;

/**
 * 选择地域
 * Created by shan on 2015/8/31.
 */
public class City extends Selector {
    /**
     * 是否可选；0为不可选 1为可选
     */
    @SerializedName("status")
    private int status;
    /**
     * 详情内容
     */
    @SerializedName("delivery_range_txt")
    private String detailContent;
    /**
     * 图标的url
     */
    @SerializedName("icon")
    private String iconUrl;
    /**
     * 名称的拼音
     */
    @SerializedName("index")
    private String index;
    /**
     * 名称
     */
    @SerializedName("name")
    private String name;
    @SerializedName("selected")
    private int selected;

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetailContent() {
        return detailContent;
    }

    public void setDetailContent(String detailContent) {
        this.detailContent = detailContent;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("status")) {
                JsonElement item = object.get("status");
                if (!item.isJsonNull()) {
                    status = item.getAsInt();
                }
            }
            if (object.has("delivery_range_txt")) {
                JsonElement item = object.get("delivery_range_txt");
                if (!item.isJsonNull()) {
                    detailContent = item.getAsString();
                }
            }
            if (object.has("icon")) {
                JsonElement item = object.get("icon");
                if (!item.isJsonNull()) {
                    iconUrl = item.getAsString();
                }
            }
            if (object.has("index")) {
                JsonElement item = object.get("index");
                if (!item.isJsonNull()) {
                    index = item.getAsString();
                }
            }
            if (object.has("name")) {
                JsonElement item = object.get("name");
                if (!item.isJsonNull()) {
                    name = item.getAsString();
                }
            }
        }

    }

    @Override
    public String toString() {
        return "City{" +
                "status=" + status +
                ", detailContent='" + detailContent + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", index='" + index + '\'' +
                ", name='" + name + '\'' +
                ", selected=" + selected +
                '}';
    }
}
