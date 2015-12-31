package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.core.internet.ApiResult;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.SearchSuggestion;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 搜索相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class SearchApi extends Api {

    /**
     * 搜索菜谱
     *
     * @param keyword
     * @param page
     * @param pageSize
     * @param apiCallback
     */
    public static void search(String keyword, int page, int pageSize, ApiCallback<ApiModelList<Food>> apiCallback) {
        String uid = StringUtils.isEmpty(UserProperties.getUserId()) ? "0" : UserProperties.getUserId();
        Api.get(SearchApi.class)
                .with("/recipe/search")
                .addParams("uid", uid)
                .addParams("keywords", keyword, true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .toModel(new ApiModelList<Food>(new Food()))
                .setApiCallback(apiCallback)
                .setCacheTime(CACHE_TEN_MINUTE)
                .executeGet();
    }

    /**
     * 搜索建议
     *
     * @param keyword
     * @param limit
     * @param apiCallback
     */
    public static ApiResult suggestion(String keyword, int limit, ApiCallback<ApiModelList<SearchSuggestion>> apiCallback) {
        return Api.get(SearchApi.class)
                .with("/recipe/autocomplete")
                .addParams("keywords", keyword, true)
                .addParams("batch", limit + "", true)
                .toModel(new ApiModelList<SearchSuggestion>(new SearchSuggestion()))
                .setApiCallback(apiCallback)
                .executeGet();
    }

    /**
     * 搜索热门
     *
     * @param apiCallback
     */
    public static void searchHotTag(ApiCallback<ApiModelList<Tags>> apiCallback) {
        Api.get(SearchApi.class)
                .with("/recipe/popular")
                .toModel(new ApiModelList<Tags>(new Tags()))
                .setApiCallback(apiCallback)
                .executeGet();
    }

}
