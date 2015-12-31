package cn.wecook.app.main.dish.order;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.modules.thirdport.SimplePlatformEventListener;
import com.wecook.common.modules.thirdport.platform.base.IPlatform;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.OrderApi;
import com.wecook.sdk.api.model.OrderPaymentInfo;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.dish.DishActivity;

/**
 * 支付列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/30
 */
public class DishPayListFragment extends BaseTitleFragment {

    public static final String EXTRA_ORDER_ID = "extra_order_id";
    /**
     * 支付完成跳转方向
     */
    public static final String EXTRA_PAY_REDIRECT = "extra_pay_redirect";


    public static final int REDIRECT_EXIT = 0;//退出到
    public static final int REDIRECT_ORDER_LIST = 1;
    public static final int REDIRECT_ORDER_DETAIL = 2;

    private static final int PAY_MODE_WECHAT = OrderApi.PAYMENT_TYPE_WECHAT;
    private static final int PAY_MODE_ALIPAY = OrderApi.PAYMENT_TYPE_ALIPAY;

    private TextView mWechatPay;
    private TextView mAlipay;
    private TextView mPayTotal;
    private TextView mCouponPrice;
    private View mDoPayAction;

    private int mPaymentType = PAY_MODE_ALIPAY;
    private String mOrderId;
    private String mTaskId;
    private OrderPaymentInfo mOrderPaymentInfo;
    private ConfirmDialog mShowCancelDialog;

    private int mPayRedirect = REDIRECT_EXIT;
    private LoadingDialog mPayCheckDialog;
    private int mCheckPayStatusTime;

