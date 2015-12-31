package cn.wecook.app.main.dish;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

import cn.wecook.app.R;
import cn.wecook.app.main.dish.list.DishGroupListFragment;
import cn.wecook.app.main.dish.list.DishSpecialListFragment;
import cn.wecook.app.main.dish.order.DishOrderDetailFragment;
import cn.wecook.app.main.dish.order.DishOrderStateListFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantDetailFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantFragment;
import cn.wecook.app.main.dish.shopcart.DishShopCartFragment;

/**
 * 买菜帮手服务
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/16
 */
public class DishActivity extends BaseSwipeActivity {

    /**
     * 套餐
     */
    public static final int PAGE_GROUP = 1;
    /**
     * 餐厅
     */
    public static final int PAGE_RESTAURANT = 2;
    /**
     * 餐厅详情
     */
    public static final int PAGE_RESTAURANT_DETAIL = 3;
    /**
     * 订单状态列表
     */
    public static final int PAGE_ORDER_LIST = 4;
    /**
     * 订单详情
     */
    public static final int PAGE_ORDER_DETAIL = 5;
    /**
     * 菜品详情
     */
    public static final int PAGE_DISH_DETAIL = 6;
    /**
     * 今日特价
     */
    public static final int PAGE_SPECIAL_DISH = 7;
    /**
     * 购物车
     */
    public static final int PAGE_SHOP_CART = 8;

    public static final String EXTRA_PAGE = "page";

    private int mPage;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UserProperties.INTENT_LOGIN.equals(action)) {
                //同步数据
                DishPolicy.get().translateLocalData();
                DishPolicy.get().updateAndSyncShopCart(true, null);
            }
        }
    };


    @Override
    protected int getContentLayoutId() {
        return R.layout.app_dish_content_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(UserProperties.INTENT_LOGIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }


    @Override
    protected BaseFragment onCreateFragment(Bundle intentBundle) {
        if (intentBundle != null) {
            mPage = intentBundle.getInt(EXTRA_PAGE, 0);
        }

        BaseFragment fragment = null;
        switch (mPage) {
            case PAGE_GROUP:
                fragment = BaseFragment.getInstance(DishGroupListFragment.class, intentBundle);
                break;
            case PAGE_RESTAURANT:
                fragment = BaseFragment.getInstance(DishRestaurantFragment.class, intentBundle);
                break;
            case PAGE_RESTAURANT_DETAIL:
                fragment = BaseFragment.getInstance(DishRestaurantDetailFragment.class, intentBundle);
                break;
            case PAGE_ORDER_LIST:
                fragment = BaseFragment.getInstance(DishOrderStateListFragment.class, intentBundle);
                break;
            case PAGE_ORDER_DETAIL:
                fragment = BaseFragment.getInstance(DishOrderDetailFragment.class, intentBundle);
                break;
            case PAGE_DISH_DETAIL:
                fragment = BaseFragment.getInstance(DishDetailFragment.class, intentBundle);
                break;
            case PAGE_SPECIAL_DISH:
                fragment = BaseFragment.getInstance(DishSpecialListFragment.class, intentBundle);
                break;
            case PAGE_SHOP_CART:
                fragment = BaseFragment.getInstance(DishShopCartFragment.class, intentBundle);
                break;
        }
        return fragment;
    }

    @Override
    protected void onStop() {
        super.onStop();
        DishPolicy.get().saveLocalShopCart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

}
