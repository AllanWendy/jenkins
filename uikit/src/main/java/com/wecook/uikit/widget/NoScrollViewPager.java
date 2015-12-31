package com.wecook.uikit.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * 滑动可控的Viewpager
 * Created by simon on 15/9/12.
 */
public class NoScrollViewPager extends ViewPager {
    private boolean noScroll = true;
    private float minMove = 10;
    private ScrollListener scrollListener;
    private float downX;
    private int currentItemPosition = -1;
    private long lastTime;

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float upX = ev.getX();
                if (Math.abs(downX - upX) > minMove && (lastTime == 0 || Math.abs(lastTime - System.currentTimeMillis()) > 1200)) {
                    lastTime = System.currentTimeMillis();
                    int currentItem = getCurrentItem();
                    int tempItem = currentItem;
                    if (downX > upX) {
                        if (++tempItem < getAdapter().getCount()) {
                            if (currentItem != currentItemPosition) {
                                currentItemPosition = currentItem;
                                if (scrollListener != null) {
                                    scrollListener.scroll(tempItem, currentItem, ScrollListener.TYPE_RIGHT_SCROLL);
                                }
                            }
                        }
                    } else {
                        if (--tempItem >= 0) {
                            if (currentItem != currentItemPosition) {
                                currentItemPosition = currentItem;
                                if (scrollListener != null) {
                                    scrollListener.scroll(tempItem, currentItem, ScrollListener.TYPE_LEFT_SCROLL);
                                }
                            }
                        }
                    }
                }
                break;
        }
            return true;
    }

    /**
     * 设置滑动监听
     *
     * @param scrollListener
     */
    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    /**
     * 左右滑动监听
     */
    public static interface ScrollListener {
        public static int TYPE_LEFT_SCROLL = 0;
        public static int TYPE_RIGHT_SCROLL = 1;

        void scroll(int inPosition, int outPosition, int type);
    }
}
