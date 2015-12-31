package cn.wecook.app.main.dish.shopcart;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
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
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.AddressApi;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.legacy.OrderApi;
import com.wecook.sdk.api.legacy.WalletApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Coupon;
import com.wecook.sdk.api.model.DeliveryRestaurant;
import com.wecook.sdk.api.model.DeliveryRestaurantList;
import com.wecook.sdk.api.model.DeliveryTime;
import com.wecook.sdk.api.model.Notice;
import com.wecook.sdk.api.model.PointAddress;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.base.DataModel;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseListFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.main.dish.address.DishAddressListFragment;
import cn.wecook.app.main.dish.address.DishPointAddressListFragment;
import cn.wecook.app.main.dish.order.DishOrderStateListFragment;
import cn.wecook.app.main.dish.order.DishPayListFragment;

/**
 * 订单确定界面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/23
 */
public class DishOrderCheckFragment extends BaseListFragment {

    private View mAddressTimeView;
    private View mAddressLayout;

    private View mPointAddressLayout;//自提点
    private TextView mPointAddressTitle;//自提点Title
    private TextView mPointAddressSubTitle;//自提点SubTitle

    private View mTimeLayout;
    private View mCouponView;
    private DishOrderCheckRestaurantsView mOrderCheckRestaurantsView;
    private MergeAdapter mAdapter;

    private TextView mOrderTotalPrice;
    private TextView mOrderCoupon;
    private View mOrderCheckOut;

    private View mUserAddressTopDiv;
    private View mUserAddressBottomDiv;
    private TextView mUserName;
    private TextView mUserPhone;
    private TextView mUserAddress;
    private LinearLayout mDeliveryTimeGroup;
    private TextView mDeliveryTimeType;//配送类型
    private TextView mDeliveryTime;//配送时间
    private ImageView mDeliveryTimeIcon;//配送类型右箭头

    private TextView mOrderNotice;

    private View mCouponGroup;
    private TextView mCouponPrice;
    private TextView mCouponDesc;

    private TextView mWalletSelector;
    private View mWalletLayout;

    private Coupon mCoupon;//使用的优惠券
    private List<Coupon> mAvailableCoupons;
    private Address mAddress;//收菜地址
    private PointAddress mPointAddress;//自提点
    private List<Address> mAddressList;
    private DeliveryRestaurantList mDeliveryRestaurants;
    private float mWalletRemainder;//菜金余额
    private boolean mIsUseWallet;//是否使用菜金

    private LoadingDialog mCheckOutLoadingDialog;

