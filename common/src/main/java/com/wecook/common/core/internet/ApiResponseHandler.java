package com.wecook.common.core.internet;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.wecook.common.core.debug.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ApiResponseHandler extends JsonHttpResponseHandler {

    private Api mApi;
    private ApiResult apiResult;
    private int statusCode;
    private int statusState;
    private String responseString;
    private Throwable throwable;

    public ApiResponseHandler(Api api, ApiResult result) {
        mApi = api;
        apiResult = result;
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseString) {

        if (apiResult.getApiModel() != null) {
            apiResult.getApiModel().statusState = ApiModel.STATE_OK;
            apiResult.getApiModel().statusCode = statusCode;
            try {
                apiResult.parseModel(responseString);
            } catch (JSONException e) {
                Logger.w("API", "api[" + mApi + "] parse error ! json:" + responseString, e);
            }
        }
        this.statusCode = statusCode;
        statusState = ApiModel.STATE_OK;
        this.responseString = responseString;
        mApi.dispatchApiCallback(this, apiResult);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        ApiResult apiResult = new ApiResult(mApi);
        if (apiResult.getApiModel() != null) {
            apiResult.getApiModel().statusState = ApiModel.STATE_FAIL;
            apiResult.getApiModel().statusCode = statusCode;
            try {
                apiResult.parseModel(responseString);
            } catch (JSONException e) {
                Logger.w("API", "api[" + mApi + "] parse error ! json:" + responseString, e);
            }
        }
        this.statusCode = statusCode;
        statusState = ApiModel.STATE_FAIL;
        this.responseString = responseString;
        this.throwable = throwable;
        mApi.dispatchApiCallback(this, apiResult);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        ApiResult apiResult = new ApiResult(mApi);
        if (apiResult.getApiModel() != null) {
            apiResult.getApiModel().statusState = ApiModel.STATE_FAIL;
            apiResult.getApiModel().statusCode = statusCode;
            try {
                apiResult.parseModel(errorResponse);
            } catch (JSONException e) {
                Logger.w("API", "api[" + mApi + "] parse error ! json:" + errorResponse.toString(), e);
            }
        }
        this.statusCode = statusCode;
        statusState = ApiModel.STATE_FAIL;
        if (errorResponse != null) {
            this.responseString = errorResponse.toString();
        }
        this.throwable = throwable;
        mApi.dispatchApiCallback(this, apiResult);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        ApiResult apiResult = new ApiResult(mApi);
        if (apiResult.getApiModel() != null) {
            apiResult.getApiModel().statusState = ApiModel.STATE_FAIL;
            apiResult.getApiModel().statusCode = statusCode;
            try {
                apiResult.parseModel(errorResponse);
            } catch (JSONException e) {
                Logger.w("API", "api[" + mApi + "] parse error ! json:" + errorResponse.toString(), e);
            }
        }
        this.statusCode = statusCode;
        statusState = ApiModel.STATE_FAIL;
        if (errorResponse != null) {
            this.responseString = errorResponse.toString();
        }
        this.throwable = throwable;
        mApi.dispatchApiCallback(this, apiResult);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        ApiResult apiResult = new ApiResult(mApi);
        if (apiResult.getApiModel() != null) {
            apiResult.getApiModel().statusState = ApiModel.STATE_OK;
            apiResult.getApiModel().statusCode = statusCode;
            try {
                apiResult.parseModel(response);
            } catch (JSONException e) {
                Logger.w("API", "api[" + mApi + "] parse error ! json:" + response.toString(), e);
            }
        }
        this.statusCode = statusCode;
        statusState = ApiModel.STATE_OK;
        this.responseString = response.toString();
        mApi.dispatchApiCallback(this, apiResult);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        ApiResult apiResult = new ApiResult(mApi);
        if (apiResult.getApiModel() != null) {
            apiResult.getApiModel().statusState = ApiModel.STATE_OK;
            apiResult.getApiModel().statusCode = statusCode;
            try {
                apiResult.parseModel(response);
            } catch (JSONException e) {
                Logger.w("API", "api[" + mApi + "] parse error ! json:" + response.toString(), e);
            }
        }
        this.statusCode = statusCode;
        statusState = ApiModel.STATE_OK;
        this.responseString = response.toString();
        mApi.dispatchApiCallback(this, apiResult);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getStatusState() {
        return statusState;
    }

    public String getResponseString() {
        return responseString;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}