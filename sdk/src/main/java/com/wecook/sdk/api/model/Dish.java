package com.wecook.sdk.api.model;

import android.util.JsonWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * 菜
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/5/14
 */
public class Dish extends ApiModel {

    public static final int TYPE_DISH = 1;//半成品
    public static final int TYPE_RESTAURANT = 2;//套餐

    public static final int STATE_ON_SALE = 1;
    public static final int STATE_OFF_SALE = 0;

    /**
     * id
     */
    private String dishId;

    /**
     * restaurantId
     */
    private String restaurantId;

    /**
     * 名称
     */
    private String title;

    /**
     * 图片
     */
    private String image;

    /**
     * 大图
     */
    private String imageBig;

    /**
     * 价格
     */
    private String price;

    /**
     * 正常价
     */
    private String priceNormal;

    /**
     * 分享url
     */
    private String url;

    /**
     * 类型 1:菜品2:套餐
     */
    private String type;

    /**
     * 运营位ID
     */
    private String templateId;

    /**
     * 状态
     */
    private String state;//菜品状态

    /**
     * 销量
     */
    private String saleCount;//月销量

    /**
     * 库存
     */
    private String stockCount;//库存

    /**
     * 评分
     */
    private String grade;//评分

    /**
     * 烹饪耗时
     */
    private String spend;

    /**
     * 烹饪难度
     */
    private String difficulty;

    /**
     * 口味
     */
    private String flavour;

    /**
     * 净重
     */
    private String weight;

    /**
     * 小贴士
     */
    private String tips;

    /**
     * 自备食材
     */
    private String prepare;

    /**
     * 富文本内容
     */
    private String content;

    /**
     * 富文本内容
     */
    private String description;

    /**
     * 食材来源
     */
    private String dishSourceUrl;
    /**
     * 相关餐厅信息
     */
    private Restaurant restaurant;


    /**
     * 菜品标签列表
     */
    private ApiModelList<DishTag> dishTags;

    /**
     * 菜品特色列表
     */
    private ApiModelList<DishFeature> dishFeatures;

    /**
     * 菜品封面列表
     */
    private ApiModelList<Image> dishCovers;

    /**
     * 菜品原封面列表
     */
    private ApiModelList<Image> dishOriginCovers;

    /**
     * 菜品主料食材列表
     */
    private ApiModelList<FoodIngredient> dishIngredients;

    /**
     * 菜品辅料食材列表
     */
    private ApiModelList<FoodAssist> dishAssists;
    /**
     * 菜品辅料文字
     */
    private String dishAssistText;

    /**
     * 菜品步骤列表
     */
    private ApiModelList<FoodStep> dishSteps;

