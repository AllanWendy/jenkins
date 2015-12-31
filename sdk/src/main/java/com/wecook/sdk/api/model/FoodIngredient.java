package com.wecook.sdk.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 主料
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodIngredient extends FoodResource {

    private String pinyin;
    private String category;

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.optJSONArray("ingredients");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FoodIngredient) {
            return getName().equals(((FoodIngredient) o).getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
