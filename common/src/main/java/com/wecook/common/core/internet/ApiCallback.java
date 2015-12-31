package com.wecook.common.core.internet;

/**
 * Api请求回调
 *
 * @author kevin created at 9/19/14
 * @version 1.0
 */
public abstract class ApiCallback<T extends ApiModel> {

    private int statusCode;
    private int statusState;
    private String responseString;
    private Throwable throwable;

    /**
     * 结果回调
     * @param result
     */
    public abstract void onResult(T result);

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusState() {
        return statusState;
    }

    public void setStatusState(int statusState) {
        this.statusState = statusState;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * 可用
     *
     * @return
     */
    public boolean available() {
        return statusState == ApiModel.STATE_OK &&
                (statusCode >= 200 && statusCode <= 207);
    }
}
