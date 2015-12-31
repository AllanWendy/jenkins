package com.wecook.sdk.api.model;

import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 联系方式
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/7/14
 */
public class Contact extends ApiModel {

    private String tel;
    private String qq;
    private String weixin;
    private String address;

    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonObject(json)) {
            Contact contact = JsonUtils.getModel(json, Contact.class);
            if (contact != null) {
                tel = contact.tel;
                qq = contact.qq;
                weixin = contact.weixin;
                address = contact.address;
            }
        }
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
