package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.core.internet.ApiResult;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.AddressSuggestion;
import com.wecook.sdk.api.model.Location;
import com.wecook.sdk.api.model.LocationInfo;

/**
 * 地理位置API
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/19
 */
public class LocationApi extends Api {

    public static void updateLocation(ApiCallback<Location> callback) {
        Api.get(LocationApi.class)
                .with("/location/update")
                .addParams("wid", PhoneProperties.getDeviceId(), true)
                .addParams("lat", PhoneProperties.getLat() + "", true)
                .addParams("lon", PhoneProperties.getLon() + "", true)
                .setApiCallback(callback)
                .toModel(new Location())
                .executeGet();
    }

    /**
     * 搜索地址建议
     *
     * @param keyword  关键字
     * @param region   区域
     * @param callback
     */
    public static ApiResult getLocationSuggestion(String keyword, String region, ApiCallback<ApiModelList<AddressSuggestion>> callback) {
        Api api = Api.get(LocationApi.class)
                .with("/location/suggestion")
                .addParams("query", keyword, true)
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .setApiCallback(callback)
                .toModel(new ApiModelList<AddressSuggestion>(new AddressSuggestion()));
        if (!StringUtils.isEmpty(region)) {
            api.addParams("region", region);
        }
        return api.executeGet();
    }

    /**
     * 逆地理解析
     *
     * @param lat
     * @param lon
     * @param callback
     */
    public static void getLocationInfo(String lat, String lon, ApiCallback<LocationInfo> callback) {
        Api.get(LocationApi.class)
                .with("/location/regeocoder")
                .addParams("lat", lat, true)
                .addParams("lon", lon, true)
                .toModel(new LocationInfo())
                .setApiCallback(callback)
                .executeGet();
    }
}
