package cn.wecook.app.main.dish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Category;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.DishAdapter;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.main.recommend.card.ClassifyView;

/**
 * Created by LK on 2015/9/3.
 * 菜品分类
 */
public class DishClassifyFragment extends BaseListFragment implements ClassifyView.OnLayoutItemClick {
    public static final String PARAMS_CATEGORIES = "categories";
    public static final String PARAMS_FIRSTSELECTITEM = "firstSelectItem";
    public static final String PARAMS_KEYWORDS = "keywords";
    private View mView;
    //bundle
    private ArrayList<Category> categories;
    private int firstSelectItem = 0;
    private String keywords = "荤菜";

    private List<ApiModelGroup<Dish>> mListData;
    private LinearLayout mTopView;
    private PullToRefreshListView mPullToRefreshLayout;
    private ListView mListView;

    private ImageView img_shop;//购物车
    private TextView tv_shop_count;//购物车数量

    private MergeAdapter mMergeAdapter;
    private ItemAdapter mItemAdapter;
    private ClassifyView mListItemClassifyView1;
    private ClassifyView mListItemClassifyView2;
    private ClassifyView mTopClassifyView;
    private DishOrderByView mTopOrderByView;
    private DishOrderByView mListItemOrderByView;


    private LoadMore mLoadMore;
    private int page = 1;
    private int pageSize = 20;
    private boolean isRequest = false;

    private boolean flag_request;

    private LoadingDialog loadingDialog;
    private int selectedOrderType;
    private String selectedOrderDirect;
    private String selectedOrderTimeType = DishApi.ORDER_TYPE_TIME_DEFAULT;

    private DishPolicy.OnShopCartListener listener = new DishPolicy.OnShopCartListener() {
        @Override
        public void onUpdate(int count) {
            updateShopCartCount(count);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        categories = getArguments().getParcelableArrayList(PARAMS_CATEGORIES);
        firstSelectItem = getArguments().getInt(PARAMS_FIRSTSELECTITEM);
        if (getArguments().containsKey(PARAMS_KEYWORDS)) {
            keywords = getArguments().getString(PARAMS_KEYWORDS);
        }
        loadingDialog = new LoadingDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_dish_classify, null);
        mTopView = (LinearLayout) mView.findViewById(R.id.dishclassify_top_view);
        mTopOrderByView = (DishOrderByView) mTopView.findViewById(R.id.app_dish_order_by);
        selectedOrderType = mTopOrderByView.getOrderType();
        selectedOrderDirect = mTopOrderByView.getOrderDirect();
        mPullToRefreshLayout = (PullToRefreshListView) mView.findViewById(R.id.dish_classify_list);
        img_shop = (ImageView) mView.findViewById(R.id.dish_classify_shop_img);
        tv_shop_count = (TextView) mView.findViewById(R.id.dish_classify_shop_count);
        return mView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFixed(true);
        setTitle("菜品分类");
        //顶部分类
        if (null == mTopClassifyView && null != categories) {
            mTopClassifyView = new ClassifyView(this);
            mTopClassifyView.loadLayout(categories);
            mTopClassifyView.setFlagShowImgs(false);
            mTopClassifyView.setFlagShowLines(true);
            mTopClassifyView.setFlagShowSelector(false);
            mTopClassifyView.setOnLayoutItemClick(this);
            mTopClassifyView.setFirstVisityItem(firstSelectItem);
            mTopView.addView(mTopClassifyView, 0);
        }
        mTopOrderByView.setOnItemClick(new AdapterViewCompat.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterViewCompat<?> parent, View view, int position, long id) {
                page = 1;
                selectedOrderType = mTopOrderByView.getOrderType();
                selectedOrderDirect = mTopOrderByView.getOrderDirect();
                selectedOrderTimeType = mTopOrderByView.getOrderTimeType();
                onStartUILoad();
                if (null != mListItemOrderByView) {
                    mListItemOrderByView.updateStatus(selectedOrderType, selectedOrderDirect, selectedOrderTimeType);
                }
            }
        });
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                flag_request = true;
                requestRefresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                boolean result = performLoadMore();
                if (!result) {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPullToRefreshLayout.onRefreshComplete();
                        }
                    });
                }
            }
        });
        mPullToRefreshLayout.setOnPullEventListener(new PullToRefreshBase.OnPullEventListener<ListView>() {
            @Override
            public void onPullEvent(PullToRefreshBase<ListView> refreshView, PullToRefreshBase.State state, PullToRefreshBase.Mode direction) {
                if (state == PullToRefreshBase.State.PULL_TO_REFRESH) {
                    hidePopupWindow();
                }
            }
        });
        mListView = mPullToRefreshLayout.getRefreshableView();
        mListData = new ArrayList<ApiModelGroup<Dish>>();
        if (null == mMergeAdapter) {
            mMergeAdapter = new MergeAdapter();
        }
        if (null == mItemAdapter) {
            mItemAdapter = new ItemAdapter(getContext(), mListData);
        }
        setListAdapter(mListView, mItemAdapter, new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                hidePopupWindow();
                if (null != mTopView && null != mListData && mListData.size() > 0) {
                    boolean isShow = firstVisibleItem > 1;
                    if (isShow) {
                        if (mTopView.getVisibility() != View.VISIBLE) {
                            mTopView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mTopView.getVisibility() != View.GONE) {
                            mTopView.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
        mLoadMore = getLoadMore();
        mLoadMore.setOneItemWeight(2);
        mLoadMore.setCurrentPage(page);
        mLoadMore.setAutoLoadCount(Integer.MAX_VALUE);

        img_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
                Intent intent = new Intent(getContext(), DishActivity.class);
                intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_SHOP_CART);
                startActivity(intent);
            }
        });
        getTitleBar().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hidePopupWindow();
                        break;
                }
                return false;
            }
        });
        getTitleBar().getBackView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFixed(false);
                back();
            }
        });
        //添加购物车策略
        DishPolicy.get().addOnShopCartListener(listener);
        DishPolicy.get().update();
    }

    private void hidePopupWindow() {
        if (null != mListItemOrderByView) {
            mListItemOrderByView.hideChooseTimeWindow();
        }
        if (null != mTopOrderByView) {
            mTopOrderByView.hideChooseTimeWindow();
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        setFixed(true);
        hidePopupWindow();
    }

    @Override
    public void onCardOut() {
        super.onCardOut();
        hidePopupWindow();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DishPolicy.get().removeOnShopCartListener(listener);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        hidePopupWindow();
        if (isRequest) return;
        isRequest = true;
        loadingDialog.show();
        showLoading();
        Address address = DishPolicy.get().getDishAddress();
        final String lat = address.getLocation().getLatitude();
        final String lon = address.getLocation().getLongitude();
        DishApi.searchDishList(keywords, lat, lon, selectedOrderType,
                selectedOrderDirect, selectedOrderTimeType + "", page, pageSize, new ApiCallback<ApiModelList<Dish>>() {
                    @Override
                    public void onResult(ApiModelList<Dish> result) {
                        if (page == 1 && !result.available()) {
                            //查询出错 没有数据
                            if (!mTopClassifyView.isFlagShowImgs()) {
                                mTopClassifyView.setFlagShowImgs(true);
                                mTopClassifyView.setFirstVisityItem(firstSelectItem);
                                mTopClassifyView.initViewStat();
                            }
                            showEmptyView();
                            mTopView.setVisibility(View.VISIBLE);
                            endLoading();
                        } else {
                            hideEmptyView();
                            if (mTopClassifyView.isFlagShowImgs()) {
                                mTopClassifyView.setFlagShowImgs(false);
                                mTopClassifyView.initViewStat();
                            }
                            ApiModelGroup<Dish> group = new ApiModelGroup<Dish>(2);
                            List<ApiModelGroup<Dish>> date = group.loadChildrenFromList(result);
                            if (page == 1) {
                                mListData.clear();
                            }
                            mListData.addAll(date);
                            initView();
                            mTopView.setVisibility(View.GONE);
                            mMergeAdapter.addView(mListItemClassifyView1);
                            mMergeAdapter.addView(mListItemClassifyView2);
                            mMergeAdapter.addView(mListItemOrderByView);
                            mMergeAdapter.addAdapter(mItemAdapter);
                            mPullToRefreshLayout.getRefreshableView().setAdapter(mMergeAdapter);
                            mItemAdapter.notifyDataSetChanged();
                            mMergeAdapter.notifyDataSetChanged();
                            endLoading();
                        }

                    }
                });

    }

    private void endLoading() {
        isRequest = false;
        loadingDialog.dismiss();
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                hideLoading();
                if (mPullToRefreshLayout != null) {
                    mPullToRefreshLayout.onRefreshComplete();
                }
            }
        });
    }


    /**
     * 初始化数据
     */
    private void initView() {
        //ListViewItem
        if (null == mListItemClassifyView1) {
            mListItemClassifyView1 = new ClassifyView(this);
            mListItemClassifyView1.loadLayout(categories);
            mListItemClassifyView1.setFlagShowImgs(true);
            mListItemClassifyView1.setOnLayoutItemClick(this);
            mListItemClassifyView1.setFlagShowTexts(false);
            mListItemClassifyView1.setFlagShowLines(false);
            mListItemClassifyView1.setFlagShowSelector(false);
            mListItemClassifyView1.setFlagShowBottomLine(false);
            mListItemClassifyView1.setFirstVisityItem(firstSelectItem);
        }
        if (null == mListItemClassifyView2) {
            mListItemClassifyView2 = new ClassifyView(this);
            mListItemClassifyView2.loadLayout(categories);
            mListItemClassifyView2.setFlagShowImgs(false);
            mListItemClassifyView2.setFlagShowTexts(true);
            mListItemClassifyView2.setFlagShowLines(true);
            mListItemClassifyView2.setFlagShowSelector(false);
            mListItemClassifyView2.setOnLayoutItemClick(this);
            mListItemClassifyView2.setFirstVisityItem(firstSelectItem);
        }
        if (null == mListItemOrderByView) {
            mListItemOrderByView = (DishOrderByView) LayoutInflater.from(getContext()).inflate(R.layout.view_order_by_chooser, null);
            mListItemOrderByView.updateStatus(selectedOrderType, selectedOrderDirect, selectedOrderTimeType);
            mListItemOrderByView.setBackgroundColor(getResources().getColor(R.color.uikit_white));
            mListItemOrderByView.setOnItemClick(new AdapterViewCompat.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterViewCompat<?> parent, View view, int position, long id) {
                    page = 1;
                    selectedOrderType = mListItemOrderByView.getOrderType();
                    selectedOrderDirect = mListItemOrderByView.getOrderDirect();
                    selectedOrderTimeType = mListItemOrderByView.getOrderTimeType();
                    onStartUILoad();
                    if (null != mTopOrderByView) {
                        mTopOrderByView.updateStatus(selectedOrderType, selectedOrderDirect, selectedOrderTimeType);
                    }
                }
            });
        }


    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_search);
            emptyView.setTitle("");
            emptyView.setSecondTitle("没有找到相关内容");
        }
    }

    @Override
    public void OnClickListener(int position, Category category) {
        hidePopupWindow();
        if (firstSelectItem == position) return;
        keywords = category.getUrlKeyWords();
        page = 1;
        firstSelectItem = position;
        if (null != mTopClassifyView) {
            mTopClassifyView.setFirstVisityItem(firstSelectItem);
            mTopClassifyView.changeBtnStatus(position);
        }
        if (null != mListItemClassifyView1) {
            mListItemClassifyView1.setFirstVisityItem(firstSelectItem);
            mListItemClassifyView1.changeBtnStatus(position);
        }
        if (null != mListItemClassifyView2) {
            mListItemClassifyView2.setFirstVisityItem(firstSelectItem);
            mListItemClassifyView2.changeBtnStatus(position);
        }
        onStartUILoad();
    }

    private void requestRefresh() {
        if (NetworkState.available()) {
            Api.startNoCacheMode();
            page = 1;
            onStartUILoad();
            Api.stopNoCacheMode();
        } else {
            ToastAlarm.show("网络异常，请刷新重试");
            endLoading();
        }
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {
        List list = (List) SyncHandler.sync(new SyncHandler.Sync() {
            private boolean wait;
            private Object object;

            @Override
            public void syncStart() {
                if (flag_request) return;
                flag_request = true;
                ApiCallback<ApiModelList<Dish>> callback = new ApiCallback<ApiModelList<Dish>>() {
                    @Override
                    public void onResult(ApiModelList<Dish> result) {
                        if (null == result) {
                            return;
                        } else {
                            ApiModelGroup<Dish> group = new ApiModelGroup<Dish>(2);
                            object = group.loadChildrenFromList(result);
                            wait = false;
                        }
                    }
                };

                Address address = DishPolicy.get().getDishAddress();
                final String lat = address.getLocation().getLatitude();
                final String lon = address.getLocation().getLongitude();
                page++;
                DishApi.searchDishList(keywords, lat, lon, mTopOrderByView.getOrderType(),
                        mTopOrderByView.getOrderDirect(), mTopOrderByView.getOrderTimeType() + "", page, pageSize, callback);
                wait = true;
            }

            @Override
            public boolean waiting() {
                return wait;
            }

            @Override
            public Object syncEnd() {
                flag_request = false;
                return object;
            }
        });

        return list;
    }

    @Override
    protected void onUpdateListMore(Object data, boolean hasMore) {
        super.onUpdateListMore(data, hasMore);
        if (mMergeAdapter != null) {
            mMergeAdapter.notifyDataSetChanged();
        }
        mPullToRefreshLayout.onRefreshComplete();
    }

    private void updateShopCartCount(int count) {
        if (count == 0) {
            tv_shop_count.setVisibility(View.GONE);
        } else {
            if (count > 99) {
                tv_shop_count.setText("99+");
            } else {
                tv_shop_count.setText("" + count);
            }
            tv_shop_count.setVisibility(View.VISIBLE);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setFixed(false);
            back();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
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


            return itemView;
        }

        @Override
        public void updateView(int position, int viewType, ApiModelGroup<Dish> data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            View itemLeft = findViewById(R.id.app_list_item_left);
            View itemRight = findViewById(R.id.app_list_item_right);

            if (data.getItem(0) != null) {
                itemLeft.setVisibility(View.VISIBLE);
                DishAdapter.updateItem(DishClassifyFragment.this, itemLeft, false, false, position, viewType, data.getItem(0), extra);
                supplementDishAdapter(DishClassifyFragment.this, itemLeft, false, position, viewType, data.getItem(0), extra);
            } else {
                itemLeft.setVisibility(View.INVISIBLE);
            }

            if (data.getItem(1) != null) {
                itemRight.setVisibility(View.VISIBLE);
                DishAdapter.updateItem(DishClassifyFragment.this, itemRight, false, false, position, viewType, data.getItem(1), extra);
                supplementDishAdapter(DishClassifyFragment.this, itemRight, false, position, viewType, data.getItem(1), extra);
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
