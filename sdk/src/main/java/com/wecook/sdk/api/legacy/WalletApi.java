package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.Coupon;
import com.wecook.sdk.api.model.PaymentDetail;
import com.wecook.sdk.api.model.base.DataModel;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 钱包API
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/24
 */
public class WalletApi extends Api {

    /**
     * 可用优惠券列表
     *
     * @param callback
     */
    public static void getAvailableCouponList(int page, int pageSize, String total, ApiCallback<ApiModelList<Coupon>> callback) {
        Api.get(WalletApi.class)
                .with("/coupon/list_available")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("payment", total, true)
                .toModel(new ApiModelList<Coupon>(new Coupon()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 失效优惠券列表
     *
     * @param callback
     */
    public static void getDisableCouponList(int page, int pageSize, ApiCallback<ApiModelList<Coupon>> callback) {
        Api.get(WalletApi.class)
                .with("/coupon/list_disabled")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .toModel(new ApiModelList<Coupon>(new Coupon()))
                .setApiCallback(callback)
                .executeGet();
    }


    /**
     * 获得钱包余额
     *
     * @param callback
     */
    public static void getWalletRemainder(ApiCallback<DataModel> callback) {
        Api.get(WalletApi.class)
                .with("/wallet/my")
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new DataModel())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得菜金流水
     *
     * @param page     页码
     * @param batch    条目
     * @param callback
     */
    public static void getWalletIndex(String page, String batch, ApiCallback<ApiModelList<PaymentDetail>> callback) {
        Api.get(WalletApi.class)
                .with("/wallet/index")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", page, true)
                .addParams("batch", batch, true)
                .toModel(new ApiModelList<PaymentDetail>(new PaymentDetail()))
                .setApiCallback(callback)
                .executeGet();
    }
}
