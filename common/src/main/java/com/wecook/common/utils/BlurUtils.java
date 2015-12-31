package com.wecook.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.view.WindowManager;

/**
 * 模糊效果
 *
 * @author yangzc
 */
public class BlurUtils {

    @SuppressLint("NewApi")
    public static Bitmap blurBitmap(Context context, Bitmap src, int scale, int radius) {
        try {
            if (src == null) {
                return null;
            }

            //根据屏幕比例进行裁剪
            int width = src.getWidth();
            int height = src.getHeight();
            float rate = ((float) width / (float) height);

            Point point = new Point();
            WindowManager mw = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            int screenWidth = mw.getDefaultDisplay().getWidth();
            int screenHeight = mw.getDefaultDisplay().getHeight();
            if (Build.VERSION.SDK_INT >= 13) {
                mw.getDefaultDisplay().getSize(point);
                screenWidth = point.x;
                screenHeight = point.y;
            }

            float screenRate = ((float) screenWidth / (float) screenHeight);

            int x = 0;
            int y = 0;
            if (rate > screenRate) {
                int clipWidth = (int) (height * screenRate);
                x = Math.abs(width - clipWidth) / 2;
                width = clipWidth;
            } else if (rate < screenRate) {
                int clipHeight = (int) (width / screenRate);
                y = Math.abs(height - clipHeight) / 2;
                height = clipHeight;
            }

            if (width > screenWidth) {
                width = screenWidth;
            }

            if (height > screenHeight) {
                height = screenHeight;
            }

            //缩小裁剪图片
            Bitmap scaleBitmap = null;
            Matrix matrix = new Matrix();
            matrix.setScale((1.0f / scale), (1.0f / scale));
            try {
                scaleBitmap = Bitmap.createBitmap(src, x, y, width, height, matrix, true);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (scaleBitmap != null) {
                //进行高斯模糊
                return fastBlur(context, scaleBitmap, radius);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 不进行裁剪的模糊处理
     *
     * @param context
     * @param src
     * @param scale
     * @param radius
     * @return
     */
    public static Bitmap blurBitmapWithoutClip(Context context, Bitmap src, int scale, int radius) {
        try {
            if (src == null) {
                return null;
            }

            int width = src.getWidth();
            int height = src.getHeight();
            int x = 0;
            int y = 0;

            //缩小裁剪图片
            Bitmap scaleBitmap = null;
            Matrix matrix = new Matrix();
            matrix.setScale((1.0f / scale), (1.0f / scale));
            try {
                scaleBitmap = Bitmap.createBitmap(src, x, y, width, height, matrix, true);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (scaleBitmap != null) {
                //进行高斯模糊
                return fastBlur(context, scaleBitmap, radius);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 快速模糊效果
     *
     * @param context 上下文
     * @param small   模糊的小图
     * @param radius  模糊次数
     * @return
     */
    public static Bitmap fastBlur(Context context, Bitmap small, int radius) {
        if (radius < 1) {
            return (null);
        }

        Bitmap bitmap = small.copy(Bitmap.Config.ARGB_8888, true);
        BitmapUtils.clean(small);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }


    /**
     * 图片缩放
     *
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap small(Bitmap bitmap, int width, int height) {
        Matrix matrix = new Matrix();
        matrix.postScale((width + 0.0f) / bitmap.getWidth(), (height + 0.0f)
                / bitmap.getHeight()); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }
}
