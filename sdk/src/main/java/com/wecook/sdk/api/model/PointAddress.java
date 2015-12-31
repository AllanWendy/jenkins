package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 自提点
 * Created by LK on 2015/11/12.
 */
public class PointAddress extends Address {
    private String subName;
    private String businessHour;//营业时间
    private String distance;//距离
    private String unit;//单位

    @Override
    public void parseJson(String json) throws JSONException {
        super.parseJson(json);
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                subName = JsonUtils.getModelItemAsString(object, "sub_name");
                businessHour = JsonUtils.getModelItemAsString(object, "business_hour");
                distance = JsonUtils.getModelItemAsString(object, "distance");
                unit = JsonUtils.getModelItemAsString(object, "unit");
            }
        }
    }

    public String getSubName() {
        return subName;
    }

    public String getBusinessHour() {
        return businessHour;
    }

    public String getDistance() {
        return distance;
    }

    public String getUnit() {
        return unit;
    }

    public String getFullName() {
        if (null != subName && !"".equals(subName)) {
            return getName() + "(" + subName + ")";
        }
        return getName();
    }

    /**
     * 带单位距离
     *
     * @return
     */
    public String getFullDistance() {
        if (null != unit && !"".equals(unit)) {
            return getDistance() + " " + unit;
        }
        return getDistance();
    }
}
