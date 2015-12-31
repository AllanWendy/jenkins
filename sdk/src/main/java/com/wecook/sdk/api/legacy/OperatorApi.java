package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.sdk.api.model.RecommendInfo;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 运营API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/8/14
 */
public class OperatorApi extends Api {

    public static void getRecommendInfo(String lat, String lon, ApiCallback<RecommendInfo> callback) {
        Api.get(OperatorApi.class)
                .with("/operation/home_2_5")
                .addParams("uid", UserProperties.getUserId())
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .toModel(new RecommendInfo())
                .setApiCallback(callback)
                .setCacheTime(CACHE_TEN_MINUTE)
                .executeGet();
    }
}
