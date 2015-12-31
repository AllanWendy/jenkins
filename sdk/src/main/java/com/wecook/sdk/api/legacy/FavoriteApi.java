package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.api.model.base.Favorite;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 收藏相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class FavoriteApi extends Api {

    public static final String TYPE_RECIPE = "recipe";
    public static final String TYPE_TOPIC = "topic";
    public static final String TYPE_PARTY = "events";

    /**
     * 各类我的收藏列表
     *
     * @param type
     * @param page
     * @param pageSize
     * @param callback
     */
    public static <T extends Favorite> void favoriteList(String type,
                                                         int page, int pageSize, T data,
                                                         ApiCallback<ApiModelList<T>> callback) {
        String uid = StringUtils.isEmpty(UserProperties.getUserId()) ? "0" : UserProperties.getUserId();
        String api = "";
        if (TYPE_PARTY.equals(type)) {
            api = "/favourite/events";
        } else if (TYPE_TOPIC.equals(type)) {
            api = "/favourite/topic";
        } else if (TYPE_RECIPE.equals(type)) {
            api = "/favourite/recipe";
        }
        Api.get(FavoriteApi.class)
                .with(api)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("uid", uid, true)
                .toModel(new ApiModelList<T>(data))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得收藏用户列表
     *
     * @param type
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getFavoriteUserList(String type, String foreignId, int page, int pageSize,
                                           ApiCallback<ApiModelList<User>> callback) {
        Api.get(FavoriteApi.class)
                .with("/favourite")
                .addParams("type", type, true)
                .addParams("foreign_id", foreignId, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("uid", UserProperties.getUserId())
                .toModel(new ApiModelList<User>(new User()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 添加收藏
     *
     * @param type
     * @param foreignId
     * @param callback
     */
    public static void favoriteAdd(String type, int foreignId, ApiCallback<State> callback) {
        String uid = StringUtils.isEmpty(UserProperties.getUserId()) ? "0" : UserProperties.getUserId();
        Api.get(FavoriteApi.class)
                .with("/favourite/create")
                .addParams("uid", uid, true)
                .addParams("foreign_id", foreignId + "", true)
                .addParams("type", type, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 取消收藏
     *
     * @param type
     * @param foreignId
     * @param callback
     */
    public static void favoriteRemove(String type, int foreignId, ApiCallback<State> callback) {
        String uid = StringUtils.isEmpty(UserProperties.getUserId()) ? "0" : UserProperties.getUserId();
        Api.get(FavoriteApi.class)
                .with("/favourite/cancel")
                .addParams("uid", uid, true)
                .addParams("foreign_id", foreignId + "", true)
                .addParams("type", type, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

}
