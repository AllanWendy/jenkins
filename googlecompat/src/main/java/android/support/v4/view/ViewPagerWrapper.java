package android.support.v4.view;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * ViewPager包装类
 * @author kevin
 * @version v1.0
 * @since 2015-1/16/15
 */
public class ViewPagerWrapper extends ViewPager {

    private boolean isCancelSaveState = true;
    private boolean isFixed;

    public ViewPagerWrapper(Context context) {
        super(context);
    }

    public ViewPagerWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener listener) {
        return super.setInternalPageChangeListener(listener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !isFixed && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !isFixed && super.onTouchEvent(ev);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState superState = (SavedState) super.onSaveInstanceState();
        if (isCancelSaveState) {
            superState.position = 0;
            superState.adapterState = null;
            return superState;
        }
        return superState;
    }

    public void setCurrentItemSmooth(int item) {
        setCurrentItemInternal(item, true, true, 1);
    }

    public void cancelSaveState(boolean enable) {
        isCancelSaveState = enable;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }

    public boolean isFixed() {
        return isFixed;
    }

    @Override
    public boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        return super.addViewInLayout(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }
}