    /**
     * 套餐内菜品列表
     */
    private ApiModelList<Dish> dishGroupItems;
    /**
     * 保障
     */
    private ApiModelList<Security> security;

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                if (element.getAsJsonObject().has("dishes")) {
                    return new JSONArray(element.getAsJsonObject().get("dishes").toString());
                }
            }
        }
        return super.findJSONArray(json);
    }

    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonObject(json)) {
            JsonElement element = JsonUtils.getJsonElement(json);
            if (element != null && element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                restaurantId = JsonUtils.getModelItemAsString(jsonObject, "restaurant_id");
                title = JsonUtils.getModelItemAsString(jsonObject, "title");
                type = JsonUtils.getModelItemAsString(jsonObject, "type");
                templateId = JsonUtils.getModelItemAsString(jsonObject, "templateId");
                image = JsonUtils.getModelItemAsString(jsonObject, "image");
                imageBig = JsonUtils.getModelItemAsString(jsonObject, "image_big");
                price = JsonUtils.getModelItemAsString(jsonObject, "price");
                priceNormal = JsonUtils.getModelItemAsString(jsonObject, "price_normal");
                url = JsonUtils.getModelItemAsString(jsonObject, "url");
                content = JsonUtils.getModelItemAsString(jsonObject, "content");
                grade = JsonUtils.getModelItemAsString(jsonObject, "grade");
                stockCount = JsonUtils.getModelItemAsString(jsonObject, "stock");

                spend = JsonUtils.getModelItemAsString(jsonObject, "spend");
                difficulty = JsonUtils.getModelItemAsString(jsonObject, "difficulty");
                flavour = JsonUtils.getModelItemAsString(jsonObject, "flavour");
                weight = JsonUtils.getModelItemAsString(jsonObject, "weight");
                tips = JsonUtils.getModelItemAsString(jsonObject, "tips");
                prepare = JsonUtils.getModelItemAsString(jsonObject, "prepare");
                description = JsonUtils.getModelItemAsString(jsonObject, "description");
                dishSourceUrl = JsonUtils.getModelItemAsString(jsonObject, "restaurant_intro_url");
                dishAssistText = JsonUtils.getModelItemAsString(jsonObject, "assist");

                dishTags = JsonUtils.getModelItemAsList(jsonObject, "support", new DishTag());
                if (dishTags == null) {
                    dishTags = JsonUtils.getModelItemAsList(jsonObject, "tags", new DishTag());
                }

                restaurant = JsonUtils.getModelItemAsObject(jsonObject, "restaurant", new Restaurant());
                dishId = JsonUtils.getModelItemAsString(jsonObject, "id");
                if (StringUtils.isEmpty(dishId)) {
                    dishId = JsonUtils.getModelItemAsString(jsonObject, "dish_id");
                }
                security = JsonUtils.getModelItemAsList(jsonObject, "security", new Security());
                dishFeatures = JsonUtils.getModelItemAsList(jsonObject, "features", new DishFeature());
                dishCovers = JsonUtils.getModelItemAsList(jsonObject, "covers", new Image());
                if (dishCovers != null && !dishCovers.isEmpty()) {
                    Image image = dishCovers.getList().get(0);
                    setImage(image.getUrl());
                }

                dishOriginCovers = JsonUtils.getModelItemAsList(jsonObject, "covers_origin", new Image());
                dishIngredients = JsonUtils.getModelItemAsList(jsonObject, "ingredients", new FoodIngredient());
                dishAssists = JsonUtils.getModelItemAsList(jsonObject, "assist", new FoodAssist());
                dishSteps = JsonUtils.getModelItemAsList(jsonObject, "step", new FoodStep());
                dishGroupItems = JsonUtils.getModelItemAsList(jsonObject, "packages", new Dish());
                state = JsonUtils.getModelItemAsString(jsonObject, "state");
                if (StringUtils.isEmpty(state)) {
                    state = JsonUtils.getModelItemAsString(jsonObject, "status");
                }
                saleCount = JsonUtils.getModelItemAsString(jsonObject, "sales_in_month");
                if (StringUtils.isEmpty(saleCount)) {
                    saleCount = JsonUtils.getModelItemAsString(jsonObject, "sales");
                }

            }
        }
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getImageBig() {
        return imageBig;
    }

    public void setImageBig(String imageBig) {
        this.imageBig = imageBig;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDishTags(ApiModelList<DishTag> dishTags) {
        this.dishTags = dishTags;
    }

    public float getGrade() {
        return StringUtils.parseFloat(grade);
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getStockCount() {
        return StringUtils.parseInt(stockCount);
    }

    public void setStockCount(int stockCount) {
        this.stockCount = stockCount + "";
    }

    public int getSaleCount() {
        return StringUtils.parseInt(saleCount);
    }

    public void setSaleCount(int saleCount) {
        this.saleCount = saleCount + "";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTemplateId() {
        return StringUtils.parseInt(templateId);
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId + "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
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

    public String getPrice() {
        if (StringUtils.isEmpty(price)) {
            return "";
        }

        if (!StringUtils.containWith(price, "¥")) {
            return "¥" + price;
        }
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public float getRawPrice() {
        String rawPrice = price;
        if (StringUtils.containWith(price, "¥")) {
            rawPrice = price.replace("¥", "");
        }
        return Float.parseFloat(rawPrice);
    }

    public String getPriceNormal() {
        if (StringUtils.isEmpty(priceNormal) || "0.00".equals(priceNormal)) {
            return "";
        }

        if (!StringUtils.containWith(priceNormal, "¥")) {
            return "¥" + priceNormal;
        }
        return priceNormal;
    }

    public void setPriceNormal(String priceNormal) {
        this.priceNormal = priceNormal;
    }

    public float getRawPriceNormal() {
        String rawPriceNormal = priceNormal;
        if (StringUtils.containWith(priceNormal, "¥")) {
            rawPriceNormal = priceNormal.replace("¥", "");
        }
        return Float.parseFloat(rawPriceNormal);
    }

    public List<DishTag> getDishTags() {
        if (dishTags != null) {
            return dishTags.getList();
        }
        return null;
    }

    public void setDishTags(List<DishTag> dishTags) {
        if (dishTags != null && !dishTags.isEmpty()) {
            this.dishTags = new ApiModelList<DishTag>(new DishTag());
            this.dishTags.addAll(dishTags);
        }
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getRestaurantName() {
        if (restaurant != null) {
            return restaurant.getName();
        }
        return "";
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public ApiModelList<FoodAssist> getDishAssists() {
        return dishAssists;
    }

    public void setDishAssists(ApiModelList<FoodAssist> dishAssists) {
        this.dishAssists = dishAssists;
    }

    public ApiModelList<Image> getDishCovers() {
        return dishCovers;
    }

    public void setDishCovers(ApiModelList<Image> dishCovers) {
        this.dishCovers = dishCovers;
    }

    public ApiModelList<Image> getDishOriginCovers() {
        return dishOriginCovers;
    }

    public void setDishOriginCovers(ApiModelList<Image> dishOriginCovers) {
        this.dishOriginCovers = dishOriginCovers;
    }

    public ApiModelList<DishFeature> getDishFeatures() {
        return dishFeatures;
    }

    public void setDishFeatures(ApiModelList<DishFeature> dishFeatures) {
        this.dishFeatures = dishFeatures;
    }

    public ApiModelList<FoodIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(ApiModelList<FoodIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    public ApiModelList<FoodStep> getDishSteps() {
        return dishSteps;
    }

    public void setDishSteps(ApiModelList<FoodStep> dishSteps) {
        this.dishSteps = dishSteps;
    }

    public String getFlavour() {
        return flavour;
    }

    public void setFlavour(String flavour) {
        this.flavour = flavour;
    }

    public String getPrepare() {
        return prepare;
    }

    public void setPrepare(String prepare) {
        this.prepare = prepare;
    }

    public String getSpend() {
        return spend;
    }

    public void setSpend(String spend) {
        this.spend = spend;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApiModelList<Dish> getDishGroupItems() {
        return dishGroupItems;
    }

    public void setDishGroupItems(ApiModelList<Dish> dishGroupItems) {
        this.dishGroupItems = dishGroupItems;
    }

    public String getDishSourceUrl() {
        return dishSourceUrl;
    }

    public void setDishSourceUrl(String dishSourceUrl) {
        this.dishSourceUrl = dishSourceUrl;
    }

    public ApiModelList<Security> getSecurity() {
        return security;
    }

    public void setSecurity(ApiModelList<Security> security) {
        this.security = security;
    }

    public String getDishAssistText() {
        return dishAssistText;
    }

    public void setDishAssistText(String dishAssistText) {
        this.dishAssistText = dishAssistText;
    }

    /**
     * 合并 restaurant
     */
    public void mergeRestaurant(Restaurant restaurant) {
        if (this.restaurant == null) {
            this.restaurant = restaurant;
            return;
        }
        if (this.restaurant.getDeliveryTags() != null)
            restaurant.setDeliveryTags(this.restaurant.getDeliveryTags());
        if (this.restaurant.getPerferentials() != null)
            restaurant.setPerferentials(this.restaurant.getPerferentials());
        this.restaurant = restaurant;
    }

    public void writeJson(JsonWriter writer) throws IOException {
        writer.name("id").value(dishId);
        writer.name("title").value(title);
        writer.name("image").value(image);
        writer.name("image_big").value(imageBig);
        writer.name("price").value(price);
        writer.name("price_normal").value(priceNormal);
        writer.name("url").value(url);
        writer.name("type").value(type);
        writer.name("templateId").value(templateId);
        writer.name("state").value(state);
        writer.name("sales").value(saleCount);
        writer.name("stock").value(stockCount);
        writer.name("grade").value(grade);
        writer.name("content").value(content);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "dishId='" + dishId + '\'' +
                ", restaurantId='" + restaurantId + '\'' +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", imageBig='" + imageBig + '\'' +
                ", price='" + price + '\'' +
                ", priceNormal='" + priceNormal + '\'' +
                ", url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", templateId='" + templateId + '\'' +
                ", state='" + state + '\'' +
                ", saleCount='" + saleCount + '\'' +
                ", stockCount='" + stockCount + '\'' +
                ", grade='" + grade + '\'' +
                ", spend='" + spend + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", flavour='" + flavour + '\'' +
                ", weight='" + weight + '\'' +
                ", tips='" + tips + '\'' +
                ", prepare='" + prepare + '\'' +
                ", content='" + content + '\'' +
                ", description='" + description + '\'' +
                ", dishSourceUrl='" + dishSourceUrl + '\'' +
                ", restaurant=" + restaurant +
                ", dishTags=" + dishTags +
                ", dishFeatures=" + dishFeatures +
                ", dishCovers=" + dishCovers +
                ", dishOriginCovers=" + dishOriginCovers +
                ", dishIngredients=" + dishIngredients +
                ", dishAssists=" + dishAssists +
                ", dishSteps=" + dishSteps +
                ", dishGroupItems=" + dishGroupItems +
                '}';
    }
}
