package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 社交账号绑定状态
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/2
 */
public class UserBindState extends ApiModel {

    private String platformName;
    private String socialId;

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("type")) {
                    platformName = object.get("type").getAsString();
                }
                if (object.has("foreign_id")) {
                    socialId = object.get("foreign_id").getAsString();
                }
            }
        }
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

}
