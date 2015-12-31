package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 退款订单状态
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/4
 */
public class OrderRefundState extends ApiModel {

    //1:正在退款 2:退款成功
    private String status;
    private String text;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);

        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("status")) {
                JsonElement item = object.get("status");
                if (!item.isJsonNull()) {
                    status = item.getAsString();
                }
            }
            if (object.has("text")) {
                JsonElement item = object.get("text");
                if (!item.isJsonNull()) {
                    text = item.getAsString();
                }
            }
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
