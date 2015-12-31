package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * Created by LK on 2015/10/13.
 */
public class HomeFeedList<T extends ApiModel> extends ApiModelList<T> {
    private String moreTitle;
    private String moreUrl;

    public HomeFeedList(T model) {
        super(model);
    }

    @Override
    public void parseJson(String json) throws JSONException {
        super.parseJson(json);
        if (JsonUtils.isJsonObject(json)) {
            JsonElement element = JsonUtils.getJsonElement(json);
            if (element != null && element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.has("show_more_link") && getStatusState() == 1) {
                    moreTitle = JsonUtils.getModelItemAsString(jsonObject.getAsJsonObject("show_more_link"), "title");
                    moreUrl = JsonUtils.getModelItemAsString(jsonObject.getAsJsonObject("show_more_link"), "url");
                }
            }
        }
    }

    public String getMoreTitle() {
        return moreTitle;
    }

    public String getMoreUrl() {
        return moreUrl;
    }
}
