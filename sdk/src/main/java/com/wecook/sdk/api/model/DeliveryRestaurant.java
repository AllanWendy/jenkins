package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 配送时间餐厅
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/29
 */
public class DeliveryRestaurant extends ApiModel {
    /**
     * 味库配送
     */
    public static String EXPRESS_BY_WECOOK = "1";
    /**
     * 商家配送
     */
    public static String EXPRESS_BY_RESTAURANT = "2";


    private String id;
    private String express_by;// 1.味库配送 2.商家自行配送

    private ApiModelList<DeliveryDate> deliveryDateList;

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

            if (object.has("delivery_times")) {
                deliveryDateList = new ApiModelList<>(new DeliveryDate());
                deliveryDateList.parseJson(object.get("delivery_times").toString());
            }
            express_by = JsonUtils.getModelItemAsString(object, "express_by");
        }
    }

    public ApiModelList<DeliveryDate> getDeliveryDateList() {
        return deliveryDateList;
    }

    public void setDeliveryDateList(ApiModelList<DeliveryDate> deliveryDateList) {
        this.deliveryDateList = deliveryDateList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * 配送类型
     *
     * @return
     */
    public String getExpress_by() {
        return express_by;
    }
}
