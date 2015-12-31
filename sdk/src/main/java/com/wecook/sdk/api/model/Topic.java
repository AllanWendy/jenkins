package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.base.Favorite;

import org.json.JSONException;

/**
 * 专题
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/18/14
 */
public class Topic extends Favorite {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("comment")
    private String commentCount;

    @SerializedName("image")
    private String image;

    @SerializedName("create_time")
    private String createTime;

    @SerializedName("url")
    private String url;

    @SerializedName("content")
    private String content;


    @Override
    public void parseJson(String json) throws JSONException {
        Gson gson = new Gson();
        Topic tp = gson.fromJson(json, Topic.class);
        if (tp != null) {
            id = tp.id;
            title = tp.title;
            description = tp.description;
            image = tp.image;
            commentCount = tp.commentCount;
            createTime = tp.createTime;
            url = tp.url;
            content = tp.content;
            copyFavorite(tp, this);
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCommentCount() {
        return StringUtils.parseInt(commentCount);
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = "" + commentCount;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
