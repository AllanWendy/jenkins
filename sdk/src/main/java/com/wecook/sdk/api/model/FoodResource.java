package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.base.Selector;
import com.wecook.sdk.dbprovider.tables.ResourceTable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 菜谱相关材料资源
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodResource extends Selector {

    @SerializedName("name")
    private String name = "";

    @SerializedName("quantity")
    private String quantity;

    @SerializedName("foreign_id")
    private String id;

    @SerializedName("type")
    private String type;

    @SerializedName("image")
    private String image;

    @SerializedName("title")
    private String title;

    @SerializedName("shelflife")
    private String shelflife;//加入厨房时长

    @SerializedName("description")
    private String description;

    @SerializedName("create_time")
    private String createTime;

    @SerializedName("url")
    private String url;


    private ApiModelList<FoodResourceExtends> mExtends;
    private ApiModelList<FoodResourceElements> mElements;

    @Override
    public void parseJson(String json) throws JSONException {
        Gson gson = new Gson();
        FoodResource source = gson.fromJson(json, FoodResource.class);
        if (source != null) {
            name = source.name;
            quantity = source.quantity;
            id = source.id;
            type = source.type;
            image = source.image;
            title = source.title;
            shelflife = source.shelflife;
            createTime = source.createTime;
            description = source.description;
            url = source.url;
            if (!StringUtils.isEmpty(title)
                    && !title.equals(name)) {
                name = title;
            }
        }

        JSONObject jsonObject = JsonUtils.getJSONObject(json);
        if (jsonObject != null) {
            if(jsonObject.has("extends")) {
                mExtends = new ApiModelList<FoodResourceExtends>(new FoodResourceExtends());
                mExtends.parseJson(jsonObject.opt("extends").toString());
            }

            if(jsonObject.has("elements")) {
                mElements = new ApiModelList<FoodResourceElements>(new FoodResourceElements());
                mElements.parseJson(jsonObject.opt("elements").toString());
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.title = name;
    }

    public String getQuality() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getShelflife() {
        return shelflife;
    }

    public void setShelflife(String shelflife) {
        this.shelflife = shelflife;
    }


    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApiModelList<FoodResourceExtends> getmExtends() {
        return mExtends;
    }

    public void setmExtends(ApiModelList<FoodResourceExtends> mExtends) {
        this.mExtends = mExtends;
    }

    public ApiModelList<FoodResourceElements> getmElements() {
        return mElements;
    }

    public void setmElements(ApiModelList<FoodResourceElements> mElements) {
        this.mElements = mElements;
    }

    public void copyFrom(ResourceTable.ResourceDB resourceDB) {
        setId(resourceDB.id);
        setType(resourceDB.type);
        setName(resourceDB.name);
        setImage(resourceDB.image);
        setShelflife(resourceDB.shelflife);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FoodResource) {
            return ((FoodResource) o).getId().equals(getId())
                    && ((FoodResource) o).getType().equals(getType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int idHashCode = 0;
        int typeHashCode = 0;
        if (getId() != null) {
            idHashCode = getId().hashCode();
        }

        if (getType() != null) {
            typeHashCode = getType().hashCode();
        }
        return idHashCode + typeHashCode;
    }
}
