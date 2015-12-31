package com.wecook.uikit.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by LK on 2015/9/1.
 */
public class GradientView extends View {
    public GradientView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GradientView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = getHeight();
        int width = getWidth();
        if (height <= 0 || width <= 0) return;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float radio = (float) Math.sqrt(Math.pow(height / 2, 2) + Math.pow(width / 2, 2)) - 10;
        float middle_radio = (float) ((Math.max(width / 2, height / 2) - 15) / radio) * 0.95f;
        RadialGradient gradient = new RadialGradient(
                getWidth() / 2,
                getHeight() / 2,
                radio,
                new int[]{Color.argb(0, 0, 0, 0), Color.argb(0, 0, 0, 0), Color.argb(60, 0, 0, 0)},
                new float[]{0, middle_radio, 1},
                android.graphics.Shader.TileMode.CLAMP);
        paint.setDither(true);
        paint.setShader(gradient);

        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
}
