package cn.wecook.app.main.dish.order;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.OrderApi;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.Order;
import com.wecook.sdk.api.model.OrderDetail;
import com.wecook.sdk.api.model.OrderState;
import com.wecook.sdk.api.model.ShareState;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.base.StringModel;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.TitleBar;

import java.text.DecimalFormat;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.ListActionDialog;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.suggestion.SuggestionFragment;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.dish.DishDetailFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantDetailFragment;
import cn.wecook.app.main.dish.shopcart.DishShopCartFragment;

/**
 * 订单详情页面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/5
 */
public class DishOrderDetailFragment extends BaseListFragment {
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_BACK_TO_EXIT = "extra_back_to_exit";
    public static final String EXTRA_RED_PACKET_ORDER_ID = "extra_red_packet_order_id";

    public static final int TAB_ORDER_DETAIL = DishOrderTabView.TAB_ORDER_DETAIL;
    public static final int TAB_ORDER_STATE = DishOrderTabView.TAB_ORDER_STATE;

    private String mOrderId;
    private OrderDetail mOrderDetail;
    private ShareState mRedPacketShareState;
    private MergeAdapter mAdapter;
    private DecimalFormat df = new DecimalFormat("00");

    private DishOrderTabView mTabView;
    private int currentTabType = 0;//当前Tab模式


    private View mOrderStatusView;
    private RestaurantAdapter mRestaurantAdapter;
    private View mOrderInfoView;

    private OrderStateAdapter orderStateAdapter;
    //订单状态
    private TextView mStatusName;
    private TextView mStatusSubName;
    private TextView mStatusDesc;
    //优惠
    private View mCouponLayout;
    private TextView mCouponPrice;
    private View mCouponLine;
    //菜金
    private View mCJLayout;
    private TextView mCJPrice;
    private View mCJLine;
    //订单详情
    private TextView mOrderNo;
    private TextView mOrderCreateTime;
    private TextView mOrderPaymentType;
    private TextView mOrderUserName;
    private TextView mOrderPhone;
    private TextView mOrderAddress;
    private TextView mOrderDeliveryTime;//收货时间
    private TextView mOrderNote;//备注信息
    private TextView mOrderDistribution;//配送信息
    private TextView mOrderSecurity;//订单保障

    private TextView mOrderTotalPrice;

    private TextView mOrderActionDetail;
    private TextView mOrderActionState;//订单状态的action
    private View mOrderWaitPayLayout;//订单状态详情页面等待支付action
    private TextView mOrderCancelAction;
    private TextView mOrderPayAction;

    private View mOrderDetailCard;//合计（配送费，优惠，合计）
    private TextView mTotalPrice;
    private TextView mDeliveryPrice;//配送费

    private View mOrderRedPacket;
    private boolean mInLoopUpdate;
    private LoadingDialog mCheckOutLoadingDialog;
    private boolean mIsBackToExit;
    private String mRedPacketOrderId;
    private boolean requestCancelOrder = false;

    private Runnable updatePayTime = new Runnable() {
        @Override
        public void run() {
            if (mOrderDetail != null && mOrderDetail.getOrder() != null) {
                long delay = mOrderDetail.getOrder().getDelayPayTime() - 1;
                if (delay < 0) {
                    onStartUILoad();
                    return;
                }
                mOrderDetail.getOrder().setDelayPayTime(delay);
                String delayTime = getDelayTime(mOrderDetail.getOrder());
                if (mStatusSubName != null) {
                    mStatusSubName.setText("剩余支付时间：" + delayTime);
                }
                if (mOrderPayAction != null) {
                    mOrderPayAction.setText("去付款 " + delayTime);
                }
            }
            UIHandler.postDelayed(updatePayTime, 1000);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mOrderId = bundle.getString(EXTRA_ORDER_ID);
            mIsBackToExit = bundle.getBoolean(EXTRA_BACK_TO_EXIT);
            mRedPacketOrderId = bundle.getString(EXTRA_RED_PACKET_ORDER_ID);
        }
    }

