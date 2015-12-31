package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.base.Favorite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 展示栏模型
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class Banner extends Favorite {

    public static final String TYPE_TOPIC = "topic";
    public static final String TYPE_PARTY = "events";

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("image")
    public String image;

    @SerializedName("url")
    public String url;

    private String type;
    private String foreignId;
    private String commentCount;

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("rows")) {
                    return new JSONArray(object.get("rows").toString());
                }
            }
        }
        return super.findJSONArray(json);
    }

    @Override
    public void parseJson(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            Gson gson = new Gson();
            Banner banner = gson.fromJson(json, Banner.class);
            if (banner != null) {
                id = banner.id;
                title = banner.title;
                url = banner.url;
                image = banner.image;
            }

            JSONObject jsonObject = JsonUtils.getJSONObject(json);
            if (jsonObject != null && jsonObject.has("extends")) {
                JSONObject extendsObj = jsonObject.getJSONObject("extends");
                type = extendsObj.optString("type");
                foreignId = extendsObj.optString("foreign_id");
                commentCount = extendsObj.optString("comment");
                isFavourite = extendsObj.optString("is_favourite");
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public int getCommentCount() {
        return StringUtils.parseInt(commentCount);
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = "" + commentCount;
    }

    @Override
    public String toString() {
        return "Banner{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", foreignId='" + foreignId + '\'' +
                ", commentCount='" + commentCount + '\'' +
                '}';
    }
}
