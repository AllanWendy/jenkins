package cn.wecook.app.main.dish.list;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.EmptyView;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.DishAdapter;
import cn.wecook.app.utils.ShoppingAnimationUtils;

/**
 * 热销榜单
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/29
 */
public class DishHotSaleListFragment extends DishListFragment {

    public static final String EXTRA_RESTAURANT_ID = "extra_restaurant_id";

    private String mRestaurantId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle("热销榜单");
        Bundle bundle = getArguments();
        if (bundle != null) {
            mRestaurantId = bundle.getString(EXTRA_RESTAURANT_ID);
        }
        setUseShopCartPolice(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setBackgroundColor(getResources().getColor(R.color.uikit_grey_bg));
    }

    @Override
    protected UIAdapter<Dish> newAdapter(List<Dish> listData) {
        if (null != listData) {
        }
        return new ItemAdapter(this, listData);
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Dish>> callback) {
        Address address = DishPolicy.get().getDishAddress();
        String lat = address.getLocation().getLatitude();
        String lon = address.getLocation().getLongitude();
        DishApi.getHotSaleDishList(mRestaurantId, lat, lon, page, pageSize, callback);
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_search);
            emptyView.setTitle("暂无榜单");
            emptyView.setSecondTitle("");
        }
    }

    /**
     * 添加动画
     *
     * @param data
     */
    private void setAddShoppingAnimation(final View startView, final View endView, final Dish data) {
        startView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Dish.STATE_OFF_SALE + "").equals(data.getState())) {
                    ToastAlarm.show("菜品已售罄");
                } else if ((Dish.STATE_ON_SALE + "").equals(data.getState())) {
                    //加入购物车
                    DishPolicy.get().addDishToShopCart(data, new DishPolicy.OnShopCartUpdateListener() {
                        @Override
                        public void onResult(boolean success, String info) {
                            if (success) {
                                ShoppingAnimationUtils.setAnim(getActivity(), startView, endView);
                            } else {
                                ToastAlarm.show(info);
                            }
                        }
                    });
                }
            }
        });
    }

    private class ItemAdapter extends DishAdapter {
        /**
         * 单图显示
         */
        public static final int DISH_MODE_SINGLE = 1;
        /**
         * 双图显示
         */
        public static final int DISH_MODE_DOUBLE = 2;
        private int bigCount = 1;
        private int smallCount;

        public ItemAdapter(BaseFragment fragment, List<Dish> data) {
            super(fragment, data);
        }

        @Override
        public int getItemViewType(int position) {
            if (position < bigCount) {
                return DISH_MODE_SINGLE;
            } else {
                return DISH_MODE_DOUBLE;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getCount() {
            if (null != getListData() && getListData().size() > 1) {
                int superCount = getListData().size();
                smallCount = ((superCount - bigCount) + (superCount - bigCount) % 2) / 2;
                return Math.max(0, bigCount + smallCount);
            }
            return super.getCount();
        }

        @Override
        protected View newView(int viewType) {
            switch (viewType) {
                case DISH_MODE_DOUBLE:
                    View view_double = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_dish, null);
                    View itemLeft = view_double.findViewById(R.id.app_list_item_left);
                    View itemRight = view_double.findViewById(R.id.app_list_item_right);
                    setViewStatues(itemLeft);
                    setViewStatues(itemRight);
                    int itemViewWidth = (ScreenUtils.getScreenWidthInt() - 3 * ScreenUtils.dip2px(8)) / 2;
                    ScreenUtils.resizeView(itemLeft.findViewById(R.id.app_dish_cover_group), itemViewWidth, 1f);
                    ScreenUtils.resizeViewOfWidth(itemLeft.findViewById(R.id.app_dish_title_group), itemViewWidth);

                    ScreenUtils.resizeView(itemRight.findViewById(R.id.app_dish_cover_group), itemViewWidth, 1f);
                    ScreenUtils.resizeViewOfWidth(itemRight.findViewById(R.id.app_dish_title_group), itemViewWidth);

                    ScreenUtils.reMargin(itemLeft.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 75, true);
                    ScreenUtils.reMargin(itemRight.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 75, true);

                    View coverL = itemLeft.findViewById(R.id.app_dish_content);
                    ScreenUtils.resizeView(coverL, itemViewWidth, 251.5f / 172.5f);

                    View coverR = itemRight.findViewById(R.id.app_dish_content);
                    ScreenUtils.resizeView(coverR, itemViewWidth, 251.5f / 172.5f);
                    return view_double;
                case DISH_MODE_SINGLE:
                default:
                    View view_single = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_big, null);
                    int itemViewWidthBig = ScreenUtils.getScreenWidthInt() - 2 * ScreenUtils.dip2px(8);
                    setViewStatues(view_single);
                    View content = view_single.findViewById(R.id.app_dish_content);
                    ScreenUtils.resizeView(content, itemViewWidthBig, 257 / 355f);
                    return view_single;
            }
        }

        @Override
        public void updateView(int position, int viewType, final Dish data, Bundle extra) {
            switch (viewType) {
                case DISH_MODE_SINGLE:
                    ImageView restaurantImage = (ImageView) findViewById(R.id.app_dish_restaurant_image);
                    View restaurantGroup = findViewById(R.id.app_dish_restaurant_group);
                    restaurantImage.setVisibility(View.GONE);
                    getItemView().findViewById(R.id.app_dish_content).findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);
                    super.updateItem(mFragment, getItemView(), true, true, position, viewType, data, extra);

                    FrameLayout btnShopLayout = (FrameLayout) getItemView().findViewById(R.id.app_dish_shop_btn_layout);
                    setAddShoppingAnimation(btnShopLayout, getTitleBar().getShoppingCartLayout(), data);
                    if (position == 0) {
                        if (null != data.getRestaurant()) {
                            ImageFetcher.asInstance().getRequest(data.getRestaurant().
                                    getImage(), restaurantImage).clear();
                        }
                        restaurantGroup.setVisibility(View.VISIBLE);
                        restaurantImage.setVisibility(View.VISIBLE);
                        restaurantImage.setImageResource(R.drawable.app_ic_hot_sale);
                        restaurantImage.setBackgroundDrawable(null);
                        restaurantImage.setClickable(false);
                    }
                    setSaleCount(getItemView(), viewType, data);
                    break;
                case DISH_MODE_DOUBLE:
                    View viewLeft = getItemView().findViewById(R.id.app_list_item_left);
                    View viewRight = getItemView().findViewById(R.id.app_list_item_right);


                    int positionLeft = position * 2 - bigCount;
                    int positionRight = positionLeft + 1;
                    Dish dishLeft = ((positionLeft) >= getListData().size()) ? null : getListData().get(positionLeft);
                    Dish dishRight = ((positionRight) >= getListData().size()) ? null : getListData().get(positionRight);
                    if (null != dishLeft) {
                        viewLeft.setVisibility(View.VISIBLE);
                        super.updateItem(mFragment, viewLeft, true, true, positionLeft, viewType, dishLeft, extra);
                        viewLeft.findViewById(R.id.app_dish_content).findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);
                        setSaleCount(viewLeft, viewType, dishLeft);
                        FrameLayout leftBtnShopLayout = (FrameLayout) viewLeft.findViewById(R.id.app_dish_shop_btn_layout);
                        setAddShoppingAnimation(leftBtnShopLayout, getTitleBar().getShoppingCartLayout(), dishLeft);
                    } else {
                        viewLeft.setVisibility(View.INVISIBLE);
                    }
                    if (null != dishRight) {
                        viewRight.setVisibility(View.VISIBLE);
                        super.updateItem(mFragment, viewRight, true, true, positionRight, viewType, dishRight, extra);
                        viewRight.findViewById(R.id.app_dish_content).findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);
                        setSaleCount(viewRight, viewType, dishRight);
                        FrameLayout rightBtnShopLayout = (FrameLayout) viewRight.findViewById(R.id.app_dish_shop_btn_layout);
                        setAddShoppingAnimation(rightBtnShopLayout, getTitleBar().getShoppingCartLayout(), dishRight);
                    } else {
                        viewRight.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }

        private void setViewStatues(View itemView) {
            View view = itemView.findViewById(R.id.app_dish_restaurant_name);
            if (null != view) {
                view.setVisibility(View.GONE);
            }
            ScreenUtils.reMargin(itemView.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 80, true);
        }

        /**
         * 设置销售数量
         */
        private void setSaleCount(View view, int type, Dish dish) {
            TextView textView = null;
            switch (type) {
                case DISH_MODE_DOUBLE:
                    textView = (TextView) view.findViewById(R.id.app_dish_desc);
                    break;
                case DISH_MODE_SINGLE:
                    textView = (TextView) view.findViewById(R.id.app_dish_name_end);
                    break;
            }
            if (null != textView) {
                textView.setText("月售: " + dish.getSaleCount());
                textView.setTextColor(getResources().getColor(R.color.uikit_aaa));
            }
        }
    }
}
