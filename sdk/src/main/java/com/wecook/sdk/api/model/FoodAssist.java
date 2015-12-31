package com.wecook.sdk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 辅料
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodAssist extends FoodResource {
    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.optJSONArray("assist");
    }
}
