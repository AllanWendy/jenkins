package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.CommentCount;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;

import java.util.List;

/**
 * 评论相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class CommentApi extends Api {

    /**
     * 菜谱收藏
     */
    public static final String TYPE_RECIPE = "recipe";
    /**
     * 食材、厨具、调味品
     */
    public static final String TYPE_CATEGORY = "category";
    /**
     * 扫码商品
     */
    public static final String TYPE_BARCODE = "barcode";
    /**
     * 吃货新鲜事儿
     */
    public static final String TYPE_TOPIC = "topic";
    /**
     * 线下活动
     */
    public static final String TYPE_EVENTS = "events";
    /**
     * 晒厨艺
     */
    public static final String TYPE_COOKING = "cooking";

    /**
     * 获得评论列表
     *
     * @param type
     * @param id
     * @param page
     * @param pageSize
     * @param apiCallback
     */
    public static void getCommentList(String type, String id, int page, int pageSize, ApiCallback<ApiModelList<Comment>> apiCallback) {
        Api.get(CommentApi.class)
                .with("/comment")
                .addParams("type", type + "", true)
                .addParams("foreign_id", id, true)
                .addParams("batch", pageSize + "", true)
                .addParams("page", page + "", true)
                .addParams("uid", UserProperties.getUserId() + "")
                .toModel(new ApiModelList<Comment>(new Comment()))
                .setApiCallback(apiCallback)
                .executeGet();
    }

    /**
     * 增加评论
     *
     * @param uid
     * @param type
     * @param foreignId 评论对象类型ID
     * @param replyUid  回复用户的ID
     * @param content
     * @param callback
     */
    public static void createComment(String uid, String type, String foreignId, String replyUid, String content, ApiCallback<State> callback) {
        Api.get(CommentApi.class)
                .with("/comment/create")
                .addParams("type", type, true)
                .addParams("foreign_id", foreignId, true)
                .addParams("content", content, true)
                .addParams("uid", uid, true)
                .addParams("reply_uid", replyUid)
                .toModel(new State())
                .setApiCallback(callback)
                .executePost();
    }

    /**
     * 删除评论
     *
     * @param commentId
     * @param callback
     */
    public static void deleteComment(String commentId, ApiCallback<State> callback) {
        Api.get(CommentApi.class)
                .with("/comment/delete")
                .addParams("id", commentId, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得餐厅评论列表
     *
     * @param restaurantId 餐厅ID
     * @param commentType  按照用户评分级别搜索评论，0全部,1好评，2中评，3差评
     * @param page         页码
     * @param pageSize     每页条数
     * @param callback
     */
    public static void getCommentListOfRestaurant(String restaurantId, int commentType,
                                                  int page, int pageSize,
                                                  ApiCallback<ApiModelList<Comment>> callback) {

        Api.get(CommentApi.class)
                .with("/comment/restaurant")
                .addParams("restaurant_id", restaurantId, true)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .addParams("type", "" + commentType, true)
                .toModel(new ApiModelList<Comment>(new Comment()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得餐厅评论列表
     *
     * @param dishId 餐厅ID
     * @param commentType  按照用户评分级别搜索评论，0全部,1好评，2中评，3差评
     * @param page         页码
     * @param pageSize     每页条数
     * @param callback
     */
    public static void getCommentListOfDish(String dishId, int commentType,
                                                  int page, int pageSize,
                                                  ApiCallback<ApiModelList<Comment>> callback) {

        Api.get(CommentApi.class)
                .with("/comment/dishes")
                .addParams("foreign_id", dishId, true)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .addParams("type", "" + commentType, true)
                .toModel(new ApiModelList<Comment>(new Comment()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得餐厅评论数的概览
     *
     * @param restaurantId
     * @param callback
     */
    public static void getCommentRestaurantOverview(String restaurantId, ApiCallback<CommentCount> callback) {
        Api.get(CommentApi.class)
                .with("/comment/restaurant_overview")
                .addParams("restaurant_id", restaurantId, true)
                .toModel(new CommentCount())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得餐厅评论数的概览
     *
     * @param dishId
     * @param callback
     */
    public static void getCommentDishOverview(String dishId, ApiCallback<CommentCount> callback) {
        Api.get(CommentApi.class)
                .with("/comment/dish_overview")
                .addParams("dish_id", dishId, true)
                .toModel(new CommentCount())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 创建订单菜品的评论
     *
     * @param orderId            订单ID
     * @param dishIdsAndScores   菜品ID以及其对应评分，如：46,5;47,4。
     * @param dishComments 评论内容数组，按顺序对应到菜品ID和评分列表中，用户不输入则默认为"好评！"
     * @param callback
     */
    public static void createDishComment(String orderId, String dishIdsAndScores,
                                         List<String> dishComments, List<String> dishMediaIds, String deliveryScore, String deliveryContent, String anonymous, ApiCallback<State> callback) {
        Api.get(CommentApi.class)
                .with("/comment/dishes_create")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("order_id", orderId, true)
                .addParams("dish_ids_scores", dishIdsAndScores, true)
                .addParams("contents", dishComments, true)
                .addParams("dish_media_ids", dishMediaIds)
                .addParams("delivery_score", deliveryScore)
                .addParams("delivery_content", deliveryContent)
                .addParams("anonymous", anonymous)
                .toModel(new State())
                .setApiCallback(callback)
                .executePost();
    }
}
