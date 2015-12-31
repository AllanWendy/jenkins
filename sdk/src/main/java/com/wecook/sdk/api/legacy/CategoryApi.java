package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.Category;

/**
 * 分类相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class CategoryApi extends Api {

    /**
     * 获得分类列表
     * @param callback
     */
    public static void getCategoryList(ApiCallback<ApiModelList<Category>> callback) {
        Api.get(CategoryApi.class)
                .with("/recipe/category")
                .addParams("parent_id", "0", true)
                .toModel(new ApiModelList<Category>(new Category()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_WEEK)
                .executeGet();
    }
}
