package com.wecook.sdk.policy;

/**
 * 统计
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/22/14
 */
public class LogConstant {

    public static final String SOURCE_SEARCH = "搜索";
    public static final String SOURCE_CATEGORY = "分类";
    public static final String SOURCE_GARNISH = "厨房能做的菜";
    public static final String SOURCE_INSPIRE_SEARCH = "引导搜索";
    public static final String SOURCE_RECOMMEND_TAG = "首页推荐TAG";
    public static final String SOURCE_RECOMMEND_MORE = "首页推荐更多";
    public static final String SOURCE_HOT_RECOMMEND = "精品推荐";
    public static final String SOURCE_SPECIAL_INGREDIENT = "食材专用菜谱";
    public static final String SOURCE_BANNER = "运营Banner";

    public static final String TO_WEIBLOG = "微博";
    public static final String TO_WEIXIN = "微信";
    public static final String TO_WEIXIN_FIRENTS = "朋友圈";
    public static final String TO_INSPIRE_SEARCH = "引导式搜索";
    public static final String TO_MY_INGREDIENTS = "我的食材";

    public static final String RESULT_OK = "有结果";
    public static final String RESULT_FAIL = "无结果";

    public static final String FROM_INGREIDENT_SEARCH = "食材搜索";
    public static final String FROM_INGREIDENT_LIST = "食材列表";

    public static final String TYPE_INGREDIENT = "食材";
    public static final String TYPE_CONDIANT = "调料";
    public static final String TYPE_WARE = "厨具";
    public static final String TYPE_BARCODE = "扫码";

    public static final String KEY_URI = "uri";
    public static final String KEY_NAME = "name";
    public static final String KEY_TO = "to";
    public static final String KEY_KEY = "key";
    public static final String KEY_FROM = "from";
    public static final String KEY_TYPE = "type";
    public static final String KEY_RESULT = "result";
    public static final String KEY_SOURCE = "source";
    public static final String KEY_BARCODE = "barcode";
    public static final String KEY_INDEX = "index";
    public static final String KEY_SELECTED = "selected";
    public static final String KEY_RECIPE_COUNT = "recipescount";

    /**
     * 程序启动次数
     */
    public static final String UBS_APP_START_COUNT = "UBS_APP_START_COUNT";

    /**
     * 首页搜索点击次数
     */
    public static final String UBS_HOME_SEARCH_COUNT = "UBS_HOME_SEARCH_COUNT";

    /**
     * 首页banner点击次数
     */
    public static final String UBS_HOME_BANNER_TAP_COUNT = "UBS_HOME_BANNER_TAP_COUNT";

    /**
     * 首页运营分类点击次数
     */
    public static final String UBS_HOME_CATEGORY_TAP_COUNT = "UBS_HOME_CATEGORY_TAP_COUNT";

    /**
     * 首页专题文章点击次数
     */
    public static final String UBS_HOME_TOPIC_TAP_COUNT = "UBS_HOME_TOPIC_TAP_COUNT";

    /**
     * 首页更多精选菜谱点击次数
     */
    public static final String UBS_HOME_MORE_TAP_COUNT = "UBS_HOME_MORE_TAP_COUNT";

    /**
     * 菜谱搜索结果点击次数
     */
    public static final String UBS_RECIPESEARCH_TO_RECIPE_COUNT = "UBS_RECIPESEARCH_TO_RECIPE_COUNT";

    /**
     * 菜谱搜索结果为空次数
     */
    public static final String UBS_RECIPESEARCH_RESULT_EMPTY_COUNT = "UBS_RECIPESEARCH_RESULT_EMPTY_COUNT";

    /**
     * 一级分类点击次数
     */
    public static final String UBS_CATEGORY_LEVEL1_TAP_COUNT = "UBS_CATEGORY_LEVEL1_TAP_COUNT";

    /**
     * 二级分类点击次数
     */
    public static final String UBS_CATEGORY_LEVEL2_TAP_COUNT = "UBS_CATEGORY_LEVEL2_TAP_COUNT";

    /**
     * 菜谱列表打开次数
     */
    public static final String UBS_RECIPELIST_OPEN_COUNT = "UBS_RECIPELIST_OPEN_COUNT";

    /**
     * 菜谱列表页翻页次数
     */
    public static final String UBS_RECIPELIST_NEXTPAGE_COUNT = "UBS_RECIPELIST_NEXTPAGE_COUNT";

    /**
     * 菜谱添加收藏次数
     */
    public static final String UBS_RECIPELIST_DO_LIKE_COUNT = "UBS_RECIPELIST_DO_LIKE_COUNT";

