package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.sdk.api.model.CookShowScoreResult;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 点赞服务
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/6/14
 */
public class YummyApi extends Api {

    /**
     * 评论
     */
    public static final String TYPE_COMMENT = "comment";

    /**
     * 晒厨艺
     */
    public static final String TYPE_COOKING = "cooking";

    /**
     * 为点赞
     *
     * @param type
     * @param uid
     * @param foreignId
     * @param callback
     */
    public static void addPraise(String type, String foreignId, ApiCallback<State> callback) {
        Api.get(YummyApi.class)
                .with("/praise/create")
                .addParams("type", type, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("foreign_id", foreignId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }


    /**
     * 晒厨艺评分获取
     */
    public static void updateScore(String type, String foreignId, String score, ApiCallback<CookShowScoreResult> callback) {
        Api.get(YummyApi.class)
                .with("/praise/create")
                .addParams("type", type, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("foreign_id", foreignId, true)
                .addParams("score", score)
                .toModel(new CookShowScoreResult())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 取消点赞
     *
     * @param type
     * @param uid
     * @param foreignId
     * @param callback
     */
    public static void removePraise(String type, String foreignId, ApiCallback<State> callback) {
        Api.get(YummyApi.class)
                .with("/praise/cancel")
                .addParams("type", type, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("foreign_id", foreignId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }
}
