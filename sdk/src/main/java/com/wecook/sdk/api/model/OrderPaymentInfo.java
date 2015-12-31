package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 订单费用信息
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/21
 */
public class OrderPaymentInfo extends ApiModel {
    private String orderId;//订单号
    private String taskId;//支付流水号
    private String price;//总价
    private String fare;//运费
    private String discount;//优惠券使用额
    private String crashCost;//需付金额
    private String wallet;//菜金使用额
    private String realPay;//实付

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);

        if (element != null && element.isJsonObject()) {
            orderId = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "id");
            taskId = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "task_id");
            price = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "price");
            fare = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "fare");
            discount = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "discount");
            crashCost = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "payment");
            wallet = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "wallet");
            realPay = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "real_pay");
        }
    }

    public double getCrashCost() {
        return Double.parseDouble(crashCost);
    }

    public double getDiscount() {
        return Double.parseDouble(discount);
    }

    public double getFare() {
        return Double.parseDouble(fare);
    }

    public String getOrderId() {
        return orderId;
    }

    public double getPrice() {
        return Double.parseDouble(price);
    }

    public String getTaskId() {
        return taskId;
    }

    public double getWallet() {
        return Double.parseDouble(wallet);
    }

    public double getRealPay() {
        return Double.parseDouble(realPay);
    }
}
