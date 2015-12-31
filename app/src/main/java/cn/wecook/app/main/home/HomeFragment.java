package cn.wecook.app.main.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.OrderCount;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.policy.MessageQueuePolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.shape.CircleImageView;

import cn.wecook.app.R;
import cn.wecook.app.features.preferential.PreferentialFragment;
import cn.wecook.app.main.MainFragment;
import cn.wecook.app.main.dish.DishActivity;
import cn.wecook.app.main.dish.order.DishOrderStateListFragment;
import cn.wecook.app.main.home.favorite.FavoriteListFragment;
import cn.wecook.app.main.home.message.MessageFragment;
import cn.wecook.app.main.home.recipe.MyRecipeFragment;
import cn.wecook.app.main.home.setting.SettingFragment;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.home.user.UserPageFragment;
import cn.wecook.app.main.home.user.UserProfileFragment;
import cn.wecook.app.main.home.wallet.WalletFragment;
import cn.wecook.app.main.recommend.list.cookshow.CookShowOfUserListFragment;

/**
 * “我的”界面
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class HomeFragment extends BaseTitleFragment {
    /**
     * 快速登录
     */
    public static final int DIALOG_RAPID = 0;

    /**
     * 用户登录
     */
    public static final int DIALOG_ACCOUNT = 1;

    /**
     * 注册
     */
    public static final int DIALOG_REGISTER = 2;

    /**
     * 待付款
     */
    public TextView mObligationsCount;
    /**
     * 待发货
     */
    public TextView mNoDeliverCount;
    /**
     * 待评价
     */
    public TextView mNoEvaluateCount;
    /**
     * 已退款
     */
    public TextView mRefundedCount;

    private final String BACK_PHONE = "4006517917";
    private TitleBar mTitleBar;
    private View mRootView;
    private TextView mNewMessageMark;

    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UserProperties.INTENT_LOGIN.equals(action)
                    || UserProperties.INTENT_LOGOUT.equals(action)) {
                MessageQueuePolicy.getInstance().clearMessageList();
                if (mRootView != null) {
                    updateUserLoginState(mRootView);
                    updateNewMark();
                }
            } else if (MessageQueuePolicy.ACTION_NEW_MESSAGE.equals(action)) {
                updateNewMark();
            } else if (MessageQueuePolicy.ACTION_ORDER_NEW_MESSAGE.equals(action)) {
                updateNewMark();
            } else if (UserProfileFragment.INTENT_UPD_USERINFO.equals(action)) {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUserLoginState(mRootView);
                        updateNewMark();
                    }
                });
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserProperties.INTENT_LOGIN);
        filter.addAction(UserProperties.INTENT_LOGOUT);
        filter.addAction(MessageQueuePolicy.ACTION_NEW_MESSAGE);
        filter.addAction(MessageQueuePolicy.ACTION_ORDER_NEW_MESSAGE);
        filter.addAction(UserProfileFragment.INTENT_UPD_USERINFO);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUserReceiver, filter);
        return mRootView = inflater.inflate(R.layout.fragment_home, null);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (!StringUtils.isEmpty(UserProperties.getUserId())) {
            UserApi.getInfo(UserProperties.getUserId(), new ApiCallback<User>() {
                @Override
                public void onResult(User result) {
                    if (result.available()) {
                        UserProperties.login(result);
                    }
                    onCardIn(null);
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTitleBar = getTitleBar();
        mTitleBar.setTitle(getResources().getString(R.string.app_navigation_my));

        mTitleBar.enableBack(false);
        mTitleBar.enableBottomDiv(true);
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.uikit_white));
        mTitleBar.setBottomDivLineColor(getResources().getColor(R.color.uikit_orange));

        //设置的初始化和监听
        TitleBar.ActionView settingView = new TitleBar.ActionCoveredImageView(getContext(), R.drawable.app_bt_setting);
        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next(SettingFragment.class);
            }
        });
        mTitleBar.addActionView(settingView);

        updateUserLoginState(view);

        initSquareItem(view);
        initItem(view);
        initOrder(view);

        updateNewMark();
    }

    private void initOrder(View view) {
        String[] itemNames = getResources().getStringArray(R.array.app_home_items_order_name);
        final int[] itemIds = {
                R.id.app_my_item_order_obligation,
                R.id.app_my_item_order_no_deliver,
                R.id.app_my_item_order_no_evaluate,
                R.id.app_my_item_order_refund,
        };

        int[] itemDrawables = {
                R.drawable.app_ic_home_obligation,
                R.drawable.app_ic_home_no_deliver,
                R.drawable.app_ic_home_no_evaluate,
                R.drawable.app_ic_home_refund,
        };

        //适配每一个item  初始化和监听
        for (int i = 0; i < itemIds.length; i++) {
            View layout = view.findViewById(itemIds[i]);
            TextView name = (TextView) layout.findViewById(R.id.app_my_feature_order_name);
            ImageView icon = (ImageView) layout.findViewById(R.id.app_my_feature_order_icon);
            switch (itemIds[i]) {
                case R.id.app_my_item_order_obligation:
                    mObligationsCount = (TextView) layout.findViewById(R.id.app_my_feature_order_mark);
                    break;
                case R.id.app_my_item_order_no_deliver:
                    mNoDeliverCount = (TextView) layout.findViewById(R.id.app_my_feature_order_mark);
                    break;
                case R.id.app_my_item_order_no_evaluate:
                    mNoEvaluateCount = (TextView) layout.findViewById(R.id.app_my_feature_order_mark);
                    break;
                case R.id.app_my_item_order_refund:
                    mRefundedCount = (TextView) layout.findViewById(R.id.app_my_feature_order_mark);
                    break;
            }
            icon.setImageResource(itemDrawables[i]);
            name.setText(itemNames[i]);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performOrderItemClick(v);
                }
            });
        }
    }

    private void performOrderItemClick(View v) {
        if (!UserProperties.isLogin()) {
            startActivity(new Intent(getContext(), UserLoginActivity.class));
            return;
        }
        int orderState = DishOrderStateListFragment.ORDER_STATE_ALL;
        switch (v.getId()) {
            case R.id.app_my_item_order_obligation://待付款
                orderState = DishOrderStateListFragment.ORDER_STATE_PAYING;
                LogGather.onEventMyOrderObligation();
                break;
            case R.id.app_my_item_order_no_deliver://待发货
                LogGather.onEventMyOrderNoDeliver();
                orderState = DishOrderStateListFragment.ORDER_STATE_RECEIVING;
                break;
            case R.id.app_my_item_order_no_evaluate://待评价
                LogGather.onEventMyOrderNoEvaluate();
                orderState = DishOrderStateListFragment.ORDER_STATE_EVALUATING;
                break;
            case R.id.app_my_item_order_refund://退款
                LogGather.onEventMyOrderRefund();
                orderState = DishOrderStateListFragment.ORDER_STATE_REFUNDING;
                break;
        }
        LogGather.onEventMyOrder();
        Intent intent = new Intent(getContext(), DishActivity.class);
        intent.putExtra(DishOrderStateListFragment.EXTRA_TAB, orderState);
        intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_ORDER_LIST);
        startActivity(intent);
    }

    private void initItem(View view) {
        String[] itemNames = getResources().getStringArray(R.array.app_home_items_name);
        int[] itemDrawables = {
                R.drawable.app_ic_home_wallet,
//                R.drawable.app_ic_home_coupon,
                R.drawable.app_ic_home_gain_coupon,
                R.drawable.app_ic_home_order_all,
                R.drawable.app_ic_home_call
        };

        int[] itemIds = {
                R.id.app_my_item_wallet,
//                R.id.app_my_item_coupon,
                R.id.app_my_item_gain_coupon,
                R.id.app_my_item_order,
                R.id.app_my_item_call
        };
        //适配每一个item  初始化和监听
        for (int i = 0; i < itemIds.length; i++) {
            View layout = view.findViewById(itemIds[i]);
            ImageView icon = (ImageView) layout.findViewById(R.id.app_my_feature_icon);
            TextView name = (TextView) layout.findViewById(R.id.app_my_feature_name);

            if (itemIds[i] == R.id.app_my_item_wallet) {
                TextView mTextView = (TextView) layout.findViewById(R.id.app_my_feature_sub_name);
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText("菜金、优惠券");
            }

            if (itemIds[i] == R.id.app_my_item_order) {
                TextView mTextView = (TextView) layout.findViewById(R.id.app_my_feature_sub_name);
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText("查看全部订单");
            }

            if (itemIds[i] == R.id.app_my_item_call) {
                TextView mTextView = (TextView) layout.findViewById(R.id.app_my_feature_sub_name);
                mTextView.setVisibility(View.VISIBLE);
                try {
                    String phoneNum = BACK_PHONE.substring(0, 4) + "-" + BACK_PHONE.substring(4, 7) + "-" + BACK_PHONE.substring(7);
                    mTextView.setText(phoneNum);
                } catch (Exception e) {
                    mTextView.setText(BACK_PHONE);
                }
            }

            icon.setImageResource(itemDrawables[i]);
            icon.setVisibility(View.VISIBLE);
            name.setText(itemNames[i]);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performItemClick(v);
                }
            });
        }
    }

    private void initSquareItem(View view) {
        String[] itemSquareNames = getResources().getStringArray(R.array.app_home_items_square_name);
        int[] itemSquareDrawables = {
                R.drawable.app_ic_label_message,
                R.drawable.app_ic_label_fav,
                R.drawable.app_ic_label_upload_pic,
                R.drawable.app_ic_label_recipe,
        };

        int[] itemSquareIds = {
                R.id.app_my_item_message,
                R.id.app_my_item_favorite,
                R.id.app_my_item_show,
                R.id.app_my_item_recipe,
        };

        //适配每一个正方形item  初始化和监听
        for (int i = 0; i < itemSquareIds.length; i++) {
            View layout = view.findViewById(itemSquareIds[i]);
            ImageView icon = (ImageView) layout.findViewById(R.id.app_my_feature_square_icon);
            TextView name = (TextView) layout.findViewById(R.id.app_my_feature_square_name);
            if (itemSquareIds[i] == R.id.app_my_item_message) {
                mNewMessageMark = (TextView) layout.findViewById(R.id.app_my_feature_square_mark);
            }
            icon.setImageResource(itemSquareDrawables[i]);
            name.setText(itemSquareNames[i]);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performItemClick(v);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUserReceiver);
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                updateUserLoginState(mRootView);
                updateNewMark();
            }
        });
    }

    private void updateNewMark() {
        if (mNewMessageMark != null) {
            if (MessageQueuePolicy.getInstance().hasNewMessage()) {
                mNewMessageMark.setVisibility(View.VISIBLE);
                if (MessageQueuePolicy.getInstance().getNewMessageCount() > 99) {
                    mNewMessageMark.setText("99+");
                } else {
                    mNewMessageMark.setText(MessageQueuePolicy.getInstance().getNewMessageCount() + "");
                }
            } else {
                mNewMessageMark.setVisibility(View.GONE);
            }
        }

        if (mRefundedCount != null
                && mNoEvaluateCount != null
                && mNoDeliverCount != null
                && mObligationsCount != null) {
            OrderCount orderCount = MessageQueuePolicy.getInstance().getOrderCountMessage();
            if (orderCount != null) {
                if (orderCount.mObligationsCount > 0) {
                    mObligationsCount.setVisibility(View.VISIBLE);
                    mObligationsCount.setText(orderCount.mObligationsCount > 99 ? "99+" : orderCount.mObligationsCount + "");
                } else {
                    mObligationsCount.setVisibility(View.INVISIBLE);
                }

                if (orderCount.mRefundedCount > 0) {
                    mRefundedCount.setVisibility(View.VISIBLE);
                    mRefundedCount.setText(orderCount.mRefundedCount > 99 ? "99+" : orderCount.mRefundedCount + "");
                } else {
                    mRefundedCount.setVisibility(View.INVISIBLE);
                }


                if (orderCount.mNoEvaluateCount > 0) {
                    mNoEvaluateCount.setVisibility(View.VISIBLE);
                    mNoEvaluateCount.setText(orderCount.mNoEvaluateCount > 99 ? "99+" : orderCount.mNoEvaluateCount + "");
                } else {
                    mNoEvaluateCount.setVisibility(View.INVISIBLE);
                }

                int waitTransport = orderCount.mNoDeliverCount + orderCount.mShippingCount;
                if (waitTransport > 0) {
                    mNoDeliverCount.setVisibility(View.VISIBLE);
                    mNoDeliverCount.setText(waitTransport > 99 ? "99+" : waitTransport + "");
                } else {
                    mNoDeliverCount.setVisibility(View.INVISIBLE);
                }
            } else {
                mObligationsCount.setVisibility(View.INVISIBLE);
                mRefundedCount.setVisibility(View.INVISIBLE);
                mNoEvaluateCount.setVisibility(View.INVISIBLE);
                mNoDeliverCount.setVisibility(View.INVISIBLE);
            }
        }

        Fragment fragment = getParentFragment();
        if (fragment != null && fragment instanceof MainFragment) {
            MainFragment main = (MainFragment) fragment;
            main.updateNewMark();
        }
    }

    private void performItemClick(View v) {
        if (v.getId() != R.id.app_my_item_call && !UserProperties.isLogin()) {
            startActivity(new Intent(getContext(), UserLoginActivity.class));
            return;
        }

        switch (v.getId()) {

            case R.id.app_my_item_call://电话
                LogGather.onEventMyCall();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + BACK_PHONE));
                startActivity(intent);
                break;
            case R.id.app_my_item_message://我的消息
                LogGather.onEventMyMessage();
                MessageQueuePolicy.getInstance().clearMessageList();
                next(MessageFragment.class);
                break;
            case R.id.app_my_item_favorite://收藏
                LogGather.onEventMyFav();
                next(FavoriteListFragment.class);
                break;
            case R.id.app_my_item_show://晒厨艺
                LogGather.onEventMyCookShow();
                next(CookShowOfUserListFragment.class);
                break;
            case R.id.app_my_item_recipe://我的菜谱
                LogGather.onEventMyRecipe();
                next(MyRecipeFragment.class);
                break;
            case R.id.app_my_item_wallet://我的钱包
                LogGather.onEventMyWallet();
