package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.core.internet.ApiResult;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.DishRecommend;
import com.wecook.sdk.api.model.Express;
import com.wecook.sdk.api.model.HomeFeedList;
import com.wecook.sdk.api.model.Notice;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.api.model.SearchSuggestion;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 买菜帮手接口
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/17
 */
public class DishApi extends Api {

    public static final int ORDER_TYPE_DEFAULT = 0;
    public static final int ORDER_TYPE_SALE = 1;
    public static final int ORDER_TYPE_PRICE = 2;

    public static final String ORDER_DIRECT_UP = "asc";
    public static final String ORDER_DIRECT_DOWN = "desc";


    public static final String ORDER_TYPE_TIME_DEFAULT = "default";//默认

    public static final int NOTICE_TYPE_SYS = 1;//买菜帮手官方
    public static final int NOTICE_TYPE_RESTAURANT = 2;//餐厅


//    public static void getDishRecommendInfo(String lat, String lon, int page, int pageSize, ApiCallback<DishRecommend> callback) {
//        Api.get(DishApi.class)
//                .with("/dishes/home")
//                .addParams("lat", lat, true)
//                .addParams("lon", lon, true)
//                .addParams("page", "" + page, true)
//                .addParams("batch", "" + pageSize, true)
//                .toModel(new DishRecommend())
//                .setApiCallback(callback)
//                .setCacheTime(CACHE_TEN_MINUTE)
//                .executeGet();
//    }

    /**
     * 获得买菜帮手首页推荐信息列表
     *
     * @param lon
     * @param lon
     * @param city
     * @param callback
     */
    public static void getDishRecommendInfo(String lon, String lat, String city, ApiCallback<DishRecommend> callback) {
        Api api = Api.get(DishApi.class)
                .with("/operation/home_2_6")
                .addParams("lat", lat, true)
                .addParams("lon", lon, true);
        if (null != city && !"".equals(city)) {
            api.addParams("city", city, false);
        }
        api.toModel(new DishRecommend())
                .setApiCallback(callback)
                .setCacheTime(CACHE_TEN_MINUTE)
                .executeGet();
//        String url = "http://api.wecook.com.cn/v3/dishes/home_2_5?uid=153733&" +
//                "wid=1bb4c364ba84c23d83dff856e250e63c&timestamp=111111&adcode=110000&" +
//                "access=259eea423dee18c7b865b0777cd657cc&sign=1b5e006df2378279690ede7c1d20990d&lon=116.469970&lat=39.912544";
//        Api.get(DishApi.class)
//                .toModel(new DishRecommend())
//                .setApiCallback(callback)
//                .setCacheTime(CACHE_TEN_MINUTE)
//                .executeHoleGet(url);
    }

