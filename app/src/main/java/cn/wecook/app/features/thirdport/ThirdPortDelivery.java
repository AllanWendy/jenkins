package cn.wecook.app.features.thirdport;

import android.content.Context;
import android.content.Intent;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.modules.thirdport.SimplePlatformEventListener;
import com.wecook.common.modules.thirdport.object.OrderInfo;
import com.wecook.common.modules.thirdport.platform.Alipay;
import com.wecook.common.modules.thirdport.platform.WeiChat;
import com.wecook.common.modules.thirdport.platform.base.IPlatform;
import com.wecook.sdk.api.legacy.OrderApi;
import com.wecook.sdk.api.model.OrderPaySign;
import com.wecook.uikit.fragment.BaseFragment;

import java.io.Serializable;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;

/**
 * 第三方调起功能分发器
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/24/14
 */
public class ThirdPortDelivery {

    public static void init(Context context) {
        PlatformResourceConfig config = new PlatformResourceConfig();
        PlatformManager.init(context, config);
    }

    /**
     * 分享
     *
     * @param context
     * @param obj
     * @param obj
     */
    public static void share(Context context, Serializable obj) {
        Intent intent = new Intent(context, PlatformActivity.class);
        intent.setAction(PlatformActivity.ACTION_SHARE + "");
        intent.putExtra(PlatformActivity.EXTRA_DATA, obj);
        context.startActivity(intent);
    }

    /**
     * 仅分享到微信
     *
     * @param context
     * @param obj
     * @param obj
     */
    public static void shareOnlyWeixin(Context context, Serializable obj) {
        Intent intent = new Intent(context, PlatformActivity.class);
        intent.setAction(PlatformActivity.ACTION_SHARE + "");
        intent.putExtra(PlatformActivity.EXTRA_DATA, obj);
        intent.putExtra(PlatformActivity.EXTRA_ONLY_WEIXIN, true);
        context.startActivity(intent);
    }

    /**
     * 仅分享到微信
     *
     * @param context
     * @param obj
     * @param obj
     */
    public static void shareOnlyWeixin(Context context, Serializable obj, CharSequence title) {
        Intent intent = new Intent(context, PlatformActivity.class);
        intent.setAction(PlatformActivity.ACTION_SHARE + "");
        intent.putExtra(PlatformActivity.EXTRA_DATA, obj);
        intent.putExtra(PlatformActivity.EXTRA_ONLY_WEIXIN, true);
        intent.putExtra(PlatformActivity.EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    /**
     * 分享 带标题
     *
     * @param context
     * @param obj
     * @param title
     */
    public static void share(Context context, Serializable obj, CharSequence title) {
        Intent intent = new Intent(context, PlatformActivity.class);
        intent.setAction(PlatformActivity.ACTION_SHARE + "");
        intent.putExtra(PlatformActivity.EXTRA_DATA, obj);
        intent.putExtra(PlatformActivity.EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    /**
     * 登录
     *
     * @param context
     * @param type
     */
    public static void login(BaseFragment context, int type) {
        Intent intent = new Intent(context.getContext(), PlatformActivity.class);
        intent.setAction(PlatformActivity.ACTION_LOGIN + "");
        intent.putExtra(PlatformActivity.EXTRA_PLATFORM, type);
        context.startActivity(intent);
    }

    /**
     * 登录
     *
     * @param context
     * @param type
     */
    public static void bind(BaseFragment context, int type) {
        Intent intent = new Intent(context.getContext(), PlatformActivity.class);
        intent.setAction(PlatformActivity.ACTION_LOGIN + "");
        intent.putExtra(PlatformActivity.EXTRA_PLATFORM, type);
        intent.putExtra(PlatformActivity.EXTRA_TO_BIND, true);
        context.startActivity(intent);
    }

    /**
     * 登出
     *
     * @param context
     * @param type
     */
    public static void logout(Context context, int type) {
        PlatformManager.getInstance().unbind(context, type, new SimplePlatformEventListener());
    }

    /**
     * 登出
     *
     * @param context
     */
    public static void logoutAll(Context context) {
        PlatformManager.getInstance().unbind(context, PlatformManager.PLATFORM_WEBLOG, new SimplePlatformEventListener());
        PlatformManager.getInstance().unbind(context, PlatformManager.PLATFORM_WECHAT, new SimplePlatformEventListener());
        PlatformManager.getInstance().unbind(context, PlatformManager.PLATFORM_QQ, new SimplePlatformEventListener());
    }

    public static void pay(final Context context, final String orderTaskId, final IPlatform.IPlatformEventListener eventListener) {
        final LoadingDialog dialog = new LoadingDialog(context);
        dialog.setText(R.string.app_tip_pay_loading);
        dialog.show();
        pay(context, orderTaskId, dialog, eventListener);
    }

    /**
     * 支付
     *
     * @param context
     * @param dialog
     * @param orderTaskId   支付任务ID
     * @param eventListener
     */
    public static void pay(final Context context, final String orderTaskId, final LoadingDialog dialog, final IPlatform.IPlatformEventListener eventListener) {

        if (dialog != null) {
            dialog.show();
        }
        OrderApi.getOrderSign(orderTaskId, new ApiCallback<OrderPaySign>() {
            @Override
            public void onResult(OrderPaySign result) {
                if (result.available()) {
                    OrderInfo orderInfo = null;
                    int type = 0;
                    if (result.isWeChatOrder()) {
                        type = PlatformManager.PLATFORM_WECHAT;
                        orderInfo = new WeiChat.WeChatOrderInfo();
                        OrderPaySign.WeChatOrder weChatOrder = result.getWeChatOrder();
                        ((WeiChat.WeChatOrderInfo) orderInfo).setNoncestr(weChatOrder.getNoncestr());
                        ((WeiChat.WeChatOrderInfo) orderInfo).setPackageValue(weChatOrder.getPackageValue());
                        ((WeiChat.WeChatOrderInfo) orderInfo).setPartnerid(weChatOrder.getPartnerid());
                        ((WeiChat.WeChatOrderInfo) orderInfo).setPrepayid(weChatOrder.getPrepayid());
                        ((WeiChat.WeChatOrderInfo) orderInfo).setSign(weChatOrder.getSign());
                        ((WeiChat.WeChatOrderInfo) orderInfo).setTimestamp(weChatOrder.getTimestamp());
                    } else if (result.isAlipayOrder()) {
                        type = PlatformManager.PLATFORM_ALIPAPA;
                        OrderPaySign.AlipayOrder alipayOrder = result.getAlipayOrder();
                        orderInfo = new Alipay.AlipayOrderInfo();
                        ((Alipay.AlipayOrderInfo) orderInfo).setPayUrl(alipayOrder.getPayInfo());
                    }

                    if (orderInfo != null) {
                        PlatformManager.getInstance().pay(context, type, orderInfo, new SimplePlatformEventListener() {
                            @Override
                            public void onResponsePay(IPlatform platform, boolean success, String message) {
                                super.onResponsePay(platform, success, message);
                                if (eventListener != null) {
                                    eventListener.onResponsePay(platform, success, message);
                                }
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });
                    } else {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                } else {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    /**
     * 获得第三方用户信息
     *
     * @param type
     * @return
     */
    public static Object getUserInfo(int type) {
        return PlatformManager.getInstance().getPlatform(type).getUserInfo();
    }
}
