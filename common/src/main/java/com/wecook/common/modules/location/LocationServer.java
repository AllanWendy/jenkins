package com.wecook.common.modules.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.ModuleManager;
import com.wecook.common.modules.messager.XMPushMessager;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.StringUtils;

/**
 * 定位服务
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/7/14
 */
public class LocationServer extends BaseModule implements BDLocationListener {

    public static final int EXPIRED_TIME = 1 * 3600 * 1000;
    public static final String SEND_BROADCAST_SELECTED_CITY = "send_Broadcast_Selected_City";
    public static final String SEND_BROADCAST_INDEX_CITY = "sene_Broadcast_Index_City";
    public static String SELECTED_CITY = "selected_city";
    public static String INDEX_CITY = "index_city";
    private static LocationServer sLocationServer;
    private OnLocationChangedListener mListener;
    private OnLocationResultListener mResultListener;

    private LocationClient mLocationClient;
    private double mLon;
    private double mLat;

    private String mCountry;
    private String mCity;
    private String mAddress;
    private String mAddressDesc;
    private String mStreet;
    private String mStreetNo;
    private String[] mPoiList;

    public static LocationServer asInstance() {
        if (sLocationServer == null) {
            sLocationServer = (LocationServer) ModuleManager.asInstance().getModule(LocationServer.class);
        }
        return sLocationServer;
    }

    /**
     * 获取选择的城市
     *
     * @return
     */
    public String getSelectedCity() {
        return (String) SharePreferenceProperties.get(SELECTED_CITY, "");
    }

    /**
     * 设置选择的城市
     *
     * @param selectedCity
     */
    public void setSelectedCity(String selectedCity, Activity activity, boolean isSendBroadcast) {
        if (selectedCity != null && !selectedCity.equals("") && !selectedCity.equals(getSelectedCity())) {
            SharePreferenceProperties.set(SELECTED_CITY, selectedCity);
            if (isSendBroadcast) {
                Bundle bundle = new Bundle();
                bundle.putString(SELECTED_CITY, selectedCity);
                sendLocationBroadcast(activity, bundle, SEND_BROADCAST_SELECTED_CITY);
            }
        }
    }

    /**
     * 设置选择的城市
     *
     * @param selectedCity
     */
    public void setSelectedCity(String selectedCity, Activity activity) {
        setSelectedCity(selectedCity, activity, true);
    }

    /**
     * 获取选择的城市index（服务器需要）
     *
     * @return
     */
    public String getIndexCity() {
        return (String) SharePreferenceProperties.get(INDEX_CITY, "");
    }

    /**
     * 设置选择的城市index（服务器需要）
     *
     * @param indexCity
     */
    public void setIndexCity(String indexCity, Activity activity, boolean isSendBroadcast) {

        if (activity == null) {
            return;
        }

        if (!StringUtils.isEmpty(getIndexCity())) {
            XMPushMessager.unsubscribe(activity, getIndexCity());
        }

        if (indexCity != null && !indexCity.equals("") && !indexCity.equals(getIndexCity())) {
            SharePreferenceProperties.set(INDEX_CITY, indexCity);
            if (isSendBroadcast) {
                Bundle bundle = new Bundle();
                bundle.putString(INDEX_CITY, indexCity);
                sendLocationBroadcast(activity, bundle, SEND_BROADCAST_INDEX_CITY);
            }

            XMPushMessager.subscribe(activity, indexCity);
        }
    }


    /**
     * 设置选择的城市index（服务器需要）
     *
     * @param indexCity
     */
    public void setIndexCity(String indexCity, Activity activity) {
        setIndexCity(indexCity, activity, true);
    }

