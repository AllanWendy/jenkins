package com.wecook.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.VisibleForTesting;
import android.view.Gravity;
import android.widget.ImageView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.filemaster.FileMaster;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片处理工具类
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/29/14
 */
public class BitmapUtils {

    private static final double MAX_THUMBNAIL_SIZE = 500 * FileUtils.ONE_KB;

    private static final Paint sPaint = new Paint();
    private static final Rect sBounds = new Rect();
    private static final Rect sOldBounds = new Rect();
    private static final String TAG = BitmapUtils.class.getSimpleName();
    private static Canvas sCanvas = new Canvas();

    public static Bitmap getBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static class BitmapInfo {
        public Bitmap src;
        public byte[] srcBytes;
        public File srcFile;

        public BitmapInfo() {
        }
    }

    /**
     * 获得图片
     *
     * @param path
     * @param width
     * @param height
     * @param max
     * @return
     */
    public static Bitmap getBitmap(String path, double max) {
        Bitmap srcBitmap = null;
        if (StringUtils.isEmpty(path)) {
            return srcBitmap;
        }
        int fileSize = 0;
        File file = new File(path);
        try {
            InputStream is = new FileInputStream(file);
            fileSize = is.available();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return srcBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return srcBitmap;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = 1;
        if (fileSize >= max && max != 0) {
            int sqr = (int) Math.ceil(Math.sqrt(((double) fileSize / max)));
            options.inSampleSize = sqr;
        }

        //创建经过计算之后的原始图片
        options.inJustDecodeBounds = false;
        srcBitmap = BitmapFactory.decodeFile(path, options);
        return srcBitmap;
    }

    /**
     * @param path
     * @param width  宽
     * @param height 高
     * @param max    最大大小，单位为KB
     * @return
     */
    public static BitmapInfo getBitmapBestFit(String path, int width, int height, double max) {
        BitmapInfo bitmapInfo = new BitmapInfo();
        Bitmap srcBitmap = getBitmap(path, max);
        if (srcBitmap == null) {
            return bitmapInfo;
        }
        bitmapInfo = getBitmapBestFit(srcBitmap, width, height, max, "");
        bitmapInfo.srcFile = new File(path);
        return bitmapInfo;
    }

    public static BitmapInfo getBitmapBestFit(Bitmap src, int width, int height, double max, String fileName) {
        BitmapInfo bitmapInfo = new BitmapInfo();
        Bitmap outBitmap = null;
        if (src != null) {
            //创建大小符合的输出图片
            Bitmap thumbnailBitmap = src;
            if (src.getWidth() > width && src.getHeight() > height) {
                thumbnailBitmap = scale(src, width, height, ImageView.ScaleType.CENTER_CROP, true);
            }
            thumbnailBitmap = crop(thumbnailBitmap, Gravity.CENTER, width, height, true);


            if (thumbnailBitmap != null
                    && thumbnailBitmap.getRowBytes() * thumbnailBitmap.getHeight() >= max) {
                //处理图片大小
                int compressSize = Integer.MAX_VALUE;
                int quality = 100;
                byte[] bitmapBytes = null;
                while (compressSize > max) {
                    bitmapBytes = bitmap2Bytes(thumbnailBitmap, quality);
                    compressSize = bitmapBytes.length;
                    quality -= 10;
                }

                clean(thumbnailBitmap);

                if (bitmapBytes != null) {
                    outBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                    bitmapInfo.srcBytes = bitmapBytes;
                    if (!StringUtils.isEmpty(fileName)) {
                        bitmapInfo.srcFile = FileUtils.newFile(bitmapBytes,
                                FileMaster.getInstance().getImageDir(), fileName);
                    }
                }
            } else {
                outBitmap = thumbnailBitmap;
            }

            if (outBitmap != null) {
                bitmapInfo.src = outBitmap;
                int outW = outBitmap.getWidth();
                int outH = outBitmap.getHeight();

                Logger.d("out-> width:" + outW + " height:" + outH);
            }

        }

        return bitmapInfo;
    }

    /**
     * 清理图片
     *
     * @param bitmap
     */
    public static void clean(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 获得图片数据
     *
     * @param bm
     * @param quality
     * @return
     */
    public static byte[] bitmap2Bytes(final Bitmap bm, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    /**
     * 重置图片大小
     *
     * @param bm
     * @param maxWidth
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bm, int maxWidth) {
        Bitmap returnBm;
        int w = bm.getWidth();
        int h = bm.getHeight();
        float scaleWidth;
        float scaleHeight;
        if (w > h) {
            scaleWidth = ((float) maxWidth) / w;
            scaleHeight = scaleWidth;
        } else {
            scaleHeight = ((float) maxWidth) / h;
            scaleWidth = scaleHeight;
        }

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);

        returnBm = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);
        return returnBm;
    }

    public static Bitmap createBitmapThumbnail(String path, int width, int height) {
        Bitmap bitmap = getBitmap(path, MAX_THUMBNAIL_SIZE);
        return createBitmapThumbnail(bitmap, width, height);
    }

    /**
     * 创建缩略图
     *
     * @param bitmap
     * @param iconWidth
     * @param iconHeight
     * @return
     */
    public synchronized static Bitmap createBitmapThumbnail(Bitmap bitmap,
                                                            final int iconWidth, final int iconHeight) {
        if (bitmap == null) {
            return bitmap;
        }

        int width = iconWidth;
        int height = iconHeight;

        int srcWidth = iconWidth;
        int srcHeight = iconHeight;

        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        if (width > 0 && height > 0) {
            if (width < bitmapWidth || height < bitmapHeight) {
                final float ratio = (float) bitmapWidth / bitmapHeight;

                if (bitmapWidth > bitmapHeight) {
                    height = (int) (width / ratio);
                } else if (bitmapHeight > bitmapWidth) {
                    width = (int) (height * ratio);
                }

                Bitmap.Config c = (width == srcWidth && height == srcHeight) ?
                        bitmap.getConfig() : Bitmap.Config.ARGB_8888;
                if (null == c) {
                    c = Bitmap.Config.ARGB_8888;
                }

                try {
                    final Bitmap thumb = Bitmap.createBitmap(srcWidth, srcHeight, c);
                    final Canvas canvas = sCanvas;
                    final Paint paint = sPaint;
                    paint.reset();
                    canvas.setBitmap(thumb);
                    sBounds.set((srcWidth - width) / 2, (srcHeight - height) / 2, width, height);
                    sOldBounds.set(0, 0, bitmapWidth, bitmapHeight);
                    canvas.drawBitmap(bitmap, sOldBounds, sBounds, paint);
                    return thumb;
                } catch (OutOfMemoryError e) {
                    return null;
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            } else if (bitmapWidth < width || bitmapHeight < height) {
                try {
                    final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                    final Bitmap thumb = Bitmap.createBitmap(srcWidth, srcHeight, c);
                    final Canvas canvas = sCanvas;
                    final Paint paint = sPaint;
                    paint.reset();
                    canvas.setBitmap(thumb);
                    sBounds.set(0, 0, width, height);
                    sOldBounds.set(0, 0, bitmapWidth, bitmapHeight);
                    canvas.drawBitmap(bitmap, sOldBounds, sBounds, paint);
                    return thumb;
                } catch (OutOfMemoryError e) {
                    return null;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmap;
    }

    /**
     * 等比缩放图片
     *
     * @param sourceBitmap  原图
     * @param targetWidth   目标宽度
     * @param targetHeight  目标高度
     * @param scaleType     缩放类型同ImageView.ScaleType，但只用到CENTER_CROP和 CENTER_INSIDE
     * @param recycleSource 是否回收原图
     * @return
     */
    public static Bitmap scale(Bitmap sourceBitmap, float targetWidth, float targetHeight, ImageView.ScaleType scaleType, boolean recycleSource) {
        Logger.d(TAG, "scale()...");
        if (sourceBitmap != null)
            Logger.d(TAG, "sourceBitmap.isRecycled() : " + sourceBitmap.isRecycled());
        if (sourceBitmap == null || sourceBitmap.isRecycled())
            return null;

        Bitmap scaledBitmap = null;

        float scale;

        float sourceWidth = sourceBitmap.getWidth();
        float sourceHeight = sourceBitmap.getHeight();

        float sourceRatio = sourceWidth / sourceHeight;
        float targetRatio = targetWidth / targetHeight;

        // 计算缩放比例，比较(原图宽/高比)和(目标图的宽/高比)，若前者大用高度比例，否则用宽度比例
        if (ImageView.ScaleType.CENTER_CROP.equals(scaleType))
            scale = sourceRatio > targetRatio ? targetHeight / sourceHeight : targetWidth / sourceWidth;
        else
            scale = sourceRatio < targetRatio ? targetHeight / sourceHeight : targetWidth / sourceWidth;

        // 不需要缩放，直接返回
        if (scale == 1.0F)
            return sourceBitmap;

        // 创建缩放矩阵
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);

        try {
            // 将原图缩放
            scaledBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);

        } catch (IllegalArgumentException e) {
            Logger.e("IllegalArgumentException", "IllegalArgumentException in BitmapUtil.scale(): " + e.getMessage());
        } catch (OutOfMemoryError e) {
            Logger.e("OutOfMemoryError", "OutOfMemoryError in BitmapUtil.scale(): " + e.getMessage());
        }

        if (recycleSource && sourceBitmap != scaledBitmap)
            clean(sourceBitmap);
        return scaledBitmap;
    }

    /**
     * 剪裁图片 思路：取原图与目标大小的交叉部分
     *
     * @param sourceBitmap  原图
     * @param targetWidth   剪裁到的宽度
     * @param targetHeight  剪裁到的高度
     * @param recycleSource 是否回收原图
     * @return
     */
    public static Bitmap crop(Bitmap sourceBitmap, int gravity, int targetWidth, int targetHeight, boolean recycleSource) {
        Logger.debug(TAG, "crop()...", gravity, targetWidth, targetHeight, recycleSource);
        if (sourceBitmap == null)
            return null;

        Bitmap croppedBitmap = null;

        int x = 0;
        int y = 0;
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        Logger.d(TAG, "crop()...before width:" + width + "|height:" + height);

        if (width == targetWidth && height == targetHeight) {
            return sourceBitmap;
        }
        // 按照目标比例获得合适的尺寸
        //1.找到最小边
        //2.缩放对应边
        //3.对应边和实际大小对比
        //4.大：获得最小边缩小大小
        //5.小：获得对应边缩小大小
        //6.比较目标大小
        //7.获得实际图片大小
        if (width >= height) {//1
            //2
            int scaleW = height * targetWidth / targetHeight;
            //3
            if (scaleW > width) {
                //4
                height = width * targetHeight / targetWidth;
            } else {
                //5
                width = scaleW;
            }
            //6
            if (width >= targetWidth) {
                //7
                width = targetWidth;
                height = targetHeight;
            }
        } else {
            int scaleH = width * targetHeight / targetWidth;
            if (scaleH > height) {
                width = height * targetWidth / targetHeight;
            } else {
                height = scaleH;
            }

            if (height >= targetHeight) {
                width = targetWidth;
                height = targetHeight;
            }
        }

        Logger.d(TAG, "crop()...after width:" + width + "|height:" + height);
        // 获取原图缩放之后与目标图的交叉区域
        switch (gravity) {
            case Gravity.CENTER:
                int xDiff = Math.max(0, sourceBitmap.getWidth() - width);
                int yDiff = Math.max(0, sourceBitmap.getHeight() - height);
                x = xDiff / 2;
                y = yDiff / 2;
                break;
        }

        Logger.d(TAG, "crop()... x:" + x + "|y:" + y);

        if (sourceBitmap.getWidth() == width && sourceBitmap.getHeight() == height && x == 0 && y == 0) {
            return sourceBitmap;
        }
        try {
            // 根据交叉区域进行剪裁
            croppedBitmap = Bitmap.createBitmap(sourceBitmap, x, y, width, height);
        } catch (OutOfMemoryError e) {
            Logger.e("OutOfMemoryError", "OutOfMemoryError in BitmapUtil.crop() : " + e.getMessage());
        }

        if (recycleSource && sourceBitmap != croppedBitmap)
            clean(sourceBitmap);
        return croppedBitmap;
    }

    /**
     * 裁剪图片，默认先缩放
     *
     * @param bitmap        原图
     * @param width
     * @param height
     * @param recycleSource 是否回收原图
     * @return
     */
    public static Bitmap scaleAndCrop(Bitmap bitmap, int width, int height, boolean recycleSource) {
        Logger.d(TAG, "scaleAndCrop()...");
        return scaleAndCrop(bitmap, Gravity.CENTER, width, height, recycleSource);
    }

    /**
     * @param bitmap
     * @param gravity
     * @param width
     * @param height
     * @param recycleSource
     * @return
     */
    public static Bitmap scaleAndCrop(Bitmap bitmap, int gravity, int width, int height, boolean recycleSource) {
        bitmap = scale(bitmap, width, height, ImageView.ScaleType.CENTER_CROP, recycleSource);
        return crop(bitmap, gravity, width, height, true);
    }

    /**
     * 增加圆角
     *
     * @param bitmap
     * @param width
     * @param height
     * @param radius
     * @return
     */
    public static Bitmap round(Bitmap bitmap, int width, int height, int radius, boolean recycleSource) {
        if (width == 0 || height == 0 || radius <= 0 || bitmap == null)
            return null;

        Bitmap ret = null;
        try {
            ret = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError e) {
            Logger.e("OutOfMemoryError", "OutOfMemoryError in ImageUtils.round(): " + e.getMessage());
        }
        if (ret == null)
            return null;

        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);
        canvas.drawRoundRect(rectF, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if (recycleSource)
            clean(bitmap);
        return ret;
    }


    /**
     * Convert drawable to Bitmap
     *
     * @param drawable
     * @return bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null)
            return null;

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError e) {
            Logger.d(TAG, "Exception : " + e.getMessage());
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean isRecycled(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            return false;
        }
        return true;
    }

    /**
     * 获得圆圈图片
     *
     * @param bitmap
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static Bitmap getCycleBitmap(Bitmap bitmap, int outWidth, int outHeight) {
        if (bitmap != null) {
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, outWidth, outHeight);
            Canvas bitmapCanvas = new Canvas(newBitmap);

            Bitmap mashBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            canvas.drawOval(new RectF(0.0f, 0.0f, outWidth, outHeight), paint);

            // Draw Bitmap.
            paint.reset();
            paint.setFilterBitmap(false);
            Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
            paint.setXfermode(xfermode);
            bitmapCanvas.drawBitmap(mashBitmap, 0.0f, 0.0f, paint);
            return newBitmap;
        }
        return null;
    }
}