    /**
     * 获得买菜帮手热门套餐列表
     *
     * @param lat
     * @param lon
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getDishGroupList(String lat, String lon, int page, int pageSize, ApiCallback<ApiModelList<Dish>> callback) {
        Api.get(DishApi.class)
                .with("/dishes/dishes")
                .addParams("type", "2")
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .toModel(new ApiModelList<Dish>(new Dish()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得买菜帮手今日特价
     *
     * @param lat
     * @param lon
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getSpecialDishList(String lat, String lon, int page, int pageSize, ApiCallback<ApiModelList<Dish>> callback) {
        Api.get(DishApi.class)
                .with("/dishes/dishes")
                .addParams("type", "4")
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .toModel(new ApiModelList<Dish>(new Dish()))
                .setApiCallback(callback)
                .executeGet();
    }


    /**
     * 获得买菜帮手热销榜单
     *
     * @param restaurantId
     * @param lat
     * @param lon
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getHotSaleDishList(String restaurantId, String lat, String lon, int page, int pageSize, ApiCallback<ApiModelList<Dish>> callback) {
        Api.get(DishApi.class)
                .with("/dishes/dishes")
                .addParams("order_by_sales", ORDER_DIRECT_DOWN)
                .addParams("restaurant_id", restaurantId + "")
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .toModel(new ApiModelList<Dish>(new Dish()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得买菜帮手搜索结果
     *
     * @param keyword
     * @param lat
     * @param lon
     * @param orderType
     * @param orderDirect
     * @param express_type 配送类型
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void searchDishList(String keyword, String lat, String lon,
                                      int orderType, String orderDirect, String express_type,
                                      int page, int pageSize,
                                      ApiCallback<ApiModelList<Dish>> callback) {
        Api api = Api.get(DishApi.class)
                .with("/dishes/dishes")
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("keywords", keyword + "")
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .toModel(new ApiModelList<Dish>(new Dish()))
                .setApiCallback(callback);
        if (orderType == ORDER_TYPE_SALE) {
            api.addParams("order_by_sales", orderDirect);
        } else if (orderType == ORDER_TYPE_PRICE) {
            api.addParams("order_by_price", orderDirect);
        }
        if (null != express_type) {
            if (!ORDER_TYPE_TIME_DEFAULT.equals(express_type)) {
                api.addParams("express_type", express_type);
            }
        }
        api.executeGet();
    }

    /**
     * 获得餐厅中的菜品列表
     *
     * @param keyword
     * @param restaurantId
     * @param lat
     * @param lon
     * @param orderType
     * @param orderDirect
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getDishListFromRestaurant(String keyword, String restaurantId,
                                                 String lat, String lon,
                                                 int orderType, String orderDirect,
                                                 int page, int pageSize,
                                                 ApiCallback<ApiModelList<Dish>> callback) {
        getDishListFromRestaurant(keyword, restaurantId, lat, lon, orderType, orderDirect, page, pageSize, "", callback);
    }

    /**
     * 获得餐厅菜品列表
     *
     * @param keyword
     * @param restaurantId
     * @param lat
     * @param lon
     * @param orderType
     * @param orderDirect
     * @param page
     * @param pageSize
     * @param excludeIds
     * @param callback
     */
    public static void getDishListFromRestaurant(String keyword, String restaurantId,
                                                 String lat, String lon,
                                                 int orderType, String orderDirect,
                                                 int page, int pageSize, String excludeIds,
                                                 ApiCallback<ApiModelList<Dish>> callback) {

        Api api = Api.get(DishApi.class)
                .with("/dishes/dishes")
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("restaurant_id", restaurantId + "")
                .addParams("keywords", keyword + "")
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .toModel(new ApiModelList<Dish>(new Dish()))
                .setApiCallback(callback);
        if (orderType == ORDER_TYPE_SALE) {
            api.addParams("order_by_sales", orderDirect);
        } else if (orderType == ORDER_TYPE_PRICE) {
            api.addParams("order_by_price", orderDirect);
        }

        if (!StringUtils.isEmpty(excludeIds)) {
            api.addParams("except_dishes_ids", excludeIds);
        }
        api.executeGet();
    }