    /**
     * 菜谱取消收藏次数
     */
    public static final String UBS_RECIPELIST_DO_UNLIKE_COUNT = "UBS_RECIPELIST_DO_UNLIKE_COUNT";

    /**
     * 点击菜谱进入详情页的次数
     */
    public static final String UBS_RECIPELIST_TAP_COUNT = "UBS_RECIPELIST_TAP_COUNT";

    /**
     * 菜谱详情页被分享打开的总次数
     */
    public static final String UBS_RECIPE_SHARE_COUNT = "UBS_RECIPE_SHARE_COUNT";

    /**
     * 吃货去哪列表内文章点击次数
     */
    public static final String UBS_TOPIC_EVENTLIST_TAP_COUNT = "UBS_TOPIC_EVENTLIST_TAP_COUNT";

    /**
     * 吃货新鲜事列表内文章点击次数
     */
    public static final String UBS_TOPIC_NEWSLIST_TAP_COUNT = "UBS_TOPIC_NEWSLIST_TAP_COUNT";

    /**
     * 吃货去哪文章分享次数
     */
    public static final String UBS_TOPIC_EVENT_SHARE_COUNT = "UBS_TOPIC_EVENT_SHARE_COUNT";

    /**
     * 吃货新鲜事文章分享次数
     */
    public static final String UBS_TOPIC_NEWS_SHARE_COUNT = "UBS_TOPIC_NEWS_SHARE_COUNT";

    /**
     * 晒厨艺的总次数
     */
    public static final String UBS_MYCOOKING_SHARE_COUNT = "UBS_MYCOOKING_SHARE_COUNT";

    /**
     * 进入厨房的次数
     */
    public static final String UBS_KITCHEN_ENTER_COUNT = "UBS_KITCHEN_ENTER_COUNT";

    /**
     * 扫条码的次数
     */
    public static final String UBS_BARCODESEARCH_COUNT = "UBS_BARCODESEARCH_COUNT";

    /**
     * 进入厨房物品各种详情的次数
     */
    public static final String UBS_KITCHEN_ITEM_DETAILS_COUNT = "UBS_KITCHEN_ITEM_DETAILS_COUNT";

    /**
     * 厨房能做的菜搜索次数
     */
    public static final String UBS_KITCHEN_COOKSEARCH_COUNT = "UBS_KITCHEN_COOKSEARCH_COUNT";

    /**
     * 厨房物品专用菜谱点击次数
     */
    public static final String UBS_KITCHEN_ITEM_DETAILS_RECIPE_COUNT = "UBS_KITCHEN_ITEM_DETAILS_RECIPE_COUNT";

    /**
     * 添加组菜食材的次数
     */
    public static final String UBS_KITCHEN_GUIDEDSEARCH_ADD_COUNT = "UBS_KITCHEN_GUIDEDSEARCH_ADD_COUNT";

    /**
     * 移除组菜食材的次数
     */
    public static final String UBS_KITCHEN_GUIDEDSEARCH_REMOVE_COUNT = "UBS_KITCHEN_GUIDEDSEARCH_REMOVE_COUNT";

    /**
     * 查看引导搜索结果次数
     */
    public static final String UBS_KITCHEN_GUIDEDSEARCH_GO_COUNT = "UBS_KITCHEN_GUIDEDSEARCH_GO_COUNT";

    /**
     * 晒厨艺－首页－我也晒厨艺(wyscy)
     */
    public static final String COOK_RECOMMEND_DO = "scy_sy_wyscy";

    /**
     * 晒厨艺－首页－更多晒厨艺(gdscy)
     */
    public static final String COOK_RECOMMEND_MORE = "scy_sy_gdscy";

    /**
     * 晒厨艺－详情－打分
     */
    public static final String COOK_DETAIL_SCORE = "scy_detail_df";
    /**
     * 晒厨艺－详情－分享
     */
    public static final String COOK_DETAIL_SHARE = "scy_detail_fx";
    /**
     * 晒厨艺－详情－相关菜谱
     */
    public static final String COOK_DETAIL_RELATION = "scy_detail_xgcp";
    /**
     * 晒厨艺－详情－评论
     */
    public static final String COOK_DETAIL_COMMENT = "scy_detail_pl";
    /**
     * 晒厨艺－详情－删除
     */
    public static final String COOK_DETAIL_DELETE = "scy_detail_sc";
    /**
     * 晒厨艺－详情－编辑
     */
    public static final String COOK_DETAIL_EDIT = "scy_detail_bj";
    /**
     * 晒厨艺－详情－举报
     */
    public static final String COOK_DETAIL_REPORT = "scy_detail_jb";

