package com.wecook.sdk.api.model;

import com.wecook.common.core.internet.ApiModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ID功能
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/6/14
 */
public class ID extends ApiModel {
    public String id;
    public String url;

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        id = jsonObject.optString("id");
        url = jsonObject.optString("url");
    }
}
