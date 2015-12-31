package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 引导搜索
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/10
 */
public class InspireSearchApi extends Api {


    public static void getIngredientList(String ingredients, String category,
                                         int page, int pageSize, ApiCallback callback) {
        Api.get(InspireSearchApi.class)
                .with("/GuideSearch/ingredients")
                .addParams("keys", ingredients)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .addParams("type", category)
                .setApiCallback(callback)
                .executeGet();
    }

    public static void searchIngredientList(String ingredients, String key,
                                            int page, int pageSize, ApiCallback callback) {
        Api.get(InspireSearchApi.class)
                .with("/GuideSearch/autocomplete")
                .addParams("keys", ingredients)
                .addParams("input", key)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .setApiCallback(callback)
                .executeGet();
    }

    public static void getRecipeList(String ingredients, int page, int pageSize, ApiCallback callback) {
        Api.get(InspireSearchApi.class)
                .with("/GuideSearch/recipes")
                .addParams("keys", ingredients, true)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得菜谱列表IDS
     *
     * @param ingredients
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getRecipeListIds(String ingredients, int page, int pageSize, ApiCallback callback) {
        Api.get(InspireSearchApi.class)
                .with("/GuideSearch/recipe_ids")
                .addParams("keys", ingredients, true)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .setApiCallback(callback)
                .executeGet();
    }


    public static void getCategoryList(ApiCallback callback) {
        Api.get(InspireSearchApi.class)
                .with("/GuideSearch/ingredient_types")
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_WEEK)
                .executeGet();
    }

    public static void reportMissIngredient(String ingredientName, ApiCallback callback) {
        Api.get(InspireSearchApi.class)
                .with("/GuideSearch/miss_ingredient")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("miss", ingredientName, true)
                .setApiCallback(callback)
                .executeGet();
    }
}
