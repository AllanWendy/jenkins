package com.wecook.sdk.api.model;

import com.wecook.common.core.internet.ApiModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 优惠相关
 * Created by simon on 15/9/9.
 */
public class Preferential extends ApiModel {
    private String info;
    private String url;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        info = jsonObject.getString("info");
        url = jsonObject.getString("url");
    }
}
