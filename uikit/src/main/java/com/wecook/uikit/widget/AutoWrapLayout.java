package com.wecook.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.wecook.uikit.tools.LinearWrapLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动换行
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/29/14
 */
public class AutoWrapLayout extends LinearWrapLayout {

    private List<ChildWrapState> mChildWrapStates = new ArrayList<ChildWrapState>();

    private class ChildWrapState {
        int line;
        View child;

        int offsetX;
        int offsetY;

        @Override
        public boolean equals(Object o) {
            if (o instanceof ChildWrapState) {
                return ((ChildWrapState) o).child.equals(child);
            } else if (o instanceof View) {
                return o.equals(child);
            }
            return super.equals(o);
        }
    }

    public AutoWrapLayout(Context context) {
        super(context);
    }

    public AutoWrapLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        int line = 0;
        int offsetX;
        int offsetY = getPaddingTop();
        int freeWidth = width;
        int maxHeight = 0;
        int nextOffsetX = getPaddingLeft();
        mChildWrapStates.clear();
        for (int index = 0; index < getVirtualChildCount(); index++) {
            View child = getVirtualChildAt(index);
            child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));

            freeWidth -= child.getMeasuredWidth();

            maxHeight = Math.max(maxHeight, child.getMeasuredHeight());

            if (freeWidth < 0) {
                line++;
                offsetY += maxHeight;
                //初始化
                freeWidth = width - child.getMeasuredWidth();
                maxHeight = 0;
                offsetX = 0;
                nextOffsetX = child.getMeasuredWidth();
            } else {
                offsetX = nextOffsetX;
                nextOffsetX += child.getMeasuredWidth();
            }

            ChildWrapState state = new ChildWrapState();
            state.child = child;
            state.line = line;
            state.offsetX = offsetX;
            state.offsetY = offsetY;
            mChildWrapStates.add(state);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setChildFrame(View child, int left, int top, int width, int height) {
        super.setChildFrame(child, getWrapOffsetX(child), getWrapOffsetY(child), width, height);
    }

    private int getWrapOffsetY(View child) {
        for (ChildWrapState state : mChildWrapStates) {
            if (state.child.equals(child)) {
                return state.offsetY;
            }
        }
        return 0;
    }

    private int getWrapOffsetX(View child) {
        for (ChildWrapState state : mChildWrapStates) {
            if (state.child.equals(child)) {
                return state.offsetX;
            }
        }
        return 0;
    }
}
