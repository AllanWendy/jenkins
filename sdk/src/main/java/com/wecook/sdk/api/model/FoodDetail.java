package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 菜谱详情
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodDetail extends ApiModel {

    @SerializedName("view")
    private String viewCount;

    @SerializedName("cooking")
    private String cookingCount;

    @SerializedName("favourite")
    private String favouriteCount;

    @SerializedName("comment")
    private String commentCount;

    @SerializedName("url")
    private String url;

    private String minPrice;

    private String maxPrice;

    private FoodRecipe foodRecipe;

    private ApiModelList<Comment> commentList;

    private ApiModelList<CookShow> cookShareList;


    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("recipe")) {
            JSONObject recipe = jsonObject.optJSONObject("recipe");
            this.foodRecipe = new FoodRecipe();
            this.foodRecipe.parseJson(recipe.toString());
        }

        if (jsonObject.has("range")) {
            JSONObject range = jsonObject.optJSONObject("range");
            minPrice = range.getString("min");
            maxPrice = range.getString("max");
        }

        FoodDetail foodDetail = JsonUtils.getModel(json, FoodDetail.class);
        if (foodDetail != null) {
            viewCount = foodDetail.viewCount;
            cookingCount = foodDetail.cookingCount;
            favouriteCount = foodDetail.favouriteCount;
            commentCount = foodDetail.commentCount;
            url = foodDetail.url;
        }

    }

    /**
     * 评论
     *
     * @param json
     * @throws JSONException
     */
    public void parseComment(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("list")) {
            commentList = new ApiModelList<Comment>(new Comment());
            commentList.parseJson(jsonObject.toString());
        }
    }

    /**
     * 厨艺
     *
     * @param json
     * @throws JSONException
     */
    public void parseCookShare(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("list")) {
            cookShareList = new ApiModelList<CookShow>(new CookShow());
            cookShareList.parseJson(jsonObject.toString());
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public FoodRecipe getFoodRecipe() {
        return foodRecipe;
    }

    public void setFoodRecipe(FoodRecipe foodRecipe) {
        this.foodRecipe = foodRecipe;
    }

    public ApiModelList<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(ApiModelList<Comment> commentList) {
        this.commentList = commentList;
    }

    public ApiModelList<CookShow> getCookShareList() {
        return cookShareList;
    }

    public void setCookShareList(ApiModelList<CookShow> cookShareList) {
        this.cookShareList = cookShareList;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getCookingCount() {
        return cookingCount;
    }

    public void setCookingCount(String cookingCount) {
        this.cookingCount = cookingCount;
    }

    public String getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(String favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public String getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(String minPrice) {
        this.minPrice = minPrice;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }
}
