package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 服务配置
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/22
 */
public class ServerConfig extends ApiModel {
    /**
     * 统计服务
     */
    private String countServer;

    /**
     * 代理服务
     */
    private String proxyServer;

    /**
     * 数据服务
     */
    private String service;

    private boolean isTestServer;

    public ServerConfig() {
        isTestServer = (boolean) AndroidUtils.getMetaDataFromApplication(BaseApp.getApplication(), "TEST_SERVER");
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            countServer = JsonUtils.getModelItemAsString(object, "count");
            proxyServer = JsonUtils.getModelItemAsString(object, "proxy");
            if (!isTestServer) {
                service = JsonUtils.getModelItemAsString(object, "service");
            }
        }
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public String getCountServer() {
        return countServer;
    }

    public String getService() {
        return service;
    }

    public void setCountServer(String countServer) {
        this.countServer = countServer;
    }

    public boolean isTestServer() {
        return isTestServer;
    }

    public void setIsTestServer(boolean isTestServer) {
        this.isTestServer = isTestServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public void setService(String service) {
        this.service = service;
    }
}
