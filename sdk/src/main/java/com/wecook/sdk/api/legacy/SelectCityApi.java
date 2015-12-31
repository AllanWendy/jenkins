package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.model.CheckCityByLonLat;
import com.wecook.sdk.api.model.City;
import com.wecook.sdk.api.model.SelectCity;

/**
 * 请求地域开通范围
 * Created by shan on 2015/8/31.
 */
public class SelectCityApi extends Api {
    /**
     * 获取可选择的城市
     *
     * @param callback
     */
    public static void getSelecteCityList(String city, ApiCallback<ApiModelList<City>> callback) {
        Api.get(SelectCityApi.class)
                .with("/city/index")
                .addParams("city", city)
                .toModel(new ApiModelList<City>(new City()))
                .setApiCallback(callback)
                .setCacheTime(CACHE_ONE_DAY)
                .executeGet();
    }

    /**
     * 获取当前城市
     *
     * @param lon
     * @param lat
     * @param apiCallback
     */
    public static void getIndexCityInfo(String lon, String lat, ApiCallback<SelectCity> apiCallback) {
        Api.get(SelectCityApi.class)
                .with("/city/pos")
                .addParams("lon", lon, true)
                .addParams("lat", lat, true)
                .toModel(new SelectCity())
                .setApiCallback(apiCallback)
                .executeGet();
    }

    public static void getCheckCityByLonLat(String lon, String lat, String city, ApiCallback<CheckCityByLonLat> apiCallback) {
        if (null == city || city.equals("")) {
            city = "北京市";
        }
        Api.get(SelectCityApi.class)
                .with("/city/check_city_by_lonlat")
                .addParams("lon", lon, true)
                .addParams("lat", lat, true)
                .addParams("city", city, true)
                .toModel(new CheckCityByLonLat())
                .setApiCallback(apiCallback)
                .executeGet();
    }
}
