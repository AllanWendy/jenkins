package com.wecook.sdk.api.model;

import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 首页推荐
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/7/14
 */
public class RecommendDish extends ApiModel {

    private String url;

    private ApiModelList<Dish> dishList;

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject object = JsonUtils.getJSONObject(json);

        if (object != null) {
            url = object.optString("more");

            if (object.has("list")) {
                dishList = new ApiModelList<Dish>(new Dish());
                dishList.parseJson(object.optJSONArray("list").toString());
            }
        }

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ApiModelList<Dish> getDishList() {
        return dishList;
    }

}
