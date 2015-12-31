package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.PointAddress;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 地址API
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/24
 */
public class AddressApi extends Api {

    /**
     * 获得地址列表
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getAddressList(int page, int pageSize, ApiCallback<ApiModelList<Address>> callback) {
        Api.get(AddressApi.class)
                .with("/address")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .toModel(new ApiModelList<Address>(new Address()))
                .setApiCallback(callback)
                .executeGet();
    }


    /**
     * 获得地址列表(带有经纬度)
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    public static void getAddressListWithLatLon(int page, int pageSize,
                                                ApiCallback<ApiModelList<Address>> callback) {
        Api.get(AddressApi.class)
                .with("/address/accurate_address")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", page + "", true)
                .addParams("batch", pageSize + "", true)
                .toModel(new ApiModelList<Address>(new Address()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 检查地址在配送范围内
     *
     * @param addressId
     * @param restaurantIds
     * @param callback
     */
    public static void checkAddressRange(String addressId, String restaurantIds, ApiCallback<State> callback) {
        Api.get(AddressApi.class)
                .with("/address/range_restaurant")
                .addParams("address_id", addressId, true)
                .addParams("restaurant_ids", restaurantIds, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();

    }

    /**
     * 保存地址
     *
     * @param address
     * @param callback
     */
    public static void saveAddress(Address address, ApiCallback<State> callback) {
        Api.get(AddressApi.class)
                .with("/address/save")
                .addParams("id", address.getId())
                .addParams("is_default", address.isDefault() ? "1" : "0")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("name", address.getName(), true)
                .addParams("tel", address.getTel(), true)
                .addParams("city", address.getCity(), true)
                .addParams("street", address.getStreet(), true)
                .addParams("addon", address.getAddon())
                .addParams("lon", address.getLon(), true)
                .addParams("lat", address.getLat(), true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 删除地址
     *
     * @param addressId
     * @param callback
     */
    public static void deleteAddress(String addressId, ApiCallback<State> callback) {
        Api.get(AddressApi.class)
                .with("/address/delete")
                .addParams("id", addressId, true)
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得用户有效地址列表（含仅纬度）
     *
     * @param callback
     */
    public static void getAddressList(ApiCallback<ApiModelList<Address>> callback) {
        Api.get(UserApi.class)
                .with("/address/accurate_address")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("page", "1", true)
                .addParams("batch", "10", true)
                .toModel(new ApiModelList<Address>(new Address()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得用户有效地址列表（含仅纬度）
     *
     * @param callback
     */
    public static void setDefault(String addressId, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/address/set_default")
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("id", addressId, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 附近自提点列表
     *
     * @param address_id
     * @param callback
     */
    public static void getNearbyStroresList(String address_id, ApiCallback<ApiModelList<PointAddress>> callback) {
        Api.get(UserApi.class)
                .with("/logistics/nearby_stores_list")
                .addParams("address_id", address_id, true)
                .toModel(new ApiModelList<PointAddress>(new PointAddress()))
                .setApiCallback(callback)
                .executeGet();

    }
}
