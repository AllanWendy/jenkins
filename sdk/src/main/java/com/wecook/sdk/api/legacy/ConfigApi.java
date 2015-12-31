package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.UpdateConfig;

/**
 * 配置API
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/22
 */
public class ConfigApi extends Api {

    /**
     * 检查版本
     *
     * @param currentVersion
     * @param apiCallback
     */
    public static void checkUpdateApp(String currentVersion, ApiCallback<UpdateConfig> apiCallback) {
        Api.get(ConfigApi.class)
                .with("/apps_version/version_check")
                .addParams("os", "android", true)
                .addParams("app_version", currentVersion, true)
                .toModel(new UpdateConfig())
                .setApiCallback(apiCallback)
                .executeGet();
    }

    /**
     * 上报推送相关数据
     *
     * @param callback
     */
    public static void reportPush(ApiCallback<State> callback) {
        Api.get(ConfigApi.class)
                .with("/index/push")
                .addParams("push_id", PhoneProperties.getXMPushRegisterId(), true)
                .addParams("city", LocationServer.asInstance().getIndexCity())
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }
}
