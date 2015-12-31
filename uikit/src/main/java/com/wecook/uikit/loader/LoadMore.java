package com.wecook.uikit.loader;

import android.widget.AbsListView;
import android.widget.Adapter;

import java.util.Collection;

/**
 * 列表加载更多管理者
 *
 * @author by kevin on 7/22/14.
 */
public interface LoadMore<T extends Collection> {

    public static final String EXTRA_PAGE_NO = "page";
    public static final String EXTRA_PAGE_SIZE = "page_size";

    /**
     * 设置页码范围<br/>
     * 默认为1～500
     *
     * @param startPage
     */
    public void setPageRange(int startPage, int endPage);

    /**
     * 增加1并返回页码
     *
     * @return
     */
    public int incrementAndGetPage();

    /**
     * 获得当前页码
     *
     * @return
     */
    public int getCurrentPage();

    /**
     * 设置当前页码
     *
     * @param page 大小范围可以通过{@link #setCurrentPage(int)} 设置起始页码 {@link #getPageSize()} 获得最大
     */
    public void setCurrentPage(int page);

    /**
     * 获得页码内容大小
     *
     * @return
     */
    public int getPageSize();

    /**
     * 设置页码内容大小
     *
     * @param pageSize
     */
    public void setPageSize(int pageSize);

    /**
     * 设置要进行加载的列表
     *
     * @param adapter
     * @param view
     */
    public void setListAdapter(final Adapter adapter, AbsListView view);

    public void setListAdapter(final Adapter adapter, AbsListView view, AbsListView.OnScrollListener listener);
    /**
     * @param loader
     * @return
     */
    public void setMoreLoader(UILoader<T> loader);

    /**
     * @param obj
     */
    public void preformFinished(T obj);

    /**
     * 设置加载更多之后，获得的数量
     *
     * @param size
     */
    public void setMorePageSize(int size);

    /**
     * 是否可以加载更多
     *
     * @return
     */
    public boolean hasMore();

    /**
     * 执行加载
     *
     * @param userRefresh
     * @return
     */
    public boolean doLoadMore(boolean userRefresh);

    /**
     * 设置加载监听
     *
     * @param loadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener<T> loadMoreListener);

    /**
     * 自动刷新
     *
     * @return
     */
    public boolean isAutoRefresh();

    /**
     * 重新设置
     */
    public void reset();

    /**
     * 设置单项数据权重比例
     *
     * @param weight
     */
    public void setOneItemWeight(int weight);

    public void setAutoLoadCount(int count);

    /**
     * 是否开启自动加载
     *
     * @param enable
     */
    public void enableAutoLoad(boolean enable);

    public interface OnLoadMoreListener<T> {
        void onLoaded(boolean success, T o);
    }
}