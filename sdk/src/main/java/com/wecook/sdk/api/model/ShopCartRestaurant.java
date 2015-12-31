package com.wecook.sdk.api.model;

import android.util.JsonWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车餐厅列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/20
 */
public class ShopCartRestaurant extends Restaurant {

    public static final String STATE_NORMAL = "1";//正常
    public static final String STATE_DELETED = "-1";//已删除
    public static final String STATE_DISABLE = "0";//禁用

    private List<ShopCartDish> shopCartDishes;//购物车菜品
    private boolean selectAll;
    private boolean selectAllInEdit;
    private boolean editMode;
    private String remarkContent;//备注
    private String orderDishCount;//订单下该餐厅的菜品数量
    private DeliveryTime deliveryTime;//配送时间
    private String deliverySupply;//配送服务商
    private String totalPrice;
    private String expressType;//配送类型

    @Override
    public void parseJson(String json) throws JSONException {
        super.parseJson(json);
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();

                if (object.has("count")) {
                    JsonElement item = object.get("count");
                    if (!item.isJsonNull()) {
                        orderDishCount = item.getAsString();
                    }
                }

                if (object.has("note")) {
                    JsonElement item = object.get("note");
                    if (!item.isJsonNull()) {
                        remarkContent = item.getAsString();
                    }
                }

                if (object.has("delivery_time")) {
                    JsonElement item = object.get("delivery_time");
                    if (!item.isJsonNull()) {
                        deliveryTime = new DeliveryTime();
                        deliveryTime.setFullTime(item.getAsString());
                    }
                }


                if (object.has("delivery_supply")) {
                    JsonElement item = object.get("delivery_supply");
                    if (!item.isJsonNull()) {
                        deliverySupply = item.getAsString();
                    }
                }

                if (object.has("total_price")) {
                    JsonElement item = object.get("total_price");
                    if (!item.isJsonNull()) {
                        totalPrice = item.getAsString();
                    }
                }

                ApiModelList<ShopCartDish> dishes = new ApiModelList<ShopCartDish>(new ShopCartDish());
                if (object.has("shop_cart_dishes")) {
                    dishes.parseJson(object.get("shop_cart_dishes").toString());
                } else if (object.has("dishes")) {
                    dishes.parseJson(object.get("dishes").toString());
                }
                if (!dishes.isEmpty()) {
                    shopCartDishes = new ArrayList<>();
                    shopCartDishes.addAll(dishes.getList());
                    for (ShopCartDish dish : shopCartDishes) {
                        dish.setRestaurant(this);
                    }
                }

                if (object.has("select_all")) {
                    selectAll = object.get("select_all").getAsBoolean();
                }
            }
        }
    }

    public List<ShopCartDish> getCheckoutDishes() {
        List<ShopCartDish> dishes = new ArrayList<>();
        for (ShopCartDish dish : shopCartDishes) {
            if (dish.isSelected()
                    && ShopCartDish.STATE_NORMAL.equals(dish.getState())) {
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public List<ShopCartDish> getShopCartDishes() {
        return shopCartDishes;
    }

    public void setShopCartDishes(List<ShopCartDish> shopCartDishes) {
        this.shopCartDishes = shopCartDishes;
    }

    public boolean isSelectAll() {
        if (editMode) {
            return selectAllInEdit;
        }
        return selectAll;
    }

    public void setSelectAll(boolean selectAll) {
        if (editMode) {
            this.selectAllInEdit = selectAll;
        } else {
            this.selectAll = selectAll;
        }
    }

    public String getRemarkContent() {
        return remarkContent;
    }

    public void setRemarkContent(String remarkContent) {
        this.remarkContent = remarkContent;
    }

    public DeliveryTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(DeliveryTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public int getOrderDishCount() {
        return StringUtils.parseInt(orderDishCount);
    }

    public void setOrderDishCount(String orderDishCount) {
        this.orderDishCount = orderDishCount;
    }

    public String getDeliverySupply() {
        return deliverySupply;
    }

    public void setDeliverySupply(String deliverySupply) {
        this.deliverySupply = deliverySupply;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void writeJson(JsonWriter writer) throws IOException {
        super.writeJson(writer);
        writer.name("select_all").value(selectAll);
        writer.name("count").value(orderDishCount);
        if (!shopCartDishes.isEmpty()) {
            writer.name("shop_cart_dishes");
            writer.beginArray();
            for (ShopCartDish dish : shopCartDishes) {
                writer.beginObject();
                dish.writeJson(writer);
                writer.endObject();
            }
            writer.endArray();
        }
    }

    public int getCheckoutDishCount() {
        int count = 0;
        if (shopCartDishes != null) {
            for (ShopCartDish item : shopCartDishes) {
                if (ShopCartDish.STATE_NORMAL.equals(item.getState())
                        && item.isSelected()) {
                    count += item.getQuantity();
                }
            }
        }
        return count;
    }

    public int getDishCount() {
        int count = 0;
        if (shopCartDishes != null) {
            for (ShopCartDish item : shopCartDishes) {
                count += item.getQuantity();
            }
        }
        return count;
    }

    /**
     * 返回正常菜品的小计总价
     *
     * @return
     */
    public float getCheckoutTotalPrice() {
        float total = 0;
        if (shopCartDishes != null) {
            for (ShopCartDish item : shopCartDishes) {
                if (ShopCartDish.STATE_NORMAL.equals(item.getState())
                        && item.isSelected()) {
                    float rawPrice = item.getRawPrice();
                    float quantity = item.getQuantity();
                    float result = rawPrice * quantity;
                    total = total + result;
                }
            }
        }
        return total;
    }

    public String getExpressType() {
        return expressType;
    }

    /**
     * 设置配送类型
     *
     * @param expressType
     */
    public void setExpressType(String expressType) {
        if (expressType.equals(DeliveryRestaurant.EXPRESS_BY_RESTAURANT) || expressType.equals(DeliveryRestaurant.EXPRESS_BY_WECOOK)) {
            this.expressType = expressType;
        }
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}
