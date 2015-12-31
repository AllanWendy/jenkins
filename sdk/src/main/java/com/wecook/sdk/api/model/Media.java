package com.wecook.sdk.api.model;

import com.wecook.common.core.internet.ApiModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 媒体资源数据
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/6/14
 */
public class Media extends ApiModel {

    private String id;
    private String url;
    private String image;

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        id = jsonObject.optString("id");
        url = jsonObject.optString("url");
        image = jsonObject.optString("image");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