//                Bundle walletBundle = new Bundle();
//                walletBundle.putBoolean(WebViewFragment.EXTRA_FIXED_VIEW, true);
//                if (WecookConfig.getInstance().isTest()) {
//                    walletBundle.putString(WebViewFragment.EXTRA_URL, "http://m.maicaibangshou.cn/dishes/coupon?uid="
//                            + UserProperties.getUserId() + "&wid=" + PhoneProperties.getDeviceId());
//                } else {
//                    walletBundle.putString(WebViewFragment.EXTRA_URL, "http://cai.wecook.cn/dishes/coupon?uid="
//                            + UserProperties.getUserId() + "&wid=" + PhoneProperties.getDeviceId());
//                }
//                next(WebViewFragment.class, walletBundle);
                next(WalletFragment.class);
                break;
            case R.id.app_my_item_gain_coupon://获得优惠
                LogGather.onEventMyGainCoupon();

//                Bundle gainCouponBundle = new Bundle();
//                gainCouponBundle.putBoolean(WebViewFragment.EXTRA_FIXED_VIEW, true);
//                gainCouponBundle.putString(WebViewFragment.EXTRA_URL,
//                        WecookConfig.getInstance().getCouponUrlAddress());
//                next(WebViewFragment.class, gainCouponBundle);
                next(PreferentialFragment.class);
                break;
