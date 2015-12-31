package com.wecook.sdk.api.model;

import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 【我界面】细分订单数量
 *
 * @author droid
 * @version v2.3.7
 * @since 2015.5.10
 */
public class OrderCount extends ApiModel {

    /**
     * 待付款
     */
    public int mObligationsCount;
    /**
     * 待发货
     */
    public int mNoDeliverCount;
    /**
     * 配送中
     */
    public int mShippingCount;
    /**
     * 待评价
     */
    public int mNoEvaluateCount;
    /**
     * 已退款
     */
    public int mRefundedCount;

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("data")) {
            JSONArray countList = jsonObject.optJSONArray("data");
            for (int i = 0; i < countList.length(); i++) {
                JSONObject obj = countList.optJSONObject(i);
                int status = StringUtils.parseInt(obj.optString("status"));
                int count = StringUtils.parseInt(obj.optString("count"));
                switch (status) {
                    case 1:
                        mObligationsCount = count;
                        break;
                    case 2:
                        mNoDeliverCount = count;
                        break;
                    case 3:
                        mShippingCount = count;
                        break;
                    case 6:
                        mNoEvaluateCount = count;
                        break;
                    case 10:
                        mRefundedCount = count;
                        break;
                }
            }
        }
    }

    public int getMessageCount() {
        return mObligationsCount + mNoDeliverCount + mShippingCount + mNoEvaluateCount + mRefundedCount;
    }
}
