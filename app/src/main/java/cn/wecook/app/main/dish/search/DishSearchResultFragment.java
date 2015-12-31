package cn.wecook.app.main.dish.search;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.ApiModelGroupListFragment;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.DishAdapter;
import cn.wecook.app.main.dish.DishOrderByView;
import cn.wecook.app.main.search.SearchFragment;

/**
 * 买菜帮手搜索结果页面
 *
 * @author droid
 * @version v4.0
 * @since 2015-6/18
 */
public class DishSearchResultFragment extends ApiModelGroupListFragment<Dish> {
    public static final String EXTRA_KEYWORD = "extra_keyword";

    private String mSearchKeyword;
    private DishOrderByView mOrderByView;
    private ViewGroup mSuggestionView;
    private boolean isFirst = true;
    private TitleBar titleBar;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setFixed(true);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSearchKeyword = bundle.getString(EXTRA_KEYWORD);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_dish_search_result, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFixed(false);
                back();
            }
        });
        titleBar.setSearchHint("搜索想吃的菜");
        titleBar.setSearchListener(false, false, new TitleBar.SearchListener() {

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if (!isFirst) {
                    Bundle bundle = new Bundle();
                    bundle.putString(SearchFragment.SEARCH_KEY, titleBar.getSearchText());
                    back(bundle);
                } else {
                    isFirst = false;
                }
            }

        });
        titleBar.setSearchLayer(true);
        titleBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hidePopWindow();
                }
                return false;
            }
        });
        titleBar.getSearchInput().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hidePopWindow();
                }
                return false;
            }
        });
        mOrderByView = (DishOrderByView) view.findViewById(R.id.app_dish_order_by);
        mOrderByView.setOnItemClick(new AdapterViewCompat.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterViewCompat<?> parent, View view, int position, long id) {
                finishAllLoaded(false);
                onStartUILoad();
            }
        });
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSearchKeyword != null) {
                    isFirst = true;
                    titleBar.setSearchText(mSearchKeyword);
                    titleBar.getSearchInput().setSelection(mSearchKeyword.length());
                }
            }
        });

    }

    @Override
    protected int getColumnCount() {
        return 2;
    }

    @Override
    protected void requestList(int page, int pageSize, final ApiCallback<ApiModelList<Dish>> callback) {

        Address address = DishPolicy.get().getDishAddress();
        String lat = address.getLocation().getLatitude();
        String lon = address.getLocation().getLongitude();

        DishApi.searchDishList(mSearchKeyword, lat, lon, mOrderByView.getOrderType(),
                mOrderByView.getOrderDirect(), mOrderByView.getOrderTimeType() + "", page, pageSize, callback);
    }

    @Override
    protected UIAdapter<ApiModelGroup<Dish>> newAdapter(List<ApiModelGroup<Dish>> listData) {
        return new ItemAdapter(getContext(), listData);
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_search);
            emptyView.setTitle("未找到该菜品");
            emptyView.setSecondTitle("");
        }
    }

    @Override
    public boolean back() {
        Bundle bundle = new Bundle();
        bundle.putString(SearchFragment.SEARCH_KEY, "");
        hidePopWindow();
        return super.back(bundle);
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        setFixed(true);
    }

    @Override
    public void onCardOut() {
        super.onCardOut();
        hidePopWindow();
    }

    @Override
    public boolean back(Bundle data) {
        setFixed(false);
        return super.back(data);
    }

    private void hidePopWindow() {
        if (null != mOrderByView) {
            mOrderByView.hideChooseTimeWindow();
        }
    }

    private class ItemAdapter extends UIAdapter<ApiModelGroup<Dish>> {

        public ItemAdapter(Context context, List<ApiModelGroup<Dish>> data) {
            super(context, R.layout.listview_item_dish, data);
        }

        @Override
        protected View newView(int viewType) {
            View itemView = super.newView(viewType);

            View itemLeft = itemView.findViewById(R.id.app_list_item_left);
            View itemRight = itemView.findViewById(R.id.app_list_item_right);

            int itemViewWidth = (ScreenUtils.getScreenWidthInt() - 3 * ScreenUtils.dip2px(8)) / 2;

            ScreenUtils.resizeView(itemLeft.findViewById(R.id.app_dish_cover_group), itemViewWidth, 1f);
            ScreenUtils.resizeViewOfWidth(itemLeft.findViewById(R.id.app_dish_title_group), itemViewWidth);

            ScreenUtils.resizeView(itemRight.findViewById(R.id.app_dish_cover_group), itemViewWidth, 1f);
            ScreenUtils.resizeViewOfWidth(itemRight.findViewById(R.id.app_dish_title_group), itemViewWidth);

            itemLeft.findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);
            itemRight.findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);

            ScreenUtils.reMargin(itemLeft.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 75, true);
            ScreenUtils.reMargin(itemRight.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 75, true);


            View coverL = itemLeft.findViewById(R.id.app_dish_content);
            ScreenUtils.resizeView(coverL, itemViewWidth, 251.5f / 172.5f);

            View coverR = itemRight.findViewById(R.id.app_dish_content);
            ScreenUtils.resizeView(coverR, itemViewWidth, 251.5f / 172.5f);

            return itemView;
        }

        @Override
        public void updateView(int position, int viewType, ApiModelGroup<Dish> data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            View itemLeft = findViewById(R.id.app_list_item_left);
            View itemRight = findViewById(R.id.app_list_item_right);

            if (data.getItem(0) != null) {
                itemLeft.setVisibility(View.VISIBLE);
                DishAdapter.updateItem(DishSearchResultFragment.this, itemLeft, false, false, position, viewType, data.getItem(0), extra);
                supplementDishAdapter(DishSearchResultFragment.this, itemLeft, false, position, viewType, data.getItem(0), extra);
            } else {
                itemLeft.setVisibility(View.INVISIBLE);
            }

            if (data.getItem(1) != null) {
                itemRight.setVisibility(View.VISIBLE);
                DishAdapter.updateItem(DishSearchResultFragment.this, itemRight, false, false, position, viewType, data.getItem(1), extra);
                supplementDishAdapter(DishSearchResultFragment.this, itemRight, false, position, viewType, data.getItem(1), extra);
            } else {
                itemRight.setVisibility(View.INVISIBLE);
            }

        }

        /**
         * 补充Adapter
         */
        private void supplementDishAdapter(final BaseFragment fragment, View container, boolean useBigImage,
                                           int position, int viewType, final Dish data, Bundle extra) {
            ViewGroup descGroup = (ViewGroup) container.findViewById(R.id.app_dish_desc_group);
            ImageView descIcon = (ImageView) container.findViewById(R.id.app_dish_desc_icon);
            TextView desc = (TextView) container.findViewById(R.id.app_dish_desc);
            if (descGroup != null) {
                if (descIcon != null && desc != null && !StringUtils.isEmpty(data.getRestaurantName())) {
                    descGroup.setVisibility(View.VISIBLE);
                    descIcon.setVisibility(View.VISIBLE);
                    desc.setText(data.getRestaurantName());
                } else {
                    descGroup.setVisibility(View.GONE);
                }
            }
        }

    }
}
