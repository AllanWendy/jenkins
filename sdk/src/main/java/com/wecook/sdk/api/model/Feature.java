package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 软件功能
 *
 * @author kevin
 * @version v1.0
 * @since 2015-3/6/15
 */
public class Feature extends ApiModel {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonObject(json)) {
            Feature feature = JsonUtils.getModel(json, Feature.class);
            if (feature != null) {
                id = feature.id;
                title = feature.title;
                image = feature.image;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
