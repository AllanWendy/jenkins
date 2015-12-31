package com.wecook.sdk.policy;

import android.util.JsonWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Coupon;
import com.wecook.sdk.api.model.DeliveryRestaurant;
import com.wecook.sdk.api.model.DeliveryTime;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.Location;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;

import org.json.JSONException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 买菜帮手的数据策略
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/1
 */
public class DishPolicy {

    public static final String JSON_DISH_ADDRESS = "dish_address";
    public static final String JSON_DISH_SHOP_CART = "dish_shop_cart";
    public static final String JSON_DISH_IMPERFECT_SHOP_CART = "dish_imperfect_shop_cart";

    private String mJsonDishShopCart;
    private String mJsonDishImperfectShopCart;
    private static DishPolicy sPolicy;

    private Address mDishAddress;
    private String mDishAddressJson;

    //购物车总数据
    private List<ShopCartRestaurant> mShopCart = new ArrayList<>();

    //餐厅菜品列表数据
    private LinkedHashMap<ShopCartRestaurant, List<ShopCartDish>> mDishesMap = new LinkedHashMap<>();
    //所有菜品列表数据
    private List<ShopCartDish> mDishes = new ArrayList<>();
    //所有不完整的菜品列表数据
    private List<ShopCartDish> mImperfectDishes = new ArrayList<>();

    //购物车列表数据，只在购物车界面中显示
    private List<ShopCartDish> mDishesOfShopCart = new ArrayList<>();

    //结算列表数据
    private LinkedHashMap<ShopCartRestaurant, List<ShopCartDish>> mCheckOutDishes = new LinkedHashMap<>();

    //配送时间
    private List<DeliveryTime> mDeliveryTimes = new ArrayList<>();
    //配送类型
    private List<String> mDeliveryType = new ArrayList<>();
    //类型+时间
    private Map<String, List<DeliveryTime>> mDeliveryTypeTimes = new HashMap<>();

    //当前使用的优惠券
    private Coupon mUseCoupon;

    private List<OnShopCartListener> mShopCartListeners = new ArrayList<>();

    //被选中的菜品Id列表
    private Set<String> mSelectedDishIds = new HashSet<>();

    //登录状态添加到购物车的菜品数量
    private int mAddedDishCountInLogin;

    //是否初始化工程
    private boolean mIsNothingChanged;

    private int mQuantityLimit;

    private DishPolicy() {
    }

    public static DishPolicy get() {
        if (sPolicy == null) {
            sPolicy = new DishPolicy();
        }
        return sPolicy;
    }

    public void update() {
        update(false);
    }

