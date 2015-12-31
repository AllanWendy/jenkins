package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.DeliveryRestaurantList;
import com.wecook.sdk.api.model.Order;
import com.wecook.sdk.api.model.OrderCount;
import com.wecook.sdk.api.model.OrderDetail;
import com.wecook.sdk.api.model.OrderPaySign;
import com.wecook.sdk.api.model.OrderPaymentInfo;
import com.wecook.sdk.api.model.RecentlyDishes;
import com.wecook.sdk.api.model.ShareState;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.base.StringModel;
import com.wecook.sdk.userinfo.UserProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单相关API
 *
 * @author droid
 * @version v2.3.7
 * @since 2015.5.10
 */
public class OrderApi extends Api {

    public static final int ORDER_CHECK_STATUS_STOCK_EMPTY = 0;//库存不足
    public static final int ORDER_CHECK_STATUS_NORMAL = 1;//正常
    public static final int ORDER_CHECK_STATUS_ZERO_PAY = 2;//0元消费

    public static final int PAYMENT_TYPE_WECHAT = 1;//微信
    public static final int PAYMENT_TYPE_ALIPAY = 2;//支付宝
    public static final int PAYMENT_TYPE_CRASH = 3;//到付

    /**
     * 待付款
     */
    public static final int STATUS_ORDER_WAIT_PAY = 1;
    /**
     * 已付款，等待餐厅确定
     */
    public static final int STATUS_ORDER_PAYED_WAIT_CHECK = 2;
    /**
     * 餐厅已确定，等待物流配送
     */
    public static final int STATUS_ORDER_CHECKED_WAIT_TRANSPORT = 3;
    /**
     * 物流配送成功，等待评价
     */
    public static final int STATUS_ORDER_TRANSPORTED_WAIT_EVALUATE = 6;
    /**
     * 已评价
     */
    public static final int STATUS_ORDER_EVALUATED = 8;
    /**
     * 已取消
     */
    public static final int STATUS_ORDER_CANCELED = 0;
    /**
     * 已退款
     */
    public static final int STATUS_ORDER_REFUNDED = 10;

