package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 食谱
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodRecipe extends ApiModel {

    public static final String[] DIFFICULTY_LEVEL = {
        "初级（切墩）",
        "中级（配菜）",
        "高级（掌勺）",
        "专家（大厨）",
    };

    public static final String[] TIME_LEVEL = {
        "10分钟之内",
        "10-30分钟",
        "30-60分钟",
        "1小时以上",
    };

    @SerializedName("id")
    private String id = "";

    @SerializedName("title")
    private String title = "";

    @SerializedName("image")
    private String image = "";

    @SerializedName("image_large")
    private String imageOriginal = "";

    @SerializedName("description")
    private String description = "";

    @SerializedName("tips")
    private String tips = "";

    @SerializedName("tags")
    private String tags = "";

    @SerializedName("difficulty")
    private String difficulty = "";

    @SerializedName("spendtime")
    private String time = "";

    private ApiModelList<FoodIngredient> ingredientsList;

    private ApiModelList<FoodAssist> assistList;

    private ApiModelList<FoodStep> stepList;

    private User author;

    private String localImage;

    private String imageId;

    private String createTime;

    private String modifyTime;

    /**
     * 是否食材和菜谱完全命中
     */
    private boolean isTotallyHits;

    @Override
    public void parseJson(String json) throws JSONException {

        Gson gson = new Gson();
        FoodRecipe foodRecipe = gson.fromJson(json, FoodRecipe.class);
        if (foodRecipe != null) {
            id = foodRecipe.id;
            title = foodRecipe.title;
            description = foodRecipe.description;
            image = foodRecipe.image;
            imageOriginal = foodRecipe.imageOriginal;
            tips = foodRecipe.tips;
            tags = foodRecipe.tags;
            time = foodRecipe.time;
            difficulty = foodRecipe.difficulty;
        }

        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("ingredients")) {
            ingredientsList = new ApiModelList<FoodIngredient>(new FoodIngredient());
            ingredientsList.parseJson(json);
        }

        if (jsonObject.has("assist")) {
            assistList = new ApiModelList<FoodAssist>(new FoodAssist());
            assistList.parseJson(json);
        }

        if (jsonObject.has("step")) {
            stepList = new ApiModelList<FoodStep>(new FoodStep());
            stepList.parseJson(jsonObject.getJSONArray("step").toString());
        }

        if (jsonObject.has("author")) {
            author = new User();
            author.parseJson(jsonObject.opt("author").toString());
        }

        if (jsonObject.has("media")) {
            JSONObject media = jsonObject.optJSONObject("media");
            image = media.optString("image");
            imageId = media.optString("id");
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

    public String getImageOriginal() {
        return imageOriginal;
    }

    public String getLocalImage() {
        return localImage;
    }

    public void setLocalImage(String localImage) {
        this.localImage = localImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ApiModelList<FoodIngredient> getIngredientsList() {
        return ingredientsList;
    }

    public void setIngredientsList(ApiModelList<FoodIngredient> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    public ApiModelList<FoodAssist> getAssistList() {
        return assistList;
    }

    public void setAssistList(ApiModelList<FoodAssist> assistList) {
        this.assistList = assistList;
    }

    public ApiModelList<FoodStep> getStepList() {
        return stepList;
    }

    public void setStepList(ApiModelList<FoodStep> stepList) {
        this.stepList = stepList;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public boolean isTotallyHits() {
        return isTotallyHits;
    }

    public void setTotallyHits(boolean isTotallyHits) {
        this.isTotallyHits = isTotallyHits;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FoodRecipe) {
            if (!StringUtils.isEmpty(id)) {
                return id.equals(((FoodRecipe) o).getId());
            } else if (!StringUtils.isEmpty(createTime)) {
                return createTime.equals(((FoodRecipe) o).getCreateTime());
            } else {
                return this == o;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
