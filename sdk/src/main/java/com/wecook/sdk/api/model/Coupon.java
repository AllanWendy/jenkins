package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.utils.JsonUtils;
import com.wecook.sdk.api.model.base.Selector;

import org.json.JSONException;

/**
 * 优惠券
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/24
 */
public class Coupon extends Selector {

    private String money = "0";
    private String desc;
    private String id;
    private String subDesc;
    private String color;
    private String expiry_date;//过期时间
    private String status;//0.已经过期 2.已经使用 1.可以使用

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("id")) {
                JsonElement item = object.get("id");
                if (!item.isJsonNull()) {
                    id = item.getAsString();
                }
            }
            if (object.has("title")) {
                JsonElement item = object.get("title");
                if (!item.isJsonNull()) {
                    desc = item.getAsString();
                }
            }
            if (object.has("sub_title")) {
                JsonElement item = object.get("sub_title");
                if (!item.isJsonNull()) {
                    subDesc = item.getAsString();
                }
            }
            if (null == subDesc || "".equals(subDesc)) {
                JsonElement item = object.get("subtitle");
                if (!item.isJsonNull()) {
                    subDesc = item.getAsString();
                }
            }
            if (object.has("color")) {
                JsonElement item = object.get("color");
                if (!item.isJsonNull()) {
                    color = item.getAsString();
                }
            }
            if (object.has("voucher_amount")) {
                JsonElement item = object.get("voucher_amount");
                if (!item.isJsonNull()) {
                    money = item.getAsString();
                }
            }
            if (object.has("expiry_date")) {
                JsonElement item = object.get("expiry_date");
                if (!item.isJsonNull()) {
                    expiry_date = item.getAsString();
                }
            }
            if (object.has("status")) {
                JsonElement item = object.get("status");
                if (!item.isJsonNull()) {
                    status = item.getAsString();
                }
            }
        }
    }

    public float getMoney() {
        return Float.parseFloat(money);
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSubDesc() {
        return subDesc;
    }

    public void setSubDesc(String subDesc) {
        this.subDesc = subDesc;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "money='" + money + '\'' +
                ", desc='" + desc + '\'' +
                ", id='" + id + '\'' +
                ", subDesc='" + subDesc + '\'' +
                ", expiry_date='" + expiry_date + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
