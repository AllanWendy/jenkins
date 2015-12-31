package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.Category;
import com.wecook.sdk.api.model.Suggestion;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 投诉商家
 * Created by simon on 15/9/20.
 */
public class SuggestionApi extends Api {
    /**
     * 获得分类列表
     *
     * @param callback
     */
    public static void sendSuggestion(String orderId, String mobile, String content, ApiCallback<Suggestion> callback) {
        Api.get(SuggestionApi.class)
                .with("/order/complain")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .addParams("mobile", mobile, true)
                .addParams("content", content, true)
                .toModel(new Suggestion())
                .setApiCallback(callback)
                .executeGet();
    }
}
