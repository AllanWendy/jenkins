// Copy right WeCook Inc.
package com.wecook.uikit.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.uikit.fragment.BaseFragment;

/**
 * 基础视图
 *
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public abstract class BaseView<T> extends FrameLayout implements IView {

    protected BaseFragment fragment;
    protected T data;
    private boolean hasInflated;

    public BaseView(Context context) {
        super(context);
    }

    public BaseView(BaseFragment fragment) {
        super(fragment.getContext());
        this.fragment = fragment;
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void loadLayout(int layoutId, final T data) {
        loadLayout(layoutId, data, true);
    }

    public void loadLayout(int layoutId, final T data, final boolean update) {
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(layoutId, this, true);
        hasInflated = false;
        this.data = data;
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                onFinishInflate();
                hasInflated = true;
                if (update) {
                    updateView(data);
                }
            }
        });
    }

    public void updateView(T obj) {

    }

    public void updateView() {
        if (data != null && hasInflated) {
            updateView(data);
        }
    }

    public BaseFragment getFragment() {
        return fragment;
    }

    public T getData() {
        return data;
    }

}
