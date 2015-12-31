package com.wecook.common.core.internet;

import com.android.volley.Request;
import com.loopj.android.http.RequestHandle;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Api请求结果
 *
 * @author kevin created at 9/19/14
 * @version 1.0
 */
public class ApiResult {
    private boolean isCanceled;
    private Api mApi;
    private RequestHandle mHttpRequest;
    private Request mVolleyRequest;

    public ApiResult(Api api) {
        mApi = api;
    }

    public Api getApi() {
        return mApi;
    }

    public ApiModel getApiModel() {
        return mApi.getApiModel();
    }

    public void parseModel(String responseString) throws JSONException {
        Logger.i("API", "parseModel : " + responseString);
        if (!StringUtils.isEmpty(responseString)) {
            mApi.getApiModel().parseResult(responseString);
        }
    }

    public void parseModel(JSONArray jsonArray) throws JSONException {
        if (jsonArray != null && mApi.getApiModel() != null) {
            parseModel(jsonArray.toString());
        }
    }

    public void parseModel(JSONObject jsonObject) throws JSONException {
        if (jsonObject != null && mApi.getApiModel() != null) {
            parseModel(jsonObject.toString());
        }
    }

    public void cancel() {
        isCanceled = true;
        if (mVolleyRequest != null) {
            mVolleyRequest.cancel();
        }

        if (mHttpRequest != null) {
            mHttpRequest.cancel(true);
        }
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setRequest(Request request) {
        mVolleyRequest = request;
    }

    public void setRequest(RequestHandle request) {
        mHttpRequest = request;
    }

    /**
     * 是否OK
     *
     * @return
     */
    public boolean isOK() {
        ApiModel model = getApiModel();
        return model != null && model.available();
    }
}
