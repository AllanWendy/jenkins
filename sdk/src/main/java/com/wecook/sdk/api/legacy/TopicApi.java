package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.api.model.PartyDetail;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 专题活动相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class TopicApi extends Api {

    /**
     * 活动列表
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getActivityList(int page, int pageSize, ApiCallback<ApiModelList<Party>> callback) {
        String uid = StringUtils.isEmpty(UserProperties.getUserId()) ? "0" : UserProperties.getUserId();
        Api.get(TopicApi.class)
                .with("/events")
                .addParams("uid", uid)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .toModel(new ApiModelList<Party>(new Party()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_HOUR)
                .executeGet();
    }

    /**
     * 活动详情
     * @param partyId
     * @param apiCallback
     */
    public static void getActivityDetail(int partyId, ApiCallback<PartyDetail> apiCallback) {
        String uid = StringUtils.isEmpty(UserProperties.getUserId()) ? "0" : UserProperties.getUserId();
        Api.get(TopicApi.class)
                .with("/events/detail")
                .addParams("uid", uid)
                .addParams("id", partyId + "", true)
                .toModel(new PartyDetail())
                .setApiCallback(apiCallback)
                .setCacheTime(CACHE_ONE_HOUR)
                .executeGet();
    }

    /**
     * 文章列表
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getArticleList(int page, int pageSize, ApiCallback<ApiModelList<Topic>> callback) {
        Api.get(TopicApi.class)
                .with("/topic")
                .addParams("uid", UserProperties.getUserId())
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .toModel(new ApiModelList<Topic>(new Topic()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_HOUR)
                .executeGet();
    }

}
