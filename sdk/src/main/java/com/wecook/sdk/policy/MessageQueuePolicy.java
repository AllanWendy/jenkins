package com.wecook.sdk.policy;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.wecook.common.app.BaseApp;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.api.legacy.ChatMessageApi;
import com.wecook.sdk.api.legacy.OrderApi;
import com.wecook.sdk.api.model.Count;
import com.wecook.sdk.api.model.Message;
import com.wecook.sdk.api.model.OrderCount;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 消息队列
 *
 * @author kevin
 * @version v1.0
 * @since 2015-3/9/15
 */
public class MessageQueuePolicy {

    public static final String ACTION_NEW_MESSAGE = "cn.wecook.app_new_message";

    public static final String ACTION_ORDER_NEW_MESSAGE = "cn.wecook.app_order_new_message";

    private static final String REQUEST_GET_NEW_MESSAGE = "message-queue";

    private List<Message> messageList = Collections.synchronizedList(new ArrayList<Message>());

    private static final int MIN_PAGE_SIZE = 20;

    private static MessageQueuePolicy sInstance;

    private  OrderCount mOrderCountMessage;

    private boolean isLooping;

    public static MessageQueuePolicy getInstance() {
        if (sInstance == null) {
            sInstance = new MessageQueuePolicy();
        }
        return sInstance;
    }

    /**
     * 是否有未读新消息
     *
     * @return
     */
    public boolean hasNewMessage() {
        return getNewMessageCount() != 0;
    }

    public  void setOrderCountMessage(OrderCount mOrderCountMessage) {
        this.mOrderCountMessage = mOrderCountMessage;
    }

    public  OrderCount getOrderCountMessage() {
        return mOrderCountMessage;
    }

    /**
     * 获得未读新消息数量
     *
     * @return
     */
    public int getNewMessageCount() {
        int count = 0;
        for (Message message : messageList) {
            if (!message.isRead()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获得所有消息数量
     *
     * @return
     */
    public int getAllMessageCount() {
        int count = getNewMessageCount();
        if (mOrderCountMessage != null) {
             count += mOrderCountMessage.getMessageCount();
        }

        return count;
    }

    /**
     * 已读消息
     *
     * @param messageId
     * @param callback
     */
    public void readMessage(String messageId, ApiCallback<State> callback) {
        if (UserProperties.isLogin()) {
            //请求已读通知
            ChatMessageApi.readPushNotifyMessage(messageId, callback);
        }
    }

    /**
     * 已读消息
     *
     * @param messageId
     */
    public void readMessage(final String messageId) {
        if (UserProperties.isLogin()) {
            //请求已读通知
            ChatMessageApi.readPushNotifyMessage(messageId, new ApiCallback<State>() {
                @Override
                public void onResult(State result) {
                    if (result.available()) {
                        for (Message message : messageList) {
                            if (message.getId().equals(messageId)) {
                                message.setIsRead(true);
                                return;
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 全部已读
     *
     * @param callback
     */
    public void readAllMessage(final ApiCallback<State> callback) {
        ChatMessageApi.readAllPushNotifyMessage(new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (result.available()) {
                    for (Message message : messageList) {
                        message.setIsRead(true);
                    }
                } else {
                    ToastAlarm.show("操作失败");
                }

                if (callback != null) {
                    callback.onResult(result);
                }
            }
        });
    }

    /**
     * 删除消息
     *
     * @param messageId
     * @param callback
     */
    public void deleteMessage(String messageId, ApiCallback<State> callback) {
        if (UserProperties.isLogin()) {
            Message msg = null;
            for (Message message : messageList) {
                if (messageId.equals(message.getId())) {
                    messageList.remove(message);
                    msg = message;
                    break;
                }
            }

            if (msg != null) {
                //请求删除消息
                ChatMessageApi.deletePushNotifyMessage(messageId, callback);
            }
        }
    }

    /**
     * 请求新消息列表
     *
     */
    public void requestNewMessage() {
        if (UserProperties.isLogin() && NetworkState.available()) {
            ChatMessageApi.getPushNotifyNewMessageCount(new ApiCallback<Count>() {
                @Override
                public void onResult(Count result) {

                    if (result.available()) {
                        if (result.count > 0) {
                            int count = result.count;
                            if (count < MIN_PAGE_SIZE) {
                                count = MIN_PAGE_SIZE;
                            }
                            ChatMessageApi.fetchPushNotifyNewMessage(1, count,
                                    new ApiCallback<ApiModelList<Message>>() {
                                        @Override
                                        public void onResult(ApiModelList<Message> result) {
                                            if (result.available()) {
                                                messageList.clear();
                                                messageList.addAll(result.getList());
                                                if (hasNewMessage()) {
                                                    Intent intent = new Intent(ACTION_NEW_MESSAGE);
                                                    LocalBroadcastManager.getInstance(BaseApp.getApplication())
                                                            .sendBroadcast(intent);
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                    OrderApi.getOrderCount(new ApiCallback<OrderCount>() {
                        @Override
                        public void onResult(OrderCount result) {

                            if (result.available()) {
                                setOrderCountMessage(result);
                                Intent intent = new Intent(ACTION_ORDER_NEW_MESSAGE);
                                LocalBroadcastManager.getInstance(BaseApp.getApplication())
                                        .sendBroadcast(intent);
                            }

                            startLoop();
                        }
                    });
                }
            });


        }
    }

    /**
     * 获得分页数据列表
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getNewMessageList(int page, int pageSize, final ApiCallback<ApiModelList<Message>> callback) {
        if (messageList.isEmpty()) {
            ChatMessageApi.fetchPushNotifyNewMessage(page, pageSize, new ApiCallback<ApiModelList<Message>>() {
                @Override
                public void onResult(ApiModelList<Message> result) {
                    if (result.available()) {
                        messageList.addAll(result.getList());
                    }

                    if (callback != null) {
                        callback.onResult(result);
                    }
                }
            });
        } else {
            ApiModelList<Message> list = new ApiModelList<Message>(new Message());
            list.setAvailable();
            page = page > 0 ? page - 1 : 0;
            for (int i = page; i < pageSize; i++) {
                Message message = ListUtils.getItem(messageList, i);
                if (message != null) {
                    list.add(message);
                }
            }
            if (callback != null) {
                callback.onResult(list);
            }
        }
    }

    public void clearMessageList() {
        messageList.clear();
        mOrderCountMessage = null;
    }

    private void startLoop() {
        if (isLooping) {
            return;
        }

        isLooping = true;

        UIHandler.loop(REQUEST_GET_NEW_MESSAGE, new UIHandler.LoopRunnable() {

            @Override
            public void run() {
                Logger.d("message-queue", "loop request new msg....");
                requestNewMessage();
            }

            @Override
            public void loopEnd() {
                Logger.d("message-queue", "loop end....");
                isLooping = false;
            }
        }, 60 * 1000);
    }

}
