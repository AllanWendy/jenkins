package com.wecook.sdk.api.model;

import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 数量
 *
 * @author kevin
 * @version v1.0
 * @since 2015-3/9/15
 */
public class Count extends ApiModel {

    public int count;
    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("count")) {
            String countString = jsonObject.optString("count");
            count = StringUtils.parseInt(countString);
        }
    }
}
