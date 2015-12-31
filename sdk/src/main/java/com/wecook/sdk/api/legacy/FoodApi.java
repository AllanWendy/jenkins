package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodDetail;
import com.wecook.sdk.api.model.FoodTopMenus;
import com.wecook.sdk.api.model.ID;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.dbprovider.tables.RecipeTable;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 菜谱相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class FoodApi extends Api {

    /**
     * 获得首页推荐菜谱列表
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getRecommendFoodList(int page, int pageSize, ApiCallback<ApiModelList<Food>> callback) {
        String uid = StringUtils.isEmpty(UserProperties.getUserId()) ? "0" : UserProperties.getUserId();
        Api.get(FoodApi.class)
                .with("/recipe/recommend")
                .addParams("uid", uid)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .toModel(new ApiModelList<Food>(new Food()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_HOUR)
                .executeGet();
    }

    /**
     * 获得菜谱详情
     *
     * @param id
     * @param apiCallback
     */
    public static void getFoodDetail(String id, ApiCallback<FoodDetail> apiCallback) {
        String uid = StringUtils.isEmpty(UserProperties.getUserId()) ? "0" : UserProperties.getUserId();
        Api.get(FoodApi.class)
                .with("/recipe/detail")
                .addParams("id", id, true)
                .addParams("uid", uid)
                .toModel(new FoodDetail())
                .setApiCallback(apiCallback)
                .setCacheTime(CACHE_ONE_DAY)
                .executeGet();
    }

    /**
     * 通过菜谱id串获得列表
     *
     * @param ids
     * @param callback
     */
    public static void getFoodListByIds(String ids, int page, int pageSize, ApiCallback<ApiModelList<Food>> callback) {
        Api.get(FoodApi.class)
                .with("/recipe/getbyids")
                .addParams("ids", ids, true)
                .addParams("uid", UserProperties.getUserId())
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .toModel(new ApiModelList<Food>(new Food()))
                .setApiCallback(callback)
                .executePost();
    }

    /**
     * 通过食材原料查找菜谱列表
     *
     * @param type
     * @param keywords
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getFoodListByResource(String type, String keywords, int page, int pageSize,
                                             ApiCallback<ApiModelList<Food>> callback) {
        Api.get(FoodApi.class)
                .with("/recipe/special")
                .addParams("type", type, true)
                .addParams("keywords", keywords, true)
                .addParams("uid", UserProperties.getUserId())
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .toModel(new ApiModelList<Food>(new Food()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_HOUR)
                .executeGet();
    }

    /**
     * 组菜等到的列表
     *
     * @param keywords
     *         以空格分开
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getFoodListByGarnish(String keywords, int page, int pageSize,
                                            ApiCallback<ApiModelList<Food>> callback) {
        Api.get(FoodApi.class)
                .with("/recipe/zucai")
                .addParams("keywords", keywords, true)
                .addParams("uid", UserProperties.getUserId())
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .toModel(new ApiModelList<Food>(new Food()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_HOUR)
                .executeGet();
    }

    /**
     * 发布菜谱
     *
     * @param id
     * @param callback
     */
    public static void publishFood(String id, ApiCallback<State> callback) {
        Api.get(FoodApi.class).with("/recipe/publish")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("id", id, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 保存菜谱
     *
     * @param recipeDB
     * @param callback
     */
    public static void saveFoodRecipe(RecipeTable.RecipeDB recipeDB, ApiCallback<ID> callback) {

        Api api = Api.get(FoodApi.class).with("/recipe/save")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("title", recipeDB.name, true)
                .addParams("media_id", recipeDB.imageId, true)
                .addParams("ingredients", recipeDB.ingredients, true)
                .addParams("step", recipeDB.steps, true);
        if (!StringUtils.isEmpty(recipeDB.id)) {
            api.addParams("id", recipeDB.id);
        }
        if (!StringUtils.isEmpty(recipeDB.description)) {
            api.addParams("description", recipeDB.description);
        }
        if (!StringUtils.isEmpty(recipeDB.assists)) {
            api.addParams("assist", recipeDB.assists);
        }
        if (!StringUtils.isEmpty(recipeDB.tips)) {
            api.addParams("tips", recipeDB.tips);
        }
        if (!StringUtils.isEmpty(recipeDB.tags)) {
            api.addParams("tags", recipeDB.tags);
        }
        if (!StringUtils.isEmpty(recipeDB.difficulty)) {
            api.addParams("difficulty", recipeDB.difficulty);
        }
        if (!StringUtils.isEmpty(recipeDB.cookTime)) {
            api.addParams("spendtime", recipeDB.cookTime);
        }
        api.setApiCallback(callback);
        api.toModel(new ID());
        api.executePost();
    }

    /**
     * 获得我的菜谱列表
     * 2：已发布, 3：草稿, -1:回收站
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getMyFoodRecipeList(int page, int pageSize, ApiCallback<ApiModelList<Food>> callback) {
        Api.get(FoodApi.class).with("/recipe/my")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .addParams("status", "2")
                .toModel(new ApiModelList<Food>(new Food()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得我的菜谱列表
     * 2：已发布, 3：草稿, -1:回收站
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getMyFoodRecipeList(String uid, int page, int pageSize, ApiCallback<ApiModelList<Food>> callback) {
        Api.get(FoodApi.class).with("/recipe/my")
                .addParams("uid", uid, true)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .addParams("status", "2")
                .toModel(new ApiModelList<Food>(new Food()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得我的菜谱详情
     *
     * @param id
     * @param apiCallback
     */
    public static void getMyFoodRecipeDetail(String id, ApiCallback<FoodDetail> apiCallback) {
        Api.get(FoodApi.class).with("/recipe/my_detail")
                .addParams("id", id, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new FoodDetail())
                .setApiCallback(apiCallback)
                .executeGet();
    }

    /**
     * 删除菜谱
     *
     * @param recipeId
     * @param callback
     */
    public static void deleteMyFoodRecipe(String recipeId, ApiCallback<State> callback) {
        Api.get(FoodApi.class).with("/recipe/delete")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("id", "" + recipeId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得顶部菜单
     */
    public static void getTopMenu(String lon, String lat, ApiCallback<FoodTopMenus> apiCallback) {
        Api.get(FoodApi.class)
                .with("/recipe/top_category")
                .addParams("lon", lon, true)
                .addParams("lat", lat, true)
                .toModel(new FoodTopMenus())
                .setApiCallback(apiCallback)
                .executeGet();

    }
}
