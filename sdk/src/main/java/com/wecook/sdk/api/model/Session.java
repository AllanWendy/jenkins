package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 会话
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/18/14
 */
public class Session extends ApiModel {

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("create_uid")
    private String createUid;

    @SerializedName("create_time")
    private String createTime;

    @SerializedName("update_time")
    private String updateTime;

    @Override
    public void parseJson(String json) throws JSONException {
        Session session = JsonUtils.getModel(json, Session.class);
        if (session != null) {
            sessionId = session.sessionId;
            createUid = session.createUid;
            createTime = session.createTime;
            updateTime = session.updateTime;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCreateUid() {
        return createUid;
    }

    public void setCreateUid(String createUid) {
        this.createUid = createUid;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
