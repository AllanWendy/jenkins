package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.ID;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 晒厨艺相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class CookShowApi extends Api {

    /** 精选 */
    public static final int SORT_HOT = 1;

    /** 最新 */
    public static final int SORT_NEW = 2;

    public static final String TYPE_RECIPE = "recipe";

    public static final String ACTION_COOK_SHOW = "action_cook_show";
    public static final String PARAM_COOK_SHOW = "cookshow";

    /**
     * 获得晒厨艺列表
     *
     * @param type
     * @param page
     * @param batch
     * @param callback
     */
    public static void getCookShowList(String type, String title, int page, int batch,
                                       ApiCallback<ApiModelList<CookShow>> callback) {
        Api.get(CookShowApi.class)
                .with("/cooking")
                .addParams("type", type, true)
                .addParams("title", title, true)
                .addParams("page", page + "", true)
                .addParams("batch", batch + "", true)
                .addParams("uid", UserProperties.getUserId() + "")
                .toModel(new ApiModelList<CookShow>(new CookShow()))
                .setApiCallback(callback)
                .executeGet();

    }

    /**
     * 获得晒厨艺的推荐列表
     *
     * @param sort
     * @param page
     * @param batch
     * @param callback
     */
    public static void getCookShowRecommendList(int sort, int page, int batch, boolean useCache,
                                                ApiCallback<ApiModelList<CookShow>> callback) {
        Api api = Api.get(CookShowApi.class)
                .with("/cooking/recommend")
                .addParams("batch", batch + "", true)
                .addParams("page", page + "", true)
                .addParams("sort", sort + "")
                .addParams("uid", UserProperties.getUserId() + "")
                .toModel(new ApiModelList<CookShow>(new CookShow()))
                .setApiCallback(callback);
        if (useCache) {
            api.setCacheTime(CACHE_ONE_HOUR);
        }
        api.executeGet();

    }

    /**
     * 获得晒厨艺详情
     *
     * @param id
     * @param callback
     */
    public static void getCookShowDetailInfo(String id, ApiCallback<CookShow> callback) {
        Api.get(CookShowApi.class)
                .with("/cooking/detail")
                .addParams("id", id, true)
                .addParams("uid", UserProperties.getUserId() + "")
                .toModel(new CookShow())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得用户已晒厨艺列表
     *
     * @param type
     * @param title
     * @param page
     * @param batch
     * @param uid
     * @param callback
     */
    public static void getUserCookShowList(int page, int batch,
                                           ApiCallback<ApiModelList<CookShow>> callback) {
        Api.get(CookShowApi.class)
                .with("/cooking/user")
                .addParams("type", TYPE_RECIPE, true)
                .addParams("page", page + "", true)
                .addParams("batch", batch + "", true)
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new ApiModelList<CookShow>(new CookShow()))
                .setApiCallback(callback)
                .executeGet();

    }
    /**
     * 获得用户已晒厨艺列表
     *
     * @param type
     * @param title
     * @param page
     * @param batch
     * @param uid
     * @param callback
     */
    public static void getUserCookShowList(String uid, int page, int batch,
                                           ApiCallback<ApiModelList<CookShow>> callback) {
        Api.get(CookShowApi.class)
                .with("/cooking/user")
                .addParams("type", TYPE_RECIPE, true)
                .addParams("page", page + "", true)
                .addParams("batch", batch + "", true)
                .addParams("uid", uid, true)
                .toModel(new ApiModelList<CookShow>(new CookShow()))
                .setApiCallback(callback)
                .executeGet();

    }

    /**
     * 创建一个晒厨艺
     *
     * @param type
     *         对象类型 {@link #TYPE_RECIPE}
     * @param title
     *         菜谱标题
     * @param mediaId
     *         厨艺媒体资源ID
     * @param uid
     *         用户UID
     * @param des
     *         描 述
     * @param tags
     *         标 签
     * @param callback
     */
    public static void createCookShow(String type, String title, String mediaId,
                                      String uid, String des, String tags, ApiCallback<ID> callback) {

        Api.get(CookShowApi.class)
                .with("/cooking/create")
                .addParams("type", type, true)
                .addParams("title", title, true)
                .addParams("media_id", mediaId, true)
                .addParams("uid", uid, true)
                .addParams("description", des)
                .addParams("tags", tags)
                .toModel(new ID())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 更新厨艺
     *
     * @param cid
     * @param title
     * @param des
     * @param tags
     * @param callback
     */
    public static void updateCookShow(String cid, String title, String des, String tags, ApiCallback<ID> callback) {

        Api.get(CookShowApi.class)
                .with("/cooking/update")
                .addParams("id", cid, true)
                .addParams("title", title, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("description", des)
                .addParams("tags", tags)
                .toModel(new ID())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 删除一个晒厨艺
     *
     * @param cid
     *         厨艺ID
     * @param uid
     *         用户UID
     * @param callback
     */
    public static void deleteCookShow(String cid, ApiCallback<State> callback) {
        Api.get(CookShowApi.class)
                .with("/cooking/delete")
                .addParams("id", cid, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .setApiCallback(callback)
                .toModel(new State())
                .executeGet();
    }

    /**
     * 获得晒厨艺热门标签
     *
     * @param apiCallback
     */
    public static void getCookShowHotTag(int page, int pageSize, ApiCallback<ApiModelList<Tags>> apiCallback) {
        Api.get(CookShowApi.class)
                .with("/cooking/tags")
                .addParams("uid", UserProperties.getUserId())
                .addParams("page", "" + page)
                .addParams("batch", "" + pageSize)
                .toModel(new ApiModelList<Tags>(new Tags()))
                .setApiCallback(apiCallback)
                .executeGet();
    }
}
