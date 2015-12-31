package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.utils.JsonUtils;
import com.wecook.sdk.api.model.base.Selector;

import org.json.JSONException;

/**
 * Created by simon on 15/9/4.
 */
public class FoodTopMenu extends Selector {
    /**
     * id
     */
    @SerializedName("id")
    private String id;
    /**
     * 名称
     */
    @SerializedName("name")
    private String name;
    /**
     * imgUrl
     */
    @SerializedName("icon")
    private String icon;
    /**
     * 文字搜索的url
     */
    @SerializedName("url")
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("id")) {
                JsonElement item = object.get("id");
                if (!item.isJsonNull()) {
                    id = item.getAsString();
                }
            }
            if (object.has("name")) {
                JsonElement item = object.get("name");
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
            if (object.has("url")) {
                JsonElement item = object.get("url");
                if (!item.isJsonNull()) {
                    url = item.getAsString();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "FoodTopMenu{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
