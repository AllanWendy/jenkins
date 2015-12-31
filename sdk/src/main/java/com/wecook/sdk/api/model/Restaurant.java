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
import java.util.ArrayList;
import java.util.List;

/**
 * 餐厅
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/18
 */
public class Restaurant extends ApiModel {

    private String id;
    private String name;
    private String image;
    private String grade;//评分
    private String gradeCount;//评论数
    private String sale;//月销量
    private List<Tags> perferentials = new ArrayList<>();//优惠
    private Tags deliveryTag;//配送
    private List<Tags> categorys;//分类
    private String tip;//餐厅一句话描述
    private String intormation;//餐厅描述
    private String foodSource;//食材溯源
    private String restaurantStatus;//餐厅状态 1:正常 0：禁用 -1:已删除
    private String deliveryDelay;//发货物流时间
    private String deliveryPrice;//物流单价
    private String provideMax;//餐厅产能
    private String introUrl;//餐厅介绍
    private String express_type_txt;//配送类型
    private int dishes_num;//在售菜品数量


    private Dish dish;//推荐菜品
    private ApiModelList<Dish> dishes;//菜品
    private List<Tags> deliveryTags;//配送标签

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("restaurant")) {
                    return new JSONArray(object.get("restaurant").toString());
                }

                if (object.has("restaurants")) {
                    return new JSONArray(object.get("restaurants").toString());
                }
            }
        }
        return super.findJSONArray(json);
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("id")) {
                    JsonElement item = object.get("id");
                    if (!item.isJsonNull()) {
                        id = item.getAsString();
                    }
                } else if (object.has("restaurant_id")) {
                    JsonElement item = object.get("restaurant_id");
                    if (!item.isJsonNull()) {
                        id = item.getAsString();
                    }
                }

                if (object.has("status")) {
                    JsonElement item = object.get("status");
                    if (!item.isJsonNull()) {
                        restaurantStatus = item.getAsString();
                    }
                }

                if (object.has("delivery_delay")) {
                    JsonElement item = object.get("delivery_delay");
                    if (!item.isJsonNull()) {
                        deliveryDelay = item.getAsString();
                    }
                }

                if (object.has("delivery_price")) {
                    JsonElement item = object.get("delivery_price");
                    if (!item.isJsonNull()) {
                        deliveryPrice = item.getAsString();
                    }
                }

                if (object.has("title")) {
                    JsonElement item = object.get("title");
                    if (!item.isJsonNull()) {
                        name = item.getAsString();
                    }
                }
                if (object.has("icon")) {
                    JsonElement item = object.get("icon");
                    if (!item.isJsonNull()) {
                        image = item.getAsString();
                    }
                }
                if (object.has("grade")) {
                    JsonElement item = object.get("grade");
                    if (!item.isJsonNull()) {
                        grade = item.getAsString();
                    }
                }
                if (object.has("sales_in_month")) {
                    JsonElement item = object.get("sales_in_month");
                    if (!item.isJsonNull()) {
                        sale = item.getAsString();
                    }
                }

                if (object.has("provide_max")) {
                    JsonElement item = object.get("provide_max");
                    if (!item.isJsonNull()) {
                        provideMax = item.getAsString();
                    }
                }

                if (object.has("favorable_notice_icons") || object.has("favorable_notice")) {
                    JsonElement iconsElement = object.get("favorable_notice_icons");
                    if (iconsElement == null) {
                        iconsElement = object.get("favorable_notice");
                    }
                    if (iconsElement != null && iconsElement.isJsonArray()) {
                        ApiModelList<Tags> tagsApiModelList = new ApiModelList<>(new Tags());
                        tagsApiModelList.parseJson(iconsElement.toString());
                        perferentials = tagsApiModelList.getList();
                    }
                }

                if (object.has("delivery_time_notice")) {
                    JsonElement iconsElement = object.get("delivery_time_notice");
                    if (iconsElement != null && iconsElement.isJsonObject()) {
                        deliveryTag = new Tags();
                        deliveryTag.parseJson(iconsElement.toString());
                    }
                }

                if (object.has("delivery_time_notice_list")) {
                    JsonElement iconsElement = object.get("delivery_time_notice_list");
                    if (iconsElement != null && iconsElement.isJsonArray()) {
                        ApiModelList<Tags> tagsApiModelList = new ApiModelList<>(new Tags());
                        tagsApiModelList.parseJson(iconsElement.toString());
                        deliveryTags = tagsApiModelList.getList();
                    }
                }
                if (object.has("classify")) {
                    JsonElement iconsElement = object.get("classify");
                    if (iconsElement != null && iconsElement.isJsonArray()) {
                        ApiModelList<Tags> tagsApiModelList = new ApiModelList<>(new Tags());
                        tagsApiModelList.parseJson(iconsElement.toString());
                        categorys = tagsApiModelList.getList();
                    }
                }
                if (object.has("express_type_txt")) {
                    JsonElement item = object.get("express_type_txt");
                    if (!item.isJsonNull()) {
                        express_type_txt = item.getAsString();
                    }
                }
                if (object.has("tip")) {
                    JsonElement item = object.get("tip");
                    if (!item.isJsonNull()) {
                        tip = item.getAsString();
                    }
                }

                if (object.has("comments_num")) {
                    JsonElement item = object.get("comments_num");
                    if (!item.isJsonNull()) {
                        gradeCount = item.getAsString();
                    }
                }

                if (object.has("intro")) {
                    JsonElement item = object.get("intro");
                    if (!item.isJsonNull()) {
                        intormation = item.getAsString();
                    }
                }

                if (object.has("intro_url")) {
                    JsonElement item = object.get("intro_url");
                    if (!item.isJsonNull()) {
                        introUrl = item.getAsString();
                    }
                }

                if (object.has("food_source")) {
                    JsonElement item = object.get("food_source");
                    if (!item.isJsonNull()) {
                        foodSource = item.getAsString();
                    }
                }
                if (object.has("dishes_num")) {
                    JsonElement item = object.get("dishes_num");
                    if (!item.isJsonNull()) {
                        dishes_num = item.getAsInt();
                    }
                }
                if (object.has("dish")) {
                    dish = new Dish();
                    dish.parseJson(object.get("dish").toString());
                }
                if (object.has("dishes")) {
                    dishes = new ApiModelList<Dish>(new Dish());
                    dishes.parseJson(object.get("dishes").toString());

                    for (Dish dish : dishes.getList()) {
                        dish.setTemplateId(Dish.TYPE_DISH);
                    }
                }
            }
        }
    }

    public void writeJson(JsonWriter writer) throws IOException {
        writer.name("id").value(id);
        writer.name("status").value(restaurantStatus);
        writer.name("delivery_delay").value(deliveryDelay);
        writer.name("delivery_price").value(deliveryPrice);
        writer.name("title").value(name);
        writer.name("icon").value(image);
        writer.name("grade").value(grade);
        writer.name("sales_in_month").value(sale);
        writer.name("provide_max").value(provideMax);
        writer.name("tip").value(tip);
        writer.name("comments_num").value(gradeCount);
        writer.name("intro").value(intormation);
        writer.name("food_source").value(foodSource);
        if (!dishes.isEmpty()) {
            writer.name("dishes");
            writer.beginArray();
            for (Dish dish : dishes.getList()) {
                writer.beginObject();
                dish.writeJson(writer);
                writer.endObject();
            }
            writer.endArray();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getFoodSource() {
        return foodSource;
    }

    public void setFoodSource(String foodSource) {
        this.foodSource = foodSource;
    }

    public String getGradeCount() {
        return gradeCount;
    }

    public void setGradeCount(String gradeCount) {
        this.gradeCount = gradeCount;
    }

    public String getIntormation() {
        return intormation;
    }

    public void setIntormation(String intormation) {
        this.intormation = intormation;
    }

    public List<Tags> getPerferentials() {
        return perferentials;
    }

    public void setPerferentials(List<Tags> perferentials) {
        this.perferentials = perferentials;
    }

    public Tags getDeliveryTag() {
        return deliveryTag;
    }

    public void setDeliveryTag(Tags deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    public List<Tags> getCategorys() {
        return categorys;
    }

    public void setCategorys(List<Tags> categorys) {
        this.categorys = categorys;
    }

    public String getSale() {
        return sale;
    }

    public void setSale(String sale) {
        this.sale = sale;
    }

    public String getExpress_type_txt() {
        return express_type_txt;
    }

    public void setExpress_type_txt(String express_type_txt) {
        this.express_type_txt = express_type_txt;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getIntroUrl() {
        return introUrl;
    }

    public void setIntroUrl(String introUrl) {
        this.introUrl = introUrl;
    }

    public String getDeliveryDelay() {
        return deliveryDelay;
    }

    public void setDeliveryDelay(String deliveryDelay) {
        this.deliveryDelay = deliveryDelay;
    }

    public float getDeliveryPrice() {
        return StringUtils.parseFloat(deliveryPrice);
    }

    public void setDeliveryPrice(float deliveryPrice) {
        this.deliveryPrice = "" + deliveryPrice;
    }

    public int getProvideMax() {
        return StringUtils.parseInt(provideMax);
    }

    public void setProvideMax(int provideMax) {
        this.provideMax = provideMax + "";
    }

    public String getRestaurantStatus() {
        return restaurantStatus;
    }

    public void setRestaurantStatus(String restaurantStatus) {
        this.restaurantStatus = restaurantStatus;
    }

    public List<Dish> getDishes() {
        if (dishes != null) {
            return dishes.getList();
        }
        return null;
    }

    public void setDishes(List<Dish> dishes) {
        if (dishes != null && !dishes.isEmpty()) {
            this.dishes = new ApiModelList<Dish>(new Dish());
            this.dishes.addAll(dishes);
        }
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getDishes_num() {
        return dishes_num;
    }

    public void setDishes_num(int dishes_num) {
        this.dishes_num = dishes_num;
    }

    public List<Tags> getDeliveryTags() {
        return deliveryTags;
    }

    public void setDeliveryTags(List<Tags> deliveryTags) {
        this.deliveryTags = deliveryTags;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", grade='" + grade + '\'' +
                ", gradeCount='" + gradeCount + '\'' +
                ", sale='" + sale + '\'' +
                ", perferentials=" + perferentials +
                ", deliveryTag=" + deliveryTag +
                ", categorys=" + categorys +
                ", tip='" + tip + '\'' +
                ", intormation='" + intormation + '\'' +
                ", foodSource='" + foodSource + '\'' +
                ", restaurantStatus='" + restaurantStatus + '\'' +
                ", deliveryDelay='" + deliveryDelay + '\'' +
                ", deliveryPrice='" + deliveryPrice + '\'' +
                ", provideMax='" + provideMax + '\'' +
                ", introUrl='" + introUrl + '\'' +
                ", dish=" + dish +
                ", dishes=" + dishes +
                '}';
    }
}
