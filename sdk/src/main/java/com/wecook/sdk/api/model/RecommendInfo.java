package com.wecook.sdk.api.model;

import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

import java.util.List;

/**
 * 推荐首页数据信息
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/11
 */
public class RecommendInfo extends ApiModel {

    private ApiModelList<RecommendCard> cardList;

    private List<Banner> bannerList;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonObject object = JsonUtils.getJsonObject(json);
        if (object != null) {
            if (object.has("cards")) {
                cardList = new ApiModelList<>(new RecommendCard());
                cardList.parseJson(object.get("cards").toString());
            }

            if (cardList != null) {
                for (RecommendCard card : cardList.getList()) {
                    if (RecommendCard.CARD_BANNER.equals(card.getType())) {
                        if (card.getContentCard() != null) {
                            bannerList = (List<Banner>) card.getContentCard().getDataList();
                            cardList.remove(card);
                            break;
                        }
                    }
                }
            }
        }
    }

    public List<Banner> getBannerList() {
        return bannerList;
    }

    public List<RecommendCard> getCardList() {
        if (cardList != null) {
            return cardList.getList();
        }
        return null;
    }

    public boolean hasBanner() {
        return bannerList != null && !bannerList.isEmpty();
    }

    public boolean hasCard() {
        return cardList != null && !cardList.isEmpty();
    }

}
