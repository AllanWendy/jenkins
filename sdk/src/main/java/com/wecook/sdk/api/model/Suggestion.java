package com.wecook.sdk.api.model;

import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 投诉建议model
 * Created by simon on 15/9/20.
 */
public class Suggestion extends ApiModel {

    private String info;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            JSONObject jsonObject = new JSONObject(json);
            info = jsonObject.optString("info");
        }
    }
}
