package com.wecook.uikit.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.wecook.uikit.widget.decorate.Decorative;
import com.wecook.uikit.widget.decorate.DecorativeFactory;

/**
 * 可装饰的图片
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/24/14
 */
public class DecorativeImageView extends ImageView {

    private Decorative mDecorative;

    public DecorativeImageView(Context context) {
        super(context);
        init();
    }

    public DecorativeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DecorativeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDecorative = DecorativeFactory.create(DecorativeFactory.TYPE_GRID);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mDecorative != null) {
            mDecorative.size(w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDecorative != null) {
            mDecorative.render(canvas);
        }
    }

}
