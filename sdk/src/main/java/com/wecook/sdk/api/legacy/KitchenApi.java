package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.core.internet.ApiResult;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.api.model.FoodResourceCategory;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.policy.KitchenHomePolicy;
import com.wecook.sdk.userinfo.UserProperties;

import java.util.List;

/**
 * 厨房相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class KitchenApi extends Api {

    /** 食材 */
    public static final String TYPE_INGREDIENT = "ingredient";

    /** 厨具 */
    public static final String TYPE_KITCHENWARE = "kitchenware";

    /** 调料 */
    public static final String TYPE_CONDIMENT = "condiment";

    /** 扫码获得 */
    public static final String TYPE_BARCODE = "barcode";

    /**
     * 同步获取数据列表
     *
     * @param t
     * @param callback
     * @param <T>
     */
    public static <T extends FoodResource> void syncPull(T t, ApiCallback<ApiModelList<T>> callback) {
        Api.get(KitchenApi.class)
                .with("/kitchen/get")
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new ApiModelList<T>(t))
                .setApiCallback(callback)
                .executeGet();

    }

    /**
     * 同步更新网络数据列表
     *
     * @param list json数组
     * @param callback
     */
    public static <T extends FoodResource> void syncPush(List<T> list, ApiCallback<State> callback) {
        String jsonArrays = KitchenHomePolicy.generateUpdateJsonArray(list);
        Api.get(KitchenApi.class)
                .with("/kitchen/set")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("rows", jsonArrays, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executePost();
    }

    /**
     * 添加到厨房
     *
     * @param type
     * @param foreignId
     * @param callback
     */
    public static void add(String type, String foreignId, ApiCallback<State> callback) {
        Api.get(KitchenApi.class)
                .with("/kitchen/create")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("type", type, true)
                .addParams("foreign_id", foreignId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 移除厨房
     *
     * @param type
     * @param foreignId
     * @param callback
     */
    public static void remove(String type, String foreignId, ApiCallback<State> callback) {
        Api.get(KitchenApi.class)
                .with("/kitchen/delete")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("type", type, true)
                .addParams("foreign_id", foreignId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 批量增加
     *
     * @param list
     * @param callback
     */
    public static <T extends FoodResource> void batchAdd(List<T> list, ApiCallback<State> callback) {
        String jsonArray = KitchenHomePolicy.generateUpdateJsonArray(list);
        Api.get(KitchenApi.class)
                .with("/kitchen/update")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("rows", jsonArray, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executePost();
    }

    /**
     * 批量删除
     *
     * @param list
     * @param callback
     * @param <T>
     */
    public static <T extends FoodResource> void batchRemove(List<T> list, ApiCallback<State> callback) {
        String jsonArray = KitchenHomePolicy.generateDeleteJsonArray(list);
        Api.get(KitchenApi.class)
                .with("/kitchen/remove")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("rows", jsonArray, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executePost();
    }


    /**
     * 获得详情数据
     *
     * @param type
     * @param foreignId
     * @param t
     * @param callback
     * @param <T>
     */
    public static <T extends FoodResource> void getDetail(String type, String foreignId, T t,
                                                          ApiCallback<T> callback) {
        Api.get(KitchenApi.class)
                .with("/kitchen/detail")
                .addParams("uid", UserProperties.getUserId())
                .addParams("foreign_id", foreignId, true)
                .addParams("type", type, true)
                .toModel(t)
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_DAY)
                .executeGet();
    }

    /**
     * 通过标签获得列表
     *
     * @param tag
     * @param t
     * @param callback
     * @param <T>
     */
    public static ApiResult getListByTag(String tag, ApiCallback<ApiModelList<FoodResourceCategory>> callback) {
        return Api.get(KitchenApi.class)
                .with("/kitchen/tags")
                .addParams("uid", UserProperties.getUserId())
                .addParams("keywords", tag, true)
                .toModel(new ApiModelList<FoodResourceCategory>(new FoodResourceCategory()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_DAY)
                .executeGet();
    }

    /**
     * 获得推荐数据列表
     *
     * @param page
     * @param pageSize
     * @param t
     * @param callback
     * @param <T>
     */
    public static <T extends FoodResource> ApiResult getRecommendList(int page, int pageSize, T t, ApiCallback<ApiModelList<T>> callback) {
        return Api.get(KitchenApi.class)
                .with("/kitchen/recommend")
                .addParams("uid", UserProperties.getUserId())
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .toModel(new ApiModelList<T>(t))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_DAY)
                .executeGet();
    }

    /**
     * 我的厨房食材列表
     *
     * @param type
     * @param page
     * @param pageSize
     * @param t
     * @param callback
     * @param <T>
     */
    public static <T extends FoodResource> void getOwnerList(String type, int page, int pageSize, T t, ApiCallback<ApiModelList<T>> callback) {
        Api.get(KitchenApi.class)
                .with("/kitchen/user")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("type", type)
                .toModel(new ApiModelList<T>(t))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 搜索建议
     *
     * @param keywords
     * @param pageSize
     * @param callback
     */
    public static void getSuggestionList(String keywords, int pageSize, ApiCallback<ApiModelList<FoodResource>> callback) {
        Api.get(KitchenApi.class)
                .with("/kitchen/autocomplete")
                .addParams("uid", UserProperties.getUserId())
                .addParams("batch", pageSize + "", true)
                .addParams("keywords", keywords, true)
                .toModel(new ApiModelList<FoodResource>(new FoodResource()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 条码详情
     *
     * @param code
     * @param callback
     */
    public static void getBarcodeDetail(String code, ApiCallback<FoodResource> callback) {
        Api.get(KitchenApi.class)
                .with("/kitchen/barcode")
                .addParams("uid", UserProperties.getUserId())
                .addParams("code", code, true)
                .toModel(new FoodResource())
                .setApiCallback(callback)
                .executeGet();
    }

}
