package cn.wecook.app.main.dish.order;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.OrderApi;
import com.wecook.sdk.api.model.Order;
import com.wecook.sdk.api.model.ShareState;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.ApiModelListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.text.DecimalFormat;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.MainActivity;
import cn.wecook.app.main.MainFragment;
import cn.wecook.app.main.dish.DishActivity;

/**
 * 订单状态列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/30
 */
public class DishOrderStateListFragment extends ApiModelListFragment<Order> {

    public static final String EXTRA_TAB = "extra_order_state_tab";
    public static final String EXTRA_BACK_TO_EXIT = "extra_back_to_exit";
    public static final String EXTRA_RED_PACKET_ORDER_ID = "extra_red_packet_order_id";

    public static final int ORDER_STATE_ALL = 1;
    public static final int ORDER_STATE_PAYING = 2;
    public static final int ORDER_STATE_RECEIVING = 3;
    public static final int ORDER_STATE_EVALUATING = 4;
    public static final int ORDER_STATE_REFUNDING = 5;

    private View mOrderStateAll;//全部
    private View mOrderStatePaying;//待付款
    private View mOrderStateReceiving;//待收货
    private View mOrderStateEvaluating;//待评价
    private View mOrderStateRefunding;//退款

    private int mCurrentOrderState = ORDER_STATE_ALL;
    private boolean mHasInited;
    private DecimalFormat df = new DecimalFormat("00");

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {

            boolean allDelayTimeToZero = true;
            for (Order order : getListData()) {
                if (OrderApi.STATUS_ORDER_WAIT_PAY == order.getStatus()) {
                    long delayTime = order.getDelayPayTime() - 1;
                    if (delayTime < 0) {
                        delayTime = 0;
                    } else {
                        allDelayTimeToZero = false;
                    }
                    order.setDelayPayTime(delayTime);
                }
            }

            if (!allDelayTimeToZero) {
                notifyDataSetChanged();
            }
        }
    };
    private LoadingDialog mCheckOutLoadingDialog;

    private boolean mIsBackToExit;
    private String mRedPacketOrderId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle("订单");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentOrderState = bundle.getInt(EXTRA_TAB, ORDER_STATE_ALL);
            mIsBackToExit = bundle.getBoolean(EXTRA_BACK_TO_EXIT);
            mRedPacketOrderId = bundle.getString(EXTRA_RED_PACKET_ORDER_ID);
        }

        mHasInited = false;
        mCheckOutLoadingDialog = new LoadingDialog(activity);
        mCheckOutLoadingDialog.cancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_state_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.BOTH);
        mOrderStateAll = view.findViewById(R.id.app_order_state_all);
        mOrderStatePaying = view.findViewById(R.id.app_order_state_paying);
        mOrderStateReceiving = view.findViewById(R.id.app_order_state_receiving);
        mOrderStateEvaluating = view.findViewById(R.id.app_order_state_evaluating);
        mOrderStateRefunding = view.findViewById(R.id.app_order_state_refunding);

        ((TextView) mOrderStateAll.findViewById(R.id.app_text_title)).setText("全部");
        ((TextView) mOrderStatePaying.findViewById(R.id.app_text_title)).setText("待付款");
        ((TextView) mOrderStateReceiving.findViewById(R.id.app_text_title)).setText("待收货");
        ((TextView) mOrderStateEvaluating.findViewById(R.id.app_text_title)).setText("待评价");
        ((TextView) mOrderStateRefunding.findViewById(R.id.app_text_title)).setText("退款");

        mOrderStateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOrderState(ORDER_STATE_ALL);
            }
        });
        mOrderStatePaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOrderState(ORDER_STATE_PAYING);
            }
        });
        mOrderStateReceiving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOrderState(ORDER_STATE_RECEIVING);
            }
        });
        mOrderStateEvaluating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOrderState(ORDER_STATE_EVALUATING);
            }
        });
        mOrderStateRefunding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOrderState(ORDER_STATE_REFUNDING);
            }
        });

        selectOrderState(mCurrentOrderState);

        if (!StringUtils.isEmpty(mRedPacketOrderId)) {
            performRedPacketShare(mRedPacketOrderId);
            mRedPacketOrderId = null;
        }
    }

    private void selectOrderState(int orderState) {

        if (mCurrentOrderState != orderState || !mHasInited) {
            mHasInited = true;
            getLoadMore().reset();
            showLoading();
            mCheckOutLoadingDialog.show();
            mCurrentOrderState = orderState;
            updateList();

            switch (orderState) {
                case ORDER_STATE_ALL:
                    mOrderStateAll.setSelected(true);
                    mOrderStatePaying.setSelected(false);
                    mOrderStateReceiving.setSelected(false);
                    mOrderStateEvaluating.setSelected(false);
                    mOrderStateRefunding.setSelected(false);
                    break;
                case ORDER_STATE_PAYING:
                    mOrderStateAll.setSelected(false);
                    mOrderStatePaying.setSelected(true);
                    mOrderStateReceiving.setSelected(false);
                    mOrderStateEvaluating.setSelected(false);
                    mOrderStateRefunding.setSelected(false);
                    break;
                case ORDER_STATE_RECEIVING:
                    mOrderStateAll.setSelected(false);
                    mOrderStatePaying.setSelected(false);
                    mOrderStateReceiving.setSelected(true);
                    mOrderStateEvaluating.setSelected(false);
                    mOrderStateRefunding.setSelected(false);
                    break;
                case ORDER_STATE_EVALUATING:
                    mOrderStateAll.setSelected(false);
                    mOrderStatePaying.setSelected(false);
                    mOrderStateReceiving.setSelected(false);
                    mOrderStateEvaluating.setSelected(true);
                    mOrderStateRefunding.setSelected(false);
                    break;
                case ORDER_STATE_REFUNDING:
                    mOrderStateAll.setSelected(false);
                    mOrderStatePaying.setSelected(false);
                    mOrderStateReceiving.setSelected(false);
                    mOrderStateEvaluating.setSelected(false);
                    mOrderStateRefunding.setSelected(true);
                    break;
            }
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        if (data != null) {
            mCurrentOrderState = data.getInt(EXTRA_TAB);
        }
    }

    @Override
    public void onRefreshList() {
        super.onRefreshList();
        onStartUILoad();
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_order);
            emptyView.setTitle("暂无订单");
            emptyView.setActionButton("去买菜", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharePreferenceProperties.set("main_current_position", MainFragment.TAB_FOOD);
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    getContext().startActivity(intent);
                    if (getActivity() instanceof DishActivity) {
                        finishAll();
                    }
                }
            });
        }
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Order>> callback) {
        String orderState = "";
        switch (mCurrentOrderState) {
            case ORDER_STATE_PAYING:
                orderState = "" + OrderApi.STATUS_ORDER_WAIT_PAY;
                break;
            case ORDER_STATE_RECEIVING:
                orderState = OrderApi.STATUS_ORDER_PAYED_WAIT_CHECK
                        + "," + OrderApi.STATUS_ORDER_CHECKED_WAIT_TRANSPORT;
                break;
            case ORDER_STATE_EVALUATING:
                orderState = "" + OrderApi.STATUS_ORDER_TRANSPORTED_WAIT_EVALUATE;
                break;
            case ORDER_STATE_REFUNDING:
                orderState = "" + OrderApi.STATUS_ORDER_REFUNDED;
                break;
        }
        OrderApi.orderList(page, pageSize, orderState, callback);
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
    protected int getItemLayoutId() {
        return R.layout.listview_item_order;
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, final Order data, Bundle extra) {
        super.updateItemView(view, position, viewType, data, extra);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.app_order_state_restaurant_list);
        final TextView total = (TextView) view.findViewById(R.id.app_order_state_total);
        TextView action = (TextView) view.findViewById(R.id.app_order_state_action);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.onEventDishOrderListDetail();
                //跳转到详情页面
                Bundle bundle = new Bundle();
                bundle.putString(DishOrderDetailFragment.EXTRA_ORDER_ID, data.getOrderId());
                next(DishOrderDetailFragment.class, bundle);
            }
        });

        updateRestaurantList(layout, data);
        action.setVisibility(View.GONE);
        final double totalPayment = StringUtils.parseDouble(data.getRealPay());
        switch (data.getStatus()) {
            case OrderApi.STATUS_ORDER_WAIT_PAY:
                action.setVisibility(View.VISIBLE);
                if (data.getDelayPayTime() == 0) {
                    action.setEnabled(false);
                } else {
                    action.setEnabled(true);
                }
                action.setText("去付款 " + getDelayTime(data));
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //去付款
                        performPay(data, total.getText().toString().substring(1));
                    }
                });
                break;
            case OrderApi.STATUS_ORDER_CHECKED_WAIT_TRANSPORT:
                action.setVisibility(View.VISIBLE);
                action.setEnabled(true);
                action.setText("确定收货");
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //确定收货
                        performTransport(data);
                    }
                });
                break;
            case OrderApi.STATUS_ORDER_TRANSPORTED_WAIT_EVALUATE:
                action.setVisibility(View.VISIBLE);
                action.setEnabled(true);
                action.setText("给个评价");
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //给个评价
                        LogGather.onEventDishOrderListEvaluate();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(DishOrderEvaluateFragment.EXTRA_ORDER, data);
                        next(DishOrderEvaluateFragment.class, bundle);
                    }
                });
                break;
        }

        total.setText(StringUtils.getPriceText(totalPayment));
        updateDelayPayTime();
    }

    @Override
    public void showLoading() {
        super.showLoading();
        mCheckOutLoadingDialog.show();
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        mCheckOutLoadingDialog.dismiss();
    }

    /**
     * 确定收货
     *
     * @param data
     */
    private void performTransport(final Order data) {
        new ConfirmDialog(getContext(), "请确定您已经收到菜品")
                .setTitle("温馨提示")
                .setConfirm(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mCheckOutLoadingDialog.show();
                        OrderApi.orderReceived(data.getOrderId(), new ApiCallback<State>() {
                            @Override
                            public void onResult(State result) {
                                LogGather.onEventDishOrderListGot(result.getErrorMsg());
                                if (result.available()) {
                                    ToastAlarm.show("收货成功");
                                } else {
                                    ToastAlarm.show("收货失败");
                                }
                                updateList();
                            }
                        });
                    }
                }).show();

    }

    /**
     * 更新列表
     */
    private void updateList() {
        finishAllLoaded(false);
        Api.startNoCacheMode();
        onStartUILoad();
        Api.startNoCacheMode();
    }

    /**
     * 余额支付
     *
     * @param data
     * @param payCount 实际应付金额
     */
    private void performPay(final Order data, String payCount) {
        //使用余额支付
        Bundle bundle = new Bundle();
        bundle.putString(DishPayListFragment.EXTRA_ORDER_ID, data.getOrderId());
        bundle.putInt(DishOrderStateListFragment.EXTRA_TAB, mCurrentOrderState);
        bundle.putInt(DishPayListFragment.EXTRA_PAY_REDIRECT, DishPayListFragment.REDIRECT_ORDER_LIST);
        next(DishPayListFragment.class, bundle);
    }

    private String getDelayTime(Order data) {
        long minute = data.getDelayPayTime() / 60;
        long second = data.getDelayPayTime() % 60;
        return df.format(minute) + ":" + df.format(second);
    }

    private void performRedPacketShare(String orderId) {
        OrderApi.orderRedPacket(orderId, new ApiCallback<ShareState>() {
            @Override
            public void onResult(ShareState result) {
                if (result.available()) {

                    SpannableStringBuilder sb = new SpannableStringBuilder();
                    SpannableString string1 = new SpannableString("支付成功，获得★！");
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

                    ThirdPortDelivery.shareOnlyWeixin(getContext(), result, sb);
                }
            }
        });
    }

    private void updateDelayPayTime() {
        UIHandler.postOnceDelayed(updateRunnable, 1000);
    }

    private void updateRestaurantList(LinearLayout layout, Order order) {
        if (order != null) {
            ApiModelList<ShopCartRestaurant> restaurantList = order.getRestaurantList();
            if (!restaurantList.isEmpty()) {
                layout.removeAllViews();

                for (int i = 0; i < restaurantList.getCountOfList(); i++) {
                    ShopCartRestaurant restaurant = restaurantList.getItem(i);
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.view_order_state_restaurant, null);
                    view.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
                    layout.addView(view);
                    TextView restaurantName = (TextView) view.findViewById(R.id.app_order_state_restaurant_name);
                    TextView restaurantFlowName = (TextView) view.findViewById(R.id.app_order_state_restaurant_flow_name);
                    View imageViewTop = view.findViewById(R.id.app_order_state_restaurant_image_top);
                    View imageViewBottom = view.findViewById(R.id.app_order_state_restaurant_image_bottom);
                    ImageView imageView1 = (ImageView) view.findViewById(R.id.app_order_state_restaurant_image1);
                    ImageView imageView2 = (ImageView) view.findViewById(R.id.app_order_state_restaurant_image2);
                    ImageView imageView3 = (ImageView) view.findViewById(R.id.app_order_state_restaurant_image3);
                    ImageView imageView4 = (ImageView) view.findViewById(R.id.app_order_state_restaurant_image4);
                    ImageView simpleImageView = (ImageView) view.findViewById(R.id.app_order_state_restaurant_simple_image);
                    TextView restaurantDishes = (TextView) view.findViewById(R.id.app_order_state_restaurant_dishes);
                    TextView restaurantDishesCount = (TextView) view.findViewById(R.id.app_order_state_restaurant_dishes_count);

                    //更新餐厅状态和名字
                    restaurantName.setText(restaurant.getName());
                    if (i == 0) {
                        restaurantFlowName.setVisibility(View.VISIBLE);
                        restaurantFlowName.setText(order.getStatusString());
                    } else {
                        restaurantFlowName.setVisibility(View.GONE);
                    }

                    //更新菜品多图片
                    int dishCount = restaurant.getShopCartDishes().size();
                    if (dishCount == 1) {
                        simpleImageView.setVisibility(View.VISIBLE);
                        ShopCartDish dish = ListUtils.getItem(restaurant.getShopCartDishes(), 0);
                        if (dish != null) {
                            ImageFetcher.asInstance().load(dish.getImage(), simpleImageView);
                        }
                    } else {
                        simpleImageView.setVisibility(View.GONE);
                        ImageView targeView1 = null;
                        ImageView targeView2 = null;
                        ImageView targeView3 = null;
                        ImageView targeView4 = null;

                        if (dishCount == 2) {
                            imageViewBottom.setVisibility(View.GONE);
                            targeView1 = imageView1;
                            targeView2 = imageView2;
                        } else {
                            imageViewBottom.setVisibility(View.VISIBLE);
                            if (dishCount == 3) {
                                imageView1.setVisibility(View.VISIBLE);
                                imageView2.setVisibility(View.GONE);
                                imageView3.setVisibility(View.VISIBLE);
                                imageView4.setVisibility(View.VISIBLE);
                                targeView1 = imageView1;
                                targeView2 = imageView3;
                                targeView3 = imageView4;
                            } else if (dishCount >= 4) {
                                imageView1.setVisibility(View.VISIBLE);
                                imageView2.setVisibility(View.VISIBLE);
                                imageView3.setVisibility(View.VISIBLE);
                                imageView4.setVisibility(View.VISIBLE);
                                targeView1 = imageView1;
                                targeView2 = imageView2;
                                targeView3 = imageView3;
                                targeView4 = imageView4;
                            }

                        }
                        ShopCartDish dish = ListUtils.getItem(restaurant.getShopCartDishes(), 0);
                        if (dish != null && targeView1 != null) {
                            ImageFetcher.asInstance().load(dish.getImage(), targeView1);
                        }
                        dish = ListUtils.getItem(restaurant.getShopCartDishes(), 1);
                        if (dish != null && targeView2 != null) {
                            ImageFetcher.asInstance().load(dish.getImage(), targeView2);
                        }
                        dish = ListUtils.getItem(restaurant.getShopCartDishes(), 2);
                        if (dish != null && targeView3 != null) {
                            ImageFetcher.asInstance().load(dish.getImage(), targeView3);
                        }
                        dish = ListUtils.getItem(restaurant.getShopCartDishes(), 3);
                        if (dish != null && targeView4 != null) {
                            ImageFetcher.asInstance().load(dish.getImage(), targeView4);
                        }

                    }

                    //更新菜品名字和数量
                    String dishesName = "";
                    for (ShopCartDish dish : restaurant.getShopCartDishes()) {
                        dishesName += dish.getTitle() + ",";
                    }
                    if (!StringUtils.isEmpty(dishesName)) {
                        dishesName = dishesName.substring(0, dishesName.length() - 1);
                    }

                    restaurantDishes.setText(dishesName);

                    SpannableStringBuilder sb = new SpannableStringBuilder();
                    sb.append("共");
                    String countString = restaurant.getOrderDishCount() + "";
                    SpannableString countSpan = new SpannableString(countString);
                    countSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uikit_font_orange)),
                            0, countString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.append(countSpan);
                    sb.append("道菜品");
                    restaurantDishesCount.setText(sb);

                }


            }
        }


    }
}
