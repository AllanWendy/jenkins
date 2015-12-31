package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 步骤
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodStep extends ApiModel {

    @SerializedName("text")
    private String text = "";
    @SerializedName("img")
    private String img;

    private String localImage = "";

    private boolean isOnlyText;

    private ApiModelList<Image> picture;

    private Media media;

    public FoodStep() {
    }

    public FoodStep(String text) {
        this.text = text;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        Gson gson = new Gson();
        FoodStep step = gson.fromJson(json, FoodStep.class);
        if (step != null) {
            text = step.text;
        }
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("image")) {
            picture = new ApiModelList<Image>(new Image());
            picture.parseJson(json);
        }

        if (jsonObject.has("media")) {
            media = new Media();
            media.parseJson(jsonObject.opt("media").toString());
        } else {
            isOnlyText = true;
        }
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();

            if (object.has("text")) {
                JsonElement item = object.get("text");
                if (!item.isJsonNull()) {
                    text = item.getAsString();
                }
            }
            if (object.has("img")) {
                JsonElement item = object.get("img");
                if (!item.isJsonNull()) {
                    img = item.getAsString();
                }
            }
        }

    }

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.optJSONArray("step");
    }

    public boolean isOnlyText() {
        return isOnlyText;
    }

    public void setOnlyText(boolean isOnlyText) {
        this.isOnlyText = isOnlyText;
    }

    public String getLocalImage() {
        return localImage;
    }

    public void setLocalImage(String localImage) {
        this.localImage = localImage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ApiModelList<Image> getPicture() {
        return picture;
    }

    public void setPicture(ApiModelList<Image> picture) {
        this.picture = picture;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }


    public String getOnlineImageUrl() {
        String onlineImageUrl = "";
        if (picture != null
                && picture.getItem(0) != null
                && !StringUtils.isEmpty(picture.getItem(0).getUrl())) {
            onlineImageUrl = picture.getItem(0).getUrl();
        } else if(media != null
                && !StringUtils.isEmpty(media.getImage())) {
            onlineImageUrl = media.getImage();
        }
        return onlineImageUrl;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
