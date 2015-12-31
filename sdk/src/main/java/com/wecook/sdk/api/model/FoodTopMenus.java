package com.wecook.sdk.api.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * Created by simon on 15/9/4.
 */
public class FoodTopMenus extends ApiModel {

    @SerializedName("list")
    private ApiModelList<FoodTopMenu> list;

    public ApiModelList<FoodTopMenu> getList() {
        return list;
    }

    public void setList(ApiModelList<FoodTopMenu> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "FoodTopMenus{" +
                "list=" + list +
                '}';
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonObject object = JsonUtils.getJsonObject(json);
        if (object.has("list")) {
            list = new ApiModelList<FoodTopMenu>(new FoodTopMenu());
            list.parseJson(object.getAsJsonArray("list").toString());
        }
    }
}