    public void update(boolean force) {

        if (mIsNothingChanged && !force) {
            //如果
            handleShopCartListeners();
            return;
        }
        mIsNothingChanged = true;

        if (UserProperties.isLogin()) {
            mJsonDishShopCart = JSON_DISH_SHOP_CART + UserProperties.getUserId();
            mJsonDishImperfectShopCart = JSON_DISH_IMPERFECT_SHOP_CART + UserProperties.getUserId();
        } else {
            mJsonDishShopCart = JSON_DISH_SHOP_CART;
            mJsonDishImperfectShopCart = JSON_DISH_IMPERFECT_SHOP_CART;
        }
//        saveLocalShopCart(FileUtils.readAssetsFile("shopcart.json"));
        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                loadLocalShopCart();
                fetchOnlineShopCart();
            }
        });

    }

    /**
     * 将无登录状态下的本地的数据转移到登录状态下
     */
    public void translateLocalData() {
        if (UserProperties.isLogin()) {
            String shopCartJson = (String) SharePreferenceProperties.get(JSON_DISH_SHOP_CART, "");
            if (!StringUtils.isEmpty(shopCartJson)) {
                String dishJson = "";
                try {
                    JsonElement element = JsonUtils.getJsonElement(shopCartJson);
                    if (element != null && element.isJsonArray()) {
                        JsonArray array = element.getAsJsonArray();
                        if (array.size() > 0) {
                            element = array.get(0);
                            if (element.isJsonObject()) {
                                JsonObject jsonObject = element.getAsJsonObject();
                                if (jsonObject.has("shop_cart_dishes")) {
                                    dishJson = jsonObject.get("shop_cart_dishes").toString();
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!StringUtils.isEmpty(dishJson)) {
                    SharePreferenceProperties.set(JSON_DISH_SHOP_CART, "");
                    SharePreferenceProperties.set(JSON_DISH_IMPERFECT_SHOP_CART + UserProperties.getUserId(), dishJson);
                }
            }
        }
    }

    //***********我的地址

    /**
     * 保存收菜地址
     *
     * @param address
     */
    public void saveDishAddress(Address address) {
        String json = null;
        try {
            json = address.toJson();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharePreferenceProperties.set(JSON_DISH_ADDRESS, json);
        mDishAddressJson = json;
        if (!StringUtils.isEmpty(json)) {
            mDishAddress = new Address();
            Location location = new Location();
            location.setLatitude(LocationServer.asInstance().getLat() + "");
            location.setLongitude(LocationServer.asInstance().getLon() + "");
            mDishAddress.setLocation(location);
            try {
                mDishAddress.parseJson(mDishAddressJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得收菜地址
     *
     * @return
     */
    public Address getDishAddress() {
        if (StringUtils.isEmpty(mDishAddressJson)) {
            mDishAddressJson = (String) SharePreferenceProperties.get(JSON_DISH_ADDRESS, "");
        }

        if (mDishAddress == null
                || (mDishAddress.getLocation() != null && !mDishAddress.getLocation().effective())) {
            mDishAddress = new Address();
            Location location = new Location();
            location.setLatitude(LocationServer.asInstance().getLat() + "");
            location.setLongitude(LocationServer.asInstance().getLon() + "");
            mDishAddress.setLocation(location);
            try {
                mDishAddress.parseJson(mDishAddressJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mDishAddress;
    }

    private void handleShopCartListeners() {
        int count = getShopCartCount();
        for (OnShopCartListener listener : mShopCartListeners) {
            listener.onUpdate(count);
        }
    }

    //***********我的购物车
    public void addOnShopCartListener(OnShopCartListener listener) {
        mShopCartListeners.remove(listener);
        mShopCartListeners.add(listener);
    }

    public void removeOnShopCartListener(OnShopCartListener listener) {
        mShopCartListeners.remove(listener);
    }

    public void fetchOnlineShopCart() {
        updateAndSyncShopCart(null);
    }

    /**
     * 加载本地购物车列表
     *
     * @return
     */
    public List<ShopCartRestaurant> loadLocalShopCart() {

        //读取本地数据
        String shopCartJson = (String) SharePreferenceProperties.get(mJsonDishShopCart, "");

        ApiModelList<ShopCartRestaurant> list = new ApiModelList<>(new ShopCartRestaurant());
        try {
            list.parseJson(shopCartJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //读取不完整本地数据
        String imperfectShopCartJson = (String) SharePreferenceProperties.get(mJsonDishImperfectShopCart, "");

        ApiModelList<ShopCartDish> dishList = new ApiModelList<>(new ShopCartDish());
        try {
            dishList.parseJson(imperfectShopCartJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        clearAll();

        mImperfectDishes.addAll(dishList.getList());
        if (!mImperfectDishes.isEmpty()) {
            for (ShopCartDish dish : mImperfectDishes) {
                if (dish.isSelected()) {
                    mSelectedDishIds.add(dish.getDishId());
                }
            }
        }

        mShopCart.addAll(list.getList());

        prepareShopCart();

        return mShopCart;
    }

    /**
     * 准备购物车数据
     */
    private void prepareShopCart() {
        for (int i = 0; i < mShopCart.size(); i++) {
            ShopCartRestaurant restaurant = mShopCart.get(i);

            //准备购物车展示数据
            ShopCartDish dish = new ShopCartDish();
            dish.setRestaurant(restaurant);
            dish.setItemType(ShopCartDish.ITEM_TYPE_RESTAURANT);
            mDishesOfShopCart.add(dish);
            for (ShopCartDish itemDish : restaurant.getShopCartDishes()) {
                itemDish.setItemType(ShopCartDish.ITEM_TYPE_DISH);
                itemDish.setRestaurant(restaurant);
                if (!itemDish.isSelected()) {
                    //更新从网络获取的购物车数据是否被选中
                    itemDish.setSelected(checkSelected(itemDish));
                } else {
                    //添加本地已选中的菜品到列表中
                    mSelectedDishIds.add(itemDish.getDishId());
                }
                mDishesOfShopCart.add(itemDish);
            }
            dish = new ShopCartDish();
            dish.setItemType(ShopCartDish.ITEM_TYPE_DIVIDING);
            dish.setRestaurant(restaurant);
            mDishesOfShopCart.add(dish);

            //准备购物车内部数据
            mDishesMap.put(restaurant, restaurant.getShopCartDishes());
            mDishes.addAll(restaurant.getShopCartDishes());
        }

    }

    private boolean checkSelected(ShopCartDish itemDish) {
        for (String dishId : mSelectedDishIds) {
            if (dishId != null && dishId.equals(itemDish.getDishId())) {
                return true;
            }
        }
        return itemDish.isSelected();
    }

    /**
     * 记录选中菜品
     *
     * @param dishId
     */
    public void recordSelectedDish(String dishId) {
        if (!mSelectedDishIds.contains(dishId)) {
            mSelectedDishIds.add(dishId);
        }
    }

    /**
     * 取消选中菜品
     *
     * @param dishId
     */
    public void removeSelectedDish(String dishId) {
        if (mSelectedDishIds.contains(dishId)) {
            mSelectedDishIds.remove(dishId);
        }
    }

    /**
     * 合并不完整的菜品
     */
    private boolean mergeImperfectDishOfShopCart() {
        if (!mImperfectDishes.isEmpty()) {
            for (ShopCartDish imperfect : mImperfectDishes) {

                boolean find = false;
                ShopCartDish changedDish = null;
                for (ShopCartDish dish : mDishes) {
                    if (dish.getDishId().equals(imperfect.getDishId())) {
                        dish.setQuantity(dish.getQuantity() + imperfect.getQuantity());
                        find = true;
                        changedDish = dish;
                        break;
                    }
                }

                if (!find) {
                    mDishes.add(0, imperfect);
                } else {
                    mDishes.remove(changedDish);
                    mDishes.add(0, changedDish);
                }
            }
            mImperfectDishes.clear();
            clearLocalImperfectShopCart();

            saveLocalShopCart();
            return true;
        }

        return false;
    }

    /**
     * 保存本地购物车数据
     */
    public void saveLocalShopCart() {
        //未登录用户，需要保存数据到本地，登录用户不用保存
        saveLocalImperfectShopCart();
        if (!mShopCart.isEmpty() && !UserProperties.isLogin()) {
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            try {
                jsonWriter.setLenient(true);
                jsonWriter.beginArray();
                for (ShopCartRestaurant restaurant : mShopCart) {
                    jsonWriter.beginObject();
                    restaurant.writeJson(jsonWriter);
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
                jsonWriter.flush();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String json = writer.toString();
            SharePreferenceProperties.set(mJsonDishShopCart, json);
        } else {
            SharePreferenceProperties.set(mJsonDishShopCart, "");
        }
    }

    /**
     * 保存本地不完整的购物车数据
     */
    public void saveLocalImperfectShopCart() {
        if (!mImperfectDishes.isEmpty() && !UserProperties.isLogin()) {
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            try {
                jsonWriter.setLenient(true);
                jsonWriter.beginArray();
                for (ShopCartDish dish : mImperfectDishes) {
                    jsonWriter.beginObject();
                    dish.writeJson(jsonWriter);
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
                jsonWriter.flush();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String json = writer.toString();
            SharePreferenceProperties.set(mJsonDishImperfectShopCart, json);
        } else {
            SharePreferenceProperties.set(mJsonDishImperfectShopCart, "");
        }
    }

    /**
     * 清理本地不完整的
     */
    private void clearLocalImperfectShopCart() {
        SharePreferenceProperties.set(mJsonDishImperfectShopCart, "");
    }

    /**
     * 清理本地购物车
     */
    private void clearLocalShopCart() {
        SharePreferenceProperties.set(mJsonDishShopCart, "");
    }

    /**
     * 清除本地数据
     */
    public void clearLocal() {
        clearLocalImperfectShopCart();
        clearLocalShopCart();
    }

    /**
     * 更新购物车中的数据
     *
     * @param dish
     */
    public void updateDishInShopCart(Dish dish, final OnShopCartUpdateListener listener) {
        if (NetworkState.available() && UserProperties.isLogin()) {
            ShopCartDish pendingUpdateItem = null;
            if (dish instanceof ShopCartDish) {
                pendingUpdateItem = (ShopCartDish) dish;
            } else {
                pendingUpdateItem = new ShopCartDish(dish);
            }

            DishApi.addToShopCart(pendingUpdateItem.getDishId(), pendingUpdateItem.getQuantity(),
                    "update", new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            Logger.d("update shopcart " + result.available()
                                    + " msg: " + result.getErrorMsg());
                            if (listener != null) {
                                listener.onResult(result.available(), result.getErrorMsg());
                            }
                        }
                    });
        }
    }

    /**
     * 添加到购物车
     *
     * @param dish
     * @param listener
     */
    public void addDishToShopCart(Dish dish, final OnShopCartUpdateListener listener) {
        //1.必须联网
        if (NetworkState.available()) {
            ShopCartDish pendingAddItem = null;
            for (ShopCartDish inShopCardDish : mDishes) {
                if (inShopCardDish != null && inShopCardDish.getDishId().equals(dish.getDishId())) {
                    pendingAddItem = inShopCardDish;
                    break;
                }
            }

            if (pendingAddItem == null) {
                pendingAddItem = new ShopCartDish(dish);
            }

            Restaurant restaurant = pendingAddItem.getRestaurant();
            if (restaurant != null) {
                int provideMax = restaurant.getProvideMax();
                if (provideMax > 0 && pendingAddItem.getQuantity() >= provideMax) {
                    pendingAddItem.setQuantity(provideMax);
                    if (listener != null) {
                        listener.onResult(false, "已达库存上限");
                    }
                    return;
                }

                if (DishPolicy.get().getQuantityLimit() > 0
                        && pendingAddItem.getQuantity() > DishPolicy.get().getQuantityLimit()) {
                    if (listener != null) {
                        listener.onResult(false, "已达购买上限");
                    }
                    return;
                }
            }

            pendingAddItem.setSelected(true);
            mSelectedDishIds.add(pendingAddItem.getDishId());

            if (UserProperties.isLogin()) {
                //登录用户在服务端进行添加
                DishApi.addToShopCart(dish.getDishId(), 1, "add", new ApiCallback<State>() {
                    @Override
                    public void onResult(State result) {
                        if (listener != null) {
                            listener.onResult(result.available(), result.getErrorMsg());
                        }

                        if (result.available()) {
                            //网络添加成功之后，等待数据合并
                            mAddedDishCountInLogin++;
                            mIsNothingChanged = false;
                        }
                        //更新
                        handleShopCartListeners();
                    }
                });
            } else {
                //2.检查是否包含
                boolean contain = false;
                for (ShopCartDish item : mImperfectDishes) {
                    if (item.getDishId().equals(dish.getDishId())) {
                        pendingAddItem = item;
                        item.setQuantity(item.getQuantity() + 1);
                        contain = true;
                        break;
                    }
                }

                //非登录用户在服务端进行添加
                if (!contain) {
                    mImperfectDishes.add(0, pendingAddItem);
                    pendingAddItem.setQuantity(1);
                }

                saveLocalShopCart();

                mIsNothingChanged = false;

                if (listener != null) {
                    listener.onResult(true, "");
                }

                //更新
                handleShopCartListeners();
            }

        } else {
            ToastAlarm.show("网络无效");
        }

    }

    /**
     * 从购物车移除一个菜品
     *
     * @param deleteItem
     * @param listener
     */
    public void removeFromShopCart(ShopCartDish deleteItem, final OnShopCartUpdateListener listener) {
        List<ShopCartDish> dishes = new ArrayList<>();
        dishes.add(deleteItem);
        removeFromShopCart(dishes, listener);
    }

    /**
     * 从购物车移除一组菜品
     *
     * @param deleteList
     * @param listener
     */
    public void removeFromShopCart(final List<ShopCartDish> deleteList, final OnShopCartUpdateListener listener) {

        if (!NetworkState.available()) {
            if (listener != null) {
                listener.onResult(false, "网络异常");
            }
            return;
        }

        if (UserProperties.isLogin()) {

            String cardIds = "";
            for (ShopCartDish dish : deleteList) {
                if (dish.getItemType() == ShopCartDish.ITEM_TYPE_DISH) {
                    cardIds += dish.getCartId() + ",";
                }
            }

            if (cardIds.length() > 0) {
                cardIds = cardIds.substring(0, cardIds.length() - 1);
            }

            DishApi.removeShopCart(cardIds, new ApiCallback<State>() {
                @Override
                public void onResult(State result) {
                    if (result.available()) {
                        removeFromLocalShopCart(deleteList);
                        mIsNothingChanged = false;
                        handleShopCartListeners();
                    }

                    if (listener != null) {
                        listener.onResult(result.available(), result.getErrorMsg());
                    }
                }
            });

        } else {
            removeFromLocalShopCart(deleteList);
            mIsNothingChanged = false;
            if (listener != null) {
                listener.onResult(true, "");
            }

            handleShopCartListeners();

        }

    }

    /**
     * 移除本地数据
     *
     * @param deleteList
     */
    private void removeFromLocalShopCart(List<ShopCartDish> deleteList) {
        mIsNothingChanged = false;
        mDishes.removeAll(deleteList);
        mDishesOfShopCart.removeAll(deleteList);
        for (List<ShopCartDish> list : mDishesMap.values()) {
            list.removeAll(deleteList);
        }
        Iterator<ShopCartRestaurant> restaurantIterator = mDishesMap.keySet().iterator();
        while (restaurantIterator.hasNext()) {
            ShopCartRestaurant entry = restaurantIterator.next();
            if (mDishesMap.get(entry).isEmpty()) {
                restaurantIterator.remove();
            }
        }

        for (ShopCartRestaurant restaurant : mShopCart) {
            restaurant.getShopCartDishes().removeAll(deleteList);
        }

        restaurantIterator = mShopCart.iterator();
        while (restaurantIterator.hasNext()) {
            ShopCartRestaurant entry = restaurantIterator.next();
            if (entry.getShopCartDishes().isEmpty()) {
                restaurantIterator.remove();
            }
        }

        Set<String> deleteIds = new HashSet<>();
        for (ShopCartDish dish : deleteList) {
            deleteIds.add(dish.getDishId());
        }
        mSelectedDishIds.removeAll(deleteIds);

        saveLocalShopCart();
    }

    /**
     * 从购物车中通过餐厅ID找到对应菜品列表
     *
     * @param restaurant
     * @return
     */
    public List<ShopCartDish> findDishesByRestaurantIdFromShopCart(ShopCartRestaurant restaurant) {
        return mDishesMap.get(restaurant);
    }


    /**
     * 获得菜品数量
     *
     * @return
     */
    public int getShopCartCount() {
        int total = 0;
        for (ShopCartDish dish : mImperfectDishes) {
            total += dish.getQuantity();
        }

        for (ShopCartDish dish : mDishes) {
            if (ShopCartDish.STATE_NORMAL.equals(dish.getState())) {
                total += dish.getQuantity();
            }
        }

        if (UserProperties.isLogin()) {
            return mAddedDishCountInLogin + total;
        }

        return total;
    }

    /**
     * 获得视图显示上的所有数据
     *
     * @return
     */
    public List<ShopCartDish> getListOfShopCart() {
        return mDishesOfShopCart;
    }

    /**
     * 获得视图显示的数据
     *
     * @param restaurantId
     * @return
     */
    public ShopCartDish getShopCartDivItemByRestaurantId(String restaurantId) {
        for (ShopCartDish dish : mDishesOfShopCart) {
            if (dish.getItemType() == ShopCartDish.ITEM_TYPE_DIVIDING
                    && dish.getRestaurant() != null
                    && dish.getRestaurant().getId() != null
                    && dish.getRestaurant().getId().equals(restaurantId)) {
                return dish;
            }
        }
        return null;
    }

    public void updateAndSyncShopCart(final OnShopCartUpdateListener listener) {
        updateAndSyncShopCart(false, listener);
    }

    /**
     * 更新数据
     *
     * @param forceSync
     * @param listener
     */
    public void updateAndSyncShopCart(boolean forceSync, final OnShopCartUpdateListener listener) {
        final OnShopCartUpdateListener innerListener = new OnShopCartUpdateListener() {
            @Override
            public void onResult(boolean success, String info) {
                if (listener != null) {
                    listener.onResult(success, info);
                }

                handleShopCartListeners();
            }
        };

        if (NetworkState.available()) {
            final ApiCallback<ApiModelList<ShopCartRestaurant>> callback =
                    new ApiCallback<ApiModelList<ShopCartRestaurant>>() {
                        @Override
                        public void onResult(ApiModelList<ShopCartRestaurant> result) {

                            if (result.available()) {
                                mQuantityLimit = StringUtils.parseInt(result.getExtra().getString("quantity_limit"));
                                clearAll();
                                mShopCart.addAll(result.getList());
                                prepareShopCart();
                                saveLocalShopCart();
                            }

                            innerListener.onResult(result.available(), result.getErrorMsg());
                        }
                    };

            boolean hasImperfect = mergeImperfectDishOfShopCart();
            if (UserProperties.isLogin()) {
                if (hasImperfect | forceSync) {
                    //有不完整的本地数据，先进行网络同步，再请求账户购物车列表
                    DishApi.syncShopCart(getShopCartIdAndCounts(), new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            if (result.available()) {
                                clearLocal();
                            }
                            DishApi.cartList(callback);
                        }
                    });
                } else {
                    DishApi.cartList(callback);
                }
            } else {
                //本地有数据，请求菜品更新数据列表
                if (!mDishes.isEmpty()) {
                    DishApi.cartListOffline(getShopCartIdAndCounts(), callback);
                } else {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            innerListener.onResult(true, "");
                        }
                    });
                }
            }
        } else {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    innerListener.onResult(false, "");
                }
            });
        }

    }

    /**
     * 获得所有菜品的id和数量组合串
     *
     * @return
     */
    private String getShopCartIdAndCounts() {
        String idAndCounts = "";
        if (!mDishes.isEmpty()) {
            for (int i = 0; i < mDishes.size(); i++) {
                ShopCartDish dish = mDishes.get(i);
                if (i == mDishes.size() - 1) {
                    idAndCounts += dish.getDishId() + "," + dish.getQuantity();
                } else {
                    idAndCounts += dish.getDishId() + "," + dish.getQuantity() + ";";
                }
            }
        }
        return idAndCounts;
    }

    public List<ShopCartRestaurant> getCheckoutRestaurantList() {
        List<ShopCartRestaurant> list = new ArrayList<>();
        list.addAll(mCheckOutDishes.keySet());
        return list;
    }

    /**
     * 获得购物车商品总价，包含配送费
     *
     * @return
     */
    public float getCheckoutTotalPriceWithDelivery() {
        float total = 0;
        Set<ShopCartRestaurant> restaurants = mCheckOutDishes.keySet();
        for (ShopCartRestaurant restaurant : restaurants) {
            total += restaurant.getCheckoutTotalPrice() + restaurant.getDeliveryPrice();
        }

        return total;
    }

    /**
     * 结算餐厅ID串
     *
     * @return
     */
    public String getCheckoutRestaurantListIds() {
        String ids = "";
        Set<ShopCartRestaurant> restaurants = mCheckOutDishes.keySet();
        for (ShopCartRestaurant restaurant : restaurants) {
            ids += restaurant.getId() + ",";
        }
        return StringUtils.isEmpty(ids) ? "" : ids.substring(0, ids.length() - 1);
    }

    /**
     * 去结算
     */
    public void checkOutShopCart() {
        //先清空配送时间
        releaseCheckOutRestaurantTime();
        mCheckOutDishes.clear();
        Set<Map.Entry<ShopCartRestaurant, List<ShopCartDish>>> entrySet = mDishesMap.entrySet();
        for (Map.Entry<ShopCartRestaurant, List<ShopCartDish>> entry : entrySet) {
            List<ShopCartDish> list = entry.getValue();
            List<ShopCartDish> selectedDish = new ArrayList<>();
            for (ShopCartDish dish : list) {
                if (dish.isSelected() &&
                        ShopCartDish.STATE_NORMAL.equals(dish.getState())) {
                    selectedDish.add(dish);
                }
            }
            if (!selectedDish.isEmpty()) {
                mCheckOutDishes.put(entry.getKey(), selectedDish);
            }
        }
    }

    public boolean isCheckOutEmpty() {
        return mCheckOutDishes.isEmpty();
    }

    /**
     * 清理已结账的菜品
     */
    public void clearLocalCheckOut() {
        List<ShopCartDish> checkOutList = new ArrayList<>();
        for (Map.Entry<ShopCartRestaurant, List<ShopCartDish>> entry : mCheckOutDishes.entrySet()) {
            checkOutList.addAll(entry.getValue());
        }
        removeFromLocalShopCart(checkOutList);
        handleShopCartListeners();
    }

    /**
     * 添加配送时间
     *
     * @param time
     * @param deliveryType 商户类型
     */
    public void addDeliveryTime(DeliveryTime time, String deliveryType) {
        mDeliveryTimes.add(time);
        addDeliveryType(deliveryType);
        if (mDeliveryTypeTimes.containsKey(deliveryType)) {
            if (mDeliveryTypeTimes.get(deliveryType).contains(time)) {
                return;
            }
            mDeliveryTypeTimes.get(deliveryType).add(time);
        } else {
            List<DeliveryTime> timeList = new ArrayList<>();
            timeList.add(time);
            mDeliveryTypeTimes.put(deliveryType, timeList);
        }
    }

    /**
     * 添加配送类型
     *
     * @param deliveryType 类型
     */
    public void addDeliveryType(String deliveryType) {
        if (null != mDeliveryType && !mDeliveryType.contains(deliveryType) &&
                (deliveryType.equals(DeliveryRestaurant.EXPRESS_BY_RESTAURANT) || deliveryType.equals(DeliveryRestaurant.EXPRESS_BY_WECOOK))) {
            mDeliveryType.add(deliveryType);
        }
    }

    /**
     * 更新配送时间
     */
    public void updateDeliveryTimes() {
        Set<ShopCartRestaurant> restaurants = mCheckOutDishes.keySet();
        mDeliveryTimes.clear();
        mDeliveryType.clear();
        mDeliveryTypeTimes.clear();
        for (ShopCartRestaurant restaurant : restaurants) {
            if (restaurant.getDeliveryTime() != null) {
                if (null != restaurant.getExpressType()) {
                    addDeliveryTime(restaurant.getDeliveryTime(), restaurant.getExpressType());
                } else {
                    mDeliveryTimes.add(restaurant.getDeliveryTime());
                }
            }
        }
    }

    /**
     * 获得配送时间列表
     *
     * @return
     */
    public List<DeliveryTime> getDeliveryTimes() {
        return mDeliveryTimes;
    }

    /**
     * 获取配送时间+配送类型对应表
     *
     * @return
     */
    public Map<String, List<DeliveryTime>> getDeliveryTypeTimes() {
        return mDeliveryTypeTimes;
    }

    /**
     * 获得配送类型列表
     *
     * @return
     */
    public List<String> getDeliveryType() {
        return mDeliveryType;
    }

    /**
     * 是否存在空的配送时间
     *
     * @return
     */
    public boolean hasEmptyDeliveryTimes() {
        boolean hasEmpty = false;
        Set<ShopCartRestaurant> restaurants = mCheckOutDishes.keySet();
        for (ShopCartRestaurant restaurant : restaurants) {
            if (restaurant.getDeliveryTime() == null) {
                hasEmpty = true;
                break;
            }
        }
        return hasEmpty;
    }

    public Coupon getUseCoupon() {
        return mUseCoupon;
    }

    public void setUseCoupon(Coupon coupon) {
        this.mUseCoupon = coupon;
    }

    public void clearAll() {
        mAddedDishCountInLogin = 0;
        mDishes.clear();
        mDishesMap.clear();
        mShopCart.clear();
        mDishesOfShopCart.clear();
        mImperfectDishes.clear();
        mCheckOutDishes.clear();
        mDeliveryTimes.clear();
        mDeliveryTypeTimes.clear();
        mDeliveryType.clear();
    }

    /**
     * 清空选中餐厅 已选时间
     */
    private void releaseCheckOutRestaurantTime() {
        for (ShopCartRestaurant shopCartRestaurant : getCheckoutRestaurantList()) {
            shopCartRestaurant.setDeliveryTime(null);
        }

        mDeliveryTimes.clear();
        mDeliveryTypeTimes.clear();
        mDeliveryType.clear();
    }

    public int getQuantityLimit() {
        return mQuantityLimit;
    }

    /**
     * 释放
     */
    public void release() {
        mSelectedDishIds.clear();
        mUseCoupon = null;
        clearAll();
    }

    public interface OnShopCartListener {
        void onUpdate(int count);
    }

    public interface OnShopCartUpdateListener {
        void onResult(boolean success, String info);
    }

}