    /**
     * 晒厨艺－列表－打分
     */
    public static final String COOK_LIST_SCORE = "scy_list_df";
    /**
     * 晒厨艺－列表－分享
     */
    public static final String COOK_LIST_SHARE = "scy_list_fx";
    /**
     * 晒厨艺－列表－评论
     */
    public static final String COOK_LIST_COMMENT = "scy_list_pl";

    /**
     * 晒厨艺－动作－拍照
     */
    public static final String COOK_ACTION_PHOTO = "scy_action_pz";
    /**
     * 晒厨艺－动作－选取图片
     */
    public static final String COOK_ACTION_PICK = "scy_action_xqtp";
    /**
     * 晒厨艺－动作－下一步
     */
    public static final String COOK_ACTION_PICK_NEXT = "scy_action_xyb";
    /**
     * 晒厨艺－动作－发布
     */
    public static final String COOK_ACTION_PUBLISH = "scy_action_fb";
    /**
     * 晒厨艺－动作－标签
     */
    public static final String COOK_ACTION_TAG_SELECTED = "scy_action_bq";

    /**
     * 上传菜谱－首页－上传菜谱
     */
    public static final String NEW_RECIPE_RECOMMEND_ENTER = "sccp_sy_sccp";

    /**
     * 上传菜谱－名称－下一步
     */
    public static final String NEW_RECIPE_EDIT_NAME_NEXT = "sccp_mc_xyb";

    /**
     * 上传菜谱－名称－标签
     */
    public static final String NEW_RECIPE_EDIT_NAME_TAG = "sccp_mc_bq";

    /**
     * 上传菜谱－编辑详情－发布
     */
    public static final String NEW_RECIPE_EDIT_DETAIL_PUBLISH = "sccp_bjxq_fb";

    /**
     * 上传菜谱－编辑详情－预览
     */
    public static final String NEW_RECIPE_EDIT_DETAIL_REVIEW = "sccp_bjxq_yl";

    /**
     * 上传菜谱－编辑详情－存草稿
     */
    public static final String NEW_RECIPE_EDIT_DETAIL_DRAFT = "sccp_bjxq_ccg";

    /**
     * 上传菜谱－编辑详情－添加文本步骤
     */
    public static final String NEW_RECIPE_EDIT_DETAIL_ADD_TEXT_STEP = "sccp_bjxq_tjwbbz";

    /**
     * 上传菜谱－编辑详情－添加图片步骤
     */
    public static final String NEW_RECIPE_EDIT_DETAIL_ADD_IMAGE_STEP = "sccp_bjxq_tjtpbz";

    /**
     * 上传菜谱－我－我的菜谱
     */
    public static final String NEW_RECIPE_HOME_MYRECIPE = "sccp_home_wdcp";

    /**
     * 上传菜谱－我的菜谱－上传菜谱
     */
    public static final String NEW_RECIPE_MYRECIPE_ENTER = "sccp_wdcp_sccp";

    /**
     * 上传菜谱－我的菜谱－重新编辑
     */
    public static final String NEW_RECIPE_MYRECIPE_EDIT_AGAIN = "sccp_wdcp_cxbj";

    /**
     * 上传菜谱－我的菜谱－删除
     */
    public static final String NEW_RECIPE_MYRECIPE_DELETE = "sccp_wdcp_sc";

    /**
     * 上传菜谱－我－草稿箱
     */
    public static final String NEW_RECIPE_HOME_DRAFT = "sccp_home_cgx";

    /**
     * 上传菜谱－草稿箱－清理
     */
    public static final String NEW_RECIPE_DRAFT_CLEAR = "sccp_draft_ql";

    /**
     * 上传菜谱－草稿箱－列表项
     */
    public static final String NEW_RECIPE_DRAFT_ITEM_CLICK = "sccp_draft_item";

    /**
     * 首页推荐－Banner
     */
    public static final String RECOMMEND_BANNER_CLICK = "sytj_banner_clk";

    /**
     * 首页推荐－功能－买菜
     */
    public static final String RECOMMEND_SELL_CLICK = "sytj_mc_clk";

    /**
     * 首页推荐－功能－下厨房
     */
    public static final String RECOMMEND_GARNISH_CLICK = "sytj_xcf_clk";

    /**
     * 首页推荐－功能－晒厨艺
     */
    public static final String RECOMMEND_COOKSHOW_CLICK = "sytj_scy_clk";

    /**
     * 首页推荐－功能－上传菜谱
     */
    public static final String RECOMMEND_NEW_RECIPE_CLICK = "sytj_sccp_clk";

    /**
     * 我－我的消息
     */
    public static final String MY_MESSAGE_CLICK = "wo_msg_clk";

    /**
     * 我－push消息
     */
    public static final String MY_PUSH_CLICK = "wo_push_clk";

}
