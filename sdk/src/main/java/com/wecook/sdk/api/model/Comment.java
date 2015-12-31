package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.base.Praise;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class Comment extends Praise {

    @SerializedName("id")
    private String id;

    @SerializedName("content")
    private String content;

    @SerializedName("create_time")
    private String createTime;

    @SerializedName("score")
    private String score;

    private Dish dish;

    private User author;

    private User replyto;
    /**
     * 回复评论
     */
    private ApiModelList<Comment> replyList;
    /**
     * 晒图(小图)
     */
    private List<String> images;
    /**
     * 晒图(原图)
     */
    private List<String> images_origin;
    /**
     * 用于adapter 适配器数据
     */
    private List<Image> imageData;

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("author")) {
            author = new User();
            author.parseJson(jsonObject.optJSONObject("author").toString());
        }
        if (jsonObject.has("replyto")) {
            replyto = new User();
            replyto.parseJson(jsonObject.optJSONObject("replyto").toString());
        }

        if (jsonObject.has("dish")) {
            dish = new Dish();
            dish.parseJson(jsonObject.optJSONObject("dish").toString());
        }

        if (jsonObject.has("reply_list")) {
            replyList = new ApiModelList<>(new Comment());
            replyList.parseJson(jsonObject.optJSONArray("reply_list").toString());
        }


        Gson gson = new Gson();
        Comment comment = gson.fromJson(json, Comment.class);
        if (comment != null) {
            id = comment.id;
            content = comment.content;
            createTime = comment.createTime;
            score = comment.score;
            images = comment.images;
            images_origin = comment.images_origin;
//            if (images!=null&&images_origin!=null&&images.size()>0&&images_origin.size()>0) {
//                imageData = new ArrayList<>();
//                for (int i=0;i< images.size()&&i<images_origin.size();i++){
//                    Image image = new Image();
//                    image.setImage(images.get(i));
//                    image.setImage_origin(images_origin.get(i));
//                    imageData.add(image);
//                }
//            }

            copyPraise(comment, this);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getReplyto() {
        return replyto;
    }

    public void setReplyto(User replyto) {
        this.replyto = replyto;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public ApiModelList<Comment> getReplyList() {
        return replyList;
    }

    public void setReplyList(ApiModelList<Comment> replyList) {
        this.replyList = replyList;
    }

    public List<Image> getImageData() {
        return imageData;
    }

    public void setImageData(List<Image> imageData) {
        this.imageData = imageData;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getImages_origin() {
        return images_origin;
    }

    public void setImages_origin(List<String> images_origin) {
        this.images_origin = images_origin;
    }
}
