package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 订单详情
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/5
 */
public class OrderDetail extends ApiModel {

    private String systemTime;

    private Order order;

    private Address address;

    private Coupon coupon;

    private OrderRefundState orderRefundState;

    private ShareState shareState;

    private ApiModelList<OrderState> orderState;


    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();

            if (object.has("contact")) {
                address = new Address();
                address.parseJson(object.get("contact").toString());
            }

            if (object.has("coupon")) {
                coupon = new Coupon();
                coupon.parseJson(object.get("coupon").toString());
            }

            if (object.has("refund")) {
                orderRefundState = new OrderRefundState();
                orderRefundState.parseJson(object.get("refund").toString());
            }

            if (object.has("detail")) {
                order = new Order();
                order.parseJson(object.get("detail").toString());
            }

            if (object.has("time")) {
                JsonElement item = object.get("time");
                if (!item.isJsonNull()) {
                    systemTime = item.getAsString();
                }
            }

            if (object.has("share")) {
                shareState = new ShareState();
                shareState.parseJson(object.get("share").toString());
            }
            if (object.has("change_log")) {
                orderState = new ApiModelList<OrderState>(new OrderState());
                orderState.parseJson(object.get("change_log").toString());
            }

            if (order != null && orderRefundState != null) {
                order.setRefundState(orderRefundState);
            }
        }

    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ApiModelList<OrderState> getOrderState() {
        return orderState;
    }

    public void setOrderState(ApiModelList<OrderState> orderState) {
        this.orderState = orderState;
    }

    public OrderRefundState getOrderRefundState() {
        return orderRefundState;
    }

    public void setOrderRefundState(OrderRefundState orderRefundState) {
        this.orderRefundState = orderRefundState;
    }

    public String getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(String systemTime) {
        this.systemTime = systemTime;
    }

    public boolean isUseWallet() {
        return false;
    }

    public double getUsedWallet() {
        return 23;
    }

    public ShareState getShareState() {
        return shareState;
    }

    public void setShareState(ShareState shareState) {
        this.shareState = shareState;
    }
}
