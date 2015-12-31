package com.wecook.sdk.api.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;
import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.dbprovider.tables.MessageTable;

import org.json.JSONException;

/**
 * 聊天信息
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/10/14
 */
public class Message extends ApiModel {

    public static final String TYPE_NOTIFY = "notification";
    public static final String TYPE_SYSTEM = "system";
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_RECIPE = "recipe";
    public static final String TYPE_TOPIC = "topic";
    public static final String TYPE_EVENTS = "events";
    public static final String TYPE_COOKING = "cooking";
    public static final String TYPE_DISHES = "dishes";
    public static final String TYPE_ORDER = "order";

    public static final String TYPE_INGREDIENT = KitchenApi.TYPE_INGREDIENT;
    public static final String TYPE_KITCHENWARE = KitchenApi.TYPE_KITCHENWARE;
    public static final String TYPE_CONDIMENT = KitchenApi.TYPE_CONDIMENT;
    public static final String TYPE_BARCODE = KitchenApi.TYPE_BARCODE;

    @SerializedName("content")
    private String content;

    @SerializedName("uid")
    private String uid;

    @SerializedName("id")
    private String id;

    @SerializedName("create_time")
    private String createTime;

    @SerializedName("type")
    private String type;

    @SerializedName("foreign_id")
    private String foreignId;

    @SerializedName("info")
    private String info;

    @SerializedName("is_read")
    private String isRead;

    @SerializedName("image")
    private String image;

    @SerializedName("url")
    private String url;

    private User sender;

    public Message() {
    }

    public Message(MessageTable.MessageDB db) {
        content = db.content;
        id = db.messageId;
        createTime = db.createTime;
        uid = db.receiverId;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonObject jsonObject = JsonUtils.getJsonObject(json);
        uid = JsonUtils.getModelItemAsString(jsonObject, "uid");
        id = JsonUtils.getModelItemAsString(jsonObject, "id");
        content = JsonUtils.getModelItemAsString(jsonObject, "content");
        createTime = JsonUtils.getModelItemAsString(jsonObject, "create_time");
        type = JsonUtils.getModelItemAsString(jsonObject, "type");
        foreignId = JsonUtils.getModelItemAsString(jsonObject, "foreign_id");
        info = JsonUtils.getModelItemAsString(jsonObject, "info");
        isRead = JsonUtils.getModelItemAsString(jsonObject, "is_read");
        image = JsonUtils.getModelItemAsString(jsonObject, "image");
        url = JsonUtils.getModelItemAsString(jsonObject, "url");
        sender = JsonUtils.getModelItemAsObject(jsonObject, "sender", new User());
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean isRead() {
        return "0".equals(isRead);
    }

    public void setIsRead(boolean isRead) {
        if (isRead) {
            this.isRead = "0";
        } else {
            this.isRead = "1";
        }
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
