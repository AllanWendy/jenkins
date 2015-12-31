package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.OrderApi;
import com.wecook.sdk.api.model.base.Selector;

import org.json.JSONException;

/**
 * 订单信息
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/30
 */
public class Order extends Selector {

    /**
     * 支付金额
     */
    private String payment;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 订单创建时间
     */
    private String createTime;

    /**
     * 订单内菜品数量
     */
    private String count;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 支付方式
     */
    private String paymentType;

    /**
     * 优惠价钱
     */
    private String discount;

    /**
     * 配送方式
     */
    private String deliveryType;

    /**
     * 配送费用
     */
    private String deliveryPrice;

    /**
     * 剩余支付时间，以“秒”为单位
     */
    private String delayPayTime;

    /**
     * 订单中餐厅菜品列表
     */
    private ApiModelList<ShopCartRestaurant> restaurantList;

    private OrderRefundState refundState;
    /**
     * 保障
     */
    private String security;
    /**
     * 配送信息
     */
    private String express_by;

    /**
     * 订单展示Id
     */
    private String order_id_show;

    /**
     * 现金支付金额
     */
    private String crashPay;

    /**
     * 实付金额
     */
    private String realPay;

    /**
     * 菜金
     */
    private String wallet;

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("payment")) {
                JsonElement item = object.get("payment");
                if (!item.isJsonNull()) {
                    payment = item.getAsString();
                }
            }
            crashPay = JsonUtils.getModelItemAsString(object, "no_payment_cash");
            if (object.has("status")) {
                JsonElement item = object.get("status");
                if (!item.isJsonNull()) {
                    status = item.getAsString();
                }
            }
            if (object.has("create_time")) {
                JsonElement item = object.get("create_time");
                if (!item.isJsonNull()) {
                    createTime = item.getAsString();
                }
            }
            if (object.has("count")) {
                JsonElement item = object.get("count");
                if (!item.isJsonNull()) {
                    count = item.getAsString();
                }
            }
            if (object.has("order_id")) {
                JsonElement item = object.get("order_id");
                if (!item.isJsonNull()) {
                    orderId = item.getAsString();
                }
            }

            if (object.has("remain_pay_time")) {
                JsonElement item = object.get("remain_pay_time");
                if (!item.isJsonNull()) {
                    delayPayTime = item.getAsString();
                }
            }

            if (object.has("order_id_show")) {
                JsonElement item = object.get("order_id_show");
                if (!item.isJsonNull()) {
                    order_id_show = item.getAsString();
                }
            }

            if (object.has("payment_type")) {
                JsonElement item = object.get("payment_type");
                if (!item.isJsonNull()) {
                    paymentType = item.getAsString();
                }
            }

            if (object.has("discount")) {
                JsonElement item = object.get("discount");
                if (!item.isJsonNull()) {
                    discount = item.getAsString();
                }
            }

            if (object.has("delivery_type")) {
                JsonElement item = object.get("delivery_type");
                if (!item.isJsonNull()) {
                    deliveryType = item.getAsString();
                }
            }

            if (object.has("delivery_price")) {
                JsonElement item = object.get("delivery_price");
                if (!item.isJsonNull()) {
                    deliveryPrice = item.getAsString();
                }
            }

            if (object.has("restaurants")) {
                restaurantList = new ApiModelList<>(new ShopCartRestaurant());
                restaurantList.parseJson(object.get("restaurants").toString());
            }

            if (object.has("refund")) {
                refundState = new OrderRefundState();
                refundState.parseJson(object.get("refund").toString());
            }
            if (object.has("security_text")) {
                JsonElement item = object.get("security_text");
                if (!item.isJsonNull()) {
                    security = item.getAsString();
                }
            }
            if (object.has("express_by_text")) {
                JsonElement item = object.get("express_by_text");
                if (!item.isJsonNull()) {
                    express_by = item.getAsString();
                }
            }
            realPay = JsonUtils.getModelItemAsString(object, "real_pay");
            wallet = JsonUtils.getModelItemAsString(object, "wallet");

        }

    }


    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getCreateTime() {
        return StringUtils.formatTime(StringUtils.parseLong(createTime), "yyyy-MM-dd HH:mm");
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPayment() {
        return payment;
    }

    public String getRealPay() {
        return realPay;
    }

    public void setRealPay(String realPay) {
        this.realPay = realPay;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public ApiModelList<ShopCartRestaurant> getRestaurantList() {
        return restaurantList;
    }

    public void setRestaurantList(ApiModelList<ShopCartRestaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    public int getStatus() {
        return StringUtils.parseInt(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusString() {
        if ((OrderApi.STATUS_ORDER_WAIT_PAY + "").equals(status)) {
            return "待付款";
        }
        if ((OrderApi.STATUS_ORDER_PAYED_WAIT_CHECK + "").equals(status)) {
            return "付款成功";
        }
        if ((OrderApi.STATUS_ORDER_CHECKED_WAIT_TRANSPORT + "").equals(status)) {
            return "商家已确定";
        }
        if ((OrderApi.STATUS_ORDER_TRANSPORTED_WAIT_EVALUATE + "").equals(status)) {
            return "已收货";
        }

        if (refundState != null) {
            if ("1".equals(refundState.getStatus())) {
                return "退款中";
            } else if ("2".equals(refundState.getStatus())) {
                return "退款成功";
            }
        } else {
            if ((OrderApi.STATUS_ORDER_CANCELED + "").equals(status)) {
                return "已取消";
            }
        }

        return "";
    }

    public String getStatusDesc() {
        if ((OrderApi.STATUS_ORDER_WAIT_PAY + "").equals(status)) {
            return "订单已提交，等待付款";
        }
        if ((OrderApi.STATUS_ORDER_PAYED_WAIT_CHECK + "").equals(status)) {
            return "付款成功，等待商家确认";
        }
        if ((OrderApi.STATUS_ORDER_CHECKED_WAIT_TRANSPORT + "").equals(status)) {
            return "商家已确认，等待收货";
        }
        if ((OrderApi.STATUS_ORDER_TRANSPORTED_WAIT_EVALUATE + "").equals(status)) {
            return "已收货，给个评价吧";
        }
        if (refundState != null) {
            if ("1".equals(refundState.getStatus())) {
                return "订单已取消";
            } else if ("2".equals(refundState.getStatus())) {
                return "退款成功";
            }
        } else {
            if ((OrderApi.STATUS_ORDER_CANCELED + "").equals(status)) {
                return "订单已取消";
            }
        }
        return "";
    }

    public String getDeliveryPrice() {
        return deliveryPrice;
    }

    public void setDeliveryPrice(String deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getDiscount() {
        if (null == discount || "".equals(discount)) {
            return "0";
        }
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getPaymentType() {
        if ("1".equals(paymentType)) {
            return "在线支付";
        } else if ("2".equals(paymentType)) {
            return "在线支付";
        } else {
            return "其他支付";
        }
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public long getDelayPayTime() {
        return StringUtils.parseLong(delayPayTime);
    }

    public void setDelayPayTime(long delayPayTime) {
        this.delayPayTime = "" + delayPayTime;
    }

    public OrderRefundState getRefundState() {
        return refundState;
    }

    public void setRefundState(OrderRefundState refundState) {
        this.refundState = refundState;
    }

    public String getExpress_by() {
        return express_by;
    }

    public void setExpress_by(String express_by) {
        this.express_by = express_by;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getOrder_id_show() {
        return order_id_show;
    }

    public String getCrashPay() {
        return crashPay;
    }

    public String getWallet() {
        if (null == wallet || wallet.equals("")) {
            return "0";
        }
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }
}
