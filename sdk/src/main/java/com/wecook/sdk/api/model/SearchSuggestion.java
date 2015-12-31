package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 搜索建议
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/31/14
 */
public class SearchSuggestion extends ApiModel {

    private String id;
    private String name;
    private String image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if(element.isJsonPrimitive()){
                name = json;
            } else if (element.isJsonObject()) {
                if (element.getAsJsonObject().has("id")) {
                    id = element.getAsJsonObject().get("id").getAsString();
                }
                if (element.getAsJsonObject().has("title")) {
                    name = element.getAsJsonObject().get("title").getAsString();
                }
                if (element.getAsJsonObject().has("image")) {
                    image = element.getAsJsonObject().get("image").getAsString();
                }
            }
        }
    }

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if(jsonObject.has("recipes")) {
            return jsonObject.optJSONArray("recipes");
        }
        return super.findJSONArray(json);
    }


}
