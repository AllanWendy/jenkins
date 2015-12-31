package com.wecook.common.modules.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.ModuleManager;
import com.wecook.common.utils.HttpUtils;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.params.HttpParams;

import java.util.HashSet;

/**
 * 网络状态
 *
 * @author kevin created at 9/23/14
 * @version 1.0
 */
public class NetworkState extends BaseModule{
    /**
     * Network type is unknown
     */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /**
     * Current network is GPRS
     */
    public static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    public static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    public static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    public static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    public static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    public static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    public static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    public static final int NETWORK_TYPE_HSPAP = 15;


    /**
     * Unknown network class. {@hide}
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    /**
     * Class of broadly defined "2G" networks. {@hide}
     */
    public static final int NETWORK_CLASS_2_G = 1;
    /**
     * Class of broadly defined "3G" networks. {@hide}
     */
    public static final int NETWORK_CLASS_3_G = 2;
    /**
     * Class of broadly defined "4G" networks. {@hide}
     */
    public static final int NETWORK_CLASS_4_G = 3;

    private static NetworkState sNetworkState;

    private Context mContext;

    private BroadcastReceiver mNetworkChanged;
    private State mCurrentState;
    private boolean mIsProxyed;

    private HashSet<StateListener> stateListeners = new HashSet<StateListener>();

    private NetworkState() {
    }

    public static NetworkState asInstance() {
        if (sNetworkState == null) {
            sNetworkState = (NetworkState) ModuleManager.asInstance().getModule(NetworkState.class);
        }
        return sNetworkState;
    }

    /**
     * @param context
     */
    public void setup(Context context) {
        mContext = context;
        mNetworkChanged = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    updateNetworkState(context);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mNetworkChanged, filter);

