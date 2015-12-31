package cn.wecook.app.main.dish.shopcart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.ShopCartRestaurant;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.main.dish.DishDetailFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantDetailFragment;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 购物车列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/16
 */
public class DishShopCartFragment extends BaseListFragment {

    private ShopCartItemAdapter mItemAdapter;
    private List<ShopCartDish> mItemData;
    private boolean mInEditMode;

    private TitleBar.ActionCoveredTextView mEditAction;

    private View mEditBar;
    private View mEditBarSelectAll;
    private View mEditBarDelete;

    private View mCountBar;
    private View mCountBarSelectAll;
    private TextView mCountBarTotal;
    private TextView mCountBarCheckOut;

    private boolean mIsSelectAll;
    private boolean mIsSelectAllInEditMode;

    private boolean mIsFrozenOrder;

    private DishPolicy.OnShopCartListener mOnShopCartListener;

    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UserProperties.INTENT_LOGIN.equals(action)) {
                onStartUILoad();
            }
        }
    };

    private UpdateDishCountRunnable updater;

    private LoadingDialog mLoadingDialog;
    private boolean forceUpdate = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle("购物车");
        mLoadingDialog = new LoadingDialog(getContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserProperties.INTENT_LOGIN);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUserReceiver, filter);
        mOnShopCartListener = new DishPolicy.OnShopCartListener() {
            @Override
            public void onUpdate(int count) {
                if (getContext() == null) {
                    hideLoading();
                    return;
                }

                if (mItemData.isEmpty()) {
                    showEmptyView();
                } else {
                    hideEmptyView();
                }

                if (getListView().getAdapter() == null) {
                    getListView().setAdapter(mItemAdapter);
                    setInEditMode(false);
                }
                mItemAdapter.notifyDataSetChanged();

                updateBar();

                hideLoading();
            }
        };
        DishPolicy.get().addOnShopCartListener(mOnShopCartListener);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUserReceiver);
        if (null != mOnShopCartListener) {
            DishPolicy.get().removeOnShopCartListener(mOnShopCartListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mItemData = DishPolicy.get().getListOfShopCart();
        mItemAdapter = new ShopCartItemAdapter(getContext(), mItemData);
        return inflater.inflate(R.layout.fragment_shop_cart_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditAction = new TitleBar.ActionCoveredTextView(getContext(), "编辑");
        mEditAction.setVisibility(View.GONE);
        mEditAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //编辑状态
                setInEditMode(!mInEditMode);
                if (mItemAdapter != null) {
                    mItemAdapter.notifyDataSetChanged();
                    updateBar();
                }
            }
        });
        getTitleBar().addActionView(mEditAction);

        mEditBar = view.findViewById(R.id.app_shop_cart_edit_bar);
        mEditBarSelectAll = view.findViewById(R.id.app_shop_cart_edit_bar_selector);
        mEditBarSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAllShopCart(!getSelectAll());
            }
        });
        mEditBarDelete = view.findViewById(R.id.app_shop_cart_edit_bar_delete);
        mEditBarDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.onEventDishShopCartDelete();
                List<ShopCartDish> deleteList = new ArrayList<ShopCartDish>();
                for (ShopCartDish dish : mItemData) {
                    if (dish.isSelected()) {
                        deleteList.add(dish);
                    }
                }
                if (!deleteList.isEmpty()) {
                    mLoadingDialog.show();
                    DishPolicy.get().removeFromShopCart(deleteList, new DishPolicy.OnShopCartUpdateListener() {
                        @Override
                        public void onResult(boolean success, String info) {
                            if (mItemData.isEmpty()) {
                                setInEditMode(false);
                                showEmptyView();
                            }
                            mItemAdapter.notifyDataSetChanged();
                            mLoadingDialog.dismiss();
                        }
                    });
                }
            }
        });

        mCountBar = view.findViewById(R.id.app_shop_cart_count_bar);
        mCountBarSelectAll = view.findViewById(R.id.app_shop_cart_count_bar_selector);
        mCountBarSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAllShopCart(!getSelectAll());
            }
        });
        mCountBarTotal = (TextView) view.findViewById(R.id.app_shop_cart_count_bar_total);
        mCountBarCheckOut = (TextView) view.findViewById(R.id.app_shop_cart_count_bar_check_out);
        mCountBarCheckOut.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (UserProperties.isLogin()) {
                            //确定订单
                            DishPolicy.get().checkOutShopCart();
                            if (!DishPolicy.get().isCheckOutEmpty()) {
                                //冻结购物状态
                                mIsFrozenOrder = true;
                                //同步菜品数量的更新
                                if (updater == null || updater.finished) {
                                    LogGather.onEventDishShopCartDoCheck();
                                    next(DishOrderCheckFragment.class);
                                } else {
                                    mLoadingDialog.show();
                                    AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                                        @Override
                                        public void run() {
                                            SyncHandler.sync(new SyncHandler.Sync<Object>() {
                                                @Override
                                                public void syncStart() {

                                                }

                                                @Override
                                                public boolean waiting() {
                                                    return !updater.finished;
                                                }

                                                @Override
                                                public Object syncEnd() {
                                                    return null;
                                                }
                                            });
                                        }

                                        @Override
                                        public void postUi() {
                                            super.postUi();
                                            mLoadingDialog.dismiss();
                                            LogGather.onEventDishShopCartDoCheck();
                                            next(DishOrderCheckFragment.class);
                                        }
                                    });
                                }
                            }
                        } else {
                            startActivity(new Intent(getContext(), UserLoginActivity.class));
                        }
                    }
                }
        );

        updateBar();
    }

    private void selectAllShopCart(boolean select) {
        //防止在点击结算的时候，菜品状态变化
        if (mIsFrozenOrder) {
            return;
        }

        setSelectAll(select);
        for (ShopCartDish dish : mItemData) {
            if (dish.getItemType() == ShopCartDish.ITEM_TYPE_RESTAURANT) {
                ((ShopCartRestaurant) dish.getRestaurant()).setSelectAll(getSelectAll());
            }
            dish.setSelected(getSelectAll());
        }
        mEditBarSelectAll.setSelected(getSelectAll());
        mCountBarSelectAll.setSelected(getSelectAll());
        updateBar();
        mItemAdapter.notifyDataSetChanged();
    }

    public boolean getSelectAll() {
        if (mInEditMode) {
            return mIsSelectAllInEditMode;
        } else {
            return mIsSelectAll;
        }
    }

    public void setSelectAll(boolean selectAll) {
        if (mInEditMode) {
            mIsSelectAllInEditMode = selectAll;
        } else {
            mIsSelectAll = selectAll;
        }
    }

    private void setInEditMode(boolean inEditMode) {
        mInEditMode = inEditMode;

        for (ShopCartDish dish : mItemData) {
            dish.setInEditMode(mInEditMode);
            if (dish.getRestaurant() != null && dish.getRestaurant() instanceof ShopCartRestaurant) {
                ((ShopCartRestaurant) dish.getRestaurant()).setEditMode(mInEditMode);
            }
        }

        mEditBarSelectAll.setSelected(getSelectAll());
        mCountBarSelectAll.setSelected(getSelectAll());

        if (mInEditMode) {
            mEditBar.setVisibility(View.VISIBLE);
            mCountBar.setVisibility(View.GONE);
            mEditAction.setText("完成");
        } else {
            LogGather.onEventDishShopCartEdit();
            mEditBar.setVisibility(View.GONE);
            mCountBar.setVisibility(View.VISIBLE);
            mEditAction.setText("编辑");
        }
    }

    /**
     * 更新底部bar
     */
    private void updateBar() {
        float total = 0.00f;
        int count = 0;
        setSelectAll(true);
        String currentRestaurantId = "";
        for (ShopCartDish item : mItemData) {
            if (item.getItemType() == ShopCartDish.ITEM_TYPE_DISH
                    && ShopCartDish.STATE_NORMAL.equals(item.getState())
                    && item.isSelected()) {
                total += item.getRawPrice() * item.getQuantity();
                count += item.getQuantity();
            } else if (mInEditMode && item.isSelected() && item.getItemType() == ShopCartDish.ITEM_TYPE_DISH) {
                count += item.getQuantity();
            }

            if (item.getItemType() == ShopCartDish.ITEM_TYPE_DISH && !item.isSelected()) {
                setSelectAll(false);
            }

            if (item.getRestaurant() != null && !currentRestaurantId.equals(item.getRestaurant().getId())) {
                currentRestaurantId = item.getRestaurant().getId();
                checkSelectStateInRestaurant(item);
            }
        }

        mEditBarSelectAll.setSelected(getSelectAll());
        mCountBarSelectAll.setSelected(getSelectAll());
        mEditBarDelete.setEnabled(count > 0);
        mCountBarCheckOut.setEnabled(count > 0);
        mCountBarTotal.setText(StringUtils.getPriceText(total));
        if (count > 0) {
            mCountBarCheckOut.setText("去结算(" + count + ")");
        } else {
            mCountBarCheckOut.setText("去结算");
        }

    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        mIsFrozenOrder = false;
    }

    @Override
    public void onRefreshList() {
        super.onRefreshList();
        forceUpdate = true;
        onStartUILoad();
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        //1.处理购物车数据
        //2.更新购物车数据
        showLoading();
        DishPolicy.get().update(forceUpdate);
        forceUpdate = false;
    }

    @Override
    protected boolean performRefresh() {
        forceUpdate = true;
        return super.performRefresh();
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();

        EmptyView emptyView = getEmptyView();
        if (emptyView != null && getContext() != null) {
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_shop_cart);
            emptyView.setTitle("购物车是空嗒");
            emptyView.setSecondTitle("快去挑点新鲜菜吧");
            emptyView.setActionButton("去买菜", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAll();
                }
            });
        }
    }

    private void checkSelectStateInRestaurant(ShopCartDish data) {
        ShopCartRestaurant shopCartRestaurant = (ShopCartRestaurant) data.getRestaurant();
        List<ShopCartDish> list = DishPolicy.get()
                .findDishesByRestaurantIdFromShopCart(shopCartRestaurant);
        ShopCartDish dishDiv = DishPolicy.get().getShopCartDivItemByRestaurantId(shopCartRestaurant.getId());
        if (list != null) {
            boolean isSelectAll = shopCartRestaurant.isSelectAll();
            boolean haveAllSelect = true;
            for (ShopCartDish dish : list) {
                if (isSelectAll) {
                    if (!dish.isSelected()) {
                        shopCartRestaurant.setSelectAll(false);
                        dishDiv.setSelected(false);
                        break;
                    }
                } else {
                    if (!dish.isSelected()) {
                        haveAllSelect = false;
                        break;
                    }
                }
            }
            if (haveAllSelect && !isSelectAll) {
                shopCartRestaurant.setSelectAll(true);
                dishDiv.setSelected(true);
            }
        }
    }

    @Override
    protected void showEmptyView() {
        super.showEmptyView();
        if (null != mEditAction) {
            mEditAction.setVisibility(View.GONE);
        }
    }

    @Override
    protected void hideEmptyView() {
        super.hideEmptyView();
        if (null != mEditAction) {
            mEditAction.setVisibility(View.VISIBLE);
        }
    }

    private class ShopCartItemAdapter extends UIAdapter<ShopCartDish> {

        public ShopCartItemAdapter(Context context, List<ShopCartDish> data) {
            super(context, data);
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public int getItemViewType(int position) {
            ShopCartDish item = ListUtils.getItem(mItemData, position);
            if (item != null) {
                return item.getItemType();
            }
            return super.getItemViewType(position);
        }

        @Override
        protected View newView(int viewType) {
            View view = null;
            switch (viewType) {
                case ShopCartDish.ITEM_TYPE_RESTAURANT:
                    view = LayoutInflater.from(getContext())
                            .inflate(R.layout.listview_item_restaurant_shopcart, null);
                    break;
                case ShopCartDish.ITEM_TYPE_DISH:
                    view = LayoutInflater.from(getContext())
                            .inflate(R.layout.listview_item_dish_shopcart, null);
                    break;
                case ShopCartDish.ITEM_TYPE_DIVIDING:
                    view = new View(getContext());
                    view.setBackgroundColor(getResources().getColor(R.color.uikit_grey));
                    view.setLayoutParams(new AbsListView.LayoutParams(
                            AbsListView.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(10), viewType));
                    break;
            }
            return view;
        }

        @Override
        public void updateView(int position, int viewType, final ShopCartDish data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            if (data != null) {
                switch (viewType) {
                    case ShopCartDish.ITEM_TYPE_RESTAURANT:
                        final Restaurant restaurant = data.getRestaurant();
                        if (restaurant != null && restaurant instanceof ShopCartRestaurant) {
                            TextView restaurantName = (TextView) findViewById(R.id.app_shop_cart_restaurant_name);
                            View restaurantSelector = findViewById(R.id.app_shop_cart_restaurant_selector);
                            restaurantName.setText(data.getRestaurant().getName());
                            restaurantSelector.setSelected(((ShopCartRestaurant) restaurant).isSelectAll());
                            data.setSelected(((ShopCartRestaurant) restaurant).isSelectAll());
                            restaurantSelector.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //防止在点击结算的时候，菜品状态变化
                                    if (mIsFrozenOrder) {
                                        return;
                                    }
                                    if (ShopCartRestaurant.STATE_NORMAL.equals(restaurant.getRestaurantStatus())
                                            || mInEditMode) {
                                        //选中所有
                                        List<ShopCartDish> list = DishPolicy.get()
                                                .findDishesByRestaurantIdFromShopCart((ShopCartRestaurant) restaurant);
                                        ShopCartDish dishDiv = DishPolicy.get().getShopCartDivItemByRestaurantId(restaurant.getId());
                                        if (list == null) {
                                            return;
                                        }
                                        boolean isSelectAll = ((ShopCartRestaurant) restaurant).isSelectAll();
                                        ((ShopCartRestaurant) restaurant).setSelectAll(!isSelectAll);

                                        data.setSelected(!isSelectAll);
                                        if (dishDiv != null) {
                                            dishDiv.setSelected(!isSelectAll);
                                        }

                                        for (int i = 0; i < list.size(); i++) {
                                            ShopCartDish dish = list.get(i);
                                            dish.setSelected(!isSelectAll);
                                        }
                                        updateBar();
                                        notifyDataSetChanged();
                                    }
                                }
                            });
                            if (ShopCartRestaurant.STATE_NORMAL.equals(restaurant.getRestaurantStatus())) {
                                restaurantName.setEnabled(true);

                                List<ShopCartDish> list = DishPolicy.get()
                                        .findDishesByRestaurantIdFromShopCart((ShopCartRestaurant) restaurant);
                                boolean allNotNormal = true;
                                for (ShopCartDish dish : list) {
                                    if (ShopCartDish.STATE_NORMAL.equals(dish.getState())) {
                                        allNotNormal = false;
                                        break;
                                    }
                                }

                                if (allNotNormal) {
                                    restaurantSelector.setVisibility(View.INVISIBLE);
                                } else {
                                    restaurantSelector.setVisibility(View.VISIBLE);
                                }
                            } else {
                                restaurantName.setEnabled(false);
                                if (mInEditMode) {
                                    restaurantSelector.setVisibility(View.VISIBLE);
                                } else {
                                    restaurantSelector.setVisibility(View.INVISIBLE);
                                }
                            }

                            getItemView().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (ShopCartRestaurant.STATE_NORMAL.
                                            equals(restaurant.getRestaurantStatus())) {
                                        LogGather.onEventDishShopCartGoRestaurant();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID,
                                                restaurant.getId());
                                        next(DishRestaurantDetailFragment.class, bundle);
                                    } else if (ShopCartRestaurant.STATE_DISABLE.
                                            equals(restaurant.getRestaurantStatus())) {
                                        ToastAlarm.show("该餐厅暂不开放");
                                    } else if (ShopCartRestaurant.STATE_DELETED.
                                            equals(restaurant.getRestaurantStatus())) {
                                        ToastAlarm.show("该餐厅已下架");
                                    }
                                }
                            });
                        }
                        break;
                    case ShopCartDish.ITEM_TYPE_DISH:
                        View dishSelector = findViewById(R.id.app_shop_cart_dish_selector);
                        ImageView dishImage = (ImageView) findViewById(R.id.app_shop_cart_dish_image);
                        TextView dishState = (TextView) findViewById(R.id.app_shop_cart_dish_state);
                        TextView dishName = (TextView) findViewById(R.id.app_shop_cart_dish_name);
                        TextView dishPrice = (TextView) findViewById(R.id.app_shop_cart_dish_price);
                        TextView dishPriceNormal = (TextView) findViewById(R.id.app_shop_cart_dish_price_normal);
                        View dishCountGroup = findViewById(R.id.app_shop_cart_dish_count_group);
                        View dishCountMinus = findViewById(R.id.app_shop_cart_dish_count_minus);
                        final TextView dishCount = (TextView) findViewById(R.id.app_shop_cart_dish_count);
                        View dishCountAdd = findViewById(R.id.app_shop_cart_dish_count_add);

                        dishSelector.setSelected(data.isSelected());
                        if (data.isSelected()) {
                            DishPolicy.get().recordSelectedDish(data.getDishId());
                        } else {
                            DishPolicy.get().removeSelectedDish(data.getDishId());
                        }
                        dishSelector.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //防止在点击结算的时候，菜品状态变化
                                if (mIsFrozenOrder) {
                                    return;
                                }
                                if (ShopCartDish.STATE_NORMAL.equals(data.getState())
                                        || mInEditMode) {
                                    data.setSelected(!data.isSelected());
                                    checkSelectStateInRestaurant(data);
                                    updateBar();
                                    notifyDataSetChanged();
                                }
                            }
                        });
                        ImageFetcher.asInstance().loadRoundedCorner(data.getImage(), dishImage);
                        final String state = data.getState();

                        dishName.setText(data.getTitle());
                        dishPrice.setText(data.getPrice());
                        dishPriceNormal.getPaint().setAntiAlias(true);
                        dishPriceNormal.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                        dishPriceNormal.setText(data.getPriceNormal());

                        dishCount.setText("" + data.getQuantity());
                        dishCountAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (data.getRestaurant() != null) {
                                    //当餐厅产能不足的情况下，菜品标记为售罄
                                    if (data.getRestaurant().getProvideMax() < 1) {
                                        data.setState(ShopCartDish.STATE_SALE_OFF);
                                        notifyDataSetChanged();
                                        return;
                                    }
                                    int quantity = data.getQuantity() + 1;
                                    if (quantity > data.getRestaurant().getProvideMax()) {
                                        ToastAlarm.show("已达库存上限");
                                        quantity = data.getRestaurant().getProvideMax();
                                    }

                                    if (DishPolicy.get().getQuantityLimit() > 0
                                            && data.getQuantity() >= DishPolicy.get().getQuantityLimit()) {
                                        ToastAlarm.show("已达购买上限");
                                        quantity = DishPolicy.get().getQuantityLimit();
                                    }

                                    data.setQuantity(quantity);

                                    dishCount.setText("" + data.getQuantity());
                                    updateBar();

                                    if (updater == null) {
                                        updater = new UpdateDishCountRunnable();
                                    }
                                    updater.setShopCartDish(data);
                                    UIHandler.postOnceDelayed(updater, 1000);
                                }
                            }
                        });

                        dishCountMinus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int quantity = data.getQuantity() - 1;
                                boolean alarmDelete = false;
                                if (quantity < 1) {
                                    quantity = 1;
                                    alarmDelete = true;
                                }
                                data.setQuantity(quantity);
                                dishCount.setText("" + data.getQuantity());
                                updateBar();


                                if (alarmDelete) {
                                    final ConfirmDialog dialog = new ConfirmDialog(getContext(), "确定删除该道菜？");
                                    dialog.setConfirm(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Restaurant restaurant = data.getRestaurant();
                                            List<ShopCartDish> list = DishPolicy.get()
                                                    .findDishesByRestaurantIdFromShopCart((ShopCartRestaurant) restaurant);
                                            DishPolicy.OnShopCartUpdateListener listener =
                                                    new DishPolicy.OnShopCartUpdateListener() {
                                                        @Override
                                                        public void onResult(boolean success, String info) {
                                                            if (success) {
                                                                ToastAlarm.show("删除成功");
                                                            }
                                                            if (mItemData.isEmpty()) {
                                                                setInEditMode(false);
                                                                showEmptyView();
                                                            }
                                                            notifyDataSetChanged();
                                                        }
                                                    };
                                            if (list.size() == 1) {
                                                List<ShopCartDish> deleteList = new ArrayList<ShopCartDish>();
                                                for (ShopCartDish dish : mItemData) {
                                                    if (dish.getRestaurant().equals(restaurant)) {
                                                        deleteList.add(dish);
                                                    }
                                                }
                                                DishPolicy.get().removeFromShopCart(deleteList, listener);
                                            } else {
                                                DishPolicy.get().removeFromShopCart(data, listener);
                                            }
                                        }
                                    }).show();
                                } else {
                                    if (updater == null) {
                                        updater = new UpdateDishCountRunnable();
                                    }
                                    updater.setShopCartDish(data);
                                    UIHandler.postOnceDelayed(updater, 2000);
                                }

                            }
                        });

                        getItemView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ShopCartDish.STATE_NORMAL.equals(state)) {
                                    LogGather.onEventDishShopCartGoDish();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(DishDetailFragment.EXTRA_DISH_ID, data.getDishId());
                                    bundle.putString(DishDetailFragment.EXTRA_TITLE, data.getTitle());
                                    next(DishDetailFragment.class, bundle);
                                } else if (ShopCartDish.STATE_SHELF_OFF.equals(state)) {
                                    ToastAlarm.show("该菜品已经下架");
                                }
                            }
                        });

                        if (ShopCartDish.STATE_NORMAL.equals(state)) {
                            dishSelector.setVisibility(View.VISIBLE);
                            dishState.setVisibility(View.GONE);
                            dishCountGroup.setVisibility(View.VISIBLE);
                            dishName.setEnabled(true);
                            dishPrice.setEnabled(true);
                            dishCountAdd.setEnabled(true);
                            dishCountMinus.setEnabled(true);
                        } else {
                            if (mInEditMode) {
                                dishSelector.setVisibility(View.VISIBLE);
                            } else {
                                dishSelector.setVisibility(View.INVISIBLE);
                            }

                            dishCountAdd.setEnabled(false);
                            dishCountMinus.setEnabled(false);
                            dishName.setEnabled(false);
                            dishPrice.setEnabled(false);
                            dishState.setVisibility(View.VISIBLE);
                            dishCountGroup.setVisibility(View.GONE);
                            if (ShopCartDish.STATE_SALE_OFF.equals(state)) {
                                dishState.setText("已售罄");
                                dishState.setTextColor(getResources().getColor(R.color.uikit_orange));
                            } else if (ShopCartDish.STATE_SHELF_OFF.equals(state)) {
                                dishState.setText("已下架");
                                dishState.setTextColor(getResources().getColor(R.color.uikit_font_dark));
                            }
                        }
                        break;
                }
            }

        }

    }

    private class UpdateDishCountRunnable implements Runnable {

        private ShopCartDish shopCartDish;
        private boolean finished;

        public void setShopCartDish(ShopCartDish dish) {
            shopCartDish = dish;
            finished = false;
        }

        @Override
        public void run() {
            if (shopCartDish != null) {
                DishPolicy.get().updateDishInShopCart(shopCartDish, new DishPolicy.OnShopCartUpdateListener() {
                    @Override
                    public void onResult(boolean success, String info) {
                        finished = true;
                        if (!success) {
                            ToastAlarm.show(info);
                        }
                    }
                });
            }
        }
    }
}
