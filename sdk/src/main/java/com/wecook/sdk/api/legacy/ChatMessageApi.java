package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.Count;
import com.wecook.sdk.api.model.Message;
import com.wecook.sdk.api.model.Session;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 用户会话相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class ChatMessageApi extends Api {

    public static final String SYS_RECEIVER = "1";

    /**
     * 发送消息
     *
     * @param message
     * @param callback
     */
    public static void sendChatMessage(String message, ApiCallback<State> callback) {
        Api.get(ChatMessageApi.class)
                .with("/letter/reply")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("session_id", UserProperties.getUserChatSessionId(), true)
                .addParams("content", message, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 创建会话session
     * <p/>
     * 和系统对话 reciveId=1
     */
    public static void createChatSession(String reciveId, ApiCallback<Session> callback) {
        Api.get(ChatMessageApi.class)
                .with("/letter/create")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("recive", reciveId, true)
                .toModel(new Session())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获取新消息
     */
    public static void fetchNewMessage(int page, int pageSize, ApiCallback<ApiModelList<Message>> callback) {
        Api.get(ChatMessageApi.class)
                .with("/letter/discuss")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("session_id", UserProperties.getUserChatSessionId(), true)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .toModel(new ApiModelList<Message>(new Message()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获取推送消息
     */
    public static void fetchPushNotifyNewMessage(int page, int pageSize, ApiCallback<ApiModelList<Message>> callback) {
        Api.get(ChatMessageApi.class)
                .with("/notification")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", "" + page, true)
                .addParams("batch", "" + pageSize, true)
                .toModel(new ApiModelList<Message>(new Message()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得消息数量
     *
     * @param callback
     */
    public static void getPushNotifyNewMessageCount(ApiCallback<Count> callback) {
        Api.get(ChatMessageApi.class)
                .with("/notification/count")
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new Count())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 标记消息已读
     *
     * @param msgId
     * @param callback
     */
    public static void readPushNotifyMessage(String msgId, ApiCallback<State> callback) {
        Api.get(ChatMessageApi.class)
                .with("/notification/read")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("id", msgId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 标记全部消息已读
     *
     * @param msgId
     * @param callback
     */
    public static void readAllPushNotifyMessage(ApiCallback<State> callback) {
        Api.get(ChatMessageApi.class)
                .with("/notification/readall")
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 标记消息删除
     *
     * @param msgId
     * @param callback
     */
    public static void deletePushNotifyMessage(String msgId, ApiCallback<State> callback) {
        Api.get(ChatMessageApi.class)
                .with("/notification/delete")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("id", msgId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }


}