//            case R.id.app_my_item_coupon://优惠码
//            {
//                LogGather.onEventMyCoupon();
//                Bundle couponBundle = new Bundle();
//                couponBundle.putBoolean(WebViewFragment.EXTRA_FIXED_VIEW, true);
//                if (WecookConfig.getInstance().isTest()) {
//                    couponBundle.putString(WebViewFragment.EXTRA_URL, "http://m.maicaibangshou.cn/dishes/myCoupon4Friends?uid="
//                            + UserProperties.getUserId() + "&wid=" + PhoneProperties.getDeviceId());
//                } else {
//                    couponBundle.putString(WebViewFragment.EXTRA_URL, "http://cai.wecook.cn/dishes/myCoupon4Friends?uid="
//                            + UserProperties.getUserId() + "&wid=" + PhoneProperties.getDeviceId());
//                }
//                next(WebViewFragment.class, couponBundle);
//                break;
//            }
            case R.id.app_my_item_order://订单
            {
                LogGather.onEventMyOrder();
                Intent orderIntent = new Intent(getContext(), DishActivity.class);
                orderIntent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_ORDER_LIST);
                startActivity(orderIntent);
                break;
            }
        }
    }

    private void updateUserLoginState(View view) {
        if (view == null) {
            return;
        }
        if (UserProperties.isLogin()) {
            View login = view.findViewById(R.id.app_my_layout_login);
            login.setVisibility(View.VISIBLE);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogGather.onEventMyInfo();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(UserPageFragment.EXTRA_USER, UserProperties.getUser());
                    next(UserPageFragment.class, bundle);
                }
            });
            view.findViewById(R.id.app_my_layout_unlogin).setVisibility(View.GONE);

            ImageView userAvatar = (ImageView) view.findViewById(R.id.app_my_user_avatar);
            CircleImageView circleImageView = (CircleImageView) userAvatar;
            circleImageView.setWhiteCircle(true);
            TextView userName = (TextView) view.findViewById(R.id.app_my_user_name);

            userName.setText(UserProperties.getUserName());
            if (!StringUtils.isEmpty(UserProperties.getUserAvatar())) {
                ImageFetcher.asInstance().load(UserProperties.getUserAvatar(), userAvatar, R.drawable.app_pic_default_avatar);
            }

        } else {
            view.findViewById(R.id.app_my_layout_login).setVisibility(View.GONE);
            view.findViewById(R.id.app_my_layout_unlogin).setVisibility(View.VISIBLE);
            //未登录的时候的点击登录的按钮
            view.findViewById(R.id.app_my_user_do_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), UserLoginActivity.class);
                    startActivity(intent);
                }
            });
            CircleImageView circleImageView = (CircleImageView) view.findViewById(R.id.app_my_user_avatar_do_login);
            circleImageView.setWhiteCircle(true);
            circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            });
        }
    }

}