    private String mOrderId;
    private int mCheckPayStatusTime;
    private Runnable mCheckPayState = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null) {
                if (!StringUtils.isEmpty(mOrderId)) {
                    mCheckOutLoadingDialog.show();
                    OrderApi.checkPayStatus(mOrderId, new ApiCallback<State>() {
                        /**
                         * 结果回调
                         *
                         * @param result
                         */
                        @Override
                        public void onResult(State result) {
                            if (result.available() && result.getStatusState() == 1) {
                                mCheckOutLoadingDialog.dismiss();
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
                mCheckOutLoadingDialog.dismiss();
                payFailResult();
            }
        }).show();
    }

    private void payFailResult() {
        Bundle bundle = new Bundle();
        bundle.putInt(DishOrderStateListFragment.EXTRA_TAB, DishOrderStateListFragment.ORDER_STATE_PAYING);
        bundle.putBoolean(DishOrderStateListFragment.EXTRA_BACK_TO_EXIT, true);
        next(DishOrderStateListFragment.class, bundle);
    }

    private void paySuccessResult() {
        Bundle bundle = new Bundle();
        bundle.putString(DishOrderStateListFragment.EXTRA_RED_PACKET_ORDER_ID, mOrderId);
        bundle.putInt(DishOrderStateListFragment.EXTRA_TAB, DishOrderStateListFragment.ORDER_STATE_RECEIVING);
        bundle.putBoolean(DishOrderStateListFragment.EXTRA_BACK_TO_EXIT, true);
        next(DishOrderStateListFragment.class, bundle);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setFixed(true);
        mAdapter = new MergeAdapter();
        mCheckOutLoadingDialog = new LoadingDialog(getContext());
        mCheckOutLoadingDialog.cancelable(false);
        setTitle("确定订单");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAddressTimeView = inflater.inflate(R.layout.view_order_check_address_time, null);
        mCouponView = inflater.inflate(R.layout.view_order_check_coupon, null);
        mOrderCheckRestaurantsView = new DishOrderCheckRestaurantsView(this);
        return inflater.inflate(R.layout.fragment_order_check, null);
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        setFixed(true);
        if (getActivity() == null) {
            return;
        }

        if (data != null) {
            if (data.containsKey("update_delivery_data")) {
                boolean update_delivery_data = data.getBoolean("update_delivery_data");
                if (update_delivery_data) {
                    updateDeliveryTime(true);
                }
            }
            if (data.containsKey(DishAddressListFragment.EXTRA_ADDRESS)) {
                Address address = (Address) data.getSerializable(DishAddressListFragment.EXTRA_ADDRESS);
                //增加自提点处理逻辑
                if (null != address && null != address.getId() && null != mAddress && null != mAddress.getId()) {
                    if (!address.getId().equals(mAddress.getId())) {
                        mPointAddress = null;
                        SharePreferenceProperties.set(DishPointAddressListFragment.PARAM_DEFAULT_POINT_ADDRESS_ID, "null");
                    }
                }
                mAddress = address;
                updateAddress(true);
            }
            if (data.containsKey(DishPointAddressListFragment.PARAM_DEFAULT_POINT_ADDRESS)) {
                mPointAddress = (PointAddress) data.getSerializable(DishPointAddressListFragment.PARAM_DEFAULT_POINT_ADDRESS);
                updateAddress(true);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.addView(mAddressTimeView);
        mOrderCheckRestaurantsView.loadLayout(R.layout.view_order_check_restaurant_list,
                DishPolicy.get().getCheckoutRestaurantList());
        mAdapter.addView(mOrderCheckRestaurantsView);
        mAdapter.addView(mCouponView);

        mOrderTotalPrice = (TextView) view.findViewById(R.id.app_order_check_total);
        mOrderCoupon = (TextView) view.findViewById(R.id.app_order_check_coupon);
        mOrderCheckOut = view.findViewById(R.id.app_order_check_out);

        mOrderCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCheckOut();
            }
        });

        mCouponGroup = mCouponView.findViewById(R.id.app_order_check_coupon_group);
        mCouponPrice = (TextView) mCouponView.findViewById(R.id.app_order_check_coupon_price);
        mCouponDesc = (TextView) mCouponView.findViewById(R.id.app_order_check_coupon_desc);
        mCouponView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mAvailableCoupons != null && mAvailableCoupons.size() > 0) {
                //优惠券列表
                new DishOrderCouponListDialog(DishOrderCheckFragment.this)
                        .setCouponList(mAvailableCoupons)
                        .setOnItemClick(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                mCoupon = mAvailableCoupons.get(position);
                                DishPolicy.get().setUseCoupon(mCoupon);
                                updateCoupon();
                                updateCountBar();
                            }
                        })
                        .show();