    @Override
    public void onRefreshList() {
        super.onRefreshList();
        onStartUILoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCheckOutLoadingDialog = new LoadingDialog(getContext()) {
            @Override
            public void show() {
                if (null != mCheckOutLoadingDialog && !mCheckOutLoadingDialog.isShowing()) {
                    super.show();
                }
            }
        };
        mCheckOutLoadingDialog.cancelable(false);
        mOrderStatusView = inflater.inflate(R.layout.view_order_detail_status, null);
        mOrderDetailCard = inflater.inflate(R.layout.view_dish_order_detail_total_card, null);
        mOrderInfoView = inflater.inflate(R.layout.view_order_detail_info, null);
        return inflater.inflate(R.layout.fragment_order_detail, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /**
         * 设置titlbar右边图标
         */
        TitleBar.ActionView settingView = new TitleBar.ActionCoveredImageView(getContext(), R.drawable.app_bt_suggestion);
        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入投诉页面
                if (mOrderDetail != null && mOrderDetail.getOrder() != null && !StringUtils.isEmpty(mOrderDetail.getOrder().getOrderId())) {
                    Bundle bundle = new Bundle();
                    bundle.putString(SuggestionFragment.EXTRA_ORDER_ID, mOrderDetail.getOrder().getOrderId());
                    next(SuggestionFragment.class, bundle);
                }

            }
        });
        getTitleBar().addActionView(settingView);


        //优惠
        mCouponLayout = mOrderDetailCard.findViewById(R.id.app_order_detail_info_coupon);
        mCouponPrice = (TextView) mOrderDetailCard.findViewById(R.id.app_order_detail_info_coupon_price);
        mCouponLine = mOrderDetailCard.findViewById(R.id.app_order_detail_info_coupon_line);
        //菜金
        mCJLayout = mOrderDetailCard.findViewById(R.id.app_order_detail_info_cj);
        mCJPrice = (TextView) mOrderDetailCard.findViewById(R.id.app_order_detail_info_cj_price);
        mCJLine = mOrderDetailCard.findViewById(R.id.app_order_detail_info_cj_line);
        //总计
        mTotalPrice = (TextView) mOrderDetailCard.findViewById(R.id.app_order_detail_total_price);
        //配送费
        mDeliveryPrice = (TextView) mOrderDetailCard.findViewById(R.id.app_order_detail_delivery_price);


        mStatusName = (TextView) mOrderStatusView.findViewById(R.id.app_order_detail_status_name);
        mStatusSubName = (TextView) mOrderStatusView.findViewById(R.id.app_order_detail_status_sub_name);
        mStatusDesc = (TextView) mOrderStatusView.findViewById(R.id.app_order_detail_status_desc);

        mOrderNo = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_order_no);
        mOrderCreateTime = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_create_time);
        mOrderPaymentType = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_payment_type);
        mOrderUserName = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_user);
        mOrderPhone = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_phone);
        mOrderAddress = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_address);
        mOrderDeliveryTime = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_delivery_time);
        mOrderNote = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_note);
        mOrderDistribution = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_distribution);
        mOrderNote = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_note);
        mOrderSecurity = (TextView) mOrderInfoView.findViewById(R.id.app_order_detail_info_security);

        mOrderTotalPrice = (TextView) view.findViewById(R.id.app_order_detail_total);
        mOrderWaitPayLayout = view.findViewById(R.id.app_order_detail_action_wait_pay_layout);
        mOrderActionState = (TextView) view.findViewById(R.id.app_order_detail_action_state);
        mOrderActionDetail = (TextView) view.findViewById(R.id.app_order_detail_action_detail);
        setOrderWaitPayLayoutOnclick(mOrderWaitPayLayout);
        mOrderActionDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //再下一单
                performBuyOrderAgain();
            }
        });
        mOrderRedPacket = view.findViewById(R.id.app_order_detail_red_packet);

        mTabView = (DishOrderTabView) view.findViewById(R.id.app_dish_order_tab_view);

        mTabView.setOnTabSelectListener(new DishOrderTabView.OnTabSelectListener() {
            @Override
            public void onSelect(int tab, boolean user) {
                currentTabType = tab;
                updateTabViews();
            }
        });
    }

    /**
     * 设置订单状态 未支付时所显示的action 点击事件
     *
     * @param mOrderWaitPayLayout
     */
    private void setOrderWaitPayLayoutOnclick(View mOrderWaitPayLayout) {
        mOrderCancelAction = (TextView) mOrderWaitPayLayout.findViewById(R.id.app_order_detail_action_cancel);
        mOrderPayAction = (TextView) mOrderWaitPayLayout.findViewById(R.id.app_order_detail_action_pay);
        mOrderCancelAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消订单
                if (requestCancelOrder) {
                    return;
                } else {
                    requestCancelOrder = true;
                }
                performCancelOrder();

            }
        });
        mOrderPayAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //去支付
                performPay();

            }
        });
    }

    /**
     * 显示type的views
     */
    private void updateTabViews() {
        if (mAdapter == null) {
            return;
        }
        switch (currentTabType) {
            case TAB_ORDER_DETAIL:
                //订单详情；
                mAdapter.pendingActive(mRestaurantAdapter, true);
                mAdapter.pendingActive(mOrderDetailCard, true);
                mAdapter.pendingActive(mOrderInfoView, true);
                mAdapter.pendingActive(orderStateAdapter, false);
                setActionVisible(currentTabType);
                mOrderRedPacket.setVisibility(View.GONE);
                break;
            case TAB_ORDER_STATE:
                //订单状态:
                mAdapter.pendingActive(mRestaurantAdapter, false);
                mAdapter.pendingActive(mOrderDetailCard, false);
                mAdapter.pendingActive(mOrderInfoView, false);
                mAdapter.pendingActive(orderStateAdapter, true);
                setActionVisible(currentTabType);
                if (mRedPacketShareState != null) {
                    mOrderRedPacket.setVisibility(View.VISIBLE);
                }
                break;
        }
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        showLoading();
        OrderApi.orderDetail(mOrderId, new ApiCallback<OrderDetail>() {
            @Override
            public void onResult(OrderDetail result) {
                if (result.available() && getContext() != null) {
                    mOrderDetail = result;
                    mInLoopUpdate = false;
                    if (mOrderDetail.getOrder() != null && currentTabType == 0) {
                        switch (mOrderDetail.getOrder().getStatus()) {
                            case OrderApi.STATUS_ORDER_EVALUATED:
                            case OrderApi.STATUS_ORDER_CANCELED:
                            case OrderApi.STATUS_ORDER_REFUNDED:
                                //已评价，已取消订单，已退款   进入订单详情页面
                                currentTabType = TAB_ORDER_DETAIL;
                                break;
                            default:
                                //其他状态   进入订单状态页面
                                currentTabType = TAB_ORDER_STATE;
                                break;
                        }
                    }
                    updateDetailView();
                    mTabView.selectTab(currentTabType);
                }
                //获取红包状态
                mRedPacketShareState = result.getShareState();
                if (mOrderRedPacket != null) {
                    if (mRedPacketShareState != null && currentTabType == TAB_ORDER_STATE) {
                        mOrderRedPacket.setVisibility(View.VISIBLE);
                    } else {
                        mOrderRedPacket.setVisibility(View.GONE);
                    }
                }

                hideLoading();
            }
        });

        if (!StringUtils.isEmpty(mRedPacketOrderId)) {
            OrderApi.orderRedPacket(mRedPacketOrderId, new ApiCallback<ShareState>() {
                @Override
                public void onResult(ShareState result) {
                    mRedPacketShareState = result;
                    performRedPacketShare();
                }
            });

            mRedPacketOrderId = null;
        }
    }

    private void performRedPacketShare() {
        if (mRedPacketShareState != null) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            SpannableString string1 = new SpannableString("分享★！");
            string1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uikit_font_dark)),
                    0, string1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString string2 = new SpannableString("分享后您和好友均可领取");
            string2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uikit_font_grep)),
                    0, string2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            string2.setSpan(new AbsoluteSizeSpan(14, true),
                    0, string2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            sb.append(string1);
            sb.append("\n");
            sb.append(string2);

            ThirdPortDelivery.shareOnlyWeixin(getContext(), mRedPacketShareState, sb);
        }
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        if (mCheckOutLoadingDialog != null) {
            mCheckOutLoadingDialog.dismiss();
        }
    }

    /**
     * 更新页面数据
     */
    private void updateDetailView() {
        if (mOrderDetail != null
                && mOrderDetail.getOrder() != null
                && mOrderDetail.getOrder().getRestaurantList() != null) {
            mAdapter = new MergeAdapter();
            mRestaurantAdapter = new RestaurantAdapter(getContext(),
                    mOrderDetail.getOrder().getRestaurantList().getList());
            ApiModelList<OrderState> orderState = mOrderDetail.getOrderState();
            if (orderState != null && orderState.getList() != null) {
                orderStateAdapter = new OrderStateAdapter(getContext(), orderState.getList());
            }
            ShopCartRestaurant shopCartRestaurant = mOrderDetail.getOrder().getRestaurantList().getList().get(0);
            if (shopCartRestaurant != null) {
                setTitle(shopCartRestaurant.getName());
            }
            updateStatusView();
            updateTotalCardInfoView();
            updateMoreInfoView();
            updateCountBar();

            if (orderStateAdapter != null) {
                mAdapter.addAdapter(orderStateAdapter);
            }
            mAdapter.addAdapter(mRestaurantAdapter);
            mAdapter.addView(mOrderDetailCard);
            mAdapter.addView(mOrderInfoView);
            getListView().setAdapter(mAdapter);
            updateTabViews();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新小结内容
     */
    private void updateTotalCardInfoView() {
        if (mOrderDetail != null) {
            //配送费
            if (mOrderDetail.getOrder() != null) {
                String deliveryPrice = mOrderDetail.getOrder().getDeliveryPrice();
                double deliveryPriceRaw = StringUtils.parseDouble(deliveryPrice);
                mDeliveryPrice.setText(StringUtils.getPriceText(deliveryPriceRaw));
            }
            //优惠
            if (mOrderDetail.getOrder() != null && (Double.parseDouble(mOrderDetail.getOrder().getDiscount())) > 0) {
                mCouponLayout.setVisibility(View.VISIBLE);
                mCouponLine.setVisibility(View.VISIBLE);
                mCouponPrice.setText("- " + StringUtils.getPriceText(Double.parseDouble(mOrderDetail.getOrder().getDiscount())));
            } else {
                mCouponLayout.setVisibility(View.GONE);
                mCouponLine.setVisibility(View.GONE);
            }
            //菜金
            if (mOrderDetail.getOrder() != null && null != mOrderDetail.getOrder().getWallet() && (Double.parseDouble(mOrderDetail.getOrder().getWallet())) > 0) {
                mCJLayout.setVisibility(View.VISIBLE);
                mCJLine.setVisibility(View.VISIBLE);
                final double totalPaymentRaw = StringUtils.parseDouble(mOrderDetail.getOrder().getWallet());
                mCJPrice.setText("- " + StringUtils.getPriceText(totalPaymentRaw));
            } else {
                mCJLayout.setVisibility(View.GONE);
                mCJLine.setVisibility(View.GONE);
            }
            // 实付
            if (mOrderDetail.getOrder() != null) {
                final double totalPaymentRaw = StringUtils.parseDouble(mOrderDetail.getOrder().getRealPay());
                mTotalPrice.setText(StringUtils.getPriceText(totalPaymentRaw));
            }
        }
    }

    /**
     * 更新底部结算动作界面
     */
    private void updateCountBar() {
        if (mOrderDetail.getOrder() != null) {
            final double totalPaymentRaw = StringUtils.parseDouble(mOrderDetail.getOrder().getRealPay());
            mOrderTotalPrice.setText(StringUtils.getPriceText(totalPaymentRaw));

            switch (mOrderDetail.getOrder().getStatus()) {
                case OrderApi.STATUS_ORDER_WAIT_PAY:
                    setActionTextAndClick("再下一单", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //再下一单
                            performBuyOrderAgain();
                        }

                    }, mOrderActionState);
                    break;
                case OrderApi.STATUS_ORDER_PAYED_WAIT_CHECK:
                    setActionTextAndClick("取消订单", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //取消订单
                            performCancelOrder();
                        }
                    }, mOrderActionState);
                    break;
                case OrderApi.STATUS_ORDER_CHECKED_WAIT_TRANSPORT:
                    setActionTextAndClick("确定收货", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //确定收货
                            performTransport();
                        }
                    }, mOrderActionState);
                    break;
                case OrderApi.STATUS_ORDER_TRANSPORTED_WAIT_EVALUATE:
                    setActionTextAndClick("给个评价", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //给个评价
                            performEvaluate();
                        }
                    }, mOrderActionState);
                    break;
                case OrderApi.STATUS_ORDER_EVALUATED:
                case OrderApi.STATUS_ORDER_CANCELED:
                case OrderApi.STATUS_ORDER_REFUNDED:
                    setActionTextAndClick("再下一单", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //再下一单
                            performBuyOrderAgain();
                        }
                    }, mOrderActionState);
                    break;
            }
            setActionVisible(currentTabType);
        }
    }

    /**
     * 设置多个 action 文字 点击事件
     *
     * @param text
     */
    private void setActionTextAndClick(String text, View.OnClickListener clickListener, TextView... view) {
        setViewsText(text, view);
        setViewsOnClickListener(clickListener, view);
    }

    /**
     * 设置多个view的onCliclk
     *
     * @param clickListener
     */
    private void setViewsOnClickListener(View.OnClickListener clickListener, View... view) {
        if (clickListener != null) {
            for (int i = 0; i < view.length; i++) {
                if (view[i] != null) {
                    view[i].setOnClickListener(clickListener);
                }
            }
        }
    }

    /**
     * 设置多个view的文字
     */
    private void setViewsText(String text, TextView... view) {
        if (!StringUtils.isEmpty(text)) {
            for (int i = 0; i < view.length; i++) {
                if (view[i] != null) {
                    view[i].setText(text);
                }
            }
        }
    }

    /**
     * 设置当前底部action的visible
     *
     * @param currentTabType
     */
    private TextView setActionVisible(int currentTabType) {
        switch (currentTabType) {
            case TAB_ORDER_DETAIL:
                //订单详情页面
                mOrderActionDetail.setVisibility(View.VISIBLE);
                mOrderActionState.setVisibility(View.GONE);
                mOrderWaitPayLayout.setVisibility(View.GONE);
                return mOrderActionDetail;
            case TAB_ORDER_STATE:
                //点单状态页面
            case OrderApi.STATUS_ORDER_EVALUATED:
            case OrderApi.STATUS_ORDER_CANCELED:
            case OrderApi.STATUS_ORDER_REFUNDED:
                mOrderActionDetail.setVisibility(View.GONE);
                if (mOrderDetail != null) {
                    if (mOrderDetail.getOrder().getStatus() == OrderApi.STATUS_ORDER_WAIT_PAY) {
                        mOrderActionState.setVisibility(View.GONE);
                        mOrderWaitPayLayout.setVisibility(View.VISIBLE);
                    } else if (mOrderDetail.getOrder().getStatus() == OrderApi.STATUS_ORDER_EVALUATED || mOrderDetail.getOrder().getStatus() == OrderApi.STATUS_ORDER_CANCELED || mOrderDetail.getOrder().getStatus() == OrderApi.STATUS_ORDER_REFUNDED) {
                        mOrderActionDetail.setVisibility(View.GONE);
                        mOrderActionState.setVisibility(View.GONE);
                        mOrderWaitPayLayout.setVisibility(View.GONE);
                    } else {
                        mOrderActionState.setVisibility(View.VISIBLE);
                        mOrderWaitPayLayout.setVisibility(View.GONE);
                    }
                } else {
                    mOrderActionState.setVisibility(View.VISIBLE);
                    mOrderWaitPayLayout.setVisibility(View.GONE);
                }
                return mOrderActionState;
        }
        return null;
    }

    /**
     * 执行评论
     */
    private void performEvaluate() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(DishOrderEvaluateFragment.EXTRA_ORDER, mOrderDetail.getOrder());
        next(DishOrderEvaluateFragment.class, bundle);
    }

    /**
     * 执行收货
     */
    private void performTransport() {
        new ConfirmDialog(getContext(), "请确定您已经收到菜品")
                .setTitle("温馨提示")
                .setConfirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCheckOutLoadingDialog.show();
                        OrderApi.orderReceived(mOrderDetail.getOrder().getOrderId(), new ApiCallback<State>() {
                            @Override
                            public void onResult(State result) {
                                if (result.available()) {
                                    ToastAlarm.show("收货成功");
                                } else {
                                    ToastAlarm.show("收货失败");
                                }
                                onStartUILoad();
                            }
                        });
                    }
                }).show();
    }

    /**
     * 再下一单
     */
    private void performBuyOrderAgain() {
        mCheckOutLoadingDialog.show();
        OrderApi.orderAgain(mOrderDetail.getOrder().getOrderId(), new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                mCheckOutLoadingDialog.dismiss();
                LogGather.onEventDishOrderDetailAgain(result.getErrorMsg());
                if (result.available()) {
                    setSelectedState(mOrderDetail);
                    //跳转购物车界面
                    next(DishShopCartFragment.class);
                }
                ToastAlarm.show(result.getErrorMsg());
            }
        });
    }

    private void setSelectedState(OrderDetail detail) {
        if (detail != null
                && detail.getOrder() != null
                && detail.getOrder().getRestaurantList() != null) {
            for (ShopCartRestaurant restaurant : detail.getOrder().getRestaurantList().getList()) {
                if (restaurant != null && restaurant.getDishes() != null) {
                    for (Dish dish : restaurant.getDishes()) {
                        DishPolicy.get().recordSelectedDish(dish.getDishId());
                    }
                }
            }
        }
    }

    /**
     * 执行支付
     */
    private void performPay() {
        Bundle bundle = new Bundle();
        bundle.putString(DishPayListFragment.EXTRA_ORDER_ID, mOrderDetail.getOrder().getOrderId());
        bundle.putString(DishOrderDetailFragment.EXTRA_ORDER_ID, mOrderDetail.getOrder().getOrderId());
        bundle.putInt(DishPayListFragment.EXTRA_PAY_REDIRECT, DishPayListFragment.REDIRECT_ORDER_DETAIL);
        next(DishPayListFragment.class, bundle);
    }

    /**
     * 取消订单
     */
    private void performCancelOrder() {
        mCheckOutLoadingDialog.show();
        OrderApi.orderCancelConfirm(mOrderDetail.getOrder().getOrderId(), new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (result.getStatusState() == 0) {
                    //该订单无法被取消
                    new ConfirmDialog(getContext(), result.getErrorMsg())
                            .setTitle("温馨提示")
                            .showCancel(false)
                            .showConfirm(true)
                            .show();
                    mCheckOutLoadingDialog.dismiss();
                } else if (result.getStatusState() == 2) {
                    //将info内容提示给用户，用户点击确认再继续取消订单
                    new ConfirmDialog(getContext(), result.getErrorMsg())
                            .setTitle("温馨提示")
                            .setConfirm("继续", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    realCancelOrder();
                                }
                            })
                            .show();
                } else if (result.getStatusState() == 1) {
                    realCancelOrder();
                } else {
                    mCheckOutLoadingDialog.dismiss();
                    ToastAlarm.show("code:" + result.getStatusState());
                }
                requestCancelOrder = false;
            }
        });
    }

    private void realCancelOrder() {
        mCheckOutLoadingDialog.show();
        OrderApi.orderCancelReasonList(new ApiCallback<ApiModelList<StringModel>>() {
            @Override
            public void onResult(ApiModelList<StringModel> result) {
                if (result.available()) {
                    final String[] reasons = new String[result.getList().size()];
                    for (int i = 0; i < result.getCountOfList(); i++) {
                        StringModel stringModel = result.getItem(i);
                        reasons[i] = stringModel.getContent();
                    }
                    new ListActionDialog(getContext(), "选择取消原因", reasons, new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            mCheckOutLoadingDialog.show();
                            OrderApi.orderCancel(mOrderDetail.getOrder().getOrderId(), reasons[position],
                                    new ApiCallback<State>() {
                                        @Override
                                        public void onResult(State result) {
                                            mCheckOutLoadingDialog.dismiss();
                                            LogGather.onEventDishOrderDetailCancel(reasons[position]);
                                            if (result.available()) {
                                                ToastAlarm.show("取消成功");
                                            }
                                            onStartUILoad();
                                        }
                                    });
                        }
                    }).show();
                }
                mCheckOutLoadingDialog.dismiss();
            }
        });
    }

    /**
     * 更新订单信息
     */
    private void updateMoreInfoView() {

        if (mOrderDetail != null) {
            if (mOrderDetail.getOrder() != null) {
                mOrderNo.setText(mOrderDetail.getOrder().getOrder_id_show());
                mOrderCreateTime.setText(mOrderDetail.getOrder().getCreateTime());
                mOrderPaymentType.setText(mOrderDetail.getOrder().getPaymentType());
            }

            if (mOrderDetail.getAddress() != null) {
                mOrderUserName.setText(mOrderDetail.getAddress().getName());
                mOrderPhone.setText(mOrderDetail.getAddress().getTel());
                mOrderAddress.setText(mOrderDetail.getAddress().getStreet());
            }
            //已取消，等待付款
            if (null != mOrderDetail && null != mOrderDetail.getOrder() &&
                    (mOrderDetail.getOrder().getStatus() == OrderApi.STATUS_ORDER_CANCELED || mOrderDetail.getOrder().getStatus() == OrderApi.STATUS_ORDER_WAIT_PAY)) {
                setOrderInfoGroupVisible(mOrderNote, View.GONE);
                setOrderInfoGroupVisible(mOrderDistribution, View.GONE);
                setOrderInfoGroupVisible(mOrderDeliveryTime, View.GONE);
            } else {
                //设置备注信息
                if (mOrderDetail != null && mOrderDetail.getOrder() != null && mOrderDetail.getOrder().getRestaurantList() != null) {
                    List<ShopCartRestaurant> list = mOrderDetail.getOrder().getRestaurantList().getList();
                    if (list != null && list.size() > 0 && !StringUtils.isEmpty(list.get(0).getRemarkContent())) {
                        mOrderNote.setText(list.get(0).getRemarkContent());
                        setOrderInfoGroupVisible(mOrderNote, View.VISIBLE);
                    } else {
                        setOrderInfoGroupVisible(mOrderNote, View.GONE);
                    }
                }
                //收货时间
                if (mOrderDetail.getOrder() != null && mOrderDetail.getOrder().getRestaurantList() != null && mOrderDetail.getOrder().getRestaurantList().getList() != null && mOrderDetail.getOrder().getRestaurantList().getList().get(0) != null) {
                    mOrderDeliveryTime.setText(mOrderDetail.getOrder().getRestaurantList().getList().get(0).getDeliveryTime().getFormatTime());
                    setOrderInfoGroupVisible(mOrderDeliveryTime, View.VISIBLE);
                } else {
                    setOrderInfoGroupVisible(mOrderDeliveryTime, View.GONE);
                }
                //配送信息
                if (mOrderDetail.getOrder() != null && mOrderDetail.getOrder().getExpress_by() != null) {
                    mOrderDistribution.setText(mOrderDetail.getOrder().getExpress_by());
                    setOrderInfoGroupVisible(mOrderDistribution, View.VISIBLE);
                } else {
                    setOrderInfoGroupVisible(mOrderDistribution, View.GONE);
                }
            }

            //订单保障
            if (mOrderDetail.getOrder() != null && mOrderDetail.getOrder().getSecurity() != null) {
                String security = mOrderDetail.getOrder().getSecurity();
                int startIndex = security.indexOf("<span>");
                int endIndex = security.indexOf("</span>");
                SpannableString spannableString = new SpannableString(Html.fromHtml(mOrderDetail.getOrder().getSecurity()));
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uikit_font_orange)), startIndex, endIndex - 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mOrderSecurity.setText(spannableString);
            } else {
                setOrderInfoGroupVisible(mOrderSecurity, View.GONE);
            }
        }
    }

    /**
     * 设置Orderinfo 布局显隐
     */
    private void setOrderInfoGroupVisible(View view, int isVisible) {
        if (view == null) {
            return;
        }
        if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            viewGroup.setVisibility(isVisible);
        }
    }

    /**
     * 更新订单状态
     */
    private void updateStatusView() {
        mStatusName.setText(mOrderDetail.getOrder().getStatusDesc());
        //待支付状态下
        if (mOrderDetail.getOrder().getStatus() == OrderApi.STATUS_ORDER_WAIT_PAY) {
            mStatusSubName.setVisibility(View.VISIBLE);
            mStatusSubName.setText("剩余支付时间：" + getDelayTime(mOrderDetail.getOrder()));
            if (!mInLoopUpdate) {
                mInLoopUpdate = true;
                UIHandler.post(updatePayTime);
            }
        } else {
            mStatusSubName.setVisibility(View.GONE);
        }

        if (mOrderDetail.getOrderRefundState() != null) {
            mStatusDesc.setVisibility(View.VISIBLE);
            mStatusDesc.setText(mOrderDetail.getOrderRefundState().getText());
        } else {
            mStatusDesc.setVisibility(View.GONE);
        }

        mOrderRedPacket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示红包对话框
                performRedPacketShare();
            }
        });
    }

    private String getDelayTime(Order data) {
        long minute = data.getDelayPayTime() / 60;
        long second = data.getDelayPayTime() % 60;
        return df.format(minute) + ":" + df.format(second);
    }

    @Override
    public boolean back(Bundle data) {
        if (mIsBackToExit) {
            finishAll();
            return true;
        }
        return super.back(data);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 商家订单详情adapter
     */
    private class RestaurantAdapter extends UIAdapter<ShopCartRestaurant> {

        public RestaurantAdapter(Context context, List<ShopCartRestaurant> data) {
            super(context, R.layout.listview_item_order_detail, data);
        }

        @Override
        public void updateView(int position, int viewType, final ShopCartRestaurant data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            ViewGroup dishGroup = (ViewGroup) findViewById(R.id.app_order_detail_dish_list_layout);
            ViewGroup restaurantLayout = (ViewGroup) findViewById(R.id.app_order_detail_restaurant_layout);
            ImageView restaurantImage = (ImageView) findViewById(R.id.app_order_detail_restaurant_image);
            TextView restaurantName = (TextView) findViewById(R.id.app_order_detail_restaurant_name);

            restaurantLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //进入餐厅详情
                    Bundle bundle = new Bundle();
                    bundle.putString(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, data.getId());
                    next(DishRestaurantDetailFragment.class, bundle);
                }
            });

            ImageFetcher.asInstance().load(data.getImage(), restaurantImage);
            restaurantName.setText(data.getName());

            updateDishItem(data, dishGroup);
        }

        private void updateDishItem(ShopCartRestaurant data, ViewGroup dishGroup) {

            dishGroup.removeAllViews();
            for (int i = 0; i < data.getShopCartDishes().size(); i++) {
                final ShopCartDish dish = data.getShopCartDishes().get(i);
                View view = LayoutInflater.from(getContext()).inflate(R.layout.view_order_detail_dish_info, null);
                TextView dishName = (TextView) view.findViewById(R.id.app_order_detail_dish_name);
                TextView dishCount = (TextView) view.findViewById(R.id.app_order_detail_dish_count);
                TextView dishPrice = (TextView) view.findViewById(R.id.app_order_detail_dish_price);
                View bottomLine = view.findViewById(R.id.app_order_detail_dish_bottom_line);


                bottomLine.setVisibility(i == (data.getShopCartDishes().size() - 1) ? View.GONE : View.VISIBLE);
                dishName.setText(dish.getTitle());
                dishCount.setText("x" + dish.getQuantity());
                dishPrice.setText(StringUtils.getPriceText(dish.getRawPrice()));
                view.findViewById(R.id.app_order_detail_dish_layout).
                        setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //跳转到菜品详情
                                Bundle bundle = new Bundle();
                                bundle.putString(DishDetailFragment.EXTRA_DISH_ID, dish.getDishId());
                                next(DishDetailFragment.class, bundle);
                            }
                        });
                updPackageFoodView(dish, view);
                dishGroup.addView(view, new LinearLayout.LayoutParams(-1, -2));
            }
        }

        /**
         * 处理套餐列表
         *
         * @param dish
         * @param rootView
         */
        private void updPackageFoodView(ShopCartDish dish, View rootView) {
            final ImageView btnSetMeal = (ImageView) rootView.findViewById(R.id.app_order_detail_dish_more);
            final LinearLayout layout_more_dish = (LinearLayout) rootView.findViewById(R.id.app_order_detail_dish_more_layout);

            if (dish.isPackagesFood()) {
                for (int i = 0; i < dish.getPackages().getList().size(); i++) {
                    final ShopCartDish packageDish = dish.getPackages().getList().get(i);
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_detail_package_item_info, null);
                    TextView title = (TextView) view.findViewById(R.id.app_order_detail_package_dish_name);
                    TextView count = (TextView) view.findViewById(R.id.app_order_detail_package_dish_count);

                    title.setText(packageDish.getTitle());
                    count.setText("x" + dish.getQuantity() * packageDish.getQuantity());

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转到菜品详情
                            Bundle bundle = new Bundle();
                            bundle.putString(DishDetailFragment.EXTRA_DISH_ID, packageDish.getDishId());
                            next(DishDetailFragment.class, bundle);
                        }
                    });

                    layout_more_dish.addView(view);
                }

                View lineView = LayoutInflater.from(getContext()).inflate(R.layout.uikit_view_detail_line, null);
                View line = lineView.findViewById(R.id.uikit_view_line);
                ScreenUtils.reMargin(line, ScreenUtils.dip2px(15), 0, 0, 0);
                layout_more_dish.addView(lineView);

                btnSetMeal.setVisibility(View.VISIBLE);
                btnSetMeal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layout_more_dish.setVisibility(layout_more_dish.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                        btnSetMeal.setImageResource(layout_more_dish.getVisibility() == View.GONE ? R.drawable.app_bt_circle_button_down : R.drawable.app_bt_circle_button_up);
                    }
                });
            } else {
                btnSetMeal.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 订单状态adapter
     */
    class OrderStateAdapter extends UIAdapter<OrderState> {

        private int totalSize = 0;

        public OrderStateAdapter(Context context, List<OrderState> data) {
            super(context, R.layout.listview_item_order_state, data);
            if (data != null) {
                this.totalSize = data.size();
            }
        }

        @Override
        protected View newView(int viewType, int position) {
            View itemView = super.newView(viewType, position);
            View div = itemView.findViewById(R.id.app_order_state_div);
            div.setVisibility((0 == position) ? View.VISIBLE : View.GONE);
            return itemView;
        }

        @Override
        public void updateView(int position, int viewType, OrderState data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            View mUpLine = findViewById(R.id.app_dish_order_state_upline);
            View mDownLine = findViewById(R.id.app_dish_order_state_downline);
            ImageView mIcon = (ImageView) findViewById(R.id.app_dish_order_state_icon);
            TextView mTitle = (TextView) findViewById(R.id.app_dish_order_state_title);
            TextView mDesc = (TextView) findViewById(R.id.app_dish_order_state_desc);
            TextView mTime = (TextView) findViewById(R.id.app_dish_order_state_time);
            View mLine = findViewById(R.id.app_dish_order_state_line);
            if (data != null) {
                itemLineManager(position, mUpLine, mDownLine);

                ImageFetcher.asInstance().load(data.getIcon(), mIcon);
                mTitle.setText(data.getTitle());
                if (position == 0) {
                    mTitle.setTextColor(0xffff644e);
                } else {
                    mTitle.setTextColor(0xff4a4a4a);
                }
                if (StringUtils.isEmpty(data.getDesc())) {
                    mDesc.setVisibility(View.GONE);
                } else {
                    if (data.getDesc().contains("电话") || data.getDesc().contains("手机")) {
                        mDesc.setAutoLinkMask(Linkify.PHONE_NUMBERS);
                    } else {
                        mDesc.setAutoLinkMask(0);
                    }
                    mDesc.setText(data.getDesc());
                    mDesc.setVisibility(View.VISIBLE);
                }
                if (position == (totalSize - 1)) {
                    mLine.setVisibility(View.GONE);
                }

                mTime.setText(data.getTime());
            }
        }

        private void itemLineManager(int position, View mUpLine, View mDownLine) {
            if (position == 0) {
                //首位
                mUpLine.setVisibility(View.INVISIBLE);
                if (totalSize == 1) {
                    mDownLine.setVisibility(View.INVISIBLE);
                } else {
                    mDownLine.setVisibility(View.VISIBLE);

                }
            } else if (position == (totalSize - 1)) {
                //尾部
                mUpLine.setVisibility(View.VISIBLE);
                mDownLine.setVisibility(View.INVISIBLE);
            } else {
                mUpLine.setVisibility(View.VISIBLE);
                mDownLine.setVisibility(View.VISIBLE);
            }
        }
    }
}
