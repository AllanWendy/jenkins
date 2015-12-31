package com.wecook.common.core.internet;

/**
 * Api配置器
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public abstract class ApiConfiger extends ApiModel {
    /**
     * 注册服务接口
     *
     * @return
     */
    public abstract String getRegisterServer();

    /**
     * 获得服务根地址
     *
     * @return
     */
    public abstract String getService();

    /**
     * 获得统计地址
     *
     * @return
     */
    public abstract String getCountServer();

    /**
     * 获得代理地址
     *
     * @return
     */
    public abstract String getProxyServer();

    /**
     * 签名key
     * @return
     */
    public abstract String getAccessKey();

    /**
     * 加密key
     * @return
     */
    public abstract String getSecurityKey();

}