//                } else {
//                    ToastAlarm.show("没有可用优惠券");
//                }
            }
        });

        mWalletLayout = mCouponView.findViewById(R.id.app_order_check_wallet_layout);
        mWalletSelector = (TextView) mCouponView.findViewById(R.id.app_order_check_wallet);
        mWalletLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsUseWallet = !mIsUseWallet;
                mWalletSelector.setSelected(mIsUseWallet);
                updateCountBar();
            }
        });

        mOrderNotice = (TextView) mAddressTimeView.findViewById(R.id.app_order_check_notice);

        mUserAddressTopDiv = mAddressTimeView.findViewById(R.id.app_order_check_address_group_top_div);
        mUserAddressBottomDiv = mAddressTimeView.findViewById(R.id.app_order_check_address_group_bottom_div);
        mUserName = (TextView) mAddressTimeView.findViewById(R.id.app_order_check_name);
        mUserPhone = (TextView) mAddressTimeView.findViewById(R.id.app_order_check_phone);
        mUserAddress = (TextView) mAddressTimeView.findViewById(R.id.app_order_check_address);
        mDeliveryTimeGroup = (LinearLayout) mAddressTimeView.findViewById(R.id.app_order_check_delivery_time_layout);
        mDeliveryTimeType = (TextView) mAddressTimeView.findViewById(R.id.app_order_check_delivery_time_type);
        mDeliveryTime = (TextView) mAddressTimeView.findViewById(R.id.app_order_check_delivery_time_tv);
        mDeliveryTimeIcon = (ImageView) mAddressTimeView.findViewById(R.id.app_order_check_delivery_time_icon);
        mAddressLayout = mAddressTimeView.findViewById(R.id.app_order_check_address_group);
        mPointAddressLayout = mAddressTimeView.findViewById(R.id.app_order_check_address_point_layout);
        mPointAddressTitle = (TextView) mAddressTimeView.findViewById(R.id.app_order_check_address_point_title);
        mPointAddressSubTitle = (TextView) mAddressTimeView.findViewById(R.id.app_order_check_address_point_subtitle);

        mTimeLayout = mAddressTimeView.findViewById(R.id.app_order_check_time_group);
        mAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddressLayout.setSelected(false);
                //地址选择
                Bundle bundle = new Bundle();
                bundle.putSerializable(DishAddressListFragment.EXTRA_ADDRESS, mAddress);
                bundle.putSerializable(DishAddressListFragment.EXTRA_AUTO_TO_EDIT, true);
                next(DishAddressListFragment.class, bundle);
            }
        });
        mTimeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderApi.getDeliveryTimeList(DishPolicy.get().getCheckoutRestaurantListIds(),
                        new ApiCallback<DeliveryRestaurantList>() {
                            @Override
                            public void onResult(DeliveryRestaurantList result) {
                                if (result.available()) {
                                    mDeliveryRestaurants = result;
                                }
                                updateDeliveryTime(true);
                            }

                        });
            }
        });

        updateAddress(false);
        updateCoupon();
        updateDeliveryTime(true);
    }

    /**
     * 支付
     */
    private void doCheckOut() {
        //支付结算
        if (checkOrderInfo()) {
            String couponId = "";
            if (mCoupon != null) {
                couponId = mCoupon.getId();
            }
            String logistics_store_id = null;
            if (null != mPointAddress && !StringUtils.isEmpty(mPointAddress.getId())) {
                logistics_store_id = mPointAddress.getId();
            }
            mCheckOutLoadingDialog.show();
            OrderApi.createOrder(mAddress.getId(), couponId, "" + getWalletConst(), logistics_store_id,
                    DishPolicy.get().getCheckoutRestaurantList(),
                    new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            LogGather.onEventDishOrderCheckDo(result.getErrorMsg());
                            mOrderId = result.getExtra().getString("order_id");
                            if (result.available()) {
                                setDefaultAddrestAndPoint();
                                //需要进行现金支付，跳转到支付列表
                                mCheckOutLoadingDialog.dismiss();
                                UIHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Bundle bundle = new Bundle();
                                        bundle.putString(DishPayListFragment.EXTRA_ORDER_ID, mOrderId);
                                        next(DishPayListFragment.class, bundle);
                                    }
                                }, 500);
                            } else {
                                mCheckOutLoadingDialog.dismiss();
                                int status = result.getStatusState();
                                if (status == OrderApi.ORDER_CHECK_STATUS_STOCK_EMPTY) {
                                    //库存不足
                                    new ConfirmDialog(getContext(), result.getErrorMsg())
                                            .setTitle("温馨提示")
                                            .setConfirm(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //跳转到购物车并更新购物车
                                                    back(new Bundle());
                                                }
                                            }).show();
                                } else if (status == OrderApi.ORDER_CHECK_STATUS_ZERO_PAY) {
                                    //优惠券满额或者菜金满额支付
                                    setDefaultAddrestAndPoint();
                                    //跳转到订单列表
                                    mCheckPayStatusTime = 0;
                                    UIHandler.post(mCheckPayState);
                                    DishPolicy.get().clearLocalCheckOut();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * 设置默认地址和自提点
     */
    private void setDefaultAddrestAndPoint() {
//        //默认地址
//        if (null != mAddress && null != mAddress.getId()) {
//            AddressApi.setDefault(mAddress.getId(), null);
//        }
        //自提点
        if (null != mPointAddress && null != mPointAddress.getId()) {
            SharePreferenceProperties.set(DishPointAddressListFragment.PARAM_DEFAULT_POINT_ADDRESS_ID, mPointAddress.getId());
        }
    }

    /**
     * 检查订单状态
     *
     * @return
     */
    private boolean checkOrderInfo() {
        //地址
        if (mAddress == null
                || StringUtils.isEmpty(mAddress.getId())) {
            ToastAlarm.show("收菜地址不能为空");
            mAddressLayout.setSelected(true);
            return false;
        }

        //配送时间
        if (DishPolicy.get().hasEmptyDeliveryTimes()) {
            ToastAlarm.show("配送时间不能为空");
            mTimeLayout.setSelected(true);
            return false;
        }

        return true;
    }

    private void updateCoupon() {
        if (mCoupon != null && mAvailableCoupons.size() > 1) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append("省");
            String priceText = StringUtils.getPriceText(mCoupon.getMoney());
            SpannableString price = new SpannableString(priceText);
            price.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uikit_font_orange)),
                    0, priceText.length(), 0);
            price.setSpan(new RelativeSizeSpan(1.3f), 0, priceText.length(), 0);
            sb.append(price);
            //2.5.2 文案这个不显示
