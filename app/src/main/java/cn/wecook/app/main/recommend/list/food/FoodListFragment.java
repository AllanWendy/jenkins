package cn.wecook.app.main.recommend.list.food;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.SyncHandler.Sync;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.adapter.FoodListAdapter;
import cn.wecook.app.features.publish.PublishFoodActivity;

/**
 * 菜谱列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public class FoodListFragment extends BaseListFragment {

    public static final String EXTRA_HIDE_TITLE_BAR = "extra_hide_title_bar";
    public static final String EXTRA_START_PAGE = "extra_start_page";
    public static final String EXTRA_START_LINE = "extra_start_line";

    private PullToRefreshListView mPullToRefreshLayout;
    private ListView mListView;
    private FoodListAdapter mFoodListAdapter;
    private List<ApiModelGroup<Food>> mFoodData;
    private int mListSelected;
    private LoadMore mLoadMore;
    private int mStartPage = 1;

    private boolean hideTitleBar;
    private FoodTopMenuManager foodTopMenuManager;
    private boolean isFinishedRequestFoodlist;//请求列表是否完成
    private boolean isFinishedRequestFoodTopMenu;//请求顶部数据是否完成
    private LoadingListener loadingListener;//加载状态监听

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            setTitle(bundle.getString(EXTRA_TITLE));
            mStartPage = bundle.getInt(EXTRA_START_PAGE, 1);
            mListSelected = bundle.getInt(EXTRA_START_LINE, 0);
            hideTitleBar = bundle.getBoolean(EXTRA_HIDE_TITLE_BAR);
        } else {
            setTitle(R.string.app_recommend_label_hot);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (hideTitleBar) {
            enableTitleBar(false);
        }

        TitleBar.ActionTextView upload = new TitleBar.ActionTextView(getContext(), "上传菜谱");
        upload.setTextColor(getResources().getColor(R.color.uikit_font_orange));
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.setLogMarker(LogGather.MARK.FROM, "精品菜谱");
                LogGather.onEventPublishIn();
                Intent intent = new Intent(getContext(), PublishFoodActivity.class);
                startActivity(intent);
            }
        });
        getTitleBar().addActionView(upload);
        mPullToRefreshLayout = (PullToRefreshListView) view.findViewById(R.id.app_food_list);
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                boolean result = performLoadMore();
                if (!result) {
                    mPullToRefreshLayout.onRefreshComplete();
                }
            }
        });

        mListView = mPullToRefreshLayout.getRefreshableView();
        mFoodData = new ArrayList<>();
        mFoodListAdapter = new FoodListAdapter(this, mFoodData);
        setListAdapter(mListView, mFoodListAdapter);
        mLoadMore = getLoadMore();
        mLoadMore.setOneItemWeight(2);
        mLoadMore.setCurrentPage(mStartPage);
        mLoadMore.setAutoLoadCount(Integer.MAX_VALUE);
        mListSelected += mListView.getHeaderViewsCount();
    }

    public ListView getListView() {
        return mListView;
    }

    public FoodListAdapter getAdapter() {
        return mFoodListAdapter;
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (isDataLoaded()) {
            return;
        }
        showLoading();
        startLoadingListener();
        requestFoodList(mStartPage, mLoadMore.getPageSize(), new ApiCallback<ApiModelList<Food>>() {
            @Override
            public void onResult(ApiModelList<Food> result) {

                if (result != null && result.available()) {
                    mFoodData.clear();
                    ApiModelGroup<Food> group = new ApiModelGroup<Food>(2);
                    mFoodData.addAll(group.loadChildrenFromList(result));
                    if (mFoodData.isEmpty()) {
                        getEmptyView().setVisibility(View.VISIBLE);
                    } else {
                        updateListSelection(mListSelected);
                    }
                } else {
                    getEmptyView().setVisibility(View.VISIBLE);
                }
                endLoadingListener();
            }
        });

    }

    /**
     *  设置开始页数
     *
     * @param startPage
     */
    public void setStartPage(int startPage) {
        this.mStartPage = startPage;
    }

    /**
     * 加载状态开始监听
     */
    private void startLoadingListener() {
        if (loadingListener != null) {
            loadingListener.startLoading();
        }
    }

    /**
     * 加载状态结束监听
     */
    private void endLoadingListener() {
        if (loadingListener != null) {
            loadingListener.endLoading();
        }
    }


    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {

        List list = (List) SyncHandler.sync(new Sync() {
            private boolean wait;
            private Object object;

            @Override
            public void syncStart() {

                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_SOURCE, getClickMarker());
                MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_NEXTPAGE_COUNT, keys);

                ApiCallback<ApiModelList<Food>> callback = new ApiCallback<ApiModelList<Food>>() {
                    @Override
                    public void onResult(ApiModelList<Food> result) {
                        ApiModelGroup<Food> group = new ApiModelGroup<Food>(2);
                        object = group.loadChildrenFromList(result);
                        wait = false;
                    }
                };

                requestFoodList(currentPage, pageSize, callback);
                wait = true;
            }

            @Override
            public boolean waiting() {
                return wait;
            }

            @Override
            public Object syncEnd() {
                return object;
            }
        });

        return list;
    }

    @Override
    public void onStop() {
        super.onStop();
        mListSelected = mListView.getFirstVisiblePosition();
    }

    @Override
    protected void onUpdateListMore(Object data, boolean hasMore) {
        super.onUpdateListMore(data, hasMore);
        if (mFoodListAdapter != null) {
            mFoodListAdapter.notifyDataSetChanged();
        }
        mPullToRefreshLayout.onRefreshComplete();
    }

    private void updateListSelection(final int select) {
        if (mListView != null) {
            mListView.setSelection(select);
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mFoodListAdapter != null) {
                        mFoodListAdapter.notifyDataSetChanged();
                    }
                    mListView.setSelection(select);
                    hideLoading();
                }
            }, 500);
        }
    }

    /**
     * 请求数据
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    protected void requestFoodList(int page, int pageSize, ApiCallback<ApiModelList<Food>> callback) {
        FoodApi.getRecommendFoodList(page, pageSize, callback);
    }

    public void setLoadingListener(LoadingListener loadingListener) {
        if (loadingListener != null) {
            this.loadingListener = loadingListener;
        }
    }

    /**
     * 加载的状态监听
     */
    interface LoadingListener {
        void startLoading();

        void endLoading();
    }
}
