package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 分享数据状态
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/9/7
 */
public class ShareState extends ApiModel {

    private String title;
    private String content;
    private String icon;
    private String link;

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {

            JsonObject shareItem = element.getAsJsonObject();
            JsonObject object = shareItem;

            if (shareItem.has("share")) {
                object = shareItem.get("share").getAsJsonObject();
            }

            if (object.has("title")) {
                JsonElement item = object.get("title");
                if (!item.isJsonNull()) {
                    title = item.getAsString();
                }
            }
            if (object.has("content")) {
                JsonElement item = object.get("content");
                if (!item.isJsonNull()) {
                    content = item.getAsString();
                }
            }
            if (object.has("icon")) {
                JsonElement item = object.get("icon");
                if (!item.isJsonNull()) {
                    icon = item.getAsString();
                }
            }
            if (object.has("link")) {
                JsonElement item = object.get("link");
                if (!item.isJsonNull()) {
                    link = item.getAsString();
                }
            }

        }

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "ShareState{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", icon='" + icon + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
