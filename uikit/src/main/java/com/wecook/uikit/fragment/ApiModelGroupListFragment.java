package com.wecook.uikit.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.SyncHandler.Sync;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜谱列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public abstract class ApiModelGroupListFragment<T extends ApiModel> extends BaseListFragment {

    public static final String EXTRA_HIDE_TITLE_BAR = "extra_hide_title_bar";
    public static final String EXTRA_START_PAGE = "extra_start_page";
    public static final String EXTRA_START_LINE = "extra_start_line";

    private PullToRefreshListView mPullToRefreshLayout;
    private ListView mListView;
    private UIAdapter<ApiModelGroup<T>> mListAdapter;
    private List<ApiModelGroup<T>> mListData;
    private int mListSelected;
    private LoadMore mLoadMore;
    private int mStartPage = 1;
    private boolean mIsHideTitleBar;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            setTitle(bundle.getString(EXTRA_TITLE));
            mStartPage = bundle.getInt(EXTRA_START_PAGE, 1);
            mListSelected = bundle.getInt(EXTRA_START_LINE, 0);
            mIsHideTitleBar = bundle.getBoolean(EXTRA_HIDE_TITLE_BAR);
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mIsHideTitleBar) {
            enableTitleBar(false);
        }

        mPullToRefreshLayout = getRefreshListLayout();
        mPullToRefreshLayout.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mListView = mPullToRefreshLayout.getRefreshableView();
        mListData = new ArrayList<ApiModelGroup<T>>();
        mListAdapter = newAdapter(mListData);
        setListAdapter(mListView, mListAdapter);
        mLoadMore = getLoadMore();
        mLoadMore.setOneItemWeight(getColumnCount());
        mLoadMore.setCurrentPage(mStartPage);
        mListSelected += mListView.getHeaderViewsCount();
    }

    protected UIAdapter<ApiModelGroup<T>> newAdapter(List<ApiModelGroup<T>> listData) {
        return new ItemAdapter(getContext(), listData);
    }


    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (isDataLoaded()) {
            return;
        }
        showLoading();

        requestList(mStartPage, mLoadMore.getPageSize(), new ApiCallback<ApiModelList<T>>() {
            @Override
            public void onResult(ApiModelList<T> result) {
                if (result != null && result.available()) {
                    mListData.clear();
                    ApiModelGroup<T> group = new ApiModelGroup<T>(getColumnCount());
                    mListData.addAll(group.loadChildrenFromList(result));
                    if (mListData.isEmpty()) {
                        showEmptyView();
                        hideLoading();
                    } else {
                        hideEmptyView();
                        updateListSelection(mListSelected);
                    }
                } else {
                    showEmptyView();
                    hideLoading();
                }
            }
        });

    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {

        List list = (List) SyncHandler.sync(new Sync() {
            private boolean wait;
            private Object object;

            @Override
            public void syncStart() {

                ApiCallback<ApiModelList<T>> callback = new ApiCallback<ApiModelList<T>>() {
                    @Override
                    public void onResult(ApiModelList<T> result) {
                        ApiModelGroup<T> group = new ApiModelGroup<T>(getColumnCount());
                        object = group.loadChildrenFromList(result);
                        wait = false;
                    }
                };

                requestList(currentPage, pageSize, callback);
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
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
        mPullToRefreshLayout.onRefreshComplete();
    }

    private void updateListSelection(final int select) {
        if (mListView != null) {
            mListView.setSelection(select);
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mListAdapter != null) {
                        mListAdapter.notifyDataSetChanged();
                    }
                    mListView.setSelection(select);
                    hideLoading();
                }
            }, 500);
        }
    }

    /**
     * 更新列表项
     *
     * @param view
     * @param position
     * @param viewType
     * @param data
     * @param extra
     */
    protected void updateItemView(View view, int position, int viewType, ApiModelGroup<T> data, Bundle extra) {

    }

    /**
     * 获得列表项布局
     *
     * @return
     */
    protected int getItemLayoutId() {
        return 0;
    }

    /**
     * 获得列数
     *
     * @return
     */
    protected abstract int getColumnCount();

    /**
     * 请求数据
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    protected abstract void requestList(int page, int pageSize, ApiCallback<ApiModelList<T>> callback);

    private class ItemAdapter extends UIAdapter<ApiModelGroup<T>> {

        public ItemAdapter(Context context, List<ApiModelGroup<T>> data) {
            super(context, getItemLayoutId(), data);
        }

        @Override
        public void updateView(int position, int viewType, ApiModelGroup<T> data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            updateItemView(getItemView(), position, viewType, data, extra);
        }
    }
}
