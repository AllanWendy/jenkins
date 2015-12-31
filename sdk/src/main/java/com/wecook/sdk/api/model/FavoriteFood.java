package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.sdk.api.model.base.Favorite;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 收藏数据类型
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/28/14
 */
public class FavoriteFood extends Favorite {

    @SerializedName("id")
    private String id;

    @SerializedName("create_time")
    private String createTime;

    private Food food;


    @Override
    public void parseJson(String json) throws JSONException {
        Gson gson = new Gson();
        FavoriteFood favoriteFood = gson.fromJson(json, FavoriteFood.class);
        if (favoriteFood != null) {
            id = favoriteFood.id;
            createTime = favoriteFood.createTime;
        }

        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("recipe")) {
            food = new Food();
            food.parseJson(jsonObject.optJSONObject("recipe").toString());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }
}
