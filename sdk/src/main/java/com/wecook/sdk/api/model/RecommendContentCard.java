package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

import java.util.List;

/**
 * 内容卡片
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/11
 */
public class RecommendContentCard<T extends ApiModel> extends ApiModel {

    public static final String TYPE_HORIZONTAL = "horizontal";//滑动模式
    public static final String TYPE_TEXT = "text";//文本模式

    private String type;

    private ApiModelList dataList;

    private RecommendCard card;

    private T data;

    private T topCard;

    public RecommendContentCard(RecommendCard card, T data) {
        this.card = card;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List getDataList() {
        return dataList.getList();
    }

    public void setTopCard(T topCard) {
        this.topCard = topCard;
    }

    public T getTopCard() {
        return topCard;
    }

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);

        if (element != null) {
            if (element.isJsonArray()) {
                dataList = new ApiModelList<>(data);
                dataList.parseJson(json);
            }

            if (RecommendCard.CARD_DISH.equals(card.getType())) {
                type = TYPE_HORIZONTAL;
            } else if (RecommendCard.CARD_FOOD.equals(card.getType())) {
                type = TYPE_HORIZONTAL;
            } else if (RecommendCard.CARD_COOK.equals(card.getType())) {
                type = TYPE_HORIZONTAL;
            } else if (RecommendCard.CARD_PARTY.equals(card.getType())) {
                type = TYPE_TEXT;
            } else if (RecommendCard.CARD_TOPIC.equals(card.getType())) {
                type = TYPE_TEXT;
            }
        }
    }
}