    @Override
    public void setup(Context context) {
        super.setup(context);
        mLocationClient = new LocationClient(context);
        mLocationClient.registerLocationListener(this);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setOpenGps(true);
        option.setProdName("wecook-loc");
        option.setScanSpan(1200);
        option.disableCache(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * 获得经度
     *
     * @return
     */
    public double getLon() {
        if (isExpiredTime()) {
            PhoneProperties.setLon(0);
        }
        return PhoneProperties.getLon();
    }

    /**
     * 获得纬度
     *
     * @return
     */
    public double getLat() {
        if (isExpiredTime()) {
            PhoneProperties.setLat(0);
        }
        return PhoneProperties.getLat();
    }

    public void setOnLocationChangedListener(OnLocationChangedListener listener) {
        mListener = listener;
    }

    /**
     * 定位
     *
     * @param listener
     */
    public void location(OnLocationResultListener listener) {
        mResultListener = listener;
        location();
    }

    /**
     * 定位
     */
    public void location() {
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
            Logger.d("location", "request location.");
        }
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        try {
            if (bdLocation != null) {
                mLat = bdLocation.getLatitude();
                mLon = bdLocation.getLongitude();
                Logger.d("location", "bdLocation.getLocType() = " + bdLocation.getLocType()
                        + " mLat = " + mLat
                        + " mLon = " + mLon
                        + " time = " + bdLocation.getTime());

                if (mResultListener != null) {
                    boolean result = (bdLocation.getLocType() == BDLocation.TypeGpsLocation
                            || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation
                            || bdLocation.getLocType() == BDLocation.TypeCacheLocation
                            || bdLocation.getLocType() == BDLocation.TypeOffLineLocation)
                            && mLon != 0.0d
                            && mLat != 0.0d;
                    SharePreferenceProperties.set("location_latest_time", Long.parseLong(System.currentTimeMillis() + ""));
                    if (result) {
                        PhoneProperties.setLat(mLat);
                        PhoneProperties.setLon(mLon);
                    }

                    Logger.d("location", "getLatitude:" + bdLocation.getLatitude());
                    Logger.d("location", "getLongitude:" + bdLocation.getLongitude());
                    Logger.d("location", "getLocationDescribe:" + bdLocation.getLocationDescribe());
                    Logger.d("location", "getNetworkLocationType:" + bdLocation.getNetworkLocationType());
                    Logger.d("location", "getTime:" + bdLocation.getTime());
                    Logger.d("location", "getAltitude:" + bdLocation.getAltitude());
                    Logger.d("location", "getDerect:" + bdLocation.getDerect());
                    Logger.d("location", "getLocationWhere:" + bdLocation.getLocationWhere());
                    Logger.d("location", "getSatelliteNumber:" + bdLocation.getSatelliteNumber());
                    Logger.d("location", "getSpeed:" + bdLocation.getSpeed());
                    Logger.d("location", "getRadius:" + bdLocation.getRadius());
                    Logger.d("location", "getSemaAptag:" + bdLocation.getSemaAptag());
                    Logger.d("location", "getCountry:" + bdLocation.getCountry());
                    Logger.d("location", "getCountryCode:" + bdLocation.getCountryCode());
                    Logger.d("location", "getCity:" + bdLocation.getCity());
                    Logger.d("location", "getCityCode:" + bdLocation.getCityCode());
                    Logger.d("location", "getProvince:" + bdLocation.getProvince());
                    Logger.d("location", "getStreet:" + bdLocation.getStreet());
                    Logger.d("location", "getStreetNumber:" + bdLocation.getStreetNumber());
                    Logger.d("location", "getAddrStr:" + bdLocation.getAddrStr());
                    Logger.d("location", "getFloor:" + bdLocation.getFloor());
                    Logger.d("location", "getDistrict:" + bdLocation.getDistrict());

                    String poiString = "";
                    if (bdLocation.getPoiList() != null && !bdLocation.getPoiList().isEmpty()) {
                        mPoiList = new String[bdLocation.getPoiList().size()];
                        for (int i = 0; i < mPoiList.length; i++) {
                            Object poi = bdLocation.getPoiList().get(i);
                            if (poi instanceof Poi) {
                                mPoiList[i] = ((Poi) bdLocation.getPoiList().get(i)).getName();
                                poiString += mPoiList[i] + ";";
                            }
                        }
                    }

                    if (!StringUtils.isEmpty(poiString)) {
                        Logger.d("location", "getPoiList:" + poiString.substring(0, poiString.length() - 1));
                    }

                    Logger.d("location", "getAddress:.country" + bdLocation.getAddress().country);
                    Logger.d("location", "getAddress:.province" + bdLocation.getAddress().province);
                    Logger.d("location", "getAddress:.city" + bdLocation.getAddress().city);
                    Logger.d("location", "getAddress:.district" + bdLocation.getAddress().district);
                    Logger.d("location", "getAddress:.address" + bdLocation.getAddress().address);
                    Logger.d("location", "getAddress:.street" + bdLocation.getAddress().street);
                    Logger.d("location", "getAddress:.streetNumber" + bdLocation.getAddress().streetNumber);

                    Address address = bdLocation.getAddress();
                    mCountry = address.country;

                    mCity = address.city;
                    if (StringUtils.isEmpty(mCity)) {
                        mCity = address.province;
                    }

                    mAddress = "";
                    if (!StringUtils.isEmpty(address.province)) {
                        mAddress += address.province;
                    } else if (!StringUtils.isEmpty(address.city)) {
                        mAddress += address.city;
                    }

                    if (!StringUtils.isEmpty(address.district)) {
                        mAddress += address.district;
                    }
                    if (!StringUtils.isEmpty(address.street)) {
                        mAddress += address.street;
                    }
                    if (!StringUtils.isEmpty(address.streetNumber)) {
                        mAddress += address.streetNumber;
                    }

                    mAddressDesc = bdLocation.getLocationDescribe();

                    mStreet = address.street;
                    mStreetNo = address.streetNumber;
                    Logger.d("location", "My Address : " + mAddress);
                    Logger.d("location", "My Address Desc: " + mAddressDesc);
                    if (mResultListener != null) {
                        mResultListener.onLocationResult(result, bdLocation);
                    }

                }

                if (mListener != null) {
                    mListener.onLocationChanged(mLon, mLat);
                }
            }
        } finally {
            mLocationClient.stop();
            mResultListener = null;
            setOnLocationChangedListener(null);
        }

    }

    public boolean validate() {
        return getLon() != 0 && getLat() != 0 && !isExpiredTime() && !StringUtils.isEmpty(mAddress);
    }

    public boolean isExpiredTime() {
        long latestTime = SharePreferenceProperties.get("location_latest_time", 0L);
        long remainderTime = System.currentTimeMillis() - latestTime;
        return remainderTime > EXPIRED_TIME;
    }

    public String getLocationCountry() {
        return mCountry;
    }

    public String getLocationCity() {
        return mCity;
    }

    public String getLocationAddress() {
        return mAddress;
    }

    public String getLocationAddressDesc() {
        return mAddressDesc;
    }

    public String getLocationStreet() {
        return mStreet;
    }

    public String getLocationStreetNo() {
        return mStreetNo;
    }

    public String[] getLocationPOI() {
        return mPoiList;
    }

    /**
     * 发布广播工具
     *
     * @param activity
     * @param bundle
     * @param actionName
     */
    public void sendLocationBroadcast(Activity activity, Bundle bundle, String... actionName) {
        Intent mIntent = new Intent();
        for (int i = 0; i < actionName.length; i++) {
            mIntent.setAction(actionName[i]);
        }
        mIntent.putExtras(bundle);
        //发送广播
        if (activity != null) {
            activity.sendBroadcast(mIntent);
        }
    }

    public interface OnLocationChangedListener {
        void onLocationChanged(double lon, double lat);
    }

    public interface OnLocationResultListener {
        void onLocationResult(boolean success, BDLocation location);
    }
}
