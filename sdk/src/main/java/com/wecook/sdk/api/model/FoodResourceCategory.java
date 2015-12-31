package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 菜谱资源分类数据
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/14/14
 */
public class FoodResourceCategory extends ApiModel {

    @SerializedName("name")
    private String categoryName;

    private ApiModelList<FoodResource> list;

    @Override
    public void parseJson(String json) throws JSONException {
        if(JsonUtils.isJsonObject(json)) {
            JSONObject jsonObject = JsonUtils.getJSONObject(json);
            categoryName = jsonObject.optString("name");
            if (jsonObject.has("rows")) {
                list = new ApiModelList<FoodResource>(new FoodResource());
                list.parseJson(jsonObject.getJSONArray("rows").toString());
            }
        }

    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ApiModelList<FoodResource> getList() {
        return list;
    }

    public void setList(ApiModelList<FoodResource> list) {
        this.list = list;
    }
}
