package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.base.Favorite;

import org.json.JSONException;

/**
 * 新鲜事
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/18/14
 */
public class Party extends Favorite {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    @SerializedName("city")
    private String city;

    @SerializedName("time_start")
    private String startDate;

    @SerializedName("time_end")
    private String endDate;

    @SerializedName("url")
    private String url;

    @SerializedName("favourite")
    private String favouriteCount;

    @SerializedName("comment")
    private String commentCount;

    @Override
    public void parseJson(String json) throws JSONException {

        Gson gson = new Gson();
        Party party = gson.fromJson(json, Party.class);
        if (party != null) {
            id = party.id;
            title = party.title;
            image = party.image;
            url = party.url;
            city = party.city;
            startDate = party.startDate;
            endDate = party.endDate;
            favouriteCount = party.favouriteCount;
            commentCount = party.commentCount;
            copyFavorite(party, this);
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(String favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public int getCommentCount() {
        return StringUtils.parseInt(commentCount);
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = "" + commentCount;
    }
}
