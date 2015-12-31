package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 首页推荐
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/7/14
 */
public class Recommend extends ApiModel {

    public static final int CARD_BANNER = 0;
    public static final int CARD_FEATURE = CARD_BANNER + 1;//功能
    public static final int CARD_CATEGORY = CARD_BANNER + 2;
    public static final int CARD_DISH = CARD_BANNER + 3;
    public static final int CARD_FOOD = CARD_BANNER + 4;
    public static final int CARD_COOK = CARD_BANNER + 5;
    public static final int CARD_PARTY = CARD_BANNER + 6;
    public static final int CARD_TOPIC = CARD_BANNER + 7;


    private ApiModelList<Banner> bannerList;
    private ApiModelList<Feature> featureList;
    private ApiModelList<Category> hotCategoryList;
    private RecommendDish dishDetail;
    private ApiModelList<Food> foodList;
    private ApiModelList<CookShow> cookShowList;
    private ApiModelList<Topic> topicList;
    private ApiModelList<Party> partyList;

    private List<Integer> arrayType = new ArrayList<Integer>();

    @Override
    public void parseJson(String json) throws JSONException {
        JsonObject object = JsonUtils.getJsonObject(json);
        parseFixedListData(object);
    }

    private void parseFixedListData(JsonObject object) throws JSONException {
        if (object.has("banner")) {
            bannerList = new ApiModelList<Banner>(new Banner());
            bannerList.parseJson(object.get("banner").toString());
            arrayType.add(CARD_BANNER);
        }

        if (object.has("buttons")) {
            featureList = new ApiModelList<Feature>(new Feature());
            featureList.parseJson(object.get("buttons").toString());
            arrayType.add(CARD_FEATURE);
        }

        if (object.has("cates")) {
            hotCategoryList = new ApiModelList<Category>(new Category());
            hotCategoryList.parseJson(object.get("cates").toString());
            arrayType.add(CARD_CATEGORY);
        }

        if (object.has("dishes")) {
            dishDetail = new RecommendDish();
            dishDetail.parseJson(object.get("dishes").toString());
            arrayType.add(CARD_DISH);
        }

        if (object.has("recipe")) {
            foodList = new ApiModelList<Food>(new Food());
            foodList.parseJson(object.get("recipe").toString());
            arrayType.add(CARD_FOOD);
        }

        if (object.has("cooking")) {
            cookShowList = new ApiModelList<CookShow>(new CookShow());
            cookShowList.parseJson(object.get("cooking").toString());
            arrayType.add(CARD_COOK);
        }

        if (object.has("events")) {
            partyList = new ApiModelList<Party>(new Party());
            partyList.parseJson(object.get("events").toString());
            arrayType.add(CARD_PARTY);
        }

        if (object.has("topic")) {
            topicList = new ApiModelList<Topic>(new Topic());
            topicList.parseJson(object.get("topic").toString());
            arrayType.add(CARD_TOPIC);
        }
    }

    private void parseListData(JsonObject object) throws JSONException {
        Set<Map.Entry<String, JsonElement>> entries = object.entrySet();

        for (Map.Entry<String, JsonElement> entry : entries) {
            String key = entry.getKey();
            JsonElement element = entry.getValue();
            if ("banner".equals(key)) {
                bannerList = new ApiModelList<Banner>(new Banner());
                bannerList.parseJson(element.toString());
                arrayType.add(CARD_BANNER);
            } else if ("buttons".equals(key)) {
                featureList = new ApiModelList<Feature>(new Feature());
                featureList.parseJson(element.toString());
                arrayType.add(CARD_FEATURE);
            } else if ("cates".equals(key)) {
                hotCategoryList = new ApiModelList<Category>(new Category());
                hotCategoryList.parseJson(element.toString());
                arrayType.add(CARD_CATEGORY);
            } else if ("dishes".equals(key)) {
                dishDetail = new RecommendDish();
                dishDetail.parseJson(element.toString());
                arrayType.add(CARD_DISH);
            } else if ("recipe".equals(key)) {
                foodList = new ApiModelList<Food>(new Food());
                foodList.parseJson(element.toString());
                arrayType.add(CARD_FOOD);
            } else if ("cooking".equals(key)) {
                cookShowList = new ApiModelList<CookShow>(new CookShow());
                cookShowList.parseJson(element.toString());
                arrayType.add(CARD_COOK);
            } else if ("events".equals(key)) {
                partyList = new ApiModelList<Party>(new Party());
                partyList.parseJson(element.toString());
                arrayType.add(CARD_PARTY);
            } else if ("topic".equals(key)) {
                topicList = new ApiModelList<Topic>(new Topic());
                topicList.parseJson(element.toString());
                arrayType.add(CARD_TOPIC);
            }
        }
    }

    public int getCardCount() {
        return arrayType.size();
    }

    /**
     * 获得卡片类型
     *
     * @param pos
     * @return
     */
    public int getCardTypeByIndex(int pos) {
        return arrayType.get(pos);
    }

    public ApiModelList<Banner> getBannerList() {
        return bannerList;
    }

    public RecommendDish getDishDetail() {
        return dishDetail;
    }

    public ApiModelList<Food> getFoodList() {
        return foodList;
    }

    public ApiModelList<CookShow> getCookShowList() {
        return cookShowList;
    }

    public ApiModelList<Topic> getTopicList() {
        return topicList;
    }

    public ApiModelList<Party> getPartyList() {
        return partyList;
    }

    public ApiModelList<Category> getHotCategoryList() {
        return hotCategoryList;
    }

    public ApiModelList<Feature> getFeatureList() {
        return featureList;
    }
}
