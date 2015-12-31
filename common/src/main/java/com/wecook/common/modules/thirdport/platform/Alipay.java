package com.wecook.common.modules.thirdport.platform;

import android.content.Context;

import com.alipay.sdk.app.PayTask;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.thirdport.object.OrderInfo;
import com.wecook.common.modules.thirdport.platform.base.BasePlatform;
import com.wecook.common.utils.StringUtils;

/**
 * 支付宝
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/2/14
 */
public class Alipay extends BasePlatform {

    public Alipay(Context context) {
        super(context);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public boolean isSupportSSOShare() {
        return true;
    }

    @Override
    public boolean checkSupport() {
        return true;
    }

    @Override
    public boolean isInstalledApp() {
        return true;
    }

    @Override
    public boolean isSupportPay() {
        return true;
    }

    @Override
    public void onPay(OrderInfo orderInfo) {
        super.onPay(orderInfo);
        if (orderInfo != null && orderInfo instanceof AlipayOrderInfo) {
            final String payInfo = ((AlipayOrderInfo) orderInfo).getPayUrl();

            if (StringUtils.isEmpty(payInfo)) {
                performPay(false, "支付信息为空");
                return;
            }

            AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
                private Result mResult;

                @Override
                public void run() {
                    PayTask payTask = new PayTask((android.app.Activity) getContext());
                    String result = payTask.pay(payInfo);
                    mResult = new Result(result);
                }

                @Override
                public void postUi() {
                    super.postUi();
                    if (mResult != null) {
                        String resultStatus = mResult.resultStatus;
                        if ("9000".equals(resultStatus)) {
                            performPay(true, "支付成功");
                        } else {
                            if ("8000".equals(resultStatus)) {
                                performPay(false, "支付确认中");
                            } else {
                                performPay(false, "支付失败");
                            }
                        }
                    } else {
                        performPay(false, "支付失败");
                    }
                }
            });

        } else {
            performPay(false, "商品数据为空");
        }
    }
    /**
     * 支付宝订单数据
     */
    public static class AlipayOrderInfo extends OrderInfo {

        private String payUrl;

        public String getPayUrl() {
            return payUrl;
        }

        public void setPayUrl(String payUrl) {
            this.payUrl = payUrl;
        }
    }

    public class Result {
        String resultStatus;
        String result;
        String memo;

        public Result(String rawResult) {
            try {
                String[] resultParams = rawResult.split(";");
                for (String resultParam : resultParams) {
                    if (resultParam.startsWith("resultStatus")) {
                        resultStatus = gatValue(resultParam, "resultStatus");
                    }
                    if (resultParam.startsWith("result")) {
                        result = gatValue(resultParam, "result");
                    }
                    if (resultParam.startsWith("memo")) {
                        memo = gatValue(resultParam, "memo");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "resultStatus={" + resultStatus + "};memo={" + memo
                    + "};result={" + result + "}";
        }

        private String gatValue(String content, String key) {
            String prefix = key + "={";
            return content.substring(content.indexOf(prefix) + prefix.length(),
                    content.lastIndexOf("}"));
        }
    }
}
