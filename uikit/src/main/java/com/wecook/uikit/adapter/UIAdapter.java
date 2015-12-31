package com.wecook.uikit.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * <p>数据适配器<p/> 带有ViewHolder机制<br/> 重载{@link BaseUIAdapter#newView(int)}方法得到新的view<br/> 通过{@link
 * UIAdapter#findViewById(int)}方法找到viewHolder中的视图<br/>
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/7/14
 */
public abstract class UIAdapter<T> extends BaseUIAdapter<T, UIAdapter.UIViewHolder<T>> {

    private UIViewHolder<T> mCurrentViewHolder;

    public UIAdapter(Context context, List<T> data) {
        this(context, 0, data);
    }

    public UIAdapter(Context context, int itemLayoutResId, List<T> data) {
        super(context, itemLayoutResId, data);
    }

    @Override
    public final UIViewHolder<T> newViewHolder(int position) {
        return new UIViewHolder<T>(this, mViewHolderController);
    }

    /**
     * 获得viewHolder的视图view
     *
     * @param viewId
     * @return
     */
    public View findViewById(int viewId) {
        if (mCurrentViewHolder != null) {
            return mCurrentViewHolder.findViewById(viewId);
        }
        return null;
    }

    public View getItemView() {
        if (mCurrentViewHolder != null) {
            return mCurrentViewHolder.getView();
        }
        return null;
    }

    private void preUpdate(UIViewHolder<T> uiViewHolder) {
        mCurrentViewHolder = uiViewHolder;
    }

    /**
     * 更新数据
     *
     * @param position
     * @param data
     */
    public void updateView(int position, int viewType, T data, Bundle extra) {
    }

    /**
     * <p>视图ViewHolder<p/>
     *
     * @param <T>
     */
    public static class UIViewHolder<T> extends ViewHolder<T> {

        private View mCurrentConvertView;
        private UIAdapter<T> mAdapter;
        private ArrayMap<Integer, View> mHoldViews = new ArrayMap<>();

        public UIViewHolder(UIAdapter<T> adapter, ViewHolderController<T> controller) {
            super(controller);
            mAdapter = adapter;
        }

        @Override
        public void create(int position, int viewType, View convertView, ViewGroup parent) {
        }

        @Override
        public void update(int position, int viewType, T obj, Bundle extra, View itemView) {
            mAdapter.preUpdate(this);
            mCurrentConvertView = itemView;
            mAdapter.updateView(position, viewType, obj, extra);
        }

        /**
         * 查找视图
         *
         * @param viewId
         * @return
         */
        public View findViewById(int viewId) {
            if (mCurrentConvertView != null) {

                View item = null;
                if (mHoldViews != null) {
                    item = mHoldViews.get(viewId);
                }
                if (item == null) {
                    item = mCurrentConvertView.findViewById(viewId);
                    mHoldViews.put(viewId, item);
                }
                return item;
            }
            return null;
        }

        public View getView() {
            return mCurrentConvertView;
        }
    }

}
