package cn.wecook.app.main.dish;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.OrderApi;
import com.wecook.sdk.api.model.RecentlyDish;
import com.wecook.sdk.api.model.RecentlyDishes;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 最近购买的菜品页面
 * Created by simon on 15/9/21.
 */
public class DishRecentPurchaseFragment extends BaseTitleFragment {

    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListview;
    private int page = 1;
    private int pageSize = 10;
    private List<RecentlyDish> data = new ArrayList<RecentlyDish>();
    private RecentDishAdapter recentDishAdapter;
//    private LoadMore mLoadMore;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dish_recent_purchase, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view);
    }

    /**
     * 初始化view
     *
     * @param view
     */
    private void initView(View view) {
        setTitle("您最近购买的菜品");
        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.app_dish_recent_purchase_list);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mListview = mPullToRefreshListView.getRefreshableView();
        recentDishAdapter = new RecentDishAdapter(getContext(), data);
        mListview.setAdapter(recentDishAdapter);
//        setListAdapter(mListview, recentDishAdapter);
//        mLoadMore = getLoadMore();
//        mLoadMore.setCurrentPage(page);
//        mLoadMore.setAutoLoadCount(Integer.MAX_VALUE);

    }

    /**
     * 初始化数据
     */
    private void initData() {

    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        page = 1;
        showLoading();
        OrderApi.getOrderRecently(page, pageSize, new ApiCallback<RecentlyDishes>() {
            @Override
            public void onResult(RecentlyDishes result) {
                if (result.available()) {
                    data.clear();
                    data.addAll(result.getRecentlyDishList().getList());
                    if (recentDishAdapter != null) {
                        recentDishAdapter.notifyDataSetChanged();
                    }
                }
                hideLoading();
            }
        });
    }


//    @Override
//    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {
//
//        List list = (List) SyncHandler.sync(new SyncHandler.Sync() {
//            private boolean wait;
//            private Object object;
//
//            @Override
//            public void syncStart() {
//
//                OrderApi.getOrderRecently(currentPage, pageSize, new ApiCallback<RecentlyDishes>() {
//                    @Override
//                    public void onResult(RecentlyDishes result) {
//                        if (result != null && result.getRecentlyDishList() != null && result.getRecentlyDishList().getList() != null) {
//                            object = result.getRecentlyDishList().getList();
//                        }
//                        wait = false;
//                    }
//                });
//                wait = true;
//            }
//
//            @Override
//            public boolean waiting() {
//                return wait;
//            }
//
//            @Override
//            public Object syncEnd() {
//                return object;
//            }
//        });
//
//        return list;
//    }

    /**
     * 最近购买菜品dapater
     */
    class RecentDishAdapter extends UIAdapter<RecentlyDish> {


        public RecentDishAdapter(Context context, List<RecentlyDish> data) {
            super(context, R.layout.listview_item_recent_dish, data);
        }

        @Override
        protected View newView(int viewType, int position) {
            return super.newView(viewType, position);
        }

        @Override
        public void updateView(int position, int viewType, final RecentlyDish data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            View view = getItemView();
            TextView recentlyBuyTime = (TextView) view.findViewById(R.id.app_dish_recently_but_time);
            TextView recentlyDishDetail = (TextView) view.findViewById(R.id.app_dish_recently_dishdetail);
            TextView recentlyName = (TextView) view.findViewById(R.id.app_dish_recently_restaurant_name);
            TextView recentlyTitle = (TextView) view.findViewById(R.id.app_dish_recently_title);
            ImageView recentlyImage = (ImageView) view.findViewById(R.id.app_dish_recently_img);
            View moreView = findViewById(R.id.app_dish_recently_more_layout);

            if (data != null) {
                recentlyBuyTime.setText(data.getBuy_time());
                recentlyTitle.setText(data.getTitle());
                recentlyName.setText(data.getRestaurant().getName());
                recentlyDishDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //跳转到菜品制作界面
                        if (!StringUtils.isEmpty(data.getId())) {
                            Bundle bundle = new Bundle();
                            bundle.putString(DishPracticeFragment.EXTRA_DISH_ID, data.getId());
                            next(DishPracticeFragment.class, bundle);
                        }
                    }
                });
                ImageFetcher.asInstance().load(data.getImage(), recentlyImage);
            }
            if (position != DishRecentPurchaseFragment.this.data.size() - 1) {
                moreView.setVisibility(View.GONE);
            } else {
                moreView.setVisibility(View.VISIBLE);
                moreView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        page++;
                        showLoading();
                        OrderApi.getOrderRecently(page, pageSize, new ApiCallback<RecentlyDishes>() {
                            @Override
                            public void onResult(RecentlyDishes result) {
                                if (result.available()) {
                                    DishRecentPurchaseFragment.this.data.addAll(result.getRecentlyDishList().getList());
                                    if (recentDishAdapter != null) {
                                        recentDishAdapter.notifyDataSetChanged();
                                    }
                                }
                                hideLoading();
                            }
                        });
                    }
                });
            }
        }
    }
}
