package com.wecook.common.modules.cache;

import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ExecutorDelivery;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.debug.DebugCenter;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.common.utils.WebViewUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 网络缓存
 * <p/>
 * 使用Volley进行网络缓存
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/18/14
 */
public class HttpCache extends BaseModule {

    private static final long MAX_CACHE_SIZE = 10 * FileUtils.ONE_MB;

    /**
     * Number of network request dispatcher threads to start.
     */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    /**
     * 过期时间
     */
    private static final String CACHE_TAG_EXPIRE = "Expires";

    /**
     * 缓存控制
     */
    private static final String CACHE_TAG_CONTROL = "Cache-Control";
    private static final String DEFAULT_CACHE_DIR = "wecook-volley";

    private HashMap<String, String> headers = new HashMap<String, String>();

    private Map<String, String> params;

    private static RequestQueue requestQueue;

    private boolean mRefreshCache;

    private static String sDefaultUserAgent;

    private String mRequestTag;
    private RetryPolicy mRetryPolicy;

    public HttpCache(Context context) {
        setup(context);
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        if (requestQueue != null) {
            requestQueue.getCache().clear();
        }
    }

    /**
     * 取消特定标签的请求
     *
     * @param requestTag
     */
    public static void cancelRequest(String requestTag) {
        if (requestQueue != null && !StringUtils.isEmpty(requestTag)) {
            requestQueue.cancelAll(requestTag);
        }
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        mRetryPolicy = retryPolicy;
    }

    private class ThreadHandler extends HandlerThread {

        public ThreadHandler() {
            super("volley-delivery");
        }

    }

