package cn.wecook.app.launch;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import cn.wecook.app.R;

/**
 * Created by simon on 15/9/12.
 */
public class Guide_one extends GuideBaseFragment {

    /**
     * 动画执行时间
     */
    private long animationDuration = 500;
    private View contentView;
    private boolean isInAnimation = true;
    private ValueAnimator valueAnimator;
    private ImageView imgView;
    private ImageView checkboxView;
    private ImageView textView;
    private boolean isRightScroll;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_guide_one, null);
        }
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAnimation();
        imgView = (ImageView) view.findViewById(R.id.app_guide_one_img);
        checkboxView = (ImageView) view.findViewById(R.id.app_guide_one_checkbox);
        textView = (ImageView) view.findViewById(R.id.app_guide_one_text);
        inAnimation();
    }

    /**
     * 初始化动画
     */
    private void initAnimation() {
        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(animationDuration);
        valueAnimator.addUpdateListener(new MyAnimatorUpdateListener());
    }

    public void inAnimation() {
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        visibleAllView();
        isInAnimation = true;
        valueAnimator.setInterpolator(new OvershootInterpolator());
        valueAnimator.start();
    }

    private void visibleAllView() {
        setVisibleAllView(View.VISIBLE);
    }

    private void invisibleAllView() {
        setVisibleAllView(View.INVISIBLE);
    }


    private void setVisibleAllView(int visible) {
        imgView.setVisibility(visible);
        textView.setVisibility(visible);
        checkboxView.setVisibility(visible);
    }


    public void outAnimation() {
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        isInAnimation = false;
        valueAnimator.setInterpolator(new AnticipateInterpolator(1f));
        valueAnimator.start();
    }

    @Override
    public void inAnimationLeftToRight() {
        isRightScroll = true;
        inAnimation();

    }

    @Override
    public void inAnimationRightToLeft() {
        isRightScroll = false;
        inAnimation();

    }

    @Override
    public void outAnimationLeftToRight() {
        isRightScroll = true;
        outAnimation();

    }

    @Override
    public void outAnimationRightToLeft() {
        isRightScroll = false;
        outAnimation();

    }

    @Override
    public long getOutAnimationTime() {
        return animationDuration;
    }

    class MyAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float value = (float) valueAnimator.getAnimatedValue();
            //主图片的animation
            float scaleValue = value;
            if (!isInAnimation) {
                scaleValue = 1 - scaleValue;
            }
            imgView.setPivotY((int) (imgView.getHeight() / 2f + 0.5f));
            imgView.setPivotX((int) (imgView.getWidth() / 2f + 0.5f));
            imgView.setScaleX(scaleValue);
            imgView.setScaleY(scaleValue);

            //文字的animation
            float translationValue = value;
            if (isInAnimation) {
                //进入动画
                if (isRightScroll) {
                    //页面向右
                    translationValue = -(1 - value);
                } else {
                    //页面向左
                    translationValue = 1 - value;
                }
            } else {
                //退出动画
                if (isRightScroll) {
                    //页面向右
                    translationValue = value;
                } else {
                    //页面向左
                    translationValue = -value;
                }
            }
            translationValue = translationValue * textView.getWidth();
            textView.setTranslationX(translationValue);

        }
    }

    class MyAnimationListener implements Animator.AnimatorListener {


        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (!isInAnimation) {
                invisibleAllView();
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            if (!isInAnimation) {
                invisibleAllView();
            }
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}