    /**
     * 获得订单数量
     *
     * @param callback
     */
    public static void getOrderCount(ApiCallback<OrderCount> callback) {
        Api.get(OrderApi.class)
                .with("/order/count")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new OrderCount())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得配送时间列表
     *
     * @param restaurantListIds
     * @param callback
     */
    public static void getDeliveryTimeList(String restaurantListIds,
                                           ApiCallback<DeliveryRestaurantList> callback) {
        Api.get(OrderApi.class)
                .with("/order/restaurant_delivery_times")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("restaurant_ids", restaurantListIds, true)
                .toModel(new DeliveryRestaurantList())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 订单列表
     *
     * @param page     页数
     * @param pageSize 每页条数
     * @param status   订单状态，没传则查询全部。1.等待付款 2.付款成功,等待餐厅确认 3.商家已确认,等待送菜到家
     *                 6.配送成功 8.已评价 0.已取消订单 10.已退款
     * @param callback
     */
    public static void orderList(int page, int pageSize, String status, ApiCallback<ApiModelList<Order>> callback) {
        Api.get(OrderApi.class)
                .with("/order/index")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("status", status + "")
                .toModel(new ApiModelList<Order>(new Order()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_MINUTE)
                .executeGet();
    }

    /**
     * 订单详情
     *
     * @param orderId  订单ID
     * @param callback
     */
    public static void orderDetail(String orderId, ApiCallback<OrderDetail> callback) {
        Api.get(OrderApi.class)
                .with("/order/detail")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("id", orderId, true)
                .toModel(new OrderDetail())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 订单红包
     *
     * @param orderId
     * @param callback
     */
    public static void orderRedPacket(String orderId, ApiCallback<ShareState> callback) {
        Api.get(OrderApi.class)
                .with("/order/bonus_share")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .toModel(new ShareState())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 订单确定收货
     *
     * @param orderId  订单ID
     * @param callback
     */
    public static void orderReceived(String orderId, ApiCallback<State> callback) {
        Api.get(OrderApi.class)
                .with("/order/update_complete")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 订单取消原因列表
     */
    public static void orderCancelReasonList(ApiCallback<ApiModelList<StringModel>> callback) {
        Api.get(OrderApi.class)
                .with("/order/cancel_reason")
                .isHttps(true)
                .toModel(new ApiModelList<StringModel>(new StringModel()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_WEEK)
                .executeGet();
    }

    /**
     * 取消订单
     *
     * @param orderId  订单ID
     * @param reason   订单取消原因
     * @param callback
     */
    public static void orderCancel(String orderId, String reason, ApiCallback<State> callback) {
        Api.get(OrderApi.class)
                .with("/order/cancel")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .addParams("content", reason)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 取消订单确定
     *
     * @param orderId  订单ID
     * @param callback
     */
    public static void orderCancelConfirm(String orderId, ApiCallback<State> callback) {
        Api.get(OrderApi.class)
                .with("/order/cancel_confirm")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 创建支付任务
     *
     * @param orderId     订单ID
     * @param paymentType 支付方式1.微信 2.支付宝 3.到付
     * @param wallet      所使用的钱包余额，没有则传'0'
     * @param callback
     */
    public static void orderCreatePayTask(String orderId, int paymentType, String wallet,
                                          ApiCallback<State> callback) {
        Api.get(OrderApi.class)
                .with("/order/paytask")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .addParams("payment_type", paymentType + "", true)
                .addParams("wallet", wallet, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得订单支付信息
     *
     * @param orderId
     * @param callback
     */
    public static void orderPayInfo(String orderId, ApiCallback<OrderPaymentInfo> callback) {
        Api.get(OrderApi.class)
                .with("/order/pay")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .toModel(new OrderPaymentInfo())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 再下一单
     *
     * @param orderId
     * @param callback
     */
    public static void orderAgain(String orderId, ApiCallback<State> callback) {
        Api.get(OrderApi.class)
                .with("/order/order_again")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 创建订单
     *
     * @param addressId          地址ID
     * @param couponId           优惠券ID
     * @param restaurants        餐厅下菜品列表
     * @param logistics_store_id 自提点ID
     * @param wallet             菜金
     * @param callback
     */
    public static void createOrder(String addressId, String couponId, String wallet, String logistics_store_id,
                                   List<ShopCartRestaurant> restaurants, ApiCallback<State> callback) {
        String restaurantIds = "";
        String cartIds = "";
        List<String> deliveryTimes = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        float totalDelivery = 0;

        for (ShopCartRestaurant restaurant : restaurants) {
            restaurantIds += restaurant.getId() + ",";
            List<ShopCartDish> dishList = restaurant.getCheckoutDishes();
            for (ShopCartDish dish : dishList) {
                cartIds += dish.getCartId() + ",";
            }

            deliveryTimes.add(restaurant.getDeliveryTime().getFullTime());
            notes.add(restaurant.getRemarkContent());
            totalDelivery += restaurant.getDeliveryPrice();
        }

        if (!StringUtils.isEmpty(cartIds)) {
            cartIds = cartIds.substring(0, cartIds.length() - 1);
        }

        if (!StringUtils.isEmpty(restaurantIds)) {
            restaurantIds = restaurantIds.substring(0, restaurantIds.length() - 1);
        }

        if (StringUtils.isEmpty(wallet)) {
            wallet = "0";
        }
        Api api = Api.get(OrderApi.class)
                .with("/order/create_2_5_3")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("address_id", addressId, true)
                .addParams("cart_ids", cartIds, true)
                .addParams("restaurant_ids", restaurantIds, true)
                .addParams("delivery_time", deliveryTimes, true)
                .addParams("wallet", wallet, true)
                .addParams("note", notes)
                .addParams("fare", "" + totalDelivery)
                .addParams("coupon_id", couponId);
        if (!StringUtils.isEmpty(logistics_store_id)) {
            api.addParams("logistics_store_id", logistics_store_id);
        }
        api.toModel(new State())
                .setApiCallback(callback)
                .executePost();
    }

    /**
     * 订单签名
     *
     * @param taskId   商户订单号
     * @param callback
     */
    public static void getOrderSign(String taskId, ApiCallback<OrderPaySign> callback) {
        Api.get(OrderApi.class)
                .with("/order/paysign")
                .isHttps(true)
                .addParams("id", taskId, true)
                .toModel(new OrderPaySign())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得最近购买菜品
     *
     * @param page
     * @param batch
     * @param callback
     */
    public static void getOrderRecently(int page, int batch, ApiCallback<RecentlyDishes> callback) {
        Api.get(OrderApi.class)
                .with("/order/recently_dishes")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", page + "", true)
                .addParams("batch", batch + "", true)
                .toModel(new RecentlyDishes())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 确认支付任务状态
     *
     * @param orderId
     * @param callback
     */
    public static void checkPayStatus(String orderId, ApiCallback<State> callback) {
        Api.get(OrderApi.class)
                .with("/order/pay_status")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }
}
