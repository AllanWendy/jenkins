package com.wecook.uikit.loader;

import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ListView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.network.NetworkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 加载更多的实现
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/7/14
 */
public class LoadMoreImpl<T extends Collection> implements LoadMore<T> {
    /**
     * 最大自动加载次数
     */
    private static final int MAX_TIMES_FOR_AUTOLOAD_MORE = 3;

    /**
     * 页码最大内容数
     */
    private static final int MAX_PAGE_SIZE = 20;

    /**
     * 最大页码
     */
    private static final int MAX_PAGE = 500;

    /**
     * 是否为用户手动刷新
     */
    private boolean mIsUserRefresh;

    /**
     * 是否当前正在加载中
     */
    private boolean mIsLoadingMore;

    /**
     * 是否能加载更多
     */
    private boolean mHasMore = true;
    private int mStartPage = 1;
    private int mEndPage = mStartPage + MAX_PAGE;
    private int mPageSize = MAX_PAGE_SIZE;
    private int mGetMorePageSize;
    private int mOneItemWeight = 1;
    private int mPreAutoTime;
    private int mHeadViewCount;
    private int mMaxAutoLoadCount = MAX_TIMES_FOR_AUTOLOAD_MORE;

    private int mTotalLoadedSize;//所有加载数据的大小

    private AtomicInteger mAutoLoadTimes = new AtomicInteger(0);
    private AtomicInteger mCurrentPage = new AtomicInteger(mStartPage);
    private UILoader<T> mLoadMoreLoader;
    private Adapter mAdapter;
    private List<OnLoadMoreListener<T>> mListLoadMoreListener = new ArrayList<OnLoadMoreListener<T>>();
    private AbsListView.OnScrollListener mInnerOnScrollListener;
    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        private boolean mIsAtBottom;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mInnerOnScrollListener != null) {
                mInnerOnScrollListener.onScrollStateChanged(view, scrollState);
            }
            if (mIsLoadingMore) {
                return;
            }
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && mIsAtBottom) {
                //加载数据代码，此处省略了
                autoLoadMore();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            Logger.d("totalItemCount:" + totalItemCount + "|visibleItemCount:" + visibleItemCount
//                    + "|firstVisibleItem:" + firstVisibleItem + "|mHeadViewCount:" + mHeadViewCount);
//            Logger.d("count = " + (totalItemCount - visibleItemCount - firstVisibleItem));
//            Logger.d("result = " + (totalItemCount - visibleItemCount - firstVisibleItem <= mHeadViewCount + 1));

            if (mInnerOnScrollListener != null) {
                mInnerOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
            if (totalItemCount - visibleItemCount - firstVisibleItem <= mHeadViewCount + 1) {
                mIsAtBottom = true;
            } else {
                mIsAtBottom = false;
            }
        }
    };

    /**
     * 设置页码范围
     *
     * @param startPage
     * @param endPage
     */
    @Override
    public void setPageRange(int startPage, int endPage) {
        mStartPage = startPage;
        if (endPage < MAX_PAGE) {
            mEndPage = endPage;
        } else {
            mEndPage = MAX_PAGE;
        }
        mCurrentPage.set(startPage);
    }

    /**
     * 增加1并返回页码
     *
     * @return
     */
    @Override
    public int incrementAndGetPage() {
        int page = mCurrentPage.incrementAndGet();
        if (page > mEndPage) {
            mCurrentPage.set(mEndPage);
        }
        return mCurrentPage.get();
    }

    /**
     * 获得当前页码
     *
     * @return
     */
    @Override
    public int getCurrentPage() {
        return mCurrentPage.get();
    }

    /**
     * 设置当前页码
     *
     * @param page
     */
    @Override
    public void setCurrentPage(int page) {
        if (page >= mStartPage && page < mEndPage) {
            mCurrentPage.set(page);
        } else if (page < mStartPage) {
            mCurrentPage.set(mStartPage);
        } else if (page >= mEndPage) {
            mCurrentPage.set(mEndPage);
        }
    }

    /**
     * 获得页码内容大小
     *
     * @return
     */
    @Override
    public int getPageSize() {
        return mPageSize;
    }

    /**
     * 设置页码内容大小
     *
     * @param pageSize
     */
    @Override
    public void setPageSize(int pageSize) {
        mPageSize = pageSize;
    }

    /**
     * @param adapter
     * @param view
     */
    @Override
    public void setListAdapter(final Adapter adapter, AbsListView view) {
        setListAdapter(adapter, view, null);
    }

    public void setListAdapter(final Adapter adapter, AbsListView view, AbsListView.OnScrollListener scrollListener) {
        if (view != null) {
            if (view instanceof ListView) {
                mHeadViewCount = ((ListView) view).getFooterViewsCount();
            }
            mInnerOnScrollListener = scrollListener;
            view.setOnScrollListener(mOnScrollListener);
        }
        mAdapter = adapter;
    }

    @Override
    public void setMoreLoader(UILoader<T> loader) {
        mLoadMoreLoader = loader;
    }

    @Override
    public void preformFinished(T o) {
        //update hasMore state..
        if (o != null) {
            setMorePageSize(o.size());
        } else {
            setMorePageSize(0);
        }

        Logger.d("zl-loadmore", "onLoaded()...mGetMorePageSize = " + mGetMorePageSize);

        //dispatch listener...
        dispatchListener(o != null, o);

        mIsLoadingMore = false;
        mIsUserRefresh = false;
    }

    @Override
    public void setMorePageSize(int size) {
        mGetMorePageSize = size * mOneItemWeight;
        if (mGetMorePageSize < getPageSize()) {
            mHasMore = false;
        } else {
            mHasMore = true;
        }
    }

    @Override
    public boolean hasMore() {
        return mHasMore;
    }

    @Override
    public boolean doLoadMore(boolean userRefresh) {
        Logger.d("zl-loadmore", "onLoadMoreList()...");
        mIsUserRefresh = userRefresh;
        return autoLoadMore();
    }

    private void dispatchListener(boolean success, T o) {
        for (OnLoadMoreListener<T> listener : mListLoadMoreListener) {
            if (listener != null) {
                listener.onLoaded(success, o);
            }
        }
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener<T> loadMoreListener) {
        mListLoadMoreListener.remove(loadMoreListener);
        mListLoadMoreListener.add(loadMoreListener);
    }

    @Override
    public boolean isAutoRefresh() {
        return mIsUserRefresh;
    }

    @Override
    public void reset() {
        mCurrentPage.set(mStartPage);
        mAutoLoadTimes.set(0);
        mHasMore = true;
        mGetMorePageSize = 0;
    }

    @Override
    public void setOneItemWeight(int weight) {
        mOneItemWeight = weight;
    }

    @Override
    public void setAutoLoadCount(int count) {
        mMaxAutoLoadCount = count;
    }

    @Override
    public void enableAutoLoad(boolean enable) {
        if (!enable) {
            mPreAutoTime = mAutoLoadTimes.get();
            mAutoLoadTimes.set(MAX_TIMES_FOR_AUTOLOAD_MORE);
        } else {
            mAutoLoadTimes.set(mPreAutoTime);
        }
    }

    /**
     * 自动加载更多，限制3次
     */
    private boolean autoLoadMore() {
        Logger.d("zl-loadmore", "autoLoadMore()... " +
                "mIsLoadingMore:" + mIsLoadingMore + "|mHasMore:" + mHasMore
                + "| autoTime: " + mAutoLoadTimes.get()
                + "| mIsUserRefresh : " + mIsUserRefresh);

        if (mAdapter != null && mAutoLoadTimes.get() == 0) {
            setMorePageSize(mAdapter.getCount());
        }

        if (NetworkState.available() && !mIsLoadingMore /*&& hasMore()*/) {
            int time = mAutoLoadTimes.get();
            if (!mIsUserRefresh) {
                time = mAutoLoadTimes.incrementAndGet();
            }
            if (time < mMaxAutoLoadCount || mIsUserRefresh) {
                int current = getCurrentPage();
                int next = incrementAndGetPage();
                if (current != next) {
                    callOnListLoadMore(next, getPageSize());
                    return true;
                }
                mIsUserRefresh = false;
                setMorePageSize(0);
                Logger.d("zl-loadmore", "autoLoadMore()...ToEnd! current:" + current + "|next:" + next);
            }
        }

        dispatchListener(false, null);
        return false;
    }

    /**
     * 调用加载器
     *
     * @param page
     * @param pageSize
     */
    private void callOnListLoadMore(int page, int pageSize) {
        Logger.d("zl-loadmore", "autoLoadMore()...#page:" + page + "|pageSize:" + pageSize + "|mLoadMoreLoader:" + mLoadMoreLoader);
        if (mLoadMoreLoader != null) {
            mIsLoadingMore = true;
            Bundle params = new Bundle();
            params.putInt(EXTRA_PAGE_NO, page);
            params.putInt(EXTRA_PAGE_SIZE, pageSize);
            mLoadMoreLoader.setUILoaderListener(new UILoader.UILoaderListener<T>() {
                @Override
                public void onLoaded(T o) {
                    preformFinished(o);
                }
            });
            Logger.d("zl-loadmore", "start load more...");
            mLoadMoreLoader.start(params);
        }
    }

}
