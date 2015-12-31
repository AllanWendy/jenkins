package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 餐厅配送时间列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/29
 */
public class DeliveryRestaurantList extends ApiModel {

    private boolean isTogether;
    private String express_by;
    private String express_notice;

    private ApiModelList<DeliveryRestaurant> deliveryRestaurantList;

    private ApiModelList<DeliveryDate> deliveryDateList;

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("together")) {
                JsonElement item = object.get("together");
                if (!item.isJsonNull()) {
                    isTogether = ("1".equals(item.getAsString()));
                }
            }
            if (object.has("restaurants")) {
                deliveryRestaurantList = new ApiModelList<>(new DeliveryRestaurant());
                deliveryRestaurantList.parseJson(object.get("restaurants").toString());
            }

            if (object.has("delivery_times")) {
                deliveryDateList = new ApiModelList<>(new DeliveryDate());
                deliveryDateList.parseJson(object.get("delivery_times").toString());
            }

            express_by = JsonUtils.getModelItemAsString(object, "express_by");
            express_notice = JsonUtils.getModelItemAsString(object, "express_notice");
        }
    }

    public ApiModelList<DeliveryRestaurant> getDeliveryRestaurantList() {
        return deliveryRestaurantList;
    }

    public void setDeliveryRestaurantList(ApiModelList<DeliveryRestaurant> deliveryRestaurantList) {
        this.deliveryRestaurantList = deliveryRestaurantList;
    }

    public boolean isTogether() {
        return isTogether;
    }

    public void setIsTogether(boolean isTogether) {
        this.isTogether = isTogether;
    }

    public ApiModelList<DeliveryDate> getDeliveryDateList() {
        return deliveryDateList;
    }

    public void setDeliveryDateList(ApiModelList<DeliveryDate> deliveryDateList) {
        this.deliveryDateList = deliveryDateList;
    }

    /**
     * 配送类型
     *
     * @return
     */
    public String getExpress_by() {
        return express_by;
    }

    /**
     * 通知
     *
     * @return
     */
    public String getExpress_notice() {
        return express_notice;
    }
}
