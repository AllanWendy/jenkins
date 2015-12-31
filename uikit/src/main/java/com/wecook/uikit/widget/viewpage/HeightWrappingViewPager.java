package com.wecook.uikit.widget.viewpage;

import android.content.Context;
import android.support.v4.view.ViewPagerWrapper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * 限制ViewPager调用多次onMeasure
 *
 * @author kevin created at 7/21/14
 * @version 1.0
 */
public class HeightWrappingViewPager extends ViewPagerWrapper {
    private boolean mIsDragBack;

    public HeightWrappingViewPager(Context context) {
        super(context);
    }


    public HeightWrappingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsDragBack = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    mIsDragBack = true;
                    break;
            }
            if (getAdapter() != null && getAdapter().getCount() > 0) {
                return super.onTouchEvent(ev);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mIsDragBack = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setCurrentItem(int item) {
        mIsDragBack = false;
        super.setCurrentItem(item);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        mIsDragBack = false;
        super.setCurrentItem(item, smoothScroll);
    }

    public boolean isDragBack() {
        return mIsDragBack;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boolean wrapHeight = MeasureSpec.getMode(heightMeasureSpec)
                == MeasureSpec.AT_MOST;

        if (wrapHeight) {
            /**
             * The first super.onMeasure call made the pager take up all the
             * available height. Since we really wanted to wrap it, we need
             * to remeasure it. Luckily, after that call the first child is
             * now available. So, we take the height from it.
             */

            int width = getMeasuredWidth(), height = getMeasuredHeight();

            // Use the previously measured width but simplify the calculations
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

            /* If the pager actually has any children, take the first child's
             * height and call that our own */
            if (getChildCount() > 0) {
                View firstChild = getChildAt(0);

                /* The child was previously measured with exactly the full height.
                 * Allow it to wrap this time around. */
                firstChild.measure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));

                height = firstChild.getMeasuredHeight();
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
