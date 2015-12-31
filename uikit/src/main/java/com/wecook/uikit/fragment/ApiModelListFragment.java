package com.wecook.uikit.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModel;
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
public abstract class ApiModelListFragment<T extends ApiModel> extends BaseListFragment {

    public static final String EXTRA_START_PAGE = "extra_start_page";
    public static final String EXTRA_START_LINE = "extra_start_line";

    private PullToRefreshListView mPullToRefreshLayout;
    private ListView mListView;
    private UIAdapter<T> mAdapter;
    private List<T> mListData;
    private int mListSelected;
    private LoadMore mLoadMore;
    private int mStartPage = 1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            setTitle(bundle.getString(EXTRA_TITLE));
            mStartPage = bundle.getInt(EXTRA_START_PAGE, 1);
            mListSelected = bundle.getInt(EXTRA_START_LINE, 0);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPullToRefreshLayout = getRefreshListLayout();
        mPullToRefreshLayout.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mListView = mPullToRefreshLayout.getRefreshableView();
        mListData = new ArrayList<T>();
        mAdapter = newAdapter(mListData);
        setListAdapter(mListView, mAdapter);
        mLoadMore = getLoadMore();
        mLoadMore.setCurrentPage(mStartPage);
        mListSelected += mListView.getHeaderViewsCount();
    }

    protected UIAdapter<T> newAdapter(List<T> listData) {
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
                onListDataResult(result);
            }
        });

    }

    protected void onListDataResult(ApiModelList<T> result) {
        if (result != null && result.available()) {
            mListData.clear();
            mListData.addAll(result.getList());
            if (mListData.isEmpty()) {
                showEmptyView();
                hideLoading();
            } else {
                hideEmptyView();
                onUpdateList(mListSelected);
            }
        } else {
            showEmptyView();
            hideLoading();
        }
    }

    protected List<T> getListData() {
        return mListData;
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
                        object = result.getList();
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
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                    }
                });
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
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        mPullToRefreshLayout.onRefreshComplete();
    }

    protected void onUpdateList(final int select) {
        if (mListView != null) {
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    mListView.setSelection(select);
                    hideLoading();
                }
            }, 250);
        }
    }

    public void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public UIAdapter<T> getAdapter() {
        return mAdapter;
    }

    /**
     * 获得列表项数据
     *
     * @param position
     * @return
     */
    protected T getItem(int position) {
        if (mAdapter != null) {
            return mAdapter.getItem(position);
        }

        return null;
    }

    protected void removeItem(int position) {
        if (position >= 0 && position < mListData.size()) {
            mListData.remove(position);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    protected void removeItem(T item) {
        boolean result = mListData.remove(item);
        if (result) {
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
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
    protected void updateItemView(View view, int position, int viewType, T data, Bundle extra) {

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
     * 初始化一些数据
     */
    protected void onNewView(int viewType, View view) {

    }

    /**
     * 请求数据
     *
     * @param page
     * @param pageSize
     * @param callback
     */
    protected abstract void requestList(int page, int pageSize, ApiCallback<ApiModelList<T>> callback);

    private class ItemAdapter extends UIAdapter<T> {

        public ItemAdapter(Context context, List<T> data) {
            super(context, getItemLayoutId(), data);
        }


        @Override
        protected View newView(int viewType) {
            View itemView = super.newView(viewType);
            onNewView(viewType, itemView);
            return itemView;
        }

        @Override
        public void updateView(int position, int viewType, T data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            updateItemView(getItemView(), position, viewType, data, extra);
        }
    }

}
