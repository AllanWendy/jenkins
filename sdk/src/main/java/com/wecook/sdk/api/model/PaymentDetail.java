package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * Created by LK on 2015/10/15.
 */
public class PaymentDetail extends ApiModel {
    private String source_type;//收入，退款，消费
    private String desc;//订单号
    private String time;//时间
    private String money;//金额


    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonObject(json)) {
            JsonElement element = JsonUtils.getJsonElement(json);
            if (element != null && element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                money = JsonUtils.getModelItemAsString(jsonObject, "money");
                time = JsonUtils.getModelItemAsString(jsonObject, "time");
                source_type = JsonUtils.getModelItemAsString(jsonObject, "source_type");
                desc = JsonUtils.getModelItemAsString(jsonObject, "desc");
            }
        }
    }

    public String getSourceType() {
        return source_type;
    }

    public String getDesc() {
        return desc;
    }


    public String getTime() {
        return time;
    }


    public String getMoney() {
        return money;
    }

}