        updateNetworkState(context);
    }

    private void updateNetworkState(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        mCurrentState = new State();
        mCurrentState.available = false;
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    mCurrentState.available = true;
                    break;
                }
            }
        }
        NetworkInfo mWifi = mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi != null) {
            mCurrentState.useWifi = mWifi.isConnected();
        } else {
            mCurrentState.useWifi = false;
        }

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkClassType = getNetworkClass(tm.getNetworkType());
        switch (networkClassType) {
            case NETWORK_CLASS_4_G:
                mCurrentState.use4G = true;
                break;
            case NETWORK_CLASS_3_G:
                mCurrentState.use3G = true;
                break;
            case NETWORK_CLASS_2_G:
                mCurrentState.use2G = true;
                break;
            default:
                mCurrentState.use4G = false;
                mCurrentState.use3G = false;
                mCurrentState.use2G = false;
                break;
        }

        mCurrentState.useProxy = mIsProxyed;

        for (StateListener listener : stateListeners) {
            listener.onChanged(mCurrentState);
        }
    }

    /**
     * @param context
     */
    public void unregister(Context context) {
        if (mNetworkChanged != null) {
            context.unregisterReceiver(mNetworkChanged);
        }
    }

    /**
     * 根据当前网络状态填充代理
     *
     * @param httpParams
     */
    public void fillProxy(final HttpParams httpParams) {
        HttpHost proxy = HttpUtils.getProxy(mContext);
        if (proxy != null) {
            mIsProxyed = true;
            if (mCurrentState != null) {
                mCurrentState.useProxy = true;
            }
            httpParams.setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
        }
    }

    /**
     * 移除代理
     *
     * @param httpParams
     */
    public void removeProxy(final HttpParams httpParams) {
        mIsProxyed = false;
        if (mCurrentState != null) {
            mCurrentState.useProxy = false;
        }
        httpParams.setParameter(ConnRouteParams.DEFAULT_PROXY, null);
    }

    /**
     * 网络是否可用
     *
     * @return
     */
    public static boolean available() {
        if (NetworkState.asInstance().mCurrentState != null) {
            return NetworkState.asInstance().mCurrentState.available;
        }
        return false;
    }

    /**
     * 是否使用手机网络
     *
     * @return
     */
    public static boolean availableMobile() {
        if (NetworkState.asInstance().mCurrentState != null) {
            return NetworkState.asInstance().mCurrentState.use2G
                    || NetworkState.asInstance().mCurrentState.use3G
                    || NetworkState.asInstance().mCurrentState.use4G;
        }
        return false;
    }

    /**
     * 是否使用WIFI
     *
     * @return
     */
    public static boolean availableWifi() {
        if (NetworkState.asInstance().mCurrentState != null) {
            return NetworkState.asInstance().mCurrentState.useWifi;
        }
        return false;
    }

    /**
     * 是否使用4G
     *
     * @return
     */
    public static boolean available4G() {
        if (NetworkState.asInstance().mCurrentState != null) {
            return NetworkState.asInstance().mCurrentState.use4G;
        }
        return false;
    }

    /**
     * 是否使用3G
     *
     * @return
     */
    public static boolean available3G() {
        if (NetworkState.asInstance().mCurrentState != null) {
            return NetworkState.asInstance().mCurrentState.use3G;
        }
        return false;
    }

    /**
     * 是否使用2G
     *
     * @return
     */
    public static boolean available2G() {
        if (NetworkState.asInstance().mCurrentState != null) {
            return NetworkState.asInstance().mCurrentState.use2G;
        }
        return false;
    }

    /**
     * 是否使用代理
     *
     * @return
     */
    public static boolean availableProxy() {
        if (NetworkState.asInstance().mCurrentState != null) {
            return NetworkState.asInstance().mCurrentState.useProxy;
        }
        return false;
    }

    /**
     * 是否仅手机网络
     *
     * @return
     */
    public static boolean availableOnlyMobile() {
        return availableMobile() && !availableWifi();
    }

    /**
     * 是否仅手机网络
     *
     * @return
     */
    public static boolean availableOnlyWifi() {
        return !availableMobile() && availableWifi();
    }

    /**
     * 获得网络类型
     *
     * @param type
     * @return
     */
    private static String getNetworkTypeName(int type) {
        switch (type) {
            case NETWORK_TYPE_GPRS:
                return "GPRS";
            case NETWORK_TYPE_EDGE:
                return "EDGE";
            case NETWORK_TYPE_UMTS:
                return "UMTS";
            case NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case NETWORK_TYPE_HSPA:
                return "HSPA";
            case NETWORK_TYPE_CDMA:
                return "CDMA";
            case NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case NETWORK_TYPE_LTE:
                return "LTE";
            case NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case NETWORK_TYPE_IDEN:
                return "iDEN";
            case NETWORK_TYPE_HSPAP:
                return "HSPA+";
            default:
                return "UNKNOWN";
        }
    }

    private static int getNetworkClass(int networkType) {
        switch (networkType) {
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    /**
     * 添加网络监听
     *
     * @param listener
     */
    public static void addStateListener(StateListener listener) {
        if (listener != null) {
            NetworkState.asInstance().stateListeners.add(listener);
        }
    }

    /**
     * 移除网络监听
     *
     * @param listener
     */
    public static void removeStateListener(StateListener listener) {
        if (listener != null) {
            NetworkState.asInstance().stateListeners.remove(listener);
        }
    }

    public static String getNetworkType() {
        String network_type_3g = "3g";
        String network_type_4g = "4g";
        String network_type_2g = "2g";
        String network_type_wifi = "wifi";
        if (NetworkState.availableWifi()) {
            return network_type_wifi;
        }

        if (NetworkState.available4G()) {
            return network_type_4g;
        }

        if (NetworkState.available3G()) {
            return network_type_3g;
        }

        if (NetworkState.available2G()) {
            return network_type_2g;
        }
        return "none";
    }

    /**
     * 状态
     */
    public static class State {
        public boolean available;

        public boolean useWifi;

        public boolean use3G;

        public boolean use4G;

        public boolean use2G;

        public boolean useProxy;
    }

    /**
     * 状态监听
     */
    public static interface StateListener {
        public void onChanged(State state);
    }
}
