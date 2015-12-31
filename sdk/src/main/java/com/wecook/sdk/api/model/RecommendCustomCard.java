package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

import java.util.List;

/**
 * 定制卡片
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/11
 */
public class RecommendCustomCard extends ApiModel {

    public static final String TYPE_SIMPLE_IMAGE = "simple";//单图
    public static final String TYPE_DOUBLE_LINE = "double";//双行
    public static final String TYPE_THREE_CARD = "three";//三个卡
    public static final String TYPE_FOUR_GRID = "grid";//四宫格
    public static final String TYPE_ACTION_CARD = "action";//执行动作

    private String type;

    private ApiModelList<Card> dataList;

    private RecommendCard card;

    public RecommendCustomCard(RecommendCard card) {
        this.card = card;
    }

    public RecommendCustomCard() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Card> getDataList() {
        if (dataList != null) {
            return dataList.getList();
        }
        return null;
    }

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);

        if (element != null) {
            if (element.isJsonArray()) {
                dataList = new ApiModelList<>(new Card());
                dataList.parseJson(json);
                switch (dataList.getList().size()) {
                    case 1:
                        type = TYPE_SIMPLE_IMAGE;
                        break;
                    case 2:
                        type = TYPE_DOUBLE_LINE;
                        break;
                    case 3:
                        type = TYPE_THREE_CARD;
                        break;
                    case 4:
                        type = TYPE_FOUR_GRID;
                        break;
                    default:
                        return;
                }
                return;
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("templateId")) {
                    int templateId = object.get("templateId").getAsInt();

                    switch (templateId) {
                        case 1:
                            type = TYPE_SIMPLE_IMAGE;
                            break;
                        case 2:
                            type = TYPE_DOUBLE_LINE;
                            break;
                        case 3:
                            type = TYPE_FOUR_GRID;
                            break;
                        case 4:
                            type = TYPE_ACTION_CARD;
                            break;
                    }
                }
                if (object.has("list")) {
                    dataList = new ApiModelList<>(new Card());
                    dataList.parseJson(object.toString());
                }
            }
            //TODO 2.4.0 测试删除
            if (RecommendCard.CARD_DISH.equals(card.getType())) {
                type = TYPE_DOUBLE_LINE;
            } else if (RecommendCard.CARD_FOOD.equals(card.getType())) {
                type = TYPE_FOUR_GRID;
            } else if (RecommendCard.CARD_COOK.equals(card.getType())) {
                type = TYPE_ACTION_CARD;
            }
        }
    }

    public static class Card extends ApiModel {

        public static final String NUMBER = "number";//数字
        public static final String TIME = "time";//倒计时

        private String id;
        private String type = NUMBER;
        private String name = "";
        private String subName = "";
        private String title = "";
        private String sub_title = "";
        private String color;
        private String url;
        private String image;
        private String icon;
        private String background;

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

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSub_title() {
            return sub_title;
        }

        public void setSub_title(String sub_title) {
            this.sub_title = sub_title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getUrl() {
            if (null == url) {
                return url;
            }
            if (url.contains("p_title=")) {
                return url;
            }
            if (null == title || "".equals(title)) {
                return url;
            }

            if (url.contains("?")) {
                return url + "&p_title=" + title;
            } else {
                return url + "?p_title=" + title;
            }
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getSubName() {
            return subName;
        }

        public void setSubName(String subName) {
            this.subName = subName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void parseJson(String json) throws JSONException {
            JsonObject object = JsonUtils.getJsonObject(json);
            if (object != null) {
                if (object.has("id")) {
                    id = object.get("id").getAsString();
                }
                if (object.has("name")) {
                    name = object.get("name").getAsString();
                }
                if (object.has("sub_name")) {
                    subName = object.get("sub_name").getAsString();
                }
                if (object.has("title")) {
                    title = object.get("title").getAsString();
                }
                if (object.has("sub_title")) {
                    sub_title = object.get("sub_title").getAsString();
                }
                if (object.has("type")) {
                    type = object.get("type").getAsString();
                }
                if (object.has("color")) {
                    color = object.get("color").getAsString();
                }
                if (object.has("url")) {
                    url = object.get("url").getAsString();
                }
                if (object.has("icon")) {
                    icon = object.get("icon").getAsString();
                }
                if (object.has("image")) {
                    image = object.get("image").getAsString();
                }
                if (object.has("background")) {
                    background = object.get("background").getAsString();
                }
            }
        }

        @Override
        public String toString() {
            return "Card{" +
                    "id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", title='" + title + '\'' +
                    ", sub_title='" + sub_title + '\'' +
                    ", color='" + color + '\'' +
                    ", url='" + url + '\'' +
                    ", image='" + image + '\'' +
                    ", background='" + background + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "RecommendCustomCard{" +
                "type='" + type + '\'' +
                ", dataList=" + dataList +
                ", card=" + card +
                '}';
    }
}
