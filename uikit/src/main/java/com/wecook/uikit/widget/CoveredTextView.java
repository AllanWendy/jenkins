package com.wecook.uikit.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.wecook.uikit.R;

/**
 * 带按下效果
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/17/14
 */
public class CoveredTextView extends TextView {

    public static final int DEFAULT_MULTIPLY_COLOR = Color.argb(50, 230, 230, 230);
    public static final int CLEAR_MULTIPLY_COLOR = 0;
    private boolean mTouched;
    private int mMultiplyColor = DEFAULT_MULTIPLY_COLOR;

    public CoveredTextView(Context context) {
        super(context);
        initText();
    }

    public CoveredTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initText();
    }

    public CoveredTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initText();
    }

    private void initText() {
        setTextColor(getResources().getColorStateList(R.color.uikit_bt_default_font_orange));
    }

    public void setMultiplyColor(int color) {
        mMultiplyColor = color;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouched = true;
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                mTouched = false;
                postInvalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable[] drawables = getCompoundDrawables();
        if (isEnabled()) {
            for (Drawable drawable : drawables) {
                if (mTouched) {
                    if (drawable != null) {
                        if (mMultiplyColor != CLEAR_MULTIPLY_COLOR) {
                            drawable.setColorFilter(mMultiplyColor, PorterDuff.Mode.MULTIPLY);
                        }
                    }
                } else {
                    if (drawable != null) {
                        drawable.clearColorFilter();
                    }
                }
            }
        }
    }
}
