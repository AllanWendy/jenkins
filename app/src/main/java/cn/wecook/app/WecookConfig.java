package cn.wecook.app;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiConfiger;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.ServerConfig;
import com.wecook.sdk.api.model.SplashScreen;
import com.wecook.sdk.api.model.UpdateConfig;
import com.wecook.sdk.userinfo.UserProperties;

import org.json.JSONException;

/**
 * 服务配置
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/19/14
 */
public class WecookConfig extends ApiConfiger {

    private static final String API_ACCESS = "259eea423dee18c7b865b0777cd657cc";
    private static final String API_SECURITY_KEY = "6E820afd87518a475f83e8a279c0d367";//"59f0bd6bcc49aec63ca98af7dcb6bea1";
    private static final String API_REGISTER_SERVER = "http://api.wecook.cn/";
    private static final String API_REGISTER_SERVER_TEST = "http://api.wecook.com.cn/";
//    private static final String API_REGISTER_SERVER_TEST = "http://192.168.4.50:81/";
    private static final String API_VERSION = "v3";

    /**
     * 更新信息
     */
    private UpdateConfig updateConfig;

    /**
     * 服务信息
     */
    private ServerConfig serverConfig;

    /**
     * 启动图
     */
    private SplashScreen splashScreen;

    /**
     * 售卖开关
     */
    private String isOpenSell;

    private static WecookConfig sInstance;

    private String couponUrlAddress = "http://cai.wecook.cn/";

    private WecookConfig() {
        serverConfig = new ServerConfig();
    }

    public static WecookConfig getInstance() {
        if (sInstance == null) {
            sInstance = new WecookConfig();
        }

        return sInstance;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public UpdateConfig getUpdateConfig() {
        return updateConfig;
    }

    public SplashScreen getSplashScreen() {
        return splashScreen;
    }

    public String getService() {
        if (StringUtils.isEmpty(serverConfig.getService()) || isTest()) {
            return getRegisterServer();
        }
        return serverConfig.getService() + WecookConfig.API_VERSION;
    }

    public String getCountServer() {
        return serverConfig.getCountServer();
    }

    public String getProxyServer() {
        return serverConfig.getProxyServer();
    }

    @Override
    public String getAccessKey() {
        return API_ACCESS;
    }

    @Override
    public String getSecurityKey() {
        return API_SECURITY_KEY;
    }

    public String getRegisterServer() {
        return (serverConfig.isTestServer() ? API_REGISTER_SERVER_TEST : API_REGISTER_SERVER) + API_VERSION;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonObject object = null;
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null && element.isJsonObject()) {
            object = element.getAsJsonObject();
        }
        isOpenSell = JsonUtils.getModelItemAsString(object, "mc", "1");
        updateConfig = JsonUtils.getModelItemAsObject(object, "version", new UpdateConfig());
        serverConfig = JsonUtils.getModelItemAsObject(object, "config", new ServerConfig());
        splashScreen = JsonUtils.getModelItemAsObject(object, "splashscreen", new SplashScreen());
    }

    public boolean isTest() {
        return serverConfig.isTestServer();
    }

    public void toggleTest(boolean isTest) {
        serverConfig.setIsTestServer(isTest);
    }

    public boolean isOpenSell() {
        return "1".equals(isOpenSell);
    }

    public void toggleOpenSell(boolean isOpenSell) {
        if (isOpenSell) {
            this.isOpenSell = "1";
        } else {
            this.isOpenSell = "0";
        }
    }

    public String getCouponUrlAddress() {
        return couponUrlAddress + "dishes/coupon_import?uid=" + UserProperties.getUserId()
                + "&wid=" + PhoneProperties.getDeviceId();
    }

    public boolean isTestCouponUrlAddress() {
        return couponUrlAddress.equals("http://m.maicaibangshou.cn/");
    }

    public void toggleCouponUrlAddress(boolean isTestCouponUrlAddress) {
        if (isTestCouponUrlAddress) {
            couponUrlAddress = "http://m.maicaibangshou.cn/";
        } else {
            couponUrlAddress = "http://cai.wecook.cn/";
        }
    }
}
