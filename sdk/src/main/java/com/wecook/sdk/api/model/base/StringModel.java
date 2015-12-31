package com.wecook.sdk.api.model.base;

import com.google.gson.JsonElement;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 字符串
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/30
 */
public class StringModel extends ApiModel {

    private String content;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonPrimitive()) {
                content = element.getAsString();
            }
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