//            mCouponPrice.setVisibility(View.VISIBLE);
            mCouponPrice.setText(sb);
            if (mCoupon.getDesc().equals("不使用优惠券")) {
                mCouponDesc.setText(mCoupon.getDesc());
            } else {
                mCouponDesc.setText("已使用" + StringUtils.getPriceText(mCoupon.getMoney()));
            }
            mOrderCoupon.setVisibility(View.VISIBLE);
            mOrderCoupon.setText("(已省：" + StringUtils.getPriceText(mCoupon.getMoney()) + ")");
            mCouponGroup.setVisibility(View.VISIBLE);
        } else {
            mCouponDesc.setText("无可用");
            mOrderCoupon.setVisibility(View.GONE);
//            mCouponGroup.setVisibility(View.GONE);
//            mCouponGroupDiv.setVisibility(View.GONE);
        }
    }

    private void updateCountBar() {
        mOrderTotalPrice.setText(StringUtils.getPriceText(getTotalPrice()));
    }

    /**
     * 设置时间
     *
     * @param setViewState 设置按钮状态
     */
    private void updateDeliveryTime(boolean setViewState) {
        boolean hide_subTitle = true;
        boolean hide_goto_icon = true;
        String title = "选择配送时间";
        String subTitle = "由商家选择合作快递为您配送";

        DishPolicy dishPolicy = DishPolicy.get();
        List<DeliveryTime> chosenTimes = dishPolicy.getDeliveryTimes();
        List<String> chosenTypes = dishPolicy.getDeliveryType();
        Map<String, List<DeliveryTime>> chosenTypeTimes = dishPolicy.getDeliveryTypeTimes();
        DeliveryRestaurantList restaurantList = mDeliveryRestaurants;

        if ((null != restaurantList && null != restaurantList.getDeliveryDateList() && null != restaurantList.getDeliveryDateList().getList()
                && restaurantList.getDeliveryDateList().getList().size() > 0) || (null != restaurantList && !restaurantList.isTogether())) {
            boolean isTogether = restaurantList.isTogether();
            //处理右箭头
            hide_goto_icon = isTogether && restaurantList.getDeliveryDateList().getList().size() == 1 && restaurantList.getDeliveryDateList().getList().get(0).getDeliveryTimes().size() == 1;

            //处理配送方式
            String typeString = null;
            if (setViewState) {
                if (hide_goto_icon) {
                    String type = mDeliveryRestaurants.getExpress_by();
                    if (type.equals(DeliveryRestaurant.EXPRESS_BY_RESTAURANT)) {
                        typeString = "商家配送";
                    } else if (type.equals(DeliveryRestaurant.EXPRESS_BY_WECOOK)) {
                        typeString = "味库配送";
                    }
                } else {
                    if (null != chosenTypes && chosenTypes.size() > 0) {
                        if (chosenTypes.size() == 1) {
                            String type = chosenTypes.get(chosenTypes.size() - 1);
                            if (type.equals(DeliveryRestaurant.EXPRESS_BY_RESTAURANT)) {
                                typeString = "商家配送";
                            } else if (type.equals(DeliveryRestaurant.EXPRESS_BY_WECOOK)) {
                                typeString = "味库配送";
                            }
                        } else {
                            typeString = "味库配送 + 商家配送";
                        }
                    } else {
                        typeString = "选择配送时间";
                    }
                }
            }
            if (null != typeString) {
                title = typeString;
                //处理配送时间
                if (null != chosenTimes) {
                    hide_subTitle = (!hide_goto_icon) && (chosenTimes.size() == 0);
                }
                if (!hide_subTitle) {
                    if (hide_goto_icon) {
                        try {
                            subTitle = restaurantList.getDeliveryDateList().getList().get(0).getDeliveryTimes().get(0).getFormatTime();
                        } catch (NullPointerException e) {

                        }
                    } else {
                        if (isTogether) {
                            subTitle = chosenTimes.get(chosenTimes.size() - 1).getFormatTime();
                        } else {
                            if (null != chosenTypes && null != chosenTypeTimes) {
                                if (chosenTypes.size() == 1) {
                                    chosenTimes = chosenTypeTimes.get(chosenTypes.get(0));
                                    if (null != chosenTimes) {
                                        if (chosenTimes.size() == 1) {
                                            subTitle = chosenTimes.get(chosenTimes.size() - 1).getFormatTime();
                                        } else if (chosenTimes.size() > 1) {
                                            subTitle = chosenTimes.get(chosenTimes.size() - 1).getFormatTime() + "等多个时间";
                                        }
                                    }
                                } else if (chosenTypes.size() > 1) {
                                    chosenTimes = chosenTypeTimes.get(DeliveryRestaurant.EXPRESS_BY_WECOOK);
                                    if (null != chosenTimes) {
                                        if (null != chosenTypeTimes) {
                                            List<DeliveryTime> wecookTimes = chosenTypeTimes.get(DeliveryRestaurant.EXPRESS_BY_WECOOK);
                                            if (null != wecookTimes && wecookTimes.size() > 0) {
                                                DeliveryTime wecookFirstTime = wecookTimes.get(wecookTimes.size() - 1);
                                                if (chosenTimes.size() > 1) {
                                                    subTitle = "味库配送时间: " + wecookFirstTime.getFormatTime() + "等多个时间";
                                                } else if (chosenTimes.size() == 1) {
                                                    subTitle = "味库配送时间: " + wecookFirstTime.getFormatTime();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        mDeliveryTimeType.setText(title);
        mDeliveryTime.setText(subTitle);
        mDeliveryTimeIcon.setVisibility(hide_goto_icon ? View.GONE : View.VISIBLE);
        mDeliveryTime.setVisibility(hide_subTitle ? View.GONE : View.VISIBLE);
        if (!hide_goto_icon && null != mTimeLayout && mTimeLayout.getVisibility() == View.VISIBLE) {
            mTimeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTimeLayout.setSelected(false);
                    //配送时间选择
                    if (mDeliveryRestaurants != null) {
                        if (mDeliveryRestaurants.isTogether()) {
                            //显示配送时间对话框
                            DishOrderDeliveryTimeDialog dialog;
                            if (Build.BRAND.equals("vivo")) {
                                dialog = new DishOrderDeliveryTimeDialog(getContext(), true);
                            } else {
                                dialog = new DishOrderDeliveryTimeDialog(getContext());
                            }
                            dialog
                                    .setDeliveryTimes(mDeliveryRestaurants.getDeliveryDateList().getList())
                                    .setDeliveryType(mDeliveryRestaurants.getExpress_by())
                                    .setOnDoneClick(new DishOrderDeliveryTimeDialog.OnDoneClickListener() {
                                        @Override
                                        public void onClick(DeliveryTime time, String deliveryType) {
                                            List<ShopCartRestaurant> restaurants = DishPolicy.get().getCheckoutRestaurantList();
                                            for (ShopCartRestaurant restaurant : restaurants) {
                                                restaurant.setDeliveryTime(time);
                                            }
                                            DishPolicy.get().getDeliveryTimes().clear();
                                            DishPolicy.get().addDeliveryTime(time, deliveryType);
                                            updateDeliveryTime(true);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            ToastAlarm.show(e.getMessage());
                                            e.printStackTrace();
                                        }
                                    })
//                                    .setOnItemClick(new DishOrderDeliveryTimeDialog.OnDeliveryTimeSelectListener() {
//                                        @Override
//                                        public void onSelect(DeliveryTime time, String type) {
//                                        }
//                                    })
                                    .show();
                        } else {
                            // 跳转到配送时间列表
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(DishOrderMultiDeliveryDateFragment.EXTRA_DELIVERY_DATE,
                                    mDeliveryRestaurants);
                            next(DishOrderMultiDeliveryDateFragment.class, bundle);
                        }
                    }
                }
            });
        } else {
            List<ShopCartRestaurant> restaurants = DishPolicy.get().getCheckoutRestaurantList();
            for (ShopCartRestaurant restaurant : restaurants) {
                if (null != mDeliveryRestaurants) {
                    try {
                        restaurant.setDeliveryTime(mDeliveryRestaurants.getDeliveryDateList().getList().get(0).getDeliveryTimes().get(0));
                    } catch (NullPointerException e) {
                    }

                }
            }
            mTimeLayout.setOnClickListener(null);
        }

    }

    /**
     * 更新地址
     *
     * @param requestNearbyStore 是否请求自提点·
     */
    private void updateAddress(boolean requestNearbyStore) {
        if (mAddress != null && mAddress.isAvailable()) {
            mUserName.setText(mAddress.getName());
            mUserPhone.setText(mAddress.getTel());
            mUserAddressTopDiv.setBackgroundResource(R.drawable.app_bg_order_div);
            mUserAddressBottomDiv.setBackgroundResource(R.drawable.app_bg_order_div);
            ScreenUtils.reMargin(mUserAddressBottomDiv, 0, 0, 0, ScreenUtils.dip2px(0));
            ScreenUtils.reMargin(mUserAddressTopDiv, 0, 0, 0, 0);
            ScreenUtils.resizeViewOfHeight(mUserAddressTopDiv, ScreenUtils.dip2px(4));
            ScreenUtils.resizeViewOfHeight(mUserAddressBottomDiv, ScreenUtils.dip2px(4));
            String address = mAddress.getFullAddress();
            mUserAddress.setTextColor(getResources().getColor(R.color.uikit_font_grep));
            mUserAddress.setText(address);
            mUserPhone.setVisibility(View.VISIBLE);
            mUserAddress.setVisibility(View.VISIBLE);
            if (requestNearbyStore) {
                if (null != mAddress.getId()) {
                    requestNearbyStoresList(mAddress.getId());
                } else {
                    requestNearbyStoresList(null);
                }
            }
        } else {
            mUserName.setText("");
            mUserAddressTopDiv.setBackgroundColor(getResources().getColor(R.color.uikit_grey_light));
            mUserAddressBottomDiv.setBackgroundColor(getResources().getColor(R.color.uikit_grey_light));
            ScreenUtils.resizeViewOfHeight(mUserAddressTopDiv, ScreenUtils.dip2px(1));
            ScreenUtils.resizeViewOfHeight(mUserAddressBottomDiv, ScreenUtils.dip2px(1));
            mUserPhone.setVisibility(View.GONE);
            mUserAddress.setVisibility(View.GONE);
            if (requestNearbyStore) {
                requestNearbyStoresList(null);
            }
        }
    }

    /**
     * 获得显示在底部视图的总价格
     *
     * @return
     */

    private float getTotalPrice() {
        float totalPrice = DishPolicy.get().getCheckoutTotalPriceWithDelivery();
        if (mCoupon != null) {
            totalPrice -= mCoupon.getMoney();
        }
        if (mIsUseWallet) {
            totalPrice -= mWalletRemainder;
        }

        if (totalPrice < 0) {
            totalPrice = 0;
        }
        return totalPrice;
    }

    /**
     * 菜金花费
     *
     * @return
     */
    private float getWalletConst() {
        float totalPrice = DishPolicy.get().getCheckoutTotalPriceWithDelivery();
        if (mCoupon != null) {
            totalPrice -= mCoupon.getMoney();
        }

        if (mIsUseWallet) {
            if (totalPrice < mWalletRemainder) {
                return totalPrice;
            } else {
                return mWalletRemainder;
            }
        }
        return 0;
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        getListView().setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        if (UserProperties.isLogin()) {
            showLoading();
            //1.请求收菜地址
            AddressApi.getAddressList(1, 10, new ApiCallback<ApiModelList<Address>>() {
                @Override
                public void onResult(ApiModelList<Address> result) {
                    if (result.available()) {
                        mAddressList = new ArrayList<Address>();
                        mAddressList.addAll(result.getList());
                        for (Address address : mAddressList) {
                            if (address.isDefault()) {
                                mAddress = address;
                                break;
                            }
                        }
                        if (mAddress == null) {
                            mAddress = mAddressList.get(0);
                        }
                        mAddress.setSelected(true);
                    }

                    //检查第一个地址是否可用
                    if (mAddress != null) {
                        AddressApi.checkAddressRange(mAddress.getId(), DishPolicy.get().getCheckoutRestaurantListIds(),
                                new ApiCallback<State>() {
                                    @Override
                                    public void onResult(State result) {
                                        //不可用的地址
                                        if (!result.available()) {
                                            mAddress.setSelected(false);
                                            mAddress.setAvailable(false);
                                            mAddress = null;
                                        } else {
                                            mAddress.setAvailable(true);
                                        }
                                        updateAddress(true);
                                        hideLoading();
                                    }
                                });
                    } else {
                        hideLoading();
                    }
                }
            });

            //2.请求可用的优惠券列表
            float total = DishPolicy.get().getCheckoutTotalPriceWithDelivery();
            WalletApi.getAvailableCouponList(1, 20, total + "", new ApiCallback<ApiModelList<Coupon>>() {
                @Override
                public void onResult(ApiModelList<Coupon> result) {
                    mAvailableCoupons = new ArrayList<Coupon>();
                    Coupon nullCoupon = new Coupon();
                    nullCoupon.setDesc("不使用优惠券");
                    nullCoupon.setMoney("0");
                    mAvailableCoupons.add(nullCoupon);
                    if (result.available()) {
                        mAvailableCoupons.addAll(result.getList());
                        mCoupon = nullCoupon;
                        DishPolicy.get().setUseCoupon(nullCoupon);

                        boolean isSelected = false;//是否已经有选中的了
                        for (int i = 0; i < mAvailableCoupons.size(); i++) {
                            Coupon coupon = mAvailableCoupons.get(i);
                            if (coupon.isSelected()) {
                                isSelected = true;
                                break;
                            }
                        }

                        if (!isSelected && mAvailableCoupons.size() > 0) {
                            for (int i = 0; i < mAvailableCoupons.size(); i++) {
                                Coupon coupon = mAvailableCoupons.get(i);
                                if (coupon.getMoney() > 0) {
                                    mCoupon = coupon;
                                    if (null != mCoupon) {
                                        mCoupon.setSelected(true);
                                        break;
                                    }
                                }
                            }
                        }
//                        float totalPrice = DishPolicy.get().getCheckoutTotalPriceWithDelivery();
//                        float leastDiff = Long.MAX_VALUE;//最小差距
//                        for (Coupon coupon : mAvailableCoupons) {
//                            float diff = totalPrice - coupon.getMoney();
//                            if (diff > 0 && leastDiff > diff) {
//                                leastDiff = diff;
//                                mCoupon = coupon;
//                                DishPolicy.get().setUseCoupon(coupon);
//                            }
//                        }
//                        if (mCoupon != null) {
//                            mCoupon.setSelected(true);
//                        }
                        updateCoupon();
                    }
                    updateCountBar();
                }
            });

            //3.获得配送时间
            OrderApi.getDeliveryTimeList(DishPolicy.get().getCheckoutRestaurantListIds(),
                    new ApiCallback<DeliveryRestaurantList>() {
                        @Override
                        public void onResult(DeliveryRestaurantList result) {
                            if (result.available()) {
                                mDeliveryRestaurants = result;
                            }
                            updateDeliveryTime(true);
                        }

                    });

            //4.获得钱包余额
            WalletApi.getWalletRemainder(new ApiCallback<DataModel>() {
                @Override
                public void onResult(DataModel result) {
                    if (result.available()) {
                        mWalletRemainder = StringUtils.parseDouble(result.getExtra().getString("money"));
                    }

                    if (mWalletRemainder > 0) {
                        mWalletLayout.setVisibility(View.VISIBLE);
                        mWalletSelector.setText("菜金余额(" + StringUtils.getPriceText(mWalletRemainder).replace(" ", "") + ")");
                        updateCountBar();
                    } else {
                        mWalletLayout.setVisibility(View.GONE);
                    }
                }
            });

            //5.请求公告
            DishApi.getDishNotice(DishApi.NOTICE_TYPE_SYS, "0", new ApiCallback<Notice>() {
                @Override
                public void onResult(Notice result) {
                    if (result.available()) {
                        String noticeDesc = result.getDesc();
                        if (mOrderNotice != null) {
                            if (!StringUtils.isEmpty(noticeDesc)) {
                                mOrderNotice.setVisibility(View.VISIBLE);
                                mOrderNotice.setText("温馨提示：\n" + noticeDesc);
                            } else {
                                mOrderNotice.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
        }

    }

    /**
     * 请求附近自提点列表
     */
    private void requestNearbyStoresList(final String address_id) {
        if (StringUtils.isEmpty(address_id)) {
            mPointAddressSubTitle.setVisibility(View.GONE);
            mPointAddressLayout.setVisibility(View.GONE);
            mPointAddressTitle.setVisibility(View.GONE);
            SharePreferenceProperties.set(DishPointAddressListFragment.PARAM_DEFAULT_POINT_ADDRESS_ID, "null");
            mPointAddress = null;
            return;
        }
        if (null != mPointAddress) {
            onHasDefaultPointAddress(address_id);
        } else {
            //请求自提点列表
            AddressApi.getNearbyStroresList(address_id, new ApiCallback<ApiModelList<PointAddress>>() {
                @Override
                public void onResult(ApiModelList<PointAddress> result) {
                    if (result.available() && null != result.getList() && result.getList().size() > 1) {
                        String default_point_address_id = SharePreferenceProperties.get(DishPointAddressListFragment.PARAM_DEFAULT_POINT_ADDRESS_ID, "null");
                        if (default_point_address_id.equals("null")) {
                            //没有默认自提点
                            onNoDefaultPointAddress(address_id);
                        } else {
                            for (PointAddress pointAddress : result.getList()) {
                                if (pointAddress.getId().equals(default_point_address_id)) {
                                    mPointAddress = pointAddress;
                                    break;
                                }
                            }
                            if (null != mPointAddress) {
                                onHasDefaultPointAddress(address_id);
                            } else {
                                onNoDefaultPointAddress(address_id);
                            }
                        }
                    } else {
                        mPointAddressLayout.setVisibility(View.GONE);
                        mPointAddressTitle.setVisibility(View.GONE);
                        mPointAddressSubTitle.setVisibility(View.GONE);
                    }
                }
            });
        }

    }

    /**
     * 没有默认自提点
     */
    private void onNoDefaultPointAddress(final String address_id) {
        mPointAddressTitle.setTextColor(getResources().getColor(R.color.uikit_orange));
        mPointAddressTitle.setTextSize(14);
        mPointAddressTitle.setText("收货不便时,可选择自提点提货");
        mPointAddressSubTitle.setVisibility(View.GONE);
        mPointAddressLayout.setVisibility(View.VISIBLE);
        mPointAddressTitle.setVisibility(View.VISIBLE);
        mPointAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(DishPointAddressListFragment.PARAMTS_ADDRESS_ID, address_id);
                next(DishPointAddressListFragment.class, bundle);
            }
        });
    }

    /**
     * 设置默认自提点
     *
     * @param address_id
     */
    private void onHasDefaultPointAddress(final String address_id) {
        //选择了自提点
        //Title
        String title = mPointAddress.getFullName();
        mPointAddressTitle.setTextColor(getResources().getColor(R.color.uikit_333));
        mPointAddressTitle.setTextSize(16);
        mPointAddressTitle.setText(title);
        //SubTitle
        String subTitle = mPointAddress.getStreet();
        if (!StringUtils.isEmpty(mPointAddress.getTel())) {
            subTitle += ("  " + mPointAddress.getTel());
        }
        mPointAddressSubTitle.setText(subTitle);
        mPointAddressSubTitle.setVisibility(View.VISIBLE);
        mPointAddressLayout.setVisibility(View.VISIBLE);
        mPointAddressTitle.setVisibility(View.VISIBLE);
        //地址的SubTitle
        if (null != mUserAddress) {
            mUserAddress.setTextColor(getResources().getColor(R.color.uikit_ccc));
            mUserAddress.setText("已选自提点,凭手机号/订单号可取菜");
        }
        mPointAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(DishPointAddressListFragment.PARAMTS_ADDRESS_ID, address_id);
                bundle.putSerializable(DishPointAddressListFragment.PARAM_DEFAULT_POINT_ADDRESS, mPointAddress);
                next(DishPointAddressListFragment.class, bundle);
            }
        });
    }

    @Override
    public boolean back(Bundle data) {
        setFixed(false);
        return super.back(data);
    }
}
