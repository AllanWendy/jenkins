package com.wecook.sdk.api.model;

import android.util.JsonWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * 购物车菜品
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/20
 */
public class ShopCartDish extends Dish {

    //正常
    public static final String STATE_NORMAL = "1";
    //已售罄
    public static final String STATE_SALE_OFF = "-2";
    //已下架
    public static final String STATE_SHELF_OFF = "-3";

    public static final int ITEM_TYPE_RESTAURANT = 1;
    public static final int ITEM_TYPE_DISH = 2;
    public static final int ITEM_TYPE_DIVIDING = 3;

    private int itemType;
    private String cartId;
    private String quantity;//购买个数
    private ApiModelList<ShopCartDish> packages;//套餐菜品
    private boolean selected;
    private boolean selectedInEdit;
    private boolean editMode;

    private String comment;//评论

    private List<Image> images;//晒图

    public ShopCartDish() {
    }

    public ShopCartDish(Dish dish) {
        setDishId(dish.getDishId());
        setContent(dish.getContent());
        setDishTags(dish.getDishTags());
        setGrade(dish.getGrade() + "");
        setImage(dish.getImage());
        setImageBig(dish.getImageBig());
        setPriceNormal(dish.getPriceNormal());
        setPrice(dish.getPrice());
        setRestaurant(dish.getRestaurant());
        setState(dish.getState());
        setSaleCount(dish.getSaleCount());
        setStockCount(dish.getStockCount());
        setTemplateId(dish.getTemplateId());
        setType(dish.getType());
        setTitle(dish.getTitle());
        setUrl(dish.getUrl());
    }

    @Override
    public void parseJson(String json) throws JSONException {
        super.parseJson(json);

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null && element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            if (jsonObject.has("quantity")) {
                JsonElement item = jsonObject.get("quantity");
                if (!item.isJsonNull()) {
                    quantity = item.getAsString();
                }
            }

            if (jsonObject.has("cart_id")) {
                JsonElement item = jsonObject.get("cart_id");
                if (!item.isJsonNull()) {
                    cartId = item.getAsString();
                }
            }

            if (jsonObject.has("comment")) {
                JsonElement item = jsonObject.get("comment");
                if (!item.isJsonNull()) {
                    comment = item.getAsString();
                }
            }

            if (jsonObject.has("status")) {
                JsonElement item = jsonObject.get("status");
                if (!item.isJsonNull()) {
                    setState(item.getAsString());
                }
            }
            packages = JsonUtils.getModelItemAsList(jsonObject, "packages", new ShopCartDish());

            setSelected(JsonUtils.getModelItemAsString(jsonObject, "select").equals("true"));
        }

    }


    @Override
    public void writeJson(JsonWriter writer) throws IOException {
        super.writeJson(writer);
        writer.name("quantity").value(quantity);
        writer.name("cart_id").value(cartId);
        writer.name("select").value(isSelected() + "");
        writer.name("comment").value(comment);
        writer.name("status").value(getState());
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public int getQuantity() {
        return StringUtils.parseInt(quantity);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity + "";
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public boolean isSelected() {
        if (editMode) {
            return selectedInEdit;
        }
        return selected;
    }

    public void setSelected(boolean selected) {
        if (editMode) {
            this.selectedInEdit = selected;
        } else {
            this.selected = selected;
        }
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setInEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public ApiModelList<ShopCartDish> getPackages() {
        return packages;
    }

    /**
     * 是否为套餐
     *
     * @return
     */
    public boolean isPackagesFood() {
        return null != packages && null != packages.getList() && packages.getList().size() > 0;
    }
}
