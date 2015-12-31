package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.sdk.api.model.base.Selector;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 配送时间
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/24
 */
public class DeliveryDate extends Selector {

    private String data;
    private List<DeliveryTime> deliveryTimes;

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object != null && object.has("delivery_times")) {
                return new JSONArray(object.get("delivery_times").toString());
            }
        }
        return super.findJSONArray(json);
    }

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("date")) {
                JsonElement item = object.get("date");
                if (!item.isJsonNull()) {
                    data = item.getAsString();
                }
            }

            if (object.has("time")) {
                JsonElement item = object.get("time");
                if (!item.isJsonNull()) {
                    ApiModelList<DeliveryTime> times = new ApiModelList<>(new DeliveryTime());
                    times.parseJson(item.toString());
                    deliveryTimes = new ArrayList<>();
                    deliveryTimes.addAll(times.getList());
                }
            }
        }
        if (null != deliveryTimes) {
            for (DeliveryTime deliveryTime : deliveryTimes) {
                deliveryTime.setDate(this);
            }
        }
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<DeliveryTime> getDeliveryTimes() {
        return deliveryTimes;
    }

    public void setDeliveryTimes(List<DeliveryTime> deliveryTimes) {
        this.deliveryTimes = deliveryTimes;
    }
}
