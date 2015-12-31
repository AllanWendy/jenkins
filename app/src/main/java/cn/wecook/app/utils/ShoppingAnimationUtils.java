package cn.wecook.app.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.utils.ScreenUtils;

import cn.wecook.app.R;

/**
 * 加入购物车动画
 * Created by simon on 15/10/20.
 */
public class ShoppingAnimationUtils {
    /**
     * 购物车缩放动画
     */
    private static ValueAnimator scaleValueAnimator;

    /**
     * @param
     * @return void
     * @throws
     * @Description: 创建动画层
     */
    private static ViewGroup createAnimLayout(Activity activity) {
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setId(Integer.MAX_VALUE);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    private static View addViewToAnimLayout(final ViewGroup vg, final View view,
                                            int[] location) {
        int x = location[0];
        int y = location[1];
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    public static void setAnim(Activity activity, final View startView, View endView) {
        if (activity == null) {
            throw new IllegalStateException("加入购物车动画的activty不能为null");
        }
        if (startView == null || endView == null) {
            throw new IllegalStateException("加入购物车动画开始位置view或者结束位置view 不能为null");
        }
        //判断是否需要重新加入布局
        ImageView animationView = createAnimationImageView(activity);
        int[] start_location = new int[2];
        getLocationInWindow(start_location, startView);
        ViewGroup anim_mask_layout = createAnimLayout(activity);
        anim_mask_layout.addView(animationView);//把动画小球添加到动画层
        final View view = addViewToAnimLayout(anim_mask_layout, animationView,
                start_location);
        int[] end_location = new int[2];// 这是用来存储动画结束位置的X、Y坐标
        getLocationInWindow(end_location, endView);// shopCart是那个购物车
        startAnimation(700, start_location, end_location, animationView, anim_mask_layout, startView, endView);
    }

    /**
     * 获得view 在屏幕中的位置
     *
     * @param location
     * @param view
     */
    private static void getLocationInWindow(int[] location, View view) {
        view.getLocationInWindow(location);
        location[0] += (int) (view.getWidth() / 2f + 0.5f);
        location[1] += (int) (view.getHeight() / 2f + 0.5f);

    }

    /**
     * 开启动画
     */
    private static void startAnimation(long duration, final int[] startPoint, final int[] endPoint, final ImageView animationView, final ViewGroup animationLayout, View startView, final View endView) {
        final int spaceX = startPoint[0] - endPoint[0];
        final int spaceY = startPoint[1] - endPoint[1];
        final float exratSpaceY = 0.7f;
        ValueAnimator addShoppingValueAnimator = ValueAnimator.ofFloat(0, 1f);
        addShoppingValueAnimator.setDuration(duration);
        addShoppingValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                float changeValue = (float) Math.sin(animatedValue * (Math.PI));
                if (animationView != null) {
                    //小球
                    animationView.setPivotX(animationView.getWidth() / 2);
                    animationView.setPivotY(animationView.getHeight() / 2);
                    if (animatedValue > 0.6f) {
                        animationView.setScaleX(1 - animatedValue / 2);
                        animationView.setScaleY(1 - animatedValue / 2);
                    }
                    //水平方向
                    float valueX = 0;
                    if (Math.abs(spaceX) < ScreenUtils.dip2px(30)) {
                        valueX = -ScreenUtils.dip2px(30) * changeValue;
                    } else {
                        valueX = -spaceX * animatedValue;
                    }
                    animationView.setTranslationX(valueX);
                    //垂直方向
                    float valueY = 0;
                    if (endPoint[1] - startPoint[1] > 0) {
                        //开始位置高于结束位置
                        valueY = -(spaceY * (animatedValue - changeValue * (1 - exratSpaceY)));
                    } else {
                        //开始位置低于结束位置
                        if (Math.abs(spaceY) < ScreenUtils.dip2px(100)) {
                            valueY = -(spaceY * animatedValue + ScreenUtils.dip2px(100) * changeValue);
                        } else {
                            valueY = -(spaceY * (animatedValue + changeValue * (1 - exratSpaceY)));
                        }
                    }
                    animationView.setTranslationY(valueY);
                }
            }
        });

        addShoppingValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animationView.setVisibility(View.GONE);
                startScaleAnimation(endView);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        addShoppingValueAnimator.start();

    }

    /**
     * 放大缩小动画
     */
    private static void startScaleAnimation(final View view) {
        if (scaleValueAnimator == null) {
            scaleValueAnimator = ValueAnimator.ofFloat(0, 1f);
        }
        scaleValueAnimator.setDuration(300);
        scaleValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                float value = (float) (Math.sin(Math.PI * animatedValue) / 3f);
                if (view != null) {
                    view.setPivotX(view.getWidth() / 2);
                    view.setPivotY(view.getHeight() / 2);
                    view.setScaleX(value + 1);
                    view.setScaleY(value + 1);
                }

            }
        });

        if (scaleValueAnimator.isRunning())

        {
            scaleValueAnimator.cancel();
        }

        scaleValueAnimator.start();
    }

    /**
     * 创建动画的view－小圆点
     */
    private static ImageView createAnimationImageView(Activity activity) {
        ImageView imageView = new ImageView(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.app_shape_circle);
        return imageView;
    }

}
