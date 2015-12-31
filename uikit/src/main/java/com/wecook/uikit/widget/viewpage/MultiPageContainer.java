package com.wecook.uikit.widget.viewpage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPagerWrapper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.wecook.uikit.R;

/**
 * 可显示多页面的ViewPager容器
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/15/15
 */
public class MultiPageContainer extends FrameLayout implements ViewPager.OnPageChangeListener {

    private ViewPagerWrapper mPager;
    boolean mNeedsRedraw = false;

    private int marginsLeft;
    private int marginsRight;
    private int marginsPage;

    public MultiPageContainer(Context context) {
        super(context);
        init();
    }

    public MultiPageContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiPageContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    @SuppressLint("NewApi")
    private void init() {
        //Disable clipping of children so non-selected pages are visible
        setClipChildren(false);

        //Child clipping doesn't work with hardware acceleration in Android 3.x/4.x
        //You need to set this value here if using hardware acceleration in an
        // application targeted at these releases.
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onFinishInflate() {

        marginsLeft = (int) (getContext().getResources().getDisplayMetrics().density * 30);
        marginsRight = (int) (getContext().getResources().getDisplayMetrics().density * 30);
        marginsPage = (int) (getContext().getResources().getDisplayMetrics().density * 10);

        mPager = new ViewPagerWrapper(getContext());
        mPager.setId(R.id.viewpager);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(marginsLeft, 0, marginsRight, 0);
        addView(mPager, lp);

        mPager.setPageMargin(marginsPage);
        mPager.setInternalPageChangeListener(this);
        mPager.setClipChildren(false);
        mPager.setOffscreenPageLimit(2);
    }

    public ViewPager getViewPager() {
        return mPager;
    }

    private Point mCenter = new Point();
    private Point mInitialTouch = new Point();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCenter.x = w / 2;
        mCenter.y = h / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //We capture any touches not already handled by the ViewPager
        // to implement scrolling from a touch outside the pager bounds.
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_DOWN:
                mInitialTouch.x = (int) ev.getX();
                mInitialTouch.y = (int) ev.getY();
                break;
            default:
                int offsetX = 0;
                int offsetY = 0;
                if (mInitialTouch.x < marginsLeft + marginsPage) {
                    offsetX = Math.abs(mInitialTouch.x - (marginsLeft + marginsPage));
                } else if (mInitialTouch.x > mPager.getWidth() + marginsPage + marginsLeft) {
                    offsetX = -Math.abs(mInitialTouch.x - (mPager.getWidth() + marginsPage + marginsLeft));
                }
                ev.offsetLocation(offsetX, offsetY);
                break;
        }

        return mPager.dispatchTouchEvent(ev);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //Force the container to redraw on scrolling.
        //Without this the outer pages render initially and then stay static
        if (mNeedsRedraw) ViewCompat.postInvalidateOnAnimation(this);
    }


    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mNeedsRedraw = (state != ViewPager.SCROLL_STATE_IDLE);
    }
}
