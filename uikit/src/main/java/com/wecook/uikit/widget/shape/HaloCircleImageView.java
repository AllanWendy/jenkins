package com.wecook.uikit.widget.shape;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.wecook.uikit.R;


/**
 * http://blog.csdn.net/lmj623565791/article/details/41967509
 *
 * @author zhy
 */
public class HaloCircleImageView extends BaseImageView {

    /**
     * 绘图的Paint
     */
    private Paint mBitmapPaint;
    /**
     * 3x3 矩阵，主要用于缩小放大
     */
    private Matrix mMatrix;
    /**
     * 渲染图像，使用图像为绘制图形着色
     */
    private BitmapShader mBitmapShader;
    /**
     * 圆角的半径
     */
    private int mRadius;
    /**
     * view的宽度
     */
    private int mWidth;
    private RectF mRoundRect;


    private int mHaloOutColor;
    private int mHaloInColor;
    private int mHaloMidColor;
    private int mHaloForegroundColor;
    private float mHaloMidStrokeWidth;
    private float mHaloOutStrokeWidth;
    private float mHaloInStrokeWidth;

    public HaloCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributeSet(context, attrs);
    }

    public HaloCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributeSet(context, attrs);
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HaloCircleImageView);
        mHaloOutColor = a.getColor(R.styleable.HaloCircleImageView_outColor, 0);
        mHaloOutStrokeWidth = a.getDimensionPixelSize(R.styleable.HaloCircleImageView_outStrokeWidth, 2);
        mHaloInColor = a.getColor(R.styleable.HaloCircleImageView_inColor, 0);
        mHaloInStrokeWidth = a.getDimensionPixelSize(R.styleable.HaloCircleImageView_inStrokeWidth, 4);
        mHaloMidColor = a.getColor(R.styleable.HaloCircleImageView_midColor, 0xffffffff);
        mHaloMidStrokeWidth = a.getDimensionPixelSize(R.styleable.HaloCircleImageView_midStrokeWidth, 20);
        mHaloForegroundColor = a.getColor(R.styleable.HaloCircleImageView_foregroundColor, 0);
        mBitmapPaint.setColor(mHaloInColor);
        a.recycle();
    }

    public HaloCircleImageView(Context context) {
        this(context, null);
    }

    public void setOutColor(int color) {
        mHaloOutColor = color;
    }

    public void setInColor(int color) {
        mHaloInColor = color;
    }

    public void setForegroundColor(int color) {
        mHaloForegroundColor = color;
        postInvalidate(getLeft(), getTop(), getRight(), getBottom());
    }

    public void enableHalo(boolean enable) {
        setEnableShape(enable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /**
         * 如果类型是圆形，则强制改变view的宽高一致，以小值为准
         */
        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mRadius = mWidth / 2;
        setMeasuredDimension(mWidth, mWidth);

    }

    /**
     * 初始化BitmapShader
     */
    private void setUpShader() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        Bitmap bmp = drawableToBitamp(drawable);
        // 将bmp作为着色器，就是在指定区域内绘制bmp
        mBitmapShader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
        float scale = 1.0f;
        // 拿到bitmap宽或高的小值
        int bSize = Math.min(bmp.getWidth(), bmp.getHeight());
        scale = mWidth * 1.0f / bSize;
        // shader的变换矩阵，我们这里主要用于放大或者缩小
        mMatrix.setScale(scale, scale);
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(mMatrix);
        // 设置shader
        mBitmapPaint.setShader(mBitmapShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null || getDrawable().getIntrinsicHeight() <= 0 || getDrawable().getIntrinsicWidth() <= 0) {
            drawGroundImage(canvas);
            return;
        }
        setUpShader();
        canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
        drawGroundImage(canvas);
    }

    @Override
    public Bitmap getBitmap() {
        return null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitamp(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    private static final String STATE_INSTANCE = "state_instance";
    private static final String STATE_TYPE = "state_type";
    private static final String STATE_BORDER_RADIUS = "state_border_radius";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(((Bundle) state)
                    .getParcelable(STATE_INSTANCE));
        } else {
            super.onRestoreInstanceState(state);
        }

    }

    public void setBorderRadius(int borderRadius) {
        int pxVal = dp2px(borderRadius);
        invalidate();
    }


    public int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * 画环
     *
     * @param canvas
     */
    private void drawGroundImage(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }
        Paint paint = new Paint();
        Rect rect = getDrawable().copyBounds();
        //中间环
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(mHaloMidColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mHaloMidStrokeWidth);
        canvas.drawCircle(rect.centerX(), rect.centerY(), getWidth() / 2 - mHaloMidStrokeWidth / 2, paint);

        //内圈
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(mHaloInColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mHaloInStrokeWidth);
        canvas.drawCircle(rect.centerX(), rect.centerY(), getWidth() / 2 - mHaloMidStrokeWidth, paint);

        //外圈
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(mHaloOutColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mHaloOutStrokeWidth);
        canvas.drawCircle(rect.centerX(), rect.centerY(), getWidth() / 2 - mHaloOutStrokeWidth, paint);

        //前景色
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(mHaloForegroundColor);
        paint.setStyle(Paint.Style.FILL);
        float totalStrokeWidth = mHaloMidStrokeWidth;
        canvas.drawCircle(rect.centerX(), rect.centerY(), getWidth() / 2 - totalStrokeWidth, paint);
    }

}