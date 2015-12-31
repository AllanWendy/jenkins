package com.wecook.common.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.app.BaseApp;
import com.wecook.common.modules.property.PhoneProperties;

/**
 * 屏幕工具
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/11/14
 */
public class ScreenUtils {


    public static int[] resizeViewOnScreen(View view, float ratio) {
        return resizeViewOnScreen(view, null, ratio);
    }

    /**
     * @param ratio width/height
     */
    public static int[] resizeViewOnScreen(View view, ViewGroup.LayoutParams params, float ratio) {
        int width = StringUtils.parseInt(PhoneProperties.getScreenWidth());
        int height = (int) (width * ratio);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            if (params == null) {
                layoutParams = new ViewGroup.LayoutParams(width, height);
            } else {
                layoutParams = params;
            }
            layoutParams.width = width;
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.width = width;
            layoutParams.height = height;
        }
        return new int[]{width, height};
    }

    public static void resizeViewOfWidth(View view, int width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.width = width;
        }
    }

    public static void resizeViewOfHeight(View view, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height);
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.height = height;
        }
    }

    public static void resizeView(View view, int width, float ratio) {
        int height = (int) (width * ratio);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(width, height);
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.width = width;
            layoutParams.height = height;
        }
    }

    public static void resizeView(View view, ViewGroup group, int width, float ratio) {
        int height = (int) (width * ratio);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        try {
            if (group != null) {
                layoutParams = (ViewGroup.LayoutParams)
                        JavaRefactorUtils.invokeMethod(group, "generateDefaultLayoutParams", null, null);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(width, height);
        }

        layoutParams.width = width;
        layoutParams.height = height;

        view.setLayoutParams(layoutParams);

    }

    public static void resizeViewWithSpecial(View view, int width, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            ViewGroup group = (ViewGroup) view.getParent();
            if (group != null) {
                layoutParams = (ViewGroup.LayoutParams)
                        JavaRefactorUtils.invokeMethod(group, "generateDefaultLayoutParams", null, null);
            } else {
                layoutParams = new ViewGroup.LayoutParams(width, height);
            }
        }

        if (layoutParams != null) {
            layoutParams.width = width;
            layoutParams.height = height;

            view.setLayoutParams(layoutParams);
        }
    }

    public static void resizeViewWithPadding(View view, int width, int pl, int pt, int pr, int pb, float ratio) {
        int height = (int) (width * ratio);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(width, height);
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.width = width;
            layoutParams.height = height;
        }
        view.setPadding(pl, pt, pr, pb);
    }

    public static void resizeViewWithMargin(View view, int width, int pl, int pt, int pr, int pb, float ratio) {
        int height = (int) (width * ratio);
        ViewGroup.MarginLayoutParams layoutParams = null;
        try {
            layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        } catch (ClassCastException e) {
            return;
        }
        if (layoutParams == null) {
            layoutParams = new ViewGroup.MarginLayoutParams(width, height);
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.width = width;
            layoutParams.height = height;
        }
        layoutParams.setMargins(pl, pt, pr, pb);
    }

    public static void rePadding(View view, int padding) {
        if (view != null) {
            int pxPadding = dip2px(view.getContext(), padding);
            view.setPadding(pxPadding, pxPadding, pxPadding, pxPadding);
        }
    }

    public static void rePadding(View view, int padding, boolean tryPx) {
        if (view != null) {
            int pxPadding = dip2px(view.getContext(), padding);
            if (tryPx) {
                pxPadding = padding;
            }
            view.setPadding(pxPadding, pxPadding, pxPadding, pxPadding);
        }
    }

    public static void rePadding(View view, int pl, int pt, int pr, int pb) {
        if (view != null) {
            int pxPl = dip2px(view.getContext(), pl);
            int pxPt = dip2px(view.getContext(), pt);
            int pxPr = dip2px(view.getContext(), pr);
            int pxPb = dip2px(view.getContext(), pb);
            view.setPadding(pxPl, pxPt, pxPr, pxPb);
        }
    }

    public static void rePadding(View view, int pl, int pt, int pr, int pb, boolean tryPx) {
        if (view != null) {
            int pxPl = dip2px(view.getContext(), pl);
            int pxPt = dip2px(view.getContext(), pt);
            int pxPr = dip2px(view.getContext(), pr);
            int pxPb = dip2px(view.getContext(), pb);
            if (tryPx) {
                pxPl = pl;
                pxPt = pt;
                pxPr = pr;
                pxPb = pb;
            }
            view.setPadding(pxPl, pxPt, pxPr, pxPb);
        }
    }

    public static void reMargin(View view, ViewGroup parent, int pl, int pt, int pr, int pb) {
        reMargin(view, parent, pl, pt, pr, pb, false);
    }

    public static void reMargin(View view, ViewGroup parent, int pl, int pt, int pr, int pb, boolean tryPx) {
        ViewGroup.MarginLayoutParams layoutParams = null;
        try {
            if (parent != null) {
                layoutParams = (ViewGroup.MarginLayoutParams) JavaRefactorUtils.invokeMethod(parent, "generateDefaultLayoutParams", null, null);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }


        if (layoutParams == null) {
            layoutParams = new ViewGroup.MarginLayoutParams(-1, -1);
            view.setLayoutParams(layoutParams);
        }

        if (tryPx) {
            int pxPl = dip2px(view.getContext(), pl);
            int pxPt = dip2px(view.getContext(), pt);
            int pxPr = dip2px(view.getContext(), pr);
            int pxPb = dip2px(view.getContext(), pb);
            layoutParams.setMargins(pxPl, pxPt, pxPr, pxPb);
        } else {
            layoutParams.setMargins(pl, pt, pr, pb);
        }
    }

    public static void reMargin(View view, int pl, int pt, int pr, int pb) {
        reMargin(view, pl, pt, pr, pb, false);
    }

    public static void reMargin(View view, int pl, int pt, int pr, int pb, boolean tryPx) {
        ViewGroup.MarginLayoutParams layoutParams = null;
        try {
            layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        } catch (ClassCastException e) {
            return;
        }
        if (layoutParams == null) {
            layoutParams = new ViewGroup.MarginLayoutParams(-1, -1);
            view.setLayoutParams(layoutParams);
        }
        if (tryPx) {
            int pxPl = dip2px(view.getContext(), pl);
            int pxPt = dip2px(view.getContext(), pt);
            int pxPr = dip2px(view.getContext(), pr);
            int pxPb = dip2px(view.getContext(), pb);
            layoutParams.setMargins(pxPl, pxPt, pxPr, pxPb);
        } else {
            layoutParams.setMargins(pl, pt, pr, pb);
        }
    }

    public static int dip2px(Context context, float dip) {
        if (context != null) {
            float f = context.getResources().getDisplayMetrics().density;
            return (int) (dip * f + 0.5F);
        }
        return 0;
    }

    public static int dip2px(float dip) {
        if (BaseApp.getApplication() != null) {
            float f = BaseApp.getApplication().getResources().getDisplayMetrics().density;
            return (int) (dip * f + 0.5F);
        }
        return 0;
    }

    public static int dimen2px(Context context, int dimen) {
        if (context != null) {
            return context.getResources().getDimensionPixelOffset(dimen);
        }
        return 0;
    }

    public static float getScreenWidth() {
        return Float.parseFloat(PhoneProperties.getScreenWidth());
    }

    public static int getScreenWidthInt() {
        return StringUtils.parseInt(PhoneProperties.getScreenWidth());
    }

    public static int getScreenHeightInt() {
        return StringUtils.parseInt(PhoneProperties.getScreenHeight());
    }

    public static int getViewX(View view) {
        int x = 0;
        if (view != null) {
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            x = rect.left;
        }
        return x;
    }

    public static Rect getViewSize(View view) {
        Rect rect = new Rect();
        if (view != null) {
            view.getGlobalVisibleRect(rect);
        }
        return rect;
    }

    public static float getTextViewWordWidth(TextView textView) {
        Paint textPaint = textView.getPaint();
        return textPaint.measureText(textView.getText().toString());
    }
}
