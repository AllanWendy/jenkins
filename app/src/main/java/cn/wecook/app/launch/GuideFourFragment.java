package cn.wecook.app.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.wecook.common.utils.ScreenUtils;

import cn.wecook.app.R;

/**
 * Created by LK on 2015/9/12.
 */
public class GuideFourFragment extends GuideBaseFragment {
    private View mView;
    private ImageView imgButton;//按钮
    private ImageView imgText;//文字
    private ImageView imgEarth;//地球
    private View.OnClickListener mlistener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_guide_four, null);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imgButton = (ImageView) mView.findViewById(R.id.guid_four_img_btn);
        imgText = (ImageView) mView.findViewById(R.id.guid_four_img_text);
        imgEarth = (ImageView) mView.findViewById(R.id.guid_four_img_earth);


        imgButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        float scale = 1.02f;
                        Animation animation1 = buildScaleAnimation(1f, scale, 1f, scale, 60, true);
                        imgButton.startAnimation(animation1);
                        break;
                    case MotionEvent.ACTION_UP:
                        float scale2 = 1.02f;
                        Animation animation2 = buildScaleAnimation(scale2, 1f, scale2, 1f, 60, true);
                        imgButton.startAnimation(animation2);
                        break;
                }
                return false;
            }
        });
        imgButton.setOnClickListener(mlistener);
    }

    /**
     * 按钮点击
     *
     * @param listener
     */
    protected void onBtnClick(View.OnClickListener listener) {
        mlistener = listener;
    }

    /**
     * Alpha动画
     *
     * @param from
     * @param to
     * @param during
     */
    private Animation buildAlphaAnimation(float from, float to, long during, boolean isFillAfter) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(from, to);
        alphaAnimation.setDuration(during);//设置动画持续时间
        alphaAnimation.setFillAfter(isFillAfter);
        return alphaAnimation;
    }

    /**
     * Translate动画
     *
     * @param fromX
     * @param toX
     * @param fromY
     * @param toY
     * @param during
     * @param isFillAfter
     * @return
     */
    private Animation buildTranslateAnimation(float fromX, float toX, float fromY, float toY, long during, boolean isFillAfter) {
        TranslateAnimation translateAnimation = new TranslateAnimation(fromX, toX, fromY, toY);
        translateAnimation.setDuration(during);//设置动画持续时间
        translateAnimation.setRepeatCount(0);//设置重复次数
        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setFillAfter(isFillAfter);
        return translateAnimation;
    }

    /**
     * Scale动画
     *
     * @param fromX
     * @param toX
     * @param fromY
     * @param toY
     * @param during
     * @param isFillAfter
     * @return
     */
    private Animation buildScaleAnimation(float fromX, float toX, float fromY, float toY, long during, boolean isFillAfter) {
        final ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(during);//设置动画持续时间
        animation.setRepeatCount(0);//设置重复次数
        animation.setRepeatMode(Animation.REVERSE);
        animation.setFillAfter(isFillAfter);
        return animation;
    }

    /**
     * 进入动画
     */
    public void startInAnimationRightToLeft() {
        long duringLong = 700;
        long duringShort = 200;
        //按钮
        AnimationSet animationSetBalloon = new AnimationSet(getContext(), null);
        Animation animationBall1 = buildTranslateAnimation(0, 0, ScreenUtils.getScreenHeightInt() / 4, -50, duringLong, true);
        animationSetBalloon.addAnimation(animationBall1);
        Animation animationBall2 = buildTranslateAnimation(0, 0, -50, 50, duringShort, true);
        animationBall2.setStartOffset(duringLong);
        animationSetBalloon.addAnimation(animationBall2);
        Animation animationBall3 = buildAlphaAnimation(0.5f, 1, duringLong, true);
        animationSetBalloon.addAnimation(animationBall3);
        //文字
        AnimationSet animationSetText = new AnimationSet(getContext(), null);
        Animation animationText1 = buildTranslateAnimation(ScreenUtils.getScreenWidthInt(), -50, 0, 0, duringLong, true);
        animationSetText.addAnimation(animationText1);
        Animation animationText2 = buildTranslateAnimation(-50, 50, 0, 0, duringShort, true);
        animationText2.setStartOffset(duringLong);
        animationSetText.addAnimation(animationText2);
        Animation animationText3 = buildAlphaAnimation(0.5f, 1, duringLong, true);
        animationSetText.addAnimation(animationText3);
        //地球
        AnimationSet animationSetEarth = new AnimationSet(getContext(), null);
        Animation animationEarth1 = buildTranslateAnimation(0, 0, ScreenUtils.getScreenHeightInt(), -50, duringLong, true);
        animationSetEarth.addAnimation(animationEarth1);
        Animation animationEarth2 = buildTranslateAnimation(0, 0, -50, 50, duringShort, true);
        animationEarth2.setStartOffset(duringLong);
        animationSetEarth.addAnimation(animationEarth2);
        Animation animationEarth3 = buildAlphaAnimation(0.5f, 1, duringLong, true);
        animationSetEarth.addAnimation(animationEarth3);

        imgButton.startAnimation(animationSetBalloon);
        imgText.startAnimation(animationSetText);
        imgEarth.startAnimation(animationSetEarth);
    }

    /**
     * 进入动画
     */
    public void startInAnimationLeftToRight() {
        long duringLong = 700;
        long duringShort = 200;
        //按钮
        AnimationSet animationSetBalloon = new AnimationSet(getContext(), null);
        Animation animationBall1 = buildTranslateAnimation(0, 0, ScreenUtils.getScreenHeightInt() / 4, -50, duringLong, true);
        animationSetBalloon.addAnimation(animationBall1);
        Animation animationBall2 = buildTranslateAnimation(0, 0, -50, 50, duringShort, true);
        animationBall2.setStartOffset(duringLong);
        animationSetBalloon.addAnimation(animationBall2);
        Animation animationBall3 = buildAlphaAnimation(0.5f, 1, duringLong, true);
        animationSetBalloon.addAnimation(animationBall3);
        //文字
        AnimationSet animationSetText = new AnimationSet(getContext(), null);
        Animation animationText1 = buildTranslateAnimation(-ScreenUtils.getScreenWidthInt(), 50, 0, 0, duringLong, true);
        animationSetText.addAnimation(animationText1);
        Animation animationText2 = buildTranslateAnimation(50, -50, 0, 0, duringShort, true);
        animationText2.setStartOffset(duringLong);
        animationSetText.addAnimation(animationText2);
        Animation animationText3 = buildAlphaAnimation(0.5f, 1, duringLong, true);
        animationSetText.addAnimation(animationText3);
        //地球
        AnimationSet animationSetEarth = new AnimationSet(getContext(), null);
        Animation animationEarth1 = buildTranslateAnimation(0, 0, ScreenUtils.getScreenHeightInt(), -50, duringLong, true);
        animationSetEarth.addAnimation(animationEarth1);
        Animation animationEarth2 = buildTranslateAnimation(0, 0, -50, 50, duringShort, true);
        animationEarth2.setStartOffset(duringLong);
        animationSetEarth.addAnimation(animationEarth2);
        Animation animationEarth3 = buildAlphaAnimation(0.5f, 1, duringLong, true);
        animationSetEarth.addAnimation(animationEarth3);

        imgButton.startAnimation(animationSetBalloon);
        imgText.startAnimation(animationSetText);
        imgEarth.startAnimation(animationSetEarth);
    }

    /**
     * 退出动画
     */
    public void startOutAnimationLeftToRight() {
        long duringLong = 500;
        long duringShort = 200;
        float toAlpha = 0.3f;
        //按钮
        AnimationSet animationSetBalloon = new AnimationSet(getContext(), null);
        Animation animationBall1 = buildTranslateAnimation(0, 0, 0, -50, duringShort, true);
        animationSetBalloon.addAnimation(animationBall1);
        Animation animationBall2 = buildTranslateAnimation(0, 0, -50, ScreenUtils.getScreenHeightInt() / 4, duringLong, true);
        animationBall2.setStartOffset(duringShort);
        animationSetBalloon.addAnimation(animationBall2);
        Animation animationBall3 = buildAlphaAnimation(1, toAlpha, duringLong, true);
        animationBall3.setStartOffset(duringShort);
        animationSetBalloon.addAnimation(animationBall3);
        animationSetBalloon.setFillAfter(true);
        //文字
        AnimationSet animationSetText = new AnimationSet(getContext(), null);
        Animation animationText1 = buildTranslateAnimation(0, -50, 0, 0, duringShort, true);
        animationSetText.addAnimation(animationText1);
        Animation animationText2 = buildTranslateAnimation(-50, ScreenUtils.getScreenWidthInt(), 0, 0, duringLong, true);
        animationText2.setStartOffset(duringShort);
        animationSetText.addAnimation(animationText2);
        Animation animationText3 = buildAlphaAnimation(1, toAlpha, duringLong, true);
        animationText3.setStartOffset(duringShort);
        animationSetText.addAnimation(animationText3);
        animationSetText.setFillAfter(true);
        //地球
        AnimationSet animationSetEarth = new AnimationSet(getContext(), null);
        Animation animationEarth1 = buildTranslateAnimation(0, 0, 50, -50, duringShort, true);
        animationSetEarth.addAnimation(animationEarth1);
        Animation animationEarth2 = buildTranslateAnimation(0, 0, -50, ScreenUtils.getScreenHeightInt(), duringLong, true);
        animationEarth2.setStartOffset(duringShort);
        animationSetEarth.addAnimation(animationEarth2);
        Animation animationEarth3 = buildAlphaAnimation(1, toAlpha, duringLong, true);
        animationEarth3.setStartOffset(duringShort);
        animationSetEarth.addAnimation(animationEarth3);
        animationSetEarth.setFillAfter(true);

        imgButton.startAnimation(animationSetBalloon);
        imgText.startAnimation(animationSetText);
        imgEarth.startAnimation(animationSetEarth);
    }

    /**
     * 退出动画
     */
    public void startOutAnimationRightToLeft() {
        long duringLong = 500;
        long duringShort = 200;
        float toAlpha = 0.3f;
        //按钮
        AnimationSet animationSetBalloon = new AnimationSet(getContext(), null);
        Animation animationBall1 = buildTranslateAnimation(0, 0, 0, -50, duringShort, true);
        animationSetBalloon.addAnimation(animationBall1);
        Animation animationBall2 = buildTranslateAnimation(0, 0, -50, ScreenUtils.getScreenHeightInt() / 4, duringLong, true);
        animationBall2.setStartOffset(duringShort);
        animationSetBalloon.addAnimation(animationBall2);
        Animation animationBall3 = buildAlphaAnimation(1, toAlpha, duringLong, true);
        animationBall3.setStartOffset(duringShort);
        animationSetBalloon.addAnimation(animationBall3);
        animationSetBalloon.setFillAfter(true);
        //文字
        AnimationSet animationSetText = new AnimationSet(getContext(), null);
        Animation animationText1 = buildTranslateAnimation(0, 50, 0, 0, duringShort, true);
        animationSetText.addAnimation(animationText1);
        Animation animationText2 = buildTranslateAnimation(50, -ScreenUtils.getScreenWidthInt(), 0, 0, duringLong, true);
        animationText2.setStartOffset(duringShort);
        animationSetText.addAnimation(animationText2);
        Animation animationText3 = buildAlphaAnimation(1, toAlpha, duringLong, true);
        animationText3.setStartOffset(duringShort);
        animationSetText.addAnimation(animationText3);
        animationSetText.setFillAfter(true);
        //地球
        AnimationSet animationSetEarth = new AnimationSet(getContext(), null);
        Animation animationEarth1 = buildTranslateAnimation(0, 0, 50, -50, duringShort, true);
        animationSetEarth.addAnimation(animationEarth1);
        Animation animationEarth2 = buildTranslateAnimation(0, 0, -50, ScreenUtils.getScreenHeightInt(), duringLong, true);
        animationEarth2.setStartOffset(duringShort);
        animationSetEarth.addAnimation(animationEarth2);
        Animation animationEarth3 = buildAlphaAnimation(1, toAlpha, duringLong, true);
        animationEarth3.setStartOffset(duringShort);
        animationSetEarth.addAnimation(animationEarth3);
        animationSetEarth.setFillAfter(true);

        imgButton.startAnimation(animationSetBalloon);
        imgText.startAnimation(animationSetText);
        imgEarth.startAnimation(animationSetEarth);
    }


    @Override
    public long getOutAnimationTime() {
        return 500;
    }

    @Override
    public void inAnimationLeftToRight() {
        startInAnimationLeftToRight();
    }

    @Override
    public void inAnimationRightToLeft() {
        startInAnimationRightToLeft();
    }

    @Override
    public void outAnimationLeftToRight() {
        startOutAnimationLeftToRight();
    }

    @Override
    public void outAnimationRightToLeft() {
        startOutAnimationRightToLeft();
    }
}
