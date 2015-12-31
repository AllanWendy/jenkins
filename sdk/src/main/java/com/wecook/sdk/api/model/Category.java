package com.wecook.sdk.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 分类标签
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public class Category extends ApiModel implements Parcelable {

    @SerializedName("id")
    private String id;

    @SerializedName("parent_id")
    private String parentId;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;


    @SerializedName("url")
    private String url;
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        //重写Creator
        @Override
        public Category createFromParcel(Parcel source) {
            Category category = new Category();
            category.id = source.readString();
            category.parentId = source.readString();
            category.title = source.readString();
            category.image = source.readString();
            category.url = source.readString();
            return category;
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
    private ApiModelList<Category> subCategory;

    @Override
    public void parseJson(String json) throws JSONException {
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(json);
        Category category = gson.fromJson(element, Category.class);
        if (category != null) {
            id = category.id;
            parentId = category.parentId;
            title = category.title;
            image = category.image;
            url = category.url;
        }
        if (element.getAsJsonObject().has("_")) {
            subCategory = new ApiModelList<Category>(new Category());
            subCategory.parseJson(json);
        }
    }

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("_")) {
            return jsonObject.optJSONArray("_");
        }

        return super.findJSONArray(json);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ApiModelList<Category> getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(ApiModelList<Category> subCategory) {
        this.subCategory = subCategory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", url='" + url + '\'' +
                ", subCategory=" + subCategory +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(parentId);
        dest.writeString(title);
        dest.writeString(image);
        dest.writeString(url);
    }


    public String getUrlKeyWords() {
        String result = "";
        if (null != url && url.contains("keywords=")) {
            result = url.substring(url.indexOf("keywords=") + 9);
        }
        return result;
    }

}
