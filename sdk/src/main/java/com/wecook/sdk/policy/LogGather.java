package com.wecook.sdk.policy;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.app.BaseApp;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.modules.thirdport.platform.base.BasePlatform;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.api.model.PartyDetail;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.api.model.base.Favorable;
import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志收集器
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/18
 */
public class LogGather {

    /**
     * 日志标记
     */
    private static Map<String, Object> mLogMarker = new HashMap<String, Object>();

    public static void setLogMarker(String key, Object marker) {
        mLogMarker.put(key, marker);
    }

    public static Object getLogMarker(String key) {
        return mLogMarker.get(key);
    }


    /**
     * 我－我的消息
     */
    public static void onEventMyMessage() {
        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.MY_MESSAGE_CLICK);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_MESSAGE, new JSONObject());
    }

    /**
     * 我－我的收藏
     */
    public static void onEventMyFav() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_FAV, new JSONObject());
    }

    /**
     * 我－客服电话
     */
    public static void onEventMyCall() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_CALL, new JSONObject());
    }

    /**
     * 我－订单-待付款
     */
    public static void onEventMyOrderObligation() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_ORDER_OBLIGATION, new JSONObject());
    }

    /**
     * 我－订单-待发货
     */
    public static void onEventMyOrderNoDeliver() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_ORDER_NO_DELIVER, new JSONObject());
    }

    /**
     * 我－订单-配送中
     */
    public static void onEventMyOrderShipping() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_ORDER_SHIPPING, new JSONObject());
    }

    /**
     * 我－订单-待评价
     */
    public static void onEventMyOrderNoEvaluate() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_ORDER_NO_EVALUATE, new JSONObject());
    }

    /**
     * 我－订单-退款
     */
    public static void onEventMyOrderRefund() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_ORDER_REUND, new JSONObject());
    }

    /**
     * 我－我的厨艺
     */
    public static void onEventMyCookShow() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_COOK_SHOW, new JSONObject());
    }

    /**
     * 我－我的菜谱
     */
    public static void onEventMyRecipe() {
        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.NEW_RECIPE_HOME_MYRECIPE);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_RECIPE, new JSONObject());
    }

    /**
     * 我－我的草稿箱
     */
    public static void onEventMyDraft() {
        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.NEW_RECIPE_HOME_DRAFT);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_DRAFT, new JSONObject());
    }

    /**
     * 我－获得优惠
     */
    public static void onEventMyGainCoupon() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_GAIN_COUPON, new JSONObject());
    }

    /**
     * 我－我的钱包
     */
    public static void onEventMyWallet() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_WALLET, new JSONObject());
    }

    /**
     * 我－我的优惠券
     */
    public static void onEventMyCoupon() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_COUPON, new JSONObject());
    }

    /**
     * 我－我的地址
     */
    public static void onEventMyAddress() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_ADDRESS, new JSONObject());
    }

    /**
     * 我－我的订单
     */
    public static void onEventMyOrder() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_ORDER, new JSONObject());
    }

    /**
     * 我－头像
     */
    public static void onEventMyInfo() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_MY_AVATAR, new JSONObject());
    }

    /**
     * 分享
     */
    public static void onEventShare(BasePlatform option, boolean success, String message) {
        Object data = getLogMarker(MARK.SHARE_DATA);
        if (data != null && option != null) {

            String toName = "";
            switch (option.getType()) {
                case PlatformManager.PLATFORM_WECHAT:
                    toName = LogConstant.TO_WEIXIN;
                    break;
                case PlatformManager.PLATFORM_WECHAT_FRIENDS:
                    toName = LogConstant.TO_WEIXIN_FIRENTS;
                    break;
                case PlatformManager.PLATFORM_WEBLOG:
                    toName = LogConstant.TO_WEIBLOG;
                    break;
            }

            if (data instanceof PartyDetail) {
                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_NAME, ((PartyDetail) data).getTitle());
                keys.put(LogConstant.KEY_TO, toName);
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_TOPIC_EVENT_SHARE_COUNT, keys);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put(LOG_ZHUGE.EV_PARTY_KEY_NAME, ((PartyDetail) data).getTitle());
                map.put(LOG_ZHUGE.EV_PARTY_KEY_STATE, success);
                ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_PARTY_SHARE, new JSONObject(map));
            } else if (data instanceof Topic) {
                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_NAME, ((Topic) data).getTitle());
                keys.put(LogConstant.KEY_TO, toName);
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_TOPIC_NEWS_SHARE_COUNT, keys);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put(LOG_ZHUGE.EV_PARTY_KEY_NAME, ((Topic) data).getTitle());
                map.put(LOG_ZHUGE.EV_PARTY_KEY_STATE, success);
                ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_TOPIC_SHARE, new JSONObject(map));
            } else if (data instanceof CookShow) {
                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_NAME, ((CookShow) data).getTitle());
                keys.put(LogConstant.KEY_TO, toName);
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_MYCOOKING_SHARE_COUNT, keys);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put(LOG_ZHUGE.EV_COOKSHOW_KEY_NAME, ((CookShow) data).getTitle());
                map.put(LOG_ZHUGE.EV_COOKSHOW_SHARE_KEY_TO, toName);
                map.put(LOG_ZHUGE.EV_COOKSHOW_KEY_STATE, success);
                ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_COOKSHOW_SHARE, new JSONObject(map));
            } else if (data instanceof FoodRecipe) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(LOG_ZHUGE.EV_RECIPE_KEY_NAME, ((FoodRecipe) data).getTitle());
                map.put(LOG_ZHUGE.EV_RECIPE_KEY_STATE, success);
                ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_RECIPE_SHARE, new JSONObject(map));
            }

        }
    }

    /**
     * 评论
     *
     * @param type
     * @param data
     * @param success
     */
    public static void onEventComment(String type, Object data, boolean success) {
        if (data != null) {
            if (CommentApi.TYPE_EVENTS.equals(type)) {
                if (data instanceof Party) {
                    Map<String, Object> keys = new HashMap<String, Object>();
                    keys.put(LOG_ZHUGE.EV_PARTY_KEY_NAME, ((Party) data).getTitle());
                    keys.put(LOG_ZHUGE.EV_PARTY_KEY_STATE, success);
                    ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_PARTY_COMMENT, new JSONObject(keys));
                }
            } else if (CommentApi.TYPE_TOPIC.equals(type)) {
                if (data instanceof String) {
                    Map<String, Object> keys = new HashMap<String, Object>();
                    keys.put(LOG_ZHUGE.EV_TOPIC_KEY_NAME, data);
                    keys.put(LOG_ZHUGE.EV_TOPIC_KEY_STATE, success);
                    ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_TOPIC_COMMENT, new JSONObject(keys));
                }
            } else if (CommentApi.TYPE_RECIPE.equals(type)) {
                if (data instanceof String) {
                    Map<String, Object> keys = new HashMap<String, Object>();
                    keys.put(LOG_ZHUGE.EV_RECIPE_KEY_NAME, data);
                    keys.put(LOG_ZHUGE.EV_RECIPE_KEY_STATE, success);
                    ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_RECIPE_COMMENT, new JSONObject(keys));
                }
            } else if (CommentApi.TYPE_COOKING.equals(type)) {
                String title = "";
                if (data instanceof CookShow) {
                    title = ((CookShow) data).getTitle();
                } else if (data instanceof String) {
                    title = (String) data;
                }
                Map<String, Object> keys = new HashMap<String, Object>();
                keys.put(LOG_ZHUGE.EV_COOKSHOW_KEY_NAME, title);
                keys.put(LOG_ZHUGE.EV_COOKSHOW_KEY_STATE, success);
                ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_COOKSHOW_COMMENT, new JSONObject(keys));
            }
        }
    }

    /**
     * 收藏
     *
     * @param type
     * @param favorable
     */
    public static void onEventFav(String type, Favorable favorable) {
        if (favorable != null) {
            if (FavoriteApi.TYPE_PARTY.equals(type)) {
                if (favorable instanceof Party) {
                    Map<String, Object> keys = new HashMap<String, Object>();
                    keys.put(LOG_ZHUGE.EV_PARTY_KEY_NAME, ((Party) favorable).getTitle());
                    keys.put(LOG_ZHUGE.EV_PARTY_KEY_STATE, favorable.isFav());
                    ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_PARTY_FAV, new JSONObject(keys));
                }
            } else if (FavoriteApi.TYPE_RECIPE.equals(type)) {
                if (favorable instanceof Food) {
                    if (favorable.isFav()) {
                        //添加收藏
                        Map<String, String> keys = new HashMap<String, String>();
                        keys.put(LogConstant.KEY_NAME, ((Food) favorable).title);
                        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_RECIPELIST_DO_LIKE_COUNT, keys);
                    } else {
                        //取消收藏
                        Map<String, String> keys = new HashMap<String, String>();
                        keys.put(LogConstant.KEY_NAME, ((Food) favorable).title);
                        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_RECIPELIST_DO_UNLIKE_COUNT, keys);
                    }

                    Map<String, Object> keys = new HashMap<String, Object>();
                    keys.put(LOG_ZHUGE.EV_RECIPE_KEY_NAME, ((Food) favorable).title);
                    keys.put(LOG_ZHUGE.EV_RECIPE_KEY_STATE, favorable.isFav());
                    ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_RECIPE_FAV, new JSONObject(keys));
                }
            } else if (FavoriteApi.TYPE_TOPIC.equals(type)) {
                if (favorable instanceof Topic) {
                    Map<String, Object> keys = new HashMap<String, Object>();
                    keys.put(LOG_ZHUGE.EV_TOPIC_KEY_NAME, ((Topic) favorable).getTitle());
                    keys.put(LOG_ZHUGE.EV_TOPIC_KEY_STATE, favorable.isFav());
                    ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_TOPIC_FAV, new JSONObject(keys));
                }
            }
        }
    }

    /**
     * 首页－更多活动
     */
    public static void onEventPartyMore() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_PARTY_MORE, new JSONObject());
    }

    /**
     * 首页－更多新鲜事
     */
    public static void onEventTopicMore() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_TOPIC_MORE, new JSONObject());
    }

    /**
     * 首页－更多菜谱
     */
    public static void onEventRecipeMore() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_RECIPE_GET_MORE, new JSONObject());
    }

    /**
     * 上传菜谱－入口
     */
    public static void onEventPublishIn() {
        Object from = getLogMarker(MARK.FROM);
        if (from != null) {
            Map<String, Object> keys = new HashMap<String, Object>();
            keys.put(LOG_ZHUGE.EV_PUBLISH_GOIN_KEY_FROM, from);
            ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_PUBLISH_GOIN, new JSONObject(keys));

            if ("快捷区".equals(from)) {
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.RECOMMEND_NEW_RECIPE_CLICK);
            } else if ("我的菜谱".equals(from)) {
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.NEW_RECIPE_MYRECIPE_ENTER);
            } else if ("精品菜谱".equals(from)) {
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.NEW_RECIPE_RECOMMEND_ENTER);
            }
        }
    }

    /**
     * 上传菜谱－发布
     */
    public static void onEventPublish(boolean success, String message) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_PUBLISH_KEY_STATE, success);
        keys.put(LOG_ZHUGE.EV_PUBLISH_KEY_MSG, message);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_PUBLISH_DO, new JSONObject(keys));
    }

    /**
     * 上传菜谱－预览
     */
    public static void onEventPublishReview() {
        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.NEW_RECIPE_EDIT_DETAIL_REVIEW);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_PUBLISH_REVIEW, new JSONObject());
    }

    /**
     * 晒厨艺－入口
     */
    public static void onEventCookShowIn() {
        Object from = getLogMarker(MARK.FROM);
        if (from != null) {
            Map<String, Object> keys = new HashMap<String, Object>();
            keys.put(LOG_ZHUGE.EV_COOKSHOW_GOIN_KEY_FROM, from);
            ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_COOKSHOW_GOIN, new JSONObject(keys));

            if ("功能快捷区".equals(from)) {
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.RECOMMEND_COOKSHOW_CLICK);
            } else if ("首页晒厨艺".equals(from)) {
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.COOK_RECOMMEND_DO);
            }
        }
    }

    /**
     * 晒厨艺－下一步
     */
    public static void onEventCookShowNext() {
        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.COOK_ACTION_PICK_NEXT);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_COOKSHOW_NEXT, new JSONObject());
    }

    /**
     * 晒厨艺－发布
     *
     * @param success
     * @param msg
     */
    public static void onEventCookShowPublish(boolean success, String msg) {
        if (success) {
            MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.COOK_ACTION_PUBLISH);
        }
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_COOKSHOW_KEY_STATE, success);
        keys.put(LOG_ZHUGE.EV_COOKSHOW_KEY_MSG, msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_COOKSHOW_PUBLISH, new JSONObject(keys));
    }

    /**
     * 晒厨艺－更多
     */
    public static void onEventCookShowMore() {
        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.COOK_RECOMMEND_MORE);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_COOKSHOW_MORE, new JSONObject());
    }

    /**
     * 晒厨艺－打分
     *
     * @param score
     */
    public static void onEventCookShowScore(String name, Integer score, boolean success, String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_COOKSHOW_KEY_NAME, name);
        keys.put(LOG_ZHUGE.EV_COOKSHOW_SCORE_NUMBER, score);
        keys.put(LOG_ZHUGE.EV_COOKSHOW_KEY_STATE, success);
        keys.put(LOG_ZHUGE.EV_COOKSHOW_KEY_MSG, msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_COOKSHOW_SCORE, new JSONObject(keys));
    }

    /**
     * 组菜－入口
     */
    public static void onEventGarnishIn() {
        Object from = getLogMarker(MARK.FROM);
        if (from != null) {
            Map<String, Object> keys = new HashMap<String, Object>();
            keys.put(LOG_ZHUGE.EV_GARNISH_GOIN_KEY_FROM, from);
            ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_GARNISH_GOIN, new JSONObject(keys));

            if ("快捷区".equals(from)) {
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.RECOMMEND_GARNISH_CLICK);
            } else if ("厨房".equals(from)) {
                Map<String, String> map = new HashMap<String, String>();
                map.put(LogConstant.KEY_TO, LogConstant.TO_INSPIRE_SEARCH);
                MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_KITCHEN_ENTER_COUNT, map);
            }
        }
    }

    /**
     * 组菜－可做菜
     *
     * @param selectedKeys
     */
    public static void onEventGarnishRecipe(String selectedKeys, String count) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_GARNISH_KEY_INGREDIENT, selectedKeys);
        keys.put(LOG_ZHUGE.EV_GARNISH_KEY_RECIPE_COUNT, count);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_GARNISH_RECIPE, new JSONObject(keys));
    }

    /**
     * 组菜－搜索
     *
     * @param keyword
     * @param success
     */
    public static void onEventGarnishSearch(String keyword, String selectedKeys, boolean success) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_GARNISH_KEY_INGREDIENT, selectedKeys);
        keys.put(LOG_ZHUGE.EV_GARNISH_SEARCH_KEY_INPUT, keyword);
        keys.put(LOG_ZHUGE.EV_GARNISH_SEARCH_KEY_STATE, success);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_GARNISH_SEARCH, new JSONObject(keys));
    }

    /**
     * 组菜－搜索结果点击
     *
     * @param name
     * @param itemPos
     */
    public static void onEventGarnishSearchResult(String keyword, String selectedKeys, String name, int itemPos) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_GARNISH_SEARCH_KEY_INPUT, keyword);
        keys.put(LOG_ZHUGE.EV_GARNISH_KEY_INGREDIENT, selectedKeys);
        keys.put(LOG_ZHUGE.EV_GARNISH_SEARCH_RESULT_KEY_NAME, name);
        keys.put(LOG_ZHUGE.EV_GARNISH_SEARCH_RESULT_KEY_POSITION, itemPos);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_GARNISH_SEARCH_RESULT, new JSONObject(keys));


        Map<String, String> map = new HashMap<String, String>();
        map.put(LogConstant.KEY_FROM, LogConstant.FROM_INGREIDENT_SEARCH);
        map.put(LogConstant.KEY_NAME, name);
        map.put(LogConstant.KEY_INDEX, itemPos + "");
        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_KITCHEN_GUIDEDSEARCH_ADD_COUNT, map);
    }

    /**
     * 标签－首页
     *
     * @param tag
     */
    public static void onEventTagRecommend(String tag) {
        setLogMarker(MARK.DURATION_EVENT, LOG_ZHUGE.EV_TAG_RECIPE);

        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_TAG_KEY_NAME, tag);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_TAG_RECOMMEND, new JSONObject(keys));
    }

    /**
     * 标签－首页－更多
     */
    public static void onEventTagMore() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_TAG_MORE, new JSONObject());
    }

    /**
     * 标签－分类－项－点击
     *
     * @param type
     * @param name
     */
    public static void onEventTagSelect(String type, String name) {
        setLogMarker(MARK.DURATION_EVENT, LOG_ZHUGE.EV_TAG_RECIPE);
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_TAG_SELECT_KEY_TYPE, type);
        keys.put(LOG_ZHUGE.EV_TAG_KEY_NAME, name);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_TAG_SELECT, new JSONObject(keys));
    }

    /**
     * 标签－分类－菜谱列表－点击
     *
     * @param title
     */
    public static void onEventTagRecipe(String title) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_TAG_RECIPE_KEY_RECIPE_NAME, title);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_TAG_RECIPE, new JSONObject(keys));
    }

    /**
     * 搜索－首页
     */
    public static void onEventSearchRecommend() {
        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_HOME_SEARCH_COUNT);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_SEARCH_RECOMMEND, new JSONObject());
    }

    public static void onEventSearchInput(String keyword) {
        LogGather.setLogMarker(LogGather.MARK.DURATION_EVENT, LOG_ZHUGE.EV_SEARCH_DO);
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_SEARCH_DO_KEY_USER_INPUT, keyword);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_SEARCH_DO, new JSONObject(keys));
    }

    public static void onEventSearchTag(String tag) {
        LogGather.setLogMarker(LogGather.MARK.DURATION_EVENT, LOG_ZHUGE.EV_SEARCH_DO);
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_SEARCH_DO_KEY_HOT_TAG, tag);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_SEARCH_DO, new JSONObject(keys));
    }

    public static void onEventSearchSuggestion(String name, int pos) {
        LogGather.setLogMarker(LogGather.MARK.DURATION_EVENT, LOG_ZHUGE.EV_SEARCH_DO);
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_SEARCH_DO_KEY_USER_INPUT, name);
        keys.put(LOG_ZHUGE.EV_SEARCH_KEY_POSITION, pos);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_SEARCH_SUGGESTION, new JSONObject(keys));
    }

    public static void onEventSearchResult(String name, int pos) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_SEARCH_DO_KEY_USER_INPUT, name);
        keys.put(LOG_ZHUGE.EV_SEARCH_KEY_POSITION, pos);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_SEARCH_RESULT, new JSONObject(keys));
    }

    public static void onEventDishCardClick() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_CARD_MAIN);
    }

    public static void onEventDishCardColumnClick(String name) {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_CARD_COLUMN + name);
    }

    public static void onEventDishCardMoreClick() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_CARD_MORE);
    }

    public static void onEventDishRecommendSearch() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RECOMMEND_SEARCH);
    }

    public static void onEventDishRecommendBanner(String name) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(LOG_ZHUGE.EV_DISH_RECOMMEND_KEY_NAME, name);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RECOMMEND_BANNER, new JSONObject(keys));
    }

    public static void onEventDishRecommendColumn(String name) {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RECOMMEND_COLUMN + name);
    }

    public static void onEventDishRecommendGoShopcart() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RECOMMEND_GO_SHOP_CART);
    }

    public static void onEventDishRecommendGoDish() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RECOMMEND_GO_DISH_DETAIL);
    }

    public static void onEventDishRecommendGoRestaurant() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RECOMMEND_GO_RESTAURANT_DETAIL);
    }

    public static void onEventDishRestaurantListSetAddress() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RESTAURANT_LIST_SET_ADDRESS);
    }

    public static void onEventDishRestaurantListGoShopcart() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RESTAURANT_LIST_GO_SHOP_CAR);
    }

    public static void onEventDishRestaurantListGoDish() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RESTAURANT_LIST_GO_DISH_DETAIL);
    }

    public static void onEventDishRestaurantListGoRestaurant() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RESTAURANT_LIST_GO_RESTAURANT_DETAIL);
    }

    public static void onEventDishAddressSettingSearch() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ADDRESS_SETTING_SEARCH);
    }

    public static void onEventDishAddressSettingAutoLocation() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ADDRESS_SETTING_AUTO_LOCATION);
    }

    public static void onEventDishDetailGoShopcart() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_GO_SHOP_CART);
    }

    public static void onEventDishDetailGoRestaurantFromBottom() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_GO_RESTARUANT_FROM_BOTTOM);
    }

    public static void onEventDishDetailGoRestaurantFromInfo() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_GO_RESTARUANT_FROM_INFO);
    }

    public static void onEventDishDetailAddShopCart() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_ADD_SHOP_CART);
    }

    public static void onEventDishDetailMore() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_MORE);
    }

    public static void onEventDishDetailGroupDish() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_GROUP_DISH);
    }

    public static void onEventDishDetailGoOtherDish() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_GO_OTHER_DISH);
    }

    public static void onEventDishDetailTabEvaluate() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_TAB_EAVALUTE);
    }

    public static void onEventDishDetailTabInfo() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_TAB_INFO);
    }

    public static void onEventDishDetailTabCook() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_TAB_COOK);
    }

    public static void onEventDishDetailTabShare() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_DETAIL_SHARE);
    }

    public static void onEventDishRestaurantDetailGoShopcart() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RESTAURANT_DETAIL_GO_SHOP_CART);
    }

    public static void onEventDishRestaurantDetailGoEvaluate() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RESTAURANT_DETAIL_GO_EVALUATE);
    }

    public static void onEventDishRestaurantDetailGoSale() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RESTAURANT_DETAIL_GO_SALE);
    }

    public static void onEventDishRestaurantDetailGoNotice() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_RESTAURANT_DETAIL_GO_NOTICE);
    }

    public static void onEventDishShopCartDoCheck() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_SHOP_CART_DO_CHECK);
    }

    public static void onEventDishShopCartEdit() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_SHOP_CART_EDIT);
    }

    public static void onEventDishShopCartDelete() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_SHOP_CART_DELETE);
    }

    public static void onEventDishShopCartGoDish() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_SHOP_CART_GO_DISH);
    }

    public static void onEventDishShopCartGoRestaurant() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_SHOP_CART_GO_RESTAURANT);
    }

    public static void onEventDishOrderCheckDo(String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("创建订单状态", msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ORDER_CHECK_DO_CREATE, new JSONObject(keys));
    }

    public static void onEventDishPayListDo(String payName, String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("支付类型", payName);
        keys.put("支付状态", msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_PAY_DO, new JSONObject(keys));
    }

    public static void onEventDishOrderListPay(String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("支付状态", msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ORDER_LIST_PAY, new JSONObject(keys));
    }

    public static void onEventDishOrderListEvaluate() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ORDER_LIST_EVALUTE);
    }

    public static void onEventDishOrderListGot(String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("状态", msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ORDER_LIST_GOT, new JSONObject(keys));
    }

    public static void onEventDishOrderListDetail() {
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ORDER_LIST_DETAIL);
    }

    public static void onEventDishOrderDetailCancel(String reason) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("取消原因", reason);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ORDER_DETAIL_CANCEL, new JSONObject(keys));
    }

    public static void onEventDishOrderDetailAgain(String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("状态", msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ORDER_DETAIL_AGAIN, new JSONObject(keys));
    }

    public static void onEventDishOrderDetailPay(String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("支付状态", msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_DISH_ORDER_DETAIL_PAY, new JSONObject(keys));
    }

    public static void onEventLoginReqVerityCode(String phone, String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("手机号", phone);
        keys.put("时间", System.currentTimeMillis());
        keys.put("接口状态", msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_LOGIN_REQ_VERITY_CODE, new JSONObject(keys));
    }

    public static void onEventLoginUseVerityCode(String phone, String code, String msg) {
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put("手机号", phone);
        keys.put("验证码", code);
        keys.put("时间", System.currentTimeMillis());
        keys.put("接口状态", msg);
        ZhugeSDK.getInstance().onEvent(BaseApp.getApplication(), LOG_ZHUGE.EV_LOGIN_USE_VERITY_CODE, new JSONObject(keys));
    }

    /**
     * 诸葛IO
     */
    public static class LOG_ZHUGE {
        /**
         * 菜谱搜索
         * 1.[搜]首页右上角搜索
         * 2.[搜]执行搜索－－[搜]用户输入
         * 3.[搜]执行搜索－－[搜]热门搜索词
         * 4.[搜]搜索结果点击－－[搜]点击位置
         * 5.[搜]搜索建议点击－－[搜]点击位置
         */
        public static final String EV_SEARCH_RECOMMEND = "[搜]首页右上角搜索";

        public static final String EV_SEARCH_DO = "[搜]执行搜索";
        public static final String EV_SEARCH_DO_KEY_USER_INPUT = "[搜]用户输入";
        public static final String EV_SEARCH_DO_KEY_HOT_TAG = "[搜]热门搜索词";

        public static final String EV_SEARCH_RESULT = "[搜]搜索结果点击";
        public static final String EV_SEARCH_KEY_POSITION = "[搜]点击位置";

        public static final String EV_SEARCH_SUGGESTION = "[搜]搜索建议点击";

        /**
         * 标签系统
         * 1.[签]⾸⻚推荐标签－－[签]标签名
         * 2.[签]更多标签
         * 3.[签]标签点选－－[签]所属分类 、[签]标签名
         * 4.[签]标签菜谱点击－－[签]菜谱名
         */
        public static final String EV_TAG_RECOMMEND = "[签]⾸⻚推荐标签";
        public static final String EV_TAG_KEY_NAME = "[签]标签名";

        public static final String EV_TAG_MORE = "[签]更多标签";

        public static final String EV_TAG_SELECT = "[签]标签点选";
        public static final String EV_TAG_SELECT_KEY_TYPE = "[签]所属分类";

        public static final String EV_TAG_RECIPE = "[签]标签菜谱点击";
        public static final String EV_TAG_RECIPE_KEY_RECIPE_NAME = "[签]菜谱名";

        /**
         * 下厨房
         * 1.[厨]下厨房－－[厨]入口
         * 2.[厨]可做菜品－－[厨]挑选的食材、[厨]可做菜品个数
         * 3.[厨]搜索－－[厨]关键字、[厨]是否有数据、[厨]挑选的食材
         * 4.[厨]搜索结果点击－－[厨]关键字、[厨]是否有数据、[厨]点击位置、[厨]名字
         */
        public static final String EV_GARNISH_GOIN = "[厨]下厨房";
        public static final String EV_GARNISH_GOIN_KEY_FROM = "[厨]入口";

        public static final String EV_GARNISH_KEY_INGREDIENT = "[厨]挑选的食材";

        public static final String EV_GARNISH_RECIPE = "[厨]可做菜品";
        public static final String EV_GARNISH_KEY_RECIPE_COUNT = "[厨]可做菜品个数";

        public static final String EV_GARNISH_SEARCH = "[厨]搜索";
        public static final String EV_GARNISH_SEARCH_KEY_INPUT = "[厨]关键字";
        public static final String EV_GARNISH_SEARCH_KEY_STATE = "[厨]是否有数据";
        public static final String EV_GARNISH_SEARCH_RESULT = "[厨]搜索结果点击";
        public static final String EV_GARNISH_SEARCH_RESULT_KEY_POSITION = "[厨]点击位置";
        public static final String EV_GARNISH_SEARCH_RESULT_KEY_NAME = "[厨]名字";

        /**
         * 晒厨艺
         * 1.[晒]晒厨艺－－[晒]入口
         * 2.[晒]下一步
         * 3.[晒]发布－－[晒]是否成功、[晒]返回内容
         * 4.[晒]更多晒厨艺
         * 5.[晒]打分－－[晒]厨艺名、[晒]分值、[晒]是否成功、[晒]返回内容
         * 6.[晒]评论－－[晒]厨艺名、[晒]是否成功、[晒]返回内容
         * 7.[晒]分享－－[晒]厨艺名、[晒]分享到、[晒]是否成功、[晒]返回内容
         */
        public static final String EV_COOKSHOW_GOIN = "[晒]晒厨艺";
        public static final String EV_COOKSHOW_GOIN_KEY_FROM = "[晒]入口";

        public static final String EV_COOKSHOW_NEXT = "[晒]下一步";
        public static final String EV_COOKSHOW_PUBLISH = "[晒]发布";
        public static final String EV_COOKSHOW_MORE = "[晒]更多晒厨艺";
        public static final String EV_COOKSHOW_SCORE = "[晒]打分";
        public static final String EV_COOKSHOW_SCORE_NUMBER = "[晒]分值";
        public static final String EV_COOKSHOW_COMMENT = "[晒]评论";
        public static final String EV_COOKSHOW_SHARE = "[晒]分享";
        public static final String EV_COOKSHOW_SHARE_KEY_TO = "[晒]分享到";
        public static final String EV_COOKSHOW_KEY_NAME = "[晒]厨艺名";
        public static final String EV_COOKSHOW_KEY_STATE = "[晒]是否成功";
        public static final String EV_COOKSHOW_KEY_MSG = "[晒]返回内容";

        /**
         * 传菜谱
         * 1.[传]传菜谱－－[传]入口
         * 2.[传]发布－－[传]是否成功、[传]返回内容
         * 3.[传]预览
         */
        public static final String EV_PUBLISH_GOIN = "[传]传菜谱";
        public static final String EV_PUBLISH_GOIN_KEY_FROM = "[传]入口";
        public static final String EV_PUBLISH_DO = "[传]发布";
        public static final String EV_PUBLISH_KEY_STATE = "[传]是否成功";
        public static final String EV_PUBLISH_KEY_MSG = "[传]返回内容";
        public static final String EV_PUBLISH_REVIEW = "[传]预览";

        /**
         * 菜谱
         * 1.[谱]收藏－－[谱]是否成功、[谱]菜谱名
         * 2.[谱]分享－－[谱]是否成功、[谱]菜谱名
         * 3.[谱]评论－－[谱]是否成功、[谱]菜谱名
         */
        public static final String EV_RECIPE_FAV = "[谱]收藏";
        public static final String EV_RECIPE_SHARE = "[谱]分享";
        public static final String EV_RECIPE_COMMENT = "[谱]评论";
        public static final String EV_RECIPE_GET_MORE = "[谱]查看更多";
        public static final String EV_RECIPE_KEY_NAME = "[谱]菜谱名";
        public static final String EV_RECIPE_KEY_STATE = "[谱]是否成功";

        /**
         * 新鲜事
         * 1.[鲜]收藏－－[鲜]新鲜事名、[鲜]是否成功
         * 2.[鲜]分享－－[鲜]新鲜事名、[鲜]是否成功
         * 3.[鲜]评论－－[鲜]新鲜事名、[鲜]是否成功
         * 4.[鲜]更多新鲜事
         */
        public static final String EV_TOPIC_FAV = "[鲜]收藏";
        public static final String EV_TOPIC_SHARE = "[鲜]分享";
        public static final String EV_TOPIC_COMMENT = "[鲜]评论";
        public static final String EV_TOPIC_MORE = "[鲜]更多新鲜事";
        public static final String EV_TOPIC_KEY_NAME = "[鲜]新鲜事名";
        public static final String EV_TOPIC_KEY_STATE = "[鲜]是否成功";

        /**
         * 去哪儿
         * 1.[哪]收藏－－[哪]活动名、[哪]是否成功
         * 2.[哪]分享－－[哪]活动名、[哪]是否成功
         * 3.[哪]评论－－[哪]活动名、[哪]是否成功
         * 4.[哪]更多活动
         */
        public static final String EV_PARTY_FAV = "[哪]收藏";
        public static final String EV_PARTY_SHARE = "[哪]分享";
        public static final String EV_PARTY_COMMENT = "[哪]评论";
        public static final String EV_PARTY_MORE = "[哪]更多活动";
        public static final String EV_PARTY_KEY_NAME = "[哪]活动名";
        public static final String EV_PARTY_KEY_STATE = "[哪]是否成功";

        /**
         * 我
         * 1.[我]头像
         * 2.[我]我的消息
         * 3.[我]我的收藏
         * 4.[我]我的厨艺
         * 5.[我]我的菜谱
         * 6.[我]我的草稿
         * 7.[我]我的优惠码
         * 8.[我]我的钱包
         * 9.[我]我的订单
         * 10.[我]获得优惠
         * 11.[我-订单]待付款
         * 12.[我-订单]待发货
         * 13.[我-订单]配送中
         * 14.[我-订单]待评价
         * 15.[我-订单]退款
         */
        public static final String EV_MY_AVATAR = "[我]头像";
        public static final String EV_MY_MESSAGE = "[我]我的消息";
        public static final String EV_MY_FAV = "[我]我的收藏";
        public static final String EV_MY_COOK_SHOW = "[我]我的厨艺";
        public static final String EV_MY_RECIPE = "[我]我的菜谱";
        public static final String EV_MY_DRAFT = "[我]我的草稿";
        public static final String EV_MY_WALLET = "[我]我的钱包";
        public static final String EV_MY_COUPON = "[我]我的优惠券";
        public static final String EV_MY_ADDRESS = "[我]我的地址";
        public static final String EV_MY_ORDER = "[我]我的订单";
        public static final String EV_MY_GAIN_COUPON = "[我]获得优惠";
        public static final String EV_MY_ORDER_OBLIGATION = "[我-订单]待付款";
        public static final String EV_MY_ORDER_NO_DELIVER = "[我-订单]待发货";
        public static final String EV_MY_ORDER_SHIPPING = "[我-订单]配送中";
        public static final String EV_MY_ORDER_NO_EVALUATE = "[我-订单]待评价";
        public static final String EV_MY_ORDER_REUND = "[我-订单]退款";
        public static final String EV_MY_CALL = "[我]客服电话";

        /**
         * 买菜帮手卡片
         */
        public static final String EV_DISH_CARD_MAIN = "[帮]点击主卡片";
        public static final String EV_DISH_CARD_COLUMN = "[帮]栏目－";
        public static final String EV_DISH_CARD_MORE = "[帮]滑动查看更多";

        /**
         * 买菜帮手首页推荐
         */
        public static final String EV_DISH_RECOMMEND_SEARCH = "[帮-首页]搜索";
        public static final String EV_DISH_RECOMMEND_BANNER = "[帮-首页]Banner";
        public static final String EV_DISH_RECOMMEND_KEY_NAME = "[帮-首页]标题";
        public static final String EV_DISH_RECOMMEND_COLUMN = "[帮-首页]栏目-";
        public static final String EV_DISH_RECOMMEND_GO_DISH_DETAIL = "[帮-首页]进入菜品详情";
        public static final String EV_DISH_RECOMMEND_GO_RESTAURANT_DETAIL = "[帮-首页]进入餐厅详情";
        public static final String EV_DISH_RECOMMEND_GO_SHOP_CART = "[帮-首页]进入购物车";

        /**
         * 特色餐厅列表
         */
        public static final String EV_DISH_RESTAURANT_LIST_SET_ADDRESS = "[帮-餐厅列表]设置收菜地址";
        public static final String EV_DISH_RESTAURANT_LIST_GO_SHOP_CAR = "[帮-餐厅列表]进入购物车";
        public static final String EV_DISH_RESTAURANT_LIST_GO_RESTAURANT_DETAIL = "[帮-餐厅列表]进入餐厅";
        public static final String EV_DISH_RESTAURANT_LIST_GO_DISH_DETAIL = "[帮-餐厅列表]进入菜品";

        /**
         * 地址设置界面
         */
        public static final String EV_DISH_ADDRESS_SETTING_SEARCH = "[帮-地址设置]搜索";
        public static final String EV_DISH_ADDRESS_SETTING_AUTO_LOCATION = "[帮-地址设置]自动定位";

        /**
         * 菜品详情页面
         */
        public static final String EV_DISH_DETAIL_GO_SHOP_CART = "[帮-菜品详情]进入购物车";
        public static final String EV_DISH_DETAIL_GO_RESTARUANT_FROM_BOTTOM = "[帮-菜品详情]从底部进入餐厅";
        public static final String EV_DISH_DETAIL_GO_RESTARUANT_FROM_INFO = "[帮-菜品详情]从信息进入餐厅";
        public static final String EV_DISH_DETAIL_ADD_SHOP_CART = "[帮-菜品详情]添加购物车";
        public static final String EV_DISH_DETAIL_MORE = "[帮-菜品详情]查看更多菜品";
        public static final String EV_DISH_DETAIL_GROUP_DISH = "[帮-菜品详情]查看包含的菜品";
        public static final String EV_DISH_DETAIL_GO_OTHER_DISH = "[帮-菜品详情]查看其他菜品";
        public static final String EV_DISH_DETAIL_TAB_EAVALUTE = "[帮-菜品详情]查看评价";
        public static final String EV_DISH_DETAIL_TAB_INFO = "[帮-菜品详情]查看信息";
        public static final String EV_DISH_DETAIL_TAB_COOK = "[帮-菜品详情]查看做法";
        public static final String EV_DISH_DETAIL_SHARE = "[帮-菜品详情]分享";

        /**
         * 餐厅详情页面
         */
        public static final String EV_DISH_RESTAURANT_DETAIL_GO_SHOP_CART = "[帮-餐厅详情]进入购物车";
        public static final String EV_DISH_RESTAURANT_DETAIL_GO_EVALUATE = "[帮-餐厅详情]进入评论";
        public static final String EV_DISH_RESTAURANT_DETAIL_GO_SALE = "[帮-餐厅详情]进入月销";
        public static final String EV_DISH_RESTAURANT_DETAIL_GO_NOTICE = "[帮-餐厅详情]查看公告";

        /**
         * 购物车页面
         */
        public static final String EV_DISH_SHOP_CART_DO_CHECK = "[帮-购物车]去结算";
        public static final String EV_DISH_SHOP_CART_EDIT = "[帮-购物车]编辑";
        public static final String EV_DISH_SHOP_CART_DELETE = "[帮-购物车]删除";
        public static final String EV_DISH_SHOP_CART_GO_DISH = "[帮-购物车]进入菜品";
        public static final String EV_DISH_SHOP_CART_GO_RESTAURANT = "[帮-购物车]进入餐厅";

        /**
         * 购物车页面
         */
        public static final String EV_DISH_ORDER_CHECK_DO_CREATE = "[帮-确定订单]提交订单";

        /**
         * 支付页面
         */
        public static final String EV_DISH_PAY_DO = "[帮-支付列表]去支付";

        /**
         * 订单列表
         */
        public static final String EV_DISH_ORDER_LIST_PAY = "[帮-订单列表]点击支付";
        public static final String EV_DISH_ORDER_LIST_EVALUTE = "[帮-订单列表]点击评价";
        public static final String EV_DISH_ORDER_LIST_GOT = "[帮-订单列表]点击确定收货";
        public static final String EV_DISH_ORDER_LIST_DETAIL = "[帮-订单列表]进入详情";

        /**
         * 订单详情
         */
        public static final String EV_DISH_ORDER_DETAIL_CANCEL = "[帮-订单详情]点击取消订单";
        public static final String EV_DISH_ORDER_DETAIL_AGAIN = "[帮-订单详情]点击再下一单";
        public static final String EV_DISH_ORDER_DETAIL_PAY = "[帮-订单详情]点击支付";

        /**
         * 登录状态
         */
        public static final String EV_LOGIN_REQ_VERITY_CODE = "[登录]请求验证码";
        public static final String EV_LOGIN_USE_VERITY_CODE = "[登录]使用验证码";
    }

    public static class LOG_YOUMENG {

    }

    public static class MARK {
        public static final String SHARE_DATA = "share";
        public static final String FROM = "from";
        public static final String DURATION_EVENT = "duration_event";
    }
}