    /**
     * 获得买菜帮手菜品列表
     *
     * @param keyword
     * @param lat
     * @param lon
     * @param orderType
     * @param orderDirect
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getDishList(String keyword, String lat, String lon,
                                   int orderType, String orderDirect,
                                   int page, int pageSize,
                                   ApiCallback<ApiModelList<Dish>> callback) {

        Api api = Api.get(DishApi.class)
                .with("/dishes/dishes")
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .addParams("keywords", keyword + "")
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .toModel(new ApiModelList<Dish>(new Dish()))
                .setApiCallback(callback);
        if (orderType == ORDER_TYPE_SALE) {
            api.addParams("order_by_sales", orderDirect);
        } else if (orderType == ORDER_TYPE_PRICE) {
            api.addParams("order_by_price", orderDirect);
        }
        api.executeGet();
    }

    /**
     * 菜品详情
     *
     * @param dishId   菜品ID
     * @param callback
     */
    public static void getDishDetail(String dishId, ApiCallback<Dish> callback) {
        Api.get(DishApi.class)
                .with("/dishes/detail")
                .addParams("id", dishId, true)
                .toModel(new Dish())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得公告
     *
     * @param noticeType
     * @param parentId
     * @param callback
     */
    public static void getDishNotice(int noticeType, String parentId, ApiCallback<Notice> callback) {
        Api.get(DishApi.class)
                .with("/dishes/home_notice")
                .addParams("type", noticeType + "", true)
                .addParams("parent_id", parentId, true)
                .toModel(new Notice())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 味库v2.5首页feed流
     *
     * @param callback
     */
    public static void getHomeFeed(String lat, String lon, String city, ApiCallback<HomeFeedList<Restaurant>> callback) {
        Api api = Api.get(DishApi.class)
                .with("/dishes/home_feed")
                .addParams("page", "1", true)
                .addParams("batch", "30", true)
                .addParams("lat", lat, true)
                .addParams("lon", lon, true);
        if (null != city && !"".equals(city)) {
            api.addParams("city", city, false);
        }
        api.toModel(new HomeFeedList<Restaurant>(new Restaurant()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_TEN_MINUTE)
                .executeGet();
    }

    /**
     * 买菜帮手热门搜索标签
     *
     * @param apiCallback
     */
    public static void dishSearchHotTag(ApiCallback<ApiModelList<Tags>> apiCallback) {
        Api.get(DishApi.class)
                .with("/dishes/hotwords")
                .toModel(new ApiModelList<Tags>(new Tags()))
                .setApiCallback(apiCallback)
                .executeGet();
    }

    /**
     * 买菜帮手搜索建议
     *
     * @param keyword
     * @param limit
     * @param apiCallback
     */
    public static ApiResult dishSuggestion(String keyword, int limit, ApiCallback<ApiModelList<SearchSuggestion>> apiCallback) {
        return Api.get(DishApi.class)
                .with("/dishes/suggestion")
                .addParams("query", keyword, true)
                .addParams("page", "1", true)
                .addParams("batch", limit + "", true)
                .toModel(new ApiModelList<SearchSuggestion>(new SearchSuggestion()))
                .setApiCallback(apiCallback)
                .executeGet();
    }

    /**
     * 获得购物车列表
     *
     * @param callback
     */
    public static void cartList(ApiCallback<ApiModelList<ShopCartRestaurant>> callback) {
        Api.get(DishApi.class)
                .with("/cart/index")
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new ApiModelList<ShopCartRestaurant>(new ShopCartRestaurant()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得离线购物车列表数据
     *
     * @param idAndCounts 菜品ID、数量的组合串(123,5;134,9;)
     * @param callback
     */
    public static void cartListOffline(String idAndCounts, ApiCallback<ApiModelList<ShopCartRestaurant>> callback) {
        Api.get(DishApi.class)
                .with("/cart/check_cart")
                .addParams("dishes_ids", idAndCounts, true)
                .toModel(new ApiModelList<ShopCartRestaurant>(new ShopCartRestaurant()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 添加到购物车
     *
     * @param dishId   菜品ID
     * @param count    数量
     * @param action   add:添加购物车，会在原购物车内菜品数量为基础增加 update:更新购物车
     * @param callback
     */
    public static void addToShopCart(String dishId, int count, String action, ApiCallback<State> callback) {
        Api.get(DishApi.class)
                .with("/cart/save")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("foreign_id", dishId, true)
                .addParams("quantity", count + "")
                .addParams("action", action, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 移除购物车
     *
     * @param cartIds  购物车记录ID串
     * @param callback
     */
    public static void removeShopCart(String cartIds, ApiCallback<State> callback) {
        Api.get(DishApi.class)
                .with("/cart/delete")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("ids", cartIds, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 移除购物车
     *
     * @param idAndCounts 菜品ID、数量的组合串(123,5;134,9;)
     * @param callback
     */
    public static void syncShopCart(String idAndCounts, ApiCallback<State> callback) {
        Api.get(DishApi.class)
                .with("/cart/sync")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("dishes_ids", idAndCounts, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 请求配送类型列表
     *
     * @param callback
     */
    public static void getExpressTypeList(ApiCallback<ApiModelList<Express>> callback) {
        Api api = Api.get(DishApi.class)
                .with("/dishes/express_type_list")
                .addParams("lat", getLatitude(), true)
                .addParams("lon", getLongitude(), true);
        if (null != getCity() && !"".equals(getCity())) {
            api.addParams("city", getCity(), false);
        }
        api.toModel(new ApiModelList<Express>(new Express()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获取经度
     */
    private static String getLongitude() {
        Address address = DishPolicy.get().getDishAddress();
        final String lon = address.getLocation().getLongitude();
        return lon;
    }

    /**
     * 获取纬度
     */
    private static String getLatitude() {
        Address address = DishPolicy.get().getDishAddress();
        final String lat = address.getLocation().getLatitude();
        return lat;
    }

    /**
     * 获取City
     */
    private static String getCity() {
        Address address = DishPolicy.get().getDishAddress();
        String city = LocationServer.asInstance().getIndexCity();
        return city;
    }

}
