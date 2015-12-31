package com.wecook.common.modules.cache;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

/**
 * 网络缓存数据
 *
 * @author kevin created at 9/19/14
 * @version 1.0
 */
public abstract class HttpCacheHandler {
    /**
     * 响应的结果json
     */
    public String response;

    /**
     * 响应的网络返回
     */
    public NetworkResponse networkResponse;

    /**
     * 是否响应出错
     * 如果 error为null，说明响应成功，否则响应失败
     */
    public VolleyError error;

    /**
     * 响应结果回调
     */
    public abstract void dispatchResponse();
}
