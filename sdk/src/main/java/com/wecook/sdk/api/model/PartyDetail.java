package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.base.Favorite;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 活动详情
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/20/14
 */
public class PartyDetail extends Favorite {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    @SerializedName("content")
    private String content;

    @SerializedName("city")
    private String city;

    @SerializedName("price")
    private String price;

    @SerializedName("organizer")
    private String organizer;

    @SerializedName("source")
    private String source;

    @SerializedName("source_url")
    private String sourceUrl;

    @SerializedName("time_start")
    private String startTime;

    @SerializedName("time_end")
    private String endTime;

    @SerializedName("comment")
    private String commentCount;

    @SerializedName("favourite")
    private String favouriteCount;

    @SerializedName("url")
    private String url;

    private Location location;

    private Contact contact;

    private ApiModelList<User> favUsers;

    private ApiModelList<Comment> commentList;

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        id = jsonObject.optString("id");
        title = jsonObject.optString("title");
        image = jsonObject.optString("image");
        content = jsonObject.optString("content");
        city = jsonObject.optString("city");
        price = jsonObject.optString("price");
        organizer = jsonObject.optString("organizer");
        source = jsonObject.optString("source");
        sourceUrl = jsonObject.optString("source_url");
        startTime = jsonObject.optString("time_start");
        endTime = jsonObject.optString("time_end");
        commentCount = jsonObject.optString("comment");
        favouriteCount = jsonObject.optString("favourite");
        url = jsonObject.optString("url");
        isFavourite = jsonObject.optString("is_favourite");

        if (jsonObject.has("location")) {
            location = new Location();
            location.parseJson(jsonObject.getJSONArray("location").toString());
        }
        if (jsonObject.has("contact")) {
            contact = new Contact();
            contact.parseJson(jsonObject.optJSONObject("contact").toString());
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public String getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(String favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public ApiModelList<User> getFavUsers() {
        return favUsers;
    }

    public void setFavUsers(ApiModelList<User> favUsers) {
        this.favUsers = favUsers;
    }

    public ApiModelList<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(ApiModelList<Comment> commentList) {
        this.commentList = commentList;
    }
}
