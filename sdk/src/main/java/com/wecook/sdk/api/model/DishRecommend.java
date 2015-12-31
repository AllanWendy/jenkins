package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

import java.util.List;

/**
 * 菜品首页推荐
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/16
 */
public class DishRecommend extends ApiModel {

    //    private Operator operator;
    private ApiModelList<Banner> bannerList = new ApiModelList<Banner>(new Banner());
    private ApiModelList<Category> categoryList = new ApiModelList<Category>(new Category());
    private ApiModelList<RecommendCustomCard> cardList = new ApiModelList<RecommendCustomCard>(new RecommendCustomCard());

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("banner")) {
                    bannerList.parseJson(object.get("banner").toString());
                }

                if (object.has("classify")) {
                    categoryList.parseJson(object.get("classify").toString());
                }

                if (object.has("cards")) {
                    cardList.parseJson(object.get("cards").toString());
                }
            }
        }

    }

    public List<Banner> getBannerList() {
        if (bannerList != null) {
            return bannerList.getList();
        }
        return null;
    }


    public List<Category> getCategoryList() {
        if (categoryList != null) {
            return categoryList.getList();
        }
        return null;
    }


    public List<RecommendCustomCard> getCardLists() {
        if (cardList != null) {
            return cardList.getList();
        }
        return null;
    }


    @Override
    public String toString() {
        return "DishRecommend{" +
                "bannerList=" + bannerList +
                ", categoryList=" + categoryList +
                ", cardLists=" + cardList +
                '}';
    }

    public boolean hasBanner() {
        return bannerList != null && !bannerList.isEmpty();
    }

    public boolean hasCategory() {
        return categoryList != null && !categoryList.isEmpty();
    }

    public boolean hasCard() {
        return cardList != null && !cardList.isEmpty();
    }
    //    public Operator getOperator() {
//        return operator;
//    }
//
//    public void setOperator(Operator operator) {
//        this.operator = operator;
//    }
}
