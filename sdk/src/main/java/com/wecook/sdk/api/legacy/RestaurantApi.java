package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 餐厅相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/1
 */
public class RestaurantApi extends Api {

    /**
     * 获得买菜帮手餐厅列表
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getDishRestaurantList(String lat, String lon, int page, int pageSize, ApiCallback<ApiModelList<Restaurant>> callback) {

        Api.get(RestaurantApi.class)
                .with("/restaurant/restaurants")
                .addParams("uid", UserProperties.getUserId())
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .toModel(new ApiModelList<Restaurant>(new Restaurant()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_HALF_HOUR)
                .executeGet();
    }

    /**
     * 获得买菜帮手餐厅详情
     *
     * @param restaurantId
     * @param callback
     */
    public static void getDishRestaurantDetail(String restaurantId, ApiCallback<Restaurant> callback) {
        Api.get(RestaurantApi.class)
                .with("/restaurant/detail")
                .addParams("id", restaurantId, true)
                .toModel(new Restaurant())
                .setApiCallback(callback)
                .executeGet();
    }
}
