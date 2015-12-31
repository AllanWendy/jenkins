package com.wecook.uikit.view;

import android.content.Context;
import android.view.ViewTreeObserver;

/**
 * 动画视图
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/18/14
 */
public class BaseAnimatorView extends BaseView {

    private int mLayoutId;
    private AnimatorBuilder mBuilder;

    public BaseAnimatorView(Context context, int layoutId, AnimatorBuilder builder) {
        super(context);
        mLayoutId = layoutId;
        mBuilder = builder;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);

    }

    /**
     * 初始化视图
     */
    public void initView() {
        loadLayout(mLayoutId, null);
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                initAnimation();
                return true;
            }
        });
    }

    /**
     * 初始化动画
     */
    protected void initAnimation() {

    }

    public void startAnimation() {

    }

    public void pauseAnimation() {

    }

    public void stopAnimation() {

    }

    public static class AnimatorBuilder {

    }

}
