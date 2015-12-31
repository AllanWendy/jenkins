package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.base.Praise;

import org.json.JSONException;


/**
 * 晒厨艺
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class CookShow extends Praise {

    private String id;
    private String title;
    private String image;
    private String description;
    private String createTime;
    private String commentCount;
    private String url;
    private String praiseScore;
    private ApiModelList<Tags> tags;
    private String recipeId;
    private User user;
    private ApiModelList<User> praiseList;

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null && element.isJsonObject()) {
            id = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "id");
            title = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "title");
            image = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "image");
            description = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "description");
            createTime = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "create_time");
            isPraised = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "is_praise");
            commentCount = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "comment");
            praise = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "praise");
            url = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "url");
            praiseScore = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "praise_score");
            user = JsonUtils.getModelItemAsObject(element.getAsJsonObject(), "account", new User());
            praiseList = JsonUtils.getModelItemAsList(element.getAsJsonObject(), "praise_member", new User());
            tags = JsonUtils.getModelItemAsList(element.getAsJsonObject(), "tags", new Tags());
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateTime() {
        return StringUtils.isEmpty(createTime) ? "0" : createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public int getCommentCount() {
        return StringUtils.parseInt(commentCount);
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = "" + commentCount;
    }

    public String getPraiseScore() {
        return StringUtils.isEmpty(praiseScore) ? "0" : praiseScore;
    }

    public void setPraiseScore(String praiseScore) {
        this.praiseScore = praiseScore;
    }

    public ApiModelList<User> getPraiseList() {
        return praiseList;
    }

    public void setPraiseList(ApiModelList<User> praiseList) {
        this.praiseList = praiseList;
    }

    public void setTags(ApiModelList<Tags> tags) {
        this.tags = tags;
    }

    public ApiModelList<Tags> getTags() {
        return tags;
    }

}