package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Preferential;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 获取优惠相关
 * Created by simon on 15/9/9.
 */
public class PreferentialApi {
    /**
     * 获得地址列表
     *
     * @param callback
     */
    public static void getPreferential(String code, ApiCallback<Preferential> callback) {
        Api.get(AddressApi.class)
                .with("/popularize/join")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("code", code, true)
                .toModel(new Preferential())
                .setApiCallback(callback)
                .executeGet();
    }


}
