package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * 订单支付签名
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/5/14
 */
public class OrderPaySign extends ApiModel {

    private WeChatOrder weChatOrder;
    private AlipayOrder alipayOrder;

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = JsonUtils.getJSONObject(json);
        if (jsonObject.has("alipay")) {
            alipayOrder = AlipayOrder.parse(jsonObject.opt("alipay").toString());
        }

        if (jsonObject.has("weixin")) {
            weChatOrder = WeChatOrder.parse(jsonObject.opt("weixin").toString());
        }
    }

    public boolean isWeChatOrder() {
        return weChatOrder != null;
    }

    public boolean isAlipayOrder() {
        return alipayOrder != null;
    }

    public WeChatOrder getWeChatOrder() {
        return weChatOrder;
    }

    public AlipayOrder getAlipayOrder() {
        return alipayOrder;
    }

    public static class WeChatOrder implements Serializable {

        @SerializedName("noncestr")
        private String noncestr;

        @SerializedName("partnerid")
        private String partnerid;

        @SerializedName("prepayid")
        private String prepayid;

        @SerializedName("timestamp")
        private String timestamp;

        @SerializedName("sign")
        private String sign;

        @SerializedName("packageValue")
        private String packageValue;

        public String getNoncestr() {
            return noncestr;
        }

        public String getPartnerid() {
            return partnerid;
        }

        public String getPrepayid() {
            return prepayid;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getSign() {
            return sign;
        }

        public String getPackageValue() {
            return packageValue;
        }

        public static WeChatOrder parse(String json) {
            return JsonUtils.getModel(json, WeChatOrder.class);
        }
    }

    public static class AlipayOrder implements Serializable {

        @SerializedName("tradeNO")
        private String tradeNO;

        @SerializedName("productName")
        private String productName;

        @SerializedName("productDescription")
        private String productDescription;

        @SerializedName("amount")
        private String amount;

        @SerializedName("notifyURL")
        private String notifyURL;

        private String payInfo;

        public String getTradeNO() {
            return tradeNO;
        }

        public String getProductName() {
            return productName;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public String getAmount() {
            return amount;
        }

        public String getNotifyURL() {
            return notifyURL;
        }

        public String getPayInfo() {
            return payInfo;
        }

        public static AlipayOrder parse(String json) {
            if (JsonUtils.isJsonString(json)) {
                AlipayOrder order = new AlipayOrder();
                order.payInfo = json;
                return order;
            }
            return JsonUtils.getModel(json, AlipayOrder.class);
        }
    }
}
