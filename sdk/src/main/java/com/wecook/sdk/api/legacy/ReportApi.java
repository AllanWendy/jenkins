package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 举报
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/5/15
 */
public class ReportApi extends Api {

    /**
     * 评论
     */
    public static final String TYPE_COMMENT = "comment";

    /**
     * 晒厨艺
     */
    public static final String TYPE_COOKING = "cooking";

    /**
     * 举报不良
     *
     * @param type
     * @param id
     * @param callback
     */
    public static void reportIssue(String type, String id, ApiCallback<State> callback) {
        Api.get(ReportApi.class)
                .with("/report/create")
                .addParams("type", type, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("foreign_id", id, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }
}
