package com.wecook.sdk.api.model;

import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 卡片数据结构
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/11
 */
public class RecommendCard extends ApiModel {

    public static final String CARD_BANNER = "banner";
    public static final String CARD_DISH = "dishes";
    public static final String CARD_FOOD = "recipe";
    public static final String CARD_COOK = "cooking";
    public static final String CARD_PARTY = "events";
    public static final String CARD_TOPIC = "topic";

    private int index;

    private String name;

    private String description;

    private String type;

    private String url;

    private String icon;

    private String color;

    private RecommendCustomCard customCard;

    private RecommendContentCard contentCard;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public RecommendContentCard getContentCard() {
        return contentCard;
    }

    public RecommendCustomCard getCustomCard() {
        return customCard;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonObject jsonObject = JsonUtils.getJsonObject(json);
        if (jsonObject != null) {

            if (jsonObject.has("type")) {
                type = jsonObject.get("type").getAsString();
            }

            if (jsonObject.has("area_lt")) {
                JsonObject info = jsonObject.getAsJsonObject("area_lt");

                if (info.has("name")) {
                    name = info.get("name").getAsString();
                }

                if (info.has("sub_name")) {
                    description = info.get("sub_name").getAsString();
                }

                if (info.has("url")) {
                    url = info.get("url").getAsString();
                }

                if (info.has("icon")) {
                    icon = info.get("icon").getAsString();
                }

                if (info.has("color")) {
                    color = info.get("color").getAsString();
                } else {
                    if(CARD_DISH.equals(type)){
                        color = "#ffff644e";
                    } else if(CARD_FOOD.equals(type)){
                        color = "#ff6EC014";
                    } else if(CARD_COOK.equals(type)){
                        color = "#ff4a90e2";
                    } else if(CARD_TOPIC.equals(type)){
                        color = "#ff6EC014";
                    } else if(CARD_PARTY.equals(type)){
                        color = "#ffff644e";
                    }
                }
            }

            if (jsonObject.has("area_rt")) {
                customCard = new RecommendCustomCard(this);
                customCard.parseJson(jsonObject.get("area_rt").toString());
            }

            if (jsonObject.has("rows")) {
                if (CARD_BANNER.equals(type)) {
                    contentCard = new RecommendContentCard<>(this, new Banner());
                } else if (CARD_DISH.equals(type)) {
                    contentCard = new RecommendContentCard<>(this, new Dish());
                } else if (CARD_FOOD.equals(type)) {
                    contentCard = new RecommendContentCard<>(this, new Food());
                } else if (CARD_COOK.equals(type)) {
                    contentCard = new RecommendContentCard<>(this, new CookShow());
                } else if (CARD_TOPIC.equals(type)) {
                    contentCard = new RecommendContentCard<>(this, new Topic());
                } else if (CARD_PARTY.equals(type)) {
                    contentCard = new RecommendContentCard<>(this, new Party());
                }
                if (contentCard != null) {
                    contentCard.parseJson(jsonObject.get("rows").toString());
                }
            }

            if (!CARD_BANNER.equals(type) && customCard == null && contentCard != null) {
                ApiModel obj = (ApiModel) contentCard.getDataList().remove(0);
                contentCard.setTopCard(obj);
            }

        }
    }
}