    @Override
    public void setup(Context context) {
        super.setup(context);
        if (requestQueue == null) {
            File cacheDir = FileUtils.getSdcardDir(getContext(), DEFAULT_CACHE_DIR);


            sDefaultUserAgent = WebViewUtil.getCurrentUserAgent(context);
            HttpStack stack = null;
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new SupportHttpsHurlStack();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                String userAgent = getUserAgent();
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }

            Network network = new BasicNetwork(stack);

            ThreadHandler threadHandler = new ThreadHandler();
            threadHandler.start();

            requestQueue = new RequestQueue(new DiskCache(cacheDir, (int) MAX_CACHE_SIZE), network,
                    DEFAULT_NETWORK_THREAD_POOL_SIZE, new ExecutorDelivery(new Handler(threadHandler.getLooper())));

            VolleyLog.DEBUG = DebugCenter.isDebugable();
            requestQueue.start();

        }
    }

    /**
     * 添加缓存时间 单位：秒
     *
     * @param cacheAge
     * @return
     */
    public HttpCache addCacheAge(long cacheAge) {
        String headCacheControl = "max-age=" + cacheAge;
        String controls = headers.get(CACHE_TAG_CONTROL);
        if (controls != null) {
            controls += "," + headCacheControl;
        } else {
            controls = headCacheControl;
        }
        headers.put(CACHE_TAG_CONTROL, controls);

        return this;
    }

    /**
     * 缓存到日期
     *
     * @param date
     * @return
     */
    public HttpCache cacheToDate(String date) {
        headers.put(CACHE_TAG_EXPIRE, date);
        return this;
    }

    public HttpCache refreshCache() {
        mRefreshCache = true;
        return this;
    }

    /**
     * 设置不缓存
     */
    public HttpCache noCache() {
        String head = "no-cache";
        String controls = headers.get(CACHE_TAG_CONTROL);
        if (controls != null) {
            controls += "," + head;
        } else {
            controls = head;
        }
        headers.put(CACHE_TAG_CONTROL, controls);
        return this;
    }

    /**
     * 设置不存储
     */
    public HttpCache noStore() {
        String head = "no-store";
        String controls = headers.get(CACHE_TAG_CONTROL);
        if (controls != null) {
            controls += "," + head;
        } else {
            controls = head;
        }
        headers.put(CACHE_TAG_CONTROL, controls);
        return this;
    }

    /**
     * 强制重新请求
     */
    public HttpCache retryRequest() {
        String head = "must-revalidate";
        String controls = headers.get(CACHE_TAG_CONTROL);
        if (controls != null) {
            controls += "," + head;
        } else {
            controls = head;
        }
        headers.put(CACHE_TAG_CONTROL, controls);
        return this;
    }

    /**
     * 进行代理请求
     */
    public HttpCache proxyRequest() {
        String head = "proxy-revalidate";
        String controls = headers.get(CACHE_TAG_CONTROL);
        if (controls != null) {
            controls += "," + head;
        } else {
            controls = head;
        }
        headers.put(CACHE_TAG_CONTROL, controls);
        return this;
    }

    public void setRequestTag(String requestTag) {
        mRequestTag = requestTag;
    }

    /**
     * 设置UserAgent
     *
     * @return
     */
    public HttpCache setUserAgent() {
        headers.put("User-agent", getUserAgent());
        return this;
    }

    public static String getUserAgent() {
        if (sDefaultUserAgent == null) {
            sDefaultUserAgent = WebViewUtil.getCurrentUserAgent(BaseApp.getApplication());
        }
        return sDefaultUserAgent + " Wecook/" + PhoneProperties.getDebugVersionName() + " NetType/" + NetworkState.getNetworkType();
    }

    /**
     * 请求数据
     *
     * @param method
     * @param url
     * @param requestTag
     * @param params
     * @param handler
     */
    public Request request(int method, String url, String requestTag, Map<String, String> params, final HttpCacheHandler handler) {

        final String ordinalUrl = url;
        String cacheUrl = url;
        if (method == Request.Method.GET) {
            url = encodeParameters(ordinalUrl, params, "utf-8");
            Logger.i("API", "=API=request GET url : " + url);
            Map<String, String> cacheParams = new HashMap<String, String>(params);
            cacheParams.remove("timestamp");
            cacheParams.remove("net");
            cacheUrl = encodeParameters(ordinalUrl, cacheParams, "utf-8");
            this.params = new HashMap<>();
        } else {
            noCache();
            refreshCache();
            String postUrl = encodeParameters(ordinalUrl, params, "utf-8");
            Logger.i("API", "=API=request POST url : " + postUrl);
            this.params = new HashMap<>(params);
        }

        //设置UserAgent
        setUserAgent();

        JsonApiCacheRequest request = new JsonApiCacheRequest(method,
                url, cacheUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                handler.response = response;
                handler.dispatchResponse();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.error = error;
                handler.dispatchResponse();
            }
        }
        ) {
            @Override
            public void sendNetworkResponse(NetworkResponse response) {
                handler.networkResponse = response;
            }
        };
        if (!StringUtils.isEmpty(requestTag)) {
            request.setTag(requestTag);
        }
        if (mRefreshCache) {
            requestQueue.getCache().remove(cacheUrl);
        }

        if (mRetryPolicy != null) {
            request.setRetryPolicy(mRetryPolicy);
        } else {
            //在弱网络环境下，当请求超时的时候，会出现多次请求，导致写数据的api请求错误，在此处关闭所有api的请求策略。
            request.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 0, 1.f));
        }

        System.setProperty("http.keepAlive", "false");
        requestQueue.add(request);

        return request;
    }

    private String encodeParameters(String url, Map<String, String> params, String paramsEncoding) {

        Uri.Builder builder = Uri.parse(url).buildUpon();

//        try {
//            for (Map.Entry<String, String> entry : params.entrySet()) {
//                builder.appendQueryParameter(URLEncoder.encode(entry.getKey(), paramsEncoding),
//                        URLEncoder.encode(entry.getValue(), paramsEncoding));
//            }
//            return builder.toString();
//        } catch (UnsupportedEncodingException uee) {
//            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
//        }

        //暂不对参数进行encode
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.toString();
    }

    /**
     * POST 请求
     *
     * @param url
     * @param params
     * @param handler
     */
    public Request post(String url, Map<String, String> params, final HttpCacheHandler handler) {
        return request(Request.Method.POST, url, mRequestTag, params, handler);
    }

    /**
     * GET 请求
     *
     * @param url
     * @param params
     * @param handler
     */
    public Request get(String url, Map<String, String> params, final HttpCacheHandler handler) {
        return request(Request.Method.GET, url, mRequestTag, params, handler);
    }

    /**
     * 新创建一个缓存对象
     *
     * @param context
     * @return
     */
    public static HttpCache asInstance(Context context) {
        return new HttpCache(context);
    }


    private class SupportHttpsHurlStack extends HurlStack {
        @Override
        protected HttpURLConnection createConnection(URL url) throws IOException {
            if (url.toString().contains("https")) {
                HttpsTrustManager.allowAllSSL();
            }
            return super.createConnection(url);
        }
    }


    private static class HttpsTrustManager implements X509TrustManager {

        private static TrustManager[] trustManagers;
        private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        public boolean isClientTrusted(X509Certificate[] chain) {
            return true;
        }

        public boolean isServerTrusted(X509Certificate[] chain) {
            return true;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return _AcceptedIssuers;
        }

        public static void allowAllSSL() {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    // TODO Auto-generated method stub
                    return true;
                }

            });

            SSLContext context = null;
            if (trustManagers == null) {
                trustManagers = new TrustManager[]{new HttpsTrustManager()};
            }

            try {
                context = SSLContext.getInstance("TLS");
                context.init(null, trustManagers, new SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }

            HttpsURLConnection.setDefaultSSLSocketFactory(context
                    .getSocketFactory());
        }
    }

    /**
     * 缓存数据请求
     */
    abstract class JsonApiCacheRequest extends StringRequest {

        private String cacheKey;

        public JsonApiCacheRequest(int method, String url,
                                   Response.Listener<String> listener,
                                   Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
            this.cacheKey = url;
        }

        public JsonApiCacheRequest(int method, String url, String cacheKey,
                                   Response.Listener<String> listener,
                                   Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
            this.cacheKey = cacheKey;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            sendNetworkResponse(response);
            try {
                if (!headers.isEmpty() && response.headers != null) {
                    response.headers.putAll(headers);
                }
                String jsonString =
                        new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            }
        }

        @Override
        public String getCacheKey() {
            return cacheKey;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return params;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return headers;
        }

        /**
         * 内部分发网络回调响应
         *
         * @param response
         */
        abstract void sendNetworkResponse(NetworkResponse response);
    }
}