    private Runnable mCheckPayState = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                if (!StringUtils.isEmpty(mOrderId)) {
                    mPayCheckDialog.show();
                    OrderApi.checkPayStatus(mOrderId, new ApiCallback<State>() {
                        /**
                         * 结果回调
                         *
                         * @param result
                         */
                        @Override
                        public void onResult(State result) {
                            if (result.available() && result.getStatusState() == 1) {
                                mPayCheckDialog.dismiss();
                                ToastAlarm.show("支付成功");
                                paySuccessResult();
                            } else {
                                mCheckPayStatusTime++;
                                if (mCheckPayStatusTime >= 3) {
                                    //显示异常对话框
                                    showServerErrorDialog();
                                } else {
                                    UIHandler.postDelayed(mCheckPayState, 1000);
                                }
                            }
                        }
                    });
                }

            }
        }
    };

    /**
     * 显示服务器异常对话框
     */
    private void showServerErrorDialog() {
        ConfirmDialog error = new ConfirmDialog(getContext(),
                "支付结果请求超时，请点击重试，重新获取结果");
        error.cancelable(false);
        error.setConfirm("重试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckPayStatusTime = 0;
                UIHandler.post(mCheckPayState);
            }
        }).setCancel("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPayCheckDialog.dismiss();
                payFailResult();
            }
        }).show();
    }

    /**
     * 支付失败跳转页面
     */
    private void payFailResult() {
        Bundle bundle = new Bundle();
        if (mPayRedirect == REDIRECT_ORDER_DETAIL) {
            bundle.putString(DishOrderDetailFragment.EXTRA_ORDER_ID, mOrderId);
            bundle.putBoolean(DishOrderDetailFragment.EXTRA_BACK_TO_EXIT, true);
            next(DishOrderDetailFragment.class, bundle);
        } else {
            bundle.putBoolean(DishOrderStateListFragment.EXTRA_BACK_TO_EXIT, true);
            bundle.putInt(DishOrderStateListFragment.EXTRA_TAB, DishOrderStateListFragment.ORDER_STATE_PAYING);
            next(DishOrderStateListFragment.class, bundle);
        }
    }

    /**
     * 支付成功跳转页面
     */
    private void paySuccessResult() {
        Bundle bundle = new Bundle();
        if (mPayRedirect == REDIRECT_ORDER_DETAIL) {
            bundle.putString(DishOrderDetailFragment.EXTRA_ORDER_ID, mOrderId);
            bundle.putString(DishOrderDetailFragment.EXTRA_RED_PACKET_ORDER_ID, mOrderId);
            bundle.putBoolean(DishOrderDetailFragment.EXTRA_BACK_TO_EXIT, true);
            next(DishOrderDetailFragment.class, bundle);
        } else {
            bundle.putString(DishOrderStateListFragment.EXTRA_RED_PACKET_ORDER_ID, mOrderId);
            bundle.putBoolean(DishOrderStateListFragment.EXTRA_BACK_TO_EXIT, true);
            bundle.putInt(DishOrderStateListFragment.EXTRA_TAB, DishOrderStateListFragment.ORDER_STATE_RECEIVING);
            next(DishOrderStateListFragment.class, bundle);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrderId = bundle.getString(EXTRA_ORDER_ID);
            mPayRedirect = bundle.getInt(EXTRA_PAY_REDIRECT);
        }
        setTitle("付款");
        setFixed(true);

        mPayCheckDialog = new LoadingDialog(getContext());
        mPayCheckDialog.setText("支付结果确认中...");
    }

    @Override
    public View onCreateInnerView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pay_list, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mWechatPay = (TextView) view.findViewById(R.id.app_pay_list_wechat);
        mAlipay = (TextView) view.findViewById(R.id.app_pay_list_alipay);
        mPayTotal = (TextView) view.findViewById(R.id.app_pay_list_total);
        mCouponPrice = (TextView) view.findViewById(R.id.app_pay_list_coupon);
        mDoPayAction = view.findViewById(R.id.app_pay_list_do_pay);

        mWechatPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPaymentType(PAY_MODE_WECHAT);
            }
        });
        mAlipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPaymentType(PAY_MODE_ALIPAY);
            }
        });

        mDoPayAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPayAction();
            }
        });

        selectPaymentType(mPaymentType);

        getTitleBar().setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelCheckDialog();
            }
        });
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        OrderApi.orderPayInfo(mOrderId, new ApiCallback<OrderPaymentInfo>() {
            @Override
            public void onResult(OrderPaymentInfo result) {
                if (result.available()) {
                    mOrderPaymentInfo = result;
                    updateTotalCount();
                }
            }
        });
    }

    /**
     * 执行支付
     */
    private void performPayAction() {
        mPayCheckDialog.show();
        //1.生成支付任务ID
        OrderApi.orderCreatePayTask(mOrderId, mPaymentType, "0",
                new ApiCallback<State>() {
                    @Override
                    public void onResult(State result) {
                        if (result.available()) {
                            //2.订单生成支付签名
                            mTaskId = result.getExtra().getString("task_id");
                            PlatformManager.getInstance().enableShowNotify(false);
                            ThirdPortDelivery.pay(getContext(), mTaskId, mPayCheckDialog,
                                    new SimplePlatformEventListener() {
                                        @Override
                                        public void onResponsePay(IPlatform platform,
                                                                  boolean success,
                                                                  String message) {
                                            super.onResponsePay(platform, success, message);

                                            String paymentType = "";
                                            switch (mPaymentType) {
                                                case PAY_MODE_WECHAT:
                                                    paymentType = "微信";
                                                    break;
                                                case PAY_MODE_ALIPAY:
                                                    paymentType = "支付宝";
                                                    break;
                                            }
                                            LogGather.onEventDishPayListDo(paymentType, message);

                                            DishPolicy.get().clearLocalCheckOut();
                                            if (success) {
                                                mCheckPayStatusTime = 0;
                                                UIHandler.post(mCheckPayState);
                                            } else {
                                                ToastAlarm.show(message);
                                            }

                                            //逻辑处理完毕之后，再还原通知
                                            UIHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    PlatformManager.getInstance().enableShowNotify(true);
                                                }
                                            }, 500);
                                        }
                                    });
                        } else {
                            String paymentType = "";
                            switch (mPaymentType) {
                                case PAY_MODE_WECHAT:
                                    paymentType = "微信";
                                    break;
                                case PAY_MODE_ALIPAY:
                                    paymentType = "支付宝";
                                    break;
                            }
                            LogGather.onEventDishPayListDo(paymentType, result.getErrorMsg());
                            ToastAlarm.show(result.getErrorMsg());
                            mPayCheckDialog.dismiss();
                        }
                    }
                });
    }

    /**
     * 设置支付方式
     *
     * @param paymentType
     */
    private void selectPaymentType(int paymentType) {
        mPaymentType = paymentType;
        switch (paymentType) {
            case PAY_MODE_WECHAT:
                mWechatPay.setSelected(true);
                mAlipay.setSelected(false);
                break;
            case PAY_MODE_ALIPAY:
                mWechatPay.setSelected(false);
                mAlipay.setSelected(true);
                break;
        }
    }

    /**
     * 更新总价格
     */
    private void updateTotalCount() {
        if (mOrderPaymentInfo == null) {
            return;
        }
        if (mOrderPaymentInfo.getDiscount() > 0) {
            mCouponPrice.setVisibility(View.VISIBLE);
            mCouponPrice.setText("(已省：" + StringUtils.getPriceText(mOrderPaymentInfo.getDiscount()) + ")");
        } else {
            mCouponPrice.setVisibility(View.GONE);
        }

        mPayTotal.setText(StringUtils.getPriceText(mOrderPaymentInfo.getRealPay()));
    }

    @Override
    public boolean back(Bundle bundle) {
        showCancelCheckDialog();
        return super.back(bundle);
    }

    /**
     * 显示取消支付的提示框
     */
    private void showCancelCheckDialog() {
        if (mShowCancelDialog == null) {
            mShowCancelDialog = new ConfirmDialog(getContext(), "确定要取消支付吗？")
                    .setTitle("温馨提示")
                    .setConfirm(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getActivity() instanceof DishActivity && mPayRedirect == REDIRECT_EXIT) {
                                Bundle bundle = getArguments();
                                if (bundle == null) {
                                    bundle = new Bundle();
                                }
                                Intent intent = new Intent(getContext(), DishActivity.class);
                                if (mPayRedirect == REDIRECT_ORDER_DETAIL) {
                                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_ORDER_DETAIL);
                                } else {
                                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_ORDER_LIST);
                                    bundle.putInt(DishOrderStateListFragment.EXTRA_TAB, DishOrderStateListFragment.ORDER_STATE_PAYING);
                                }
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finishAll();
                            } else {
                                finishFragment(getArguments());
                            }

                            DishPolicy.get().clearLocalCheckOut();
                        }
                    });
            mShowCancelDialog.show();
        } else if (!mShowCancelDialog.isShowing()) {
            mShowCancelDialog.show();
        }
    }


}
