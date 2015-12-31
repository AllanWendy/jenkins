package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.base.Favorite;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 菜谱
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/7/14
 */
public class Food extends Favorite {
    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("tag")
    public String tag;

    @SerializedName("cooking")
    public String works;

    @SerializedName("view")
    public String view;

    @SerializedName("favourite")
    public String favourite;

    @SerializedName("price")
    public String price;

    @SerializedName("image")
    public String image;

    @SerializedName("create_time")
    public String createTime = "";

    private ApiModelList<Tags> tagList;

    @Override
    public void parseJson(String json) throws JSONException {

        JSONObject object = JsonUtils.getJSONObject(json);
        if (object.has("cover")) {
            image = object.getJSONObject("cover").optString("url");
        }
        if (object.has("works")) {
            works = object.optString("works");
        }

        id = object.optString("id");
        title = object.optString("title");
        tag = object.optString("tag");
        image = object.optString("image");
        price = object.optString("price");
        view = object.optString("view");
        works = object.optString("cooking");
        favourite = object.optString("favourite");
        createTime = object.optString("create_time");

        if (object.has("tags")) {
            tagList = new ApiModelList<Tags>(new Tags());
            tagList.parseJson(json);
        }

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setWorks(String works) {
        this.works = works;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setFavourite(String favourite) {
        this.favourite = favourite;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setTagList(ApiModelList<Tags> tagList) {
        this.tagList = tagList;
    }

    public String getTags() {
        String resultFormatTags = "";
        ApiModelList<Tags> tags = tagList;
        if (tags != null && !tags.isEmpty()) {
            for (Tags tag : tags.getList()) {
                resultFormatTags += "#" + tag.getName() + " ";
            }
        }
        return resultFormatTags;
    }

    public double getPrice() {
        if (StringUtils.isEmpty(price)) {
            return 0d;
        }
        try {
            return Double.parseDouble(price);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
