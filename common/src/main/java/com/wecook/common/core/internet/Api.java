// Copy right WeCook Inc.
package com.wecook.common.core.internet;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.debug.DebugCenter;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.cache.HttpCache;
import com.wecook.common.modules.cache.HttpCacheHandler;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.SecurityUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列举Api接口
 *
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public abstract class Api extends BaseModule {

    public static final long CACHE_NO_TIME = 0; //不缓存
    public static final long CACHE_ONE_WEEK = 7 * 24 * 60 * 60; // 1个星期
    public static final long CACHE_ONE_DAY = 24 * 60 * 60; // 24个小时
    public static final long CACHE_HALF_DAY = 12 * 60 * 60; // 24个小时
    public static final long CACHE_ONE_HOUR = 1 * 60 * 60; // 1个小时
    public static final long CACHE_HALF_HOUR = 30 * 60; // 半个小时
    public static final long CACHE_TEN_MINUTE = 10 * 60; // 10分钟
    public static final long CACHE_ONE_MINUTE = 1 * 60; // 1分钟

    public static final int DEFAULT_MAX_RETRY_COUNT = 3;
    public static final int DEFAULT_MAX_TIME_OUT = 10 * 1000;

    protected static AsyncHttpClient sAsyncHttpClient = new AsyncHttpClient(true, 80, 443);
    protected static SyncHttpClient sSyncHttpClient = new SyncHttpClient(true, 80, 443);

    private static final Map<Class<? extends Api>, ? extends Api> sApiPools = new HashMap<>();

    private static boolean sApiEnable = true;

    private static long sReqTime;//上报接口时间

    /**
     * 控制所有的API请求是否使用无缓存，true：不使用  false：使用
     */
    private static boolean sApiNoCacheMode = false;
    private static String sApiRequestTag;
    private static boolean sInited;
    private boolean mUnregisterLock = false;

    private Context mContext = BaseApp.getApplication();

    /* **************API请求参数列表********************* */

    /**
     * 服务器基础地址
     */
    private String serverUrl;
    /**
     * 数据请求结果的解析数据模型
     */
    private ApiModel mApiModel;
    /**
     * 相对接口地址
     */
    private String mRelativeUrl;
    /**
     * 缓存时间
     */
    private long cacheTime = 0;
    /**
     * 请求数据对
     */
    private Map<String, String> params = new HashMap<String, String>();
    /**
     * 加密请求数据对
     */
    private Map<String, String> keyParams = new HashMap<String, String>();
    /**
     * 回调
     */
    private ApiCallback mApiCallback;
    /**
     * 上传数据key
     */
    private String mBodyKey;
    /**
     * 上传数据
     */
    private byte[] mBodies;
    /**
     * 最大重试次数
     */
    private int mMaxRetryCount;
    /**
     * 超时时间
     */
    private int mMaxTimeOut;
    /**
     * Https
     */
    private boolean isHttps;

    /* **************API请求参数列表 END ********************* */

    private static ApiConfiger mGlobalConfig;

    private boolean mIsRegisterProcess;

    private static NetworkState.StateListener sNetworkListener = new NetworkState.StateListener() {
        @Override
        public void onChanged(NetworkState.State state) {
            if (state.available) {
                registerGlobalConfig(BaseApp.getApplication(), Api.getGlobalConfig(), null);
            }
        }
    };

    public static ApiConfiger getGlobalConfig() {
        return mGlobalConfig;
    }

    public static void init(ApiConfiger configer) {
        mGlobalConfig = configer;
        sAsyncHttpClient.setUserAgent(HttpCache.getUserAgent());
        NetworkState.addStateListener(sNetworkListener);
        if (DebugCenter.isDebugable()) {
            AsyncHttpClient.log.setLoggingEnabled(DebugCenter.isDebugable());
            AsyncHttpClient.log.setLoggingLevel(0);
        }
    }

    /**
     * 注册API
     *
     * @param configer
     * @param callback
     */
    private static void registerApi(Api api, ApiConfiger configer, ApiCallback<ApiConfiger> callback) {
        if (api != null) {
            api.setRegisterRequest(true)
                    .addParams("app", "wecook", true)
                    .addParams("channel", PhoneProperties.getChannel(), true)
                    .addParams("version", PhoneProperties.getVersionName(), true)
                    .addParams("lon", LocationServer.asInstance().getLon() + "")
                    .addParams("lat", LocationServer.asInstance().getLat() + "")
                    .addParams("os", PhoneProperties.getDeviceOs())
                    .addParams("os_version", PhoneProperties.getDeviceOsVersion())
                    .addParams("width", PhoneProperties.getScreenWidth())
                    .addParams("height", PhoneProperties.getScreenHeight())
                    .addParams("img_format", "webp")
                    .toModel(configer)
                    .setMaxRetryAndTimeOut(DEFAULT_MAX_RETRY_COUNT, DEFAULT_MAX_TIME_OUT)
                    .setApiCallback(callback)
                    .executeGet(configer.getRegisterServer());
        }
    }

    /**
     * 注册所有API
     *
     * @param app
     * @param configer
     */
    public static void registerGlobalConfig(Context app, final ApiConfiger configer,
                                            final ApiCallback<ApiConfiger> callback) {
        mGlobalConfig = configer;
        if (NetworkState.available()
                && AndroidUtils.isMainProcess(app) && !sInited) {
            Logger.d("API", "init sync api from remote");
            final Api api = new EmptyApi();
            sReqTime = System.currentTimeMillis();
            registerApi(api, configer, new ApiCallback<ApiConfiger>() {
                @Override
                public void onResult(ApiConfiger result) {
                    if (result != null && result.available()) {
                        mGlobalConfig = result;
                        sApiEnable = true;
                        sInited = true;
                        sReqTime = Long.MAX_VALUE;
                        Logger.d("API", "api inited");
                    }

                    if (!sInited) {
                        //每隔1秒请求重试，直到联通
                        if (System.currentTimeMillis() - sReqTime > 1000) {
                            Logger.d("API", "api need retry");
                            api.executeGet(configer.getRegisterServer());
                            sReqTime = System.currentTimeMillis();
                        }
                        return;
                    }

                    if (callback != null) {
                        callback.onResult(result);
                    }
                }
            });
        } else {
            sApiEnable = true;
        }
    }

    public Api setMaxRetryAndTimeOut(int maxRetryCount, int maxTimeOut) {
        mMaxRetryCount = maxRetryCount;
        mMaxTimeOut = maxTimeOut;
        if (mMaxRetryCount != 0 && mMaxTimeOut <= 1000) {
            mMaxTimeOut = DEFAULT_MAX_TIME_OUT;
        }
        return this;
    }

    public Api setRegisterRequest(boolean isRegisterProcess) {
        mIsRegisterProcess = isRegisterProcess;
        return this;
    }

    /**
     * 注册Api类型
     *
     * @param context
     * @param configer
     */
    public static void register(Context context, Api api, ApiConfiger configer) {
        if (configer == null) {
            configer = mGlobalConfig;
        }
        if (api != null && configer != null) {
            api.mContext = context;
            api.serverUrl = configer.getService();
//            api.serverUrl = "http://114.215.120.225:8080/"+"v3";
            api.mUnregisterLock = false;
        }
    }

    /**
     * API是否可用
     *
     * @return
     */
    public boolean apiEnable() {
        return mIsRegisterProcess || (sApiEnable && !mUnregisterLock);
    }

    /**
     * 关闭Api
     */
    public void close() {
        mUnregisterLock = true;
        mApiCallback = null;
    }

    /**
     * 获得Api对象
     *
     * @param instanceCls
     * @return
     */
    public static <T extends Api> T get(Class<T> instanceCls) {
        Api api = null;
        if (instanceCls != null) {
            synchronized (Api.class) {
                if (sApiPools.containsKey(instanceCls)) {
                    api = sApiPools.get(instanceCls);
                } else {
                    try {
                        api = instanceCls.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                if (api == null) {
                    api = new EmptyApi();
                }
                //reset api object.
                api.release();
                //init and register api object
                register(BaseApp.getApplication(), api, mGlobalConfig);
            }
        }
        return (T) api;
    }

    /**
     * 添加Api回调
     *
     * @param apiCallback
     */
    public Api setApiCallback(ApiCallback apiCallback) {
        mApiCallback = apiCallback;
        return this;
    }

    /**
     * 将数据填充到此模型中
     *
     * @param model
     * @return
     */
    public Api toModel(ApiModel model) {
        mApiModel = model;
        return this;
    }

    /**
     * 返回API模型
     *
     * @return
     */
    public ApiModel getApiModel() {
        return mApiModel;
    }

    /**
     * 基础URL
     *
     * @return
     */
    public String getServerUrl() {
        String server = serverUrl;
        if (StringUtils.isEmpty(server)) {
            Log.e("error", getGlobalConfig() + "");
            server = getGlobalConfig().getRegisterServer();
        }

        if (isHttps) {
            Uri uri = Uri.parse(server);
            return "https:" + uri.getSchemeSpecificPart();
        }

        return server;
    }


    /**
     * 设置相对Api URL
     *
     * @param relativeUrl
     * @return
     */
    public Api with(String relativeUrl) {
        mRelativeUrl = relativeUrl;
        return this;
    }

    public ApiResult executePut() {
        return executePut(false);
    }

    public ApiResult executePut(boolean sync) {
        return executePut(sync, getServerUrl() + mRelativeUrl);
    }

    /**
     * 执行Api PUT请求
     */
    public ApiResult executePut(boolean sync, String url) {
        final ApiResult apiResult = new ApiResult(Api.this);
        if (apiEnable()) {
            updateParams();

            String requestUrl = encodeParameters(url, params);
            Logger.d("API", "=API=" + requestUrl);
            RequestParams requestParams = new RequestParams(params);
            try {
                requestParams.put(mBodyKey, FileUtils.newFile(mBodies, mBodyKey + ".jpg"), "image/jpeg");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            RequestHandle handle = null;
            if (sync) {
                handle = sSyncHttpClient.post(mContext, url, requestParams, new ApiResponseHandler(this, apiResult));
            } else {
                handle = sAsyncHttpClient.post(mContext, url, requestParams, new ApiResponseHandler(this, apiResult));
            }
            apiResult.setRequest(handle);
        }
        return apiResult;
    }

    public ApiResult executeHttpClient() {
        return executeHttpClient(getServerUrl() + mRelativeUrl);
    }

    public ApiResult executeHttpClient(String url) {
        ApiResult result = new ApiResult(this);
        if (apiEnable()) {
            updateParams();
            RequestParams requestParams = new RequestParams(params);
            requestParams.setUseJsonStreamer(true);
            String requestUrl = encodeParameters(url, params);
            Logger.d("API", "=API=" + requestUrl);
            if (mMaxRetryCount != 0) {
                sAsyncHttpClient.setMaxRetriesAndTimeout(mMaxRetryCount, mMaxTimeOut);
            }
            RequestHandle handle = sAsyncHttpClient.get(mContext, url, requestParams, new ApiResponseHandler(this, result));
            result.setRequest(handle);
        }
        return result;
    }

    public ApiResult executePost() {
        return executePost(getServerUrl() + mRelativeUrl);
    }

    /**
     * 执行Api POST请求
     */
    public ApiResult executePost(String url) {
        return execute(Request.Method.POST, url);
    }

    public ApiResult executeGet() {
        return executeGet(getServerUrl() + mRelativeUrl);
    }

    /**
     * 执行Api GET请求
     *
     * @return
     */
    public ApiResult executeGet(String url) {
        return execute(Request.Method.GET, url);
    }

    private final ApiResult execute(int method, String url) {
        final ApiResult apiResult = new ApiResult(Api.this);
        if (apiEnable()) {

            updateParams();
            HttpCache httpCache = HttpCache.asInstance(mContext);
            if (cacheTime == 0) {
                httpCache.noCache();
            } else {
                httpCache.addCacheAge(cacheTime);
            }
            if (sApiNoCacheMode) {
                httpCache.refreshCache();
            }
            if (!StringUtils.isEmpty(sApiRequestTag)) {
                httpCache.setRequestTag(sApiRequestTag);
            }

            if (mMaxRetryCount != 0) {
                httpCache.setRetryPolicy(new DefaultRetryPolicy(mMaxTimeOut, mMaxRetryCount, 1.0f));
            }

            HttpCacheHandler handler = new HttpCacheHandler() {
                @Override
                public void dispatchResponse() {

                    ApiModel model = apiResult.getApiModel();
                    if (model != null) {
                        if (error == null) {
                            model.statusState = ApiModel.STATE_OK;
                        } else {
                            model.statusState = ApiModel.STATE_FAIL;
                            if (NetworkState.available()) {
                                if (networkResponse != null) {
                                    model.errorMsg = "服务出现异常#code=" + networkResponse.statusCode;
                                } else {
                                    model.errorMsg = "服务出现异常";
                                }
                            } else {
                                model.errorMsg = "网络出现异常";
                            }
                        }
                        if (networkResponse != null) {
                            model.statusCode = networkResponse.statusCode;
                        }
                        if (!apiResult.isCanceled()) {
                            try {
                                apiResult.parseModel(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Api.this.dispatchApiCallback(this, apiResult);
                }
            };

            Request request = null;
            if (method == Request.Method.GET) {
                request = httpCache.get(url, params, handler);
            } else if (method == Request.Method.POST) {
                request = httpCache.post(url, params, handler);
            }
            apiResult.setRequest(request);
        }
        return apiResult;
    }

    private String encodeParameters(String url, Map<String, String> params) {
        Uri.Builder builder = Uri.parse(url).buildUpon();
        //暂不对参数进行encode
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.toString();
    }

    private void updateParams() {
        if (params != null) {
            params.put("sign", SecurityUtils.getSignature(keyParams, getGlobalConfig().getSecurityKey()));
            params.put("access", getGlobalConfig().getAccessKey());
            params.put("timestamp", System.currentTimeMillis() + "");
            params.put("wid", PhoneProperties.getDeviceId());
            params.put("net", NetworkState.getNetworkType());
        }
    }

    /**
     * 分发api回调监听
     *
     * @param result
     */
    protected void dispatchApiCallback(final HttpCacheHandler handler, final ApiResult result) {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mApiCallback != null) {

                    if (handler.error == null) {
                        mApiCallback.setStatusState(ApiModel.STATE_OK);
                    } else {
                        mApiCallback.setStatusState(ApiModel.STATE_FAIL);
                    }

                    if (handler.networkResponse != null) {
                        mApiCallback.setStatusCode(handler.networkResponse.statusCode);
                    }

                    mApiCallback.setResponseString(handler.response);
                    mApiCallback.setThrowable(handler.error);
                    mApiCallback.onResult(result.getApiModel());
                }
            }
        });
    }

    /**
     * 分发api回调监听
     *
     * @param result
     */
    void dispatchApiCallback(final ApiResponseHandler handler, final ApiResult result) {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mApiCallback != null) {
                    mApiCallback.setResponseString(handler.getResponseString());
                    mApiCallback.setStatusCode(handler.getStatusCode());
                    mApiCallback.setStatusState(handler.getStatusState());
                    mApiCallback.setThrowable(handler.getThrowable());
                    mApiCallback.onResult(result.getApiModel());
                }
            }
        });
    }


    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @param isKey
     * @return
     */
    public Api addParams(String key, String value, boolean isKey) {
        String transValue = StringUtils.isEmpty(value) ? "" : value;
        if (isKey) {
            keyParams.put(key, transValue);
        }
        params.put(key, transValue);
        return this;
    }


    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @param isKey
     * @return
     */
    public Api addParams(String key, List<String> value, boolean isKey) {
        String values = "";
        for (int i = 0; i < value.size(); i++) {
            addParams(key + "[" + i + "]", value.get(i));
            values += value.get(i);
        }
        addParams(key + "_md5", SecurityUtils.encodeByMD5(values), isKey);
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public Api addParams(String key, String value) {
        addParams(key, value, false);
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public Api addParams(String key, List<String> value) {
        addParams(key, value, false);
        return this;
    }

    /**
     * 设置缓存时长
     *
     * @param time 单位为秒
     * @return
     */
    public Api setCacheTime(long time) {
        cacheTime = time;
        return this;
    }

    /**
     * 设置数据
     *
     * @param body
     * @return
     */
    public Api setBody(String key, byte[] body) {
        mBodyKey = key;
        mBodies = body;
        return this;
    }

    /**
     * 设置是否为Https
     *
     * @param isHttps
     * @return
     */
    public Api isHttps(boolean isHttps) {
        this.isHttps = isHttps;
        return this;
    }

    @Override
    public void release() {
        super.release();
        if (keyParams != null) {
            keyParams.clear();
        }
        if (params != null) {
            params.clear();
        }
        cacheTime = 0;
        mRelativeUrl = "";
        mApiModel = null;
        mApiCallback = null;
        mBodyKey = "";
        mBodies = null;
        mMaxRetryCount = 0;
        mMaxTimeOut = 0;
        mApiModel = null;
        serverUrl = "";
        isHttps = false;
        NetworkState.removeStateListener(sNetworkListener);
    }

    public static void setRequestTag(String requestTag) {
        sApiRequestTag = requestTag;
    }

    public static void stopAllRequest(String requestTag) {
        if (!StringUtils.isEmpty(sApiRequestTag)
                && !StringUtils.isEmpty(requestTag)
                && sApiRequestTag.equals(requestTag)) {
            HttpCache.cancelRequest(requestTag);
        }
    }

    /**
     * 停止无缓存模式
     */
    public static void stopNoCacheMode() {
        sApiNoCacheMode = false;
    }

    /**
     * 开启无缓存模式
     */
    public static void startNoCacheMode() {
        sApiNoCacheMode = true;
    }

    /**
     * 初始化api
     *
     * @param context
     * @param configer
     */
    public static void init(final Context context, ApiConfiger configer) {
        Api.registerGlobalConfig(context, configer, null);
    }

    /**
     * 初始化api
     *
     * @param context
     * @param configer
     * @param callback
     */
    public static void init(final Context context, ApiConfiger configer, ApiCallback<ApiConfiger> callback) {
        Api.registerGlobalConfig(context, configer, callback);
    }

    /**
     * 空Api
     */
    private static class EmptyApi extends Api {
    }

}
