package com.wecook.uikit.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.Api;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.uikit.R;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列表Fragment
 * <p/>
 * 带有自动加载更多功能
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/7/14
 */
public abstract class BaseListFragment extends BaseTitleFragment {

    private Map<BaseUIAdapter, LoadMore> mListLoadMoreMap = new HashMap<BaseUIAdapter, LoadMore>();
    private BaseUIAdapter mUiAdapter;
    private LoadMore mCurrentLoadMore;

    private ListView mListView;
    private PullToRefreshListView mRefreshListLayout;
    private EmptyView mEmptyView;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.uikit_fragment_listview, null);
        mView = updRootView(mView);
        return mView;
    }

    /**
     * 更新根布局
     */
    protected View updRootView(View view) {
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enableTitleBar(true);

        mEmptyView = (EmptyView) view.findViewById(R.id.uikit_empty);
        onUpdateEmptyView();
        mRefreshListLayout = (PullToRefreshListView) view.findViewById(R.id.uikit_listview);
        if (mRefreshListLayout != null) {
            mRefreshListLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    boolean result = performRefresh();
                    if (!result) {
                        UIHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRefreshListLayout.onRefreshComplete();
                            }
                        }, 300);
                    }
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                    boolean result = performLoadMore();
                    if (!result) {
                        UIHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRefreshListLayout.onRefreshComplete();
                            }
                        }, 300);
                    }
                }
            });
            mListView = mRefreshListLayout.getRefreshableView();
        }
    }

    public void enableTitleBar(boolean enable) {
        if (getTitleBar() != null) {
            getTitleBar().setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    public PullToRefreshListView getRefreshListLayout() {
        return mRefreshListLayout;
    }

    public ListView getListView() {
        return mListView;
    }

    public EmptyView getEmptyView() {
        return mEmptyView;
    }

    protected void onUpdateEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setRefreshListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStartUILoad();
                }
            });
            mEmptyView.setTitle("数据为空");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListLoadMoreMap.clear();
        mUiAdapter = null;
    }

    protected void showEmptyView() {
        onUpdateEmptyView();
        if (mEmptyView != null && mEmptyView.getVisibility() != View.VISIBLE) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    protected void showEmptyView(boolean canRefresh) {
        onUpdateEmptyView();
        if (mEmptyView != null) {
            mEmptyView.setCanRefresh(canRefresh);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    protected void hideEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideLoading() {
        super.hideLoading();

        if (mRefreshListLayout != null) {
            mRefreshListLayout.onRefreshComplete();
        }
    }

    /**
     * 获得加载更多控制
     *
     * @return
     */
    public LoadMore getLoadMore() {
        return mCurrentLoadMore;
    }

    /**
     * 设置列表数据适配
     *
     * @param view
     * @param adapter
     */
    public void setListAdapter(AbsListView view, final BaseUIAdapter adapter) {
        if (view != null && adapter != null) {
            mUiAdapter = adapter;
            if (view instanceof ListView) {
                ((ListView) view).setAdapter(adapter);
            } else if (view instanceof GridView) {
                ((GridView) view).setAdapter(adapter);
            }
        }

        useLoadMore(view, adapter);
    }

    public void setListAdapter(AbsListView view, final BaseUIAdapter adapter, AbsListView.OnScrollListener listener) {
        if (view != null && adapter != null) {
            mUiAdapter = adapter;
            if (view instanceof ListView) {
                ((ListView) view).setAdapter(adapter);
            } else if (view instanceof GridView) {
                ((GridView) view).setAdapter(adapter);
            }
        }

        useLoadMore(view, adapter, listener);
    }

    public void useLoadMore(AbsListView view, BaseUIAdapter adapter) {
        useLoadMore(view, adapter, null);
    }

    /**
     * 使用加载更多
     *
     * @param view
     * @param adapter
     */
    public void useLoadMore(AbsListView view, BaseUIAdapter adapter, AbsListView.OnScrollListener listener) {
        mUiAdapter = adapter;
        mCurrentLoadMore = mListLoadMoreMap.get(adapter);

        if (mCurrentLoadMore == null) {
            mCurrentLoadMore = new LoadMoreImpl();
            mListLoadMoreMap.put(adapter, mCurrentLoadMore);
            mCurrentLoadMore.setListAdapter(adapter, view, listener);
            UILoader loadMore = new UILoader<List>(this) {
                @Override
                public List runBackground() {
                    return onLoadMoreList(mUiAdapter, mCurrentLoadMore.getCurrentPage(), mCurrentLoadMore.getPageSize());
                }
            };
            mCurrentLoadMore.setMoreLoader(loadMore);
            mCurrentLoadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener() {
                @Override
                public void onLoaded(boolean success, Object o) {
                    Logger.d("zl-loadmore", "setUIAdapter$onLoaded()... #success:" + success);
                    if (success) {
                        onUpdateListMore(o, mCurrentLoadMore.hasMore());
                    } else {
                        onUpdateListMore(o, false);
                    }
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRefreshListLayout != null) {
                                mRefreshListLayout.onRefreshComplete();
                            }
                        }
                    });

                    onListPageChanged(mCurrentLoadMore.getCurrentPage(), mCurrentLoadMore.getPageSize());
                }
            });
        }
    }

    protected boolean performRefresh() {
        if (NetworkState.available()) {
            Api.startNoCacheMode();
            finishAllLoaded(false);
            onStartUILoad();
            Api.stopNoCacheMode();
            return true;
        }
        return false;
    }

    public boolean isPullDownToRefreshing() {
        return mRefreshListLayout.isRefreshing()
                && PullToRefreshBase.Mode.PULL_FROM_START.equals(mRefreshListLayout.getMode());
    }

    /**
     * 执行加载更多
     *
     * @return
     */
    public final boolean performLoadMore() {
        if (mCurrentLoadMore != null) {
            return mCurrentLoadMore.doLoadMore(true);
        }

        return false;
    }

    /**
     * 获得加载更多的数据
     *
     * @param adapter
     * @param currentPage
     * @param pageSize
     * @return
     */
    protected List onLoadMoreList(BaseUIAdapter adapter, int currentPage, int pageSize) {
        return null;
    }

    /**
     * 当前列表加载页码
     *
     * @param currentPage
     * @param pageSize
     */
    protected void onListPageChanged(int currentPage, int pageSize) {

    }

    /**
     * 更新列表数据
     *
     * @param data
     * @param hasMore
     */
    protected void onUpdateListMore(Object data, boolean hasMore) {
        if (data instanceof List) {
            if (mUiAdapter != null) {
                mUiAdapter.addEntrys((List) data);
            }
        }
    }

    @Override
    public void onRefreshList() {
        super.onRefreshList();
        if (mUiAdapter != null) {
            mUiAdapter.notifyDataSetChanged();
        }
    }
}
