package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.utils.JsonUtils;
import com.wecook.sdk.api.model.base.Selector;

import org.json.JSONException;

/**
 * 配送时间
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/24
 */
public class DeliveryTime extends Selector {
    private DeliveryDate date;

    private String time;

    private String fullTime;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("text")) {
                JsonElement item = object.get("text");
                if (!item.isJsonNull()) {
                    time = item.getAsString();
                }
            }
            if (object.has("value")) {
                JsonElement item = object.get("value");
                if (!item.isJsonNull()) {
                    fullTime = item.getAsString();
                }
            }
        }

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFullTime() {
        return fullTime;
    }

    /**
     * 格式化日期
     *
     * @return
     */
    public String getFormatTime() {
        if (null != date && null != time) {
            return (date.getData() + " " + time).trim();
        }
        return fullTime;
    }

    public void setFullTime(String fullTime) {
        this.fullTime = fullTime;
    }

    public DeliveryDate getDate() {
        return date;
    }

    public void setDate(DeliveryDate date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return this.fullTime.equals(((DeliveryTime) o).fullTime);
    }
}
