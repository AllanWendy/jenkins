package cn.wecook.app.features.pick;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.uikit.tools.LinearWrapLayout;

import cn.wecook.app.R;

/**
 * 晒厨艺选取布局视图
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/23/14
 * @deprecated TODO 待实现的功能
 */
public class PickLayout extends LinearWrapLayout {

    private DragHelperCallback mDragHelperCallback;
    private ViewDragHelper mDragHelper;

    private View mPickHolder;
    private View mPreview;
    private View mImagesListView;
    private View mTitleBar;
    private int mPickHolderMaxTop;
    private int mPickHolderCurrentTop;
    private int mPickHolderMinTop;
    private boolean mPickHolderDirectorUp;
    private boolean mInited;

    public PickLayout(Context context) {
        super(context);

        init(context);
    }

    public PickLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mDragHelperCallback = new DragHelperCallback();
        mDragHelper = ViewDragHelper.create(this, mDragHelperCallback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleBar = findViewById(R.id.uikit_title_bar);
        mPreview = findViewById(R.id.app_pick_preview_pages);
//        mPickHolder = findViewById(R.id.app_pick_holder);
        mImagesListView = null;//findViewById(R.id.app_pick_images);

        mPickHolderMinTop = ScreenUtils.dip2px(getContext(), 50);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return mDragHelper.shouldInterceptTouchEvent(ev);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = ScreenUtils.dip2px(getContext(), 75);
        ScreenUtils.resizeViewWithSpecial(mImagesListView, PhoneProperties.getScreenWidthInt(),
                PhoneProperties.getScreenHeightInt() - mPickHolderMinTop - height);

        measureVertical(widthMeasureSpec, heightMeasureSpec);
        Logger.i("pick", "onMeasure "
                        + " dragState : " + mDragHelper.getViewDragState()
        );
    }

    public void setChildFrame(View child, int left, int top, int width, int height) {
        if (mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            if (child == mImagesListView) {
                return;
            }
        }
        super.setChildFrame(child, left, top, width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE) {

            layoutVertical(l, t, r, b);
        }
        if (!mInited) {
            mInited = true;
            mPickHolderCurrentTop = mPickHolderMaxTop = mPickHolder.getTop();
        }

        Logger.i("pick", "onLayout maxTop : " + mPickHolderMaxTop
                        + " minTop : " + mPickHolderMinTop
                        + " dragState : " + mDragHelper.getViewDragState()
                        + " changed : " + changed
                        + " left : " + l
                        + " top : " + t
                        + " right : " + r
                        + " bottom : " + b
        );

    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mPickHolder && pointerId == 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            Logger.d("pick", "onViewPositionChanged view:" + changedView.getTop() + "|left:" + left + "|top:" + top + "| dx:" + dx + "|dy:" + dy);
            if (changedView == mPickHolder) {
                mPickHolderCurrentTop = mPickHolder.getTop();
                if (dy != 0) {
                    mPreview.offsetTopAndBottom(dy);
                    mTitleBar.offsetTopAndBottom(dy);
                    mImagesListView.offsetTopAndBottom(dy);
                }
            }
        }


        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
            Logger.d("pick", "onViewCaptured view:" + capturedChild.getTop());
            if (capturedChild == mPickHolder) {

            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            Logger.d("pick", "onViewReleased viewTop:" + releasedChild.getTop() + "|xvel:" + xvel + "|yvel:" + yvel);

            if (releasedChild == mPickHolder) {
                if (mPickHolderDirectorUp) {
                    mDragHelper.smoothSlideViewTo(mPickHolder, 0, mPickHolderMinTop);
                } else {
                    mDragHelper.smoothSlideViewTo(mPickHolder, 0, mPickHolderMaxTop);
                }
            }
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            Logger.d("pick", "getViewVerticalDragRange view:" + child.getTop());
            if (child == mPickHolder) {
                int current = mPickHolder.getTop();
                if (mPickHolderMaxTop > current && mPickHolderMinTop < current) {
                    return current;
                }
            }
            return super.getViewVerticalDragRange(child);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            Logger.d("pick", "clampViewPositionVertical view: " + child.getTop() + "|top:" + top + "|dy:" + dy);
            if (mPickHolder == child) {
                if (dy < 0) {
                    mPickHolderDirectorUp = true;
                } else {
                    mPickHolderDirectorUp = false;
                }

                if (mPickHolderMaxTop > top && mPickHolderMinTop < top) {
                    return top;
                } else {
                    return child.getTop();
                }

            }
            return super.clampViewPositionVertical(child, top, dy);
        }
    }
}
