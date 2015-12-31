package com.wecook.uikit.adapter;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
/**
 * 每一个Item的视图控制类
 *
 * @author kevin
 * @date 14-1-8
 */
public abstract class ViewHolder<T> {
    ViewHolderController<T> mAdapter;
	int mPosition;
	int mViewType;

	public ViewHolder(ViewHolderController<T> controller) {
		mAdapter = controller;
	}

	/**
	 * 生产指定位置的视图对象
	 * @param position
	 * @param viewType
     * @param convertView
     * @param parent
     */
	public abstract void create(int position, int viewType, View convertView, ViewGroup parent);

	/**
	 * 更新指定位置的项视图
     * @param position
     * @param viewType
     * @param obj
     * @param itemView
     */
	public abstract void update(int position, int viewType, T obj, Bundle extra, View itemView);

	/**
     * @param position
     * @param itemView
     */
	void updateInternal(int position, int viewType, View itemView) {
		mPosition = position;
        mViewType = viewType;
        T data = mAdapter.getItem(position);
        Bundle bundle = null;
        if (mAdapter.getViewObject(position) != null) {
            bundle = mAdapter.getViewObject(position).mExtraData;
        }

		update(position, viewType, data, bundle, itemView);
	}

	public int getPosition() {
		return mPosition;
	}

	public T getData() {
		return mAdapter.getItem(mPosition);
	}

    /**
     * 每一个Item的数据源封装,提供一个扩展属性配置 {@code mExtraData}
     *
     * @author kevin
     * @date 14-1-8
     */
    public static class ViewObject<T> {

        public T mData;
        public Bundle mExtraData;

        public ViewObject(T data) {
            mData = data;
            mExtraData = new Bundle();
        }

        public ViewObject(T data, Bundle bundle) {
            mData = data;
            if (bundle != null) {
                mExtraData = new Bundle(bundle);
            } else {
                mExtraData = new Bundle();
            }
        }
    }

}