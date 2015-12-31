package com.wecook.common.modules.thirdport.platform.base;

import com.wecook.common.modules.thirdport.object.OrderInfo;

/**
 * 可支付
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/2/14
 */
public interface Payable {

    /**
     * 是否支持支付
     *
     * @return
     */
    boolean isSupportPay();


    /**
     * 支付
     *
     * @param orderInfo
     */
    void onPay(OrderInfo orderInfo);
}
