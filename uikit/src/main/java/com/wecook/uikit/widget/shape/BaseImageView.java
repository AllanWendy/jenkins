package com.wecook.uikit.widget.shape;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Mostafa Gazar on 11/2/13.
 */
public abstract class BaseImageView extends ImageView {
    private static final String TAG = BaseImageView.class.getSimpleName();

    protected Context mContext;

    private static final Xfermode sXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    private Bitmap mMaskBitmap;
    private Paint mPaint;
    private boolean mEnableShape = true;

    public BaseImageView(Context context) {
        super(context);
        sharedConstructor(context);
    }

    public BaseImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor(context);
    }

    public BaseImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedConstructor(context);
    }

    private void sharedConstructor(Context context) {
        mContext = context;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setEnableShape(boolean enableShape) {
        if (mEnableShape != enableShape) {
            mEnableShape = enableShape;
            invalidate();
        }
    }

//    @Override
//    public void setImageDrawable(Drawable drawable) {
//        mNeedUpdated = true;
//        super.setImageDrawable(drawable);
//    }
//
//    @Override
//    public void setImageResource(int resId) {
//        mNeedUpdated = true;
//        super.setImageResource(resId);
//
//    }
//
//    @Override
//    public void setImageURI(Uri uri) {
//        mNeedUpdated = true;
//        super.setImageURI(uri);
//    }

    public void invalidate() {
        if (mMaskBitmap != null) {
            mMaskBitmap.recycle();
        }
        super.invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInEditMode()) {
            int i = canvas.saveLayer(0.0f, 0.0f, getWidth(), getHeight(),
                    null, Canvas.ALL_SAVE_FLAG);
            try {
                Bitmap bitmap = null;//mWeakBitmap != null ? mWeakBitmap.get() : null;
                // Bitmap not loaded.
                Drawable drawable = getDrawable();
                if (drawable != null) {
                    // Allocation onDraw but it's ok because it will not always be called.
                    bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas bitmapCanvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, getWidth(), getHeight());
                    drawable.draw(bitmapCanvas);

                    if (mEnableShape) {
                        // If mask is already set, skip and use cached mask.
                        if (mMaskBitmap == null || mMaskBitmap.isRecycled()) {
                            mMaskBitmap = getBitmap();
                        }

                        // Draw Bitmap.
                        mPaint.reset();
                        mPaint.setFilterBitmap(false);
                        mPaint.setXfermode(sXfermode);
                        bitmapCanvas.drawBitmap(mMaskBitmap, 0.0f, 0.0f, mPaint);

                        decorateBitmap(bitmapCanvas, drawable.copyBounds(), mPaint);
                    }
                }

                // Bitmap already loaded.
                if (bitmap != null) {
                    mPaint.reset();
                    canvas.drawBitmap(bitmap, 0.0f, 0.0f, mPaint);
                    return;
                }
            } catch (Exception e) {
                System.gc();

                Log.e(TAG, String.format("Failed to draw, Id :: %s. Error occurred :: %s", getId(), e.toString()));
            } finally {
                canvas.restoreToCount(i);
            }
        } else {
            super.onDraw(canvas);
        }
    }

    /**
     * 修饰图片
     *
     * @param canvas
     * @param rect
     * @param paint
     */
    protected void decorateBitmap(Canvas canvas, Rect rect, Paint paint) {
    }

    public abstract Bitmap getBitmap();

}
