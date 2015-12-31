package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

import java.util.List;

/**
 * 运营块
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/17
 */
public class Operator extends ApiModel {

    public static final int TYPE_DOUBLE = 1;
    public static final int TYPE_THREE = 2;
    public static final int TYPE_GRID = 3;

    private int type;
    private ApiModelList<OperatorItem> itemList;

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonArray()) {
                parseItem(json);
            } else if (element.isJsonObject()) {
                if (element.getAsJsonObject().has("list")) {
                    parseItem(element.getAsJsonObject().get("list").toString());
                }
            }
        }
    }

    private void parseItem(String json) throws JSONException {
        itemList = new ApiModelList<>(new OperatorItem());
        itemList.parseJson(json);
        if (itemList.getCountOfList() == 2) {
            type = TYPE_DOUBLE;
        } else if (itemList.getCountOfList() == 3) {
            type = TYPE_THREE;
        } else if (itemList.getCountOfList() == 4) {
            type = TYPE_GRID;
        }
    }

    public List<OperatorItem> getItemList() {
        if (itemList != null) {
            return itemList.getList();
        }
        return null;
    }

    public void setItemList(List<OperatorItem> itemList) {
        if (itemList != null && !itemList.isEmpty()) {
            this.itemList = new ApiModelList<OperatorItem>(new OperatorItem());
            this.itemList.addAll(itemList);
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
