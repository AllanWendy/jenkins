package com.wecook.uikit.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.R;
import com.wecook.uikit.widget.indicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * 标题栏
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/14/14
 */
public class TitleBar extends FrameLayout {

    private CoveredImageView mBackView;
    private TextView mBackTitleView;
    private TextView mTitleView;
    private View mBottomDivLine;
    private ProgressBar mBottomProgressBar;
    private ViewGroup mActionGroup;
    private ViewGroup mMainLayer;
    private ViewGroup mSearchLayer;
    private ImageView mSearchBtn;
    private EditText mSearchInput;
    private CoveredTextView mSearchAction;
    private View mSearchInputClear;
    private TitlePageIndicator mTitleIndicator;

    private List<Animator> mAnimatorList;
    private SearchListener mSearchListener;

    //购物车相关
    private FrameLayout mShoppingCartLayout;
    private ImageView mShopCardImg;
    private TextView mShopCardCount;
    //提示
    private FrameLayout mRemindLayout;
    private TextView mRemindText;

    private ObjectAnimator mFadeOutAnimator;
    private TextWatcher mTextWatcher = new TextWatcher() {
        //TODO
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (mSearchListener != null) {
                mSearchListener.beforeTextChanged(s, start, count, after);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mSearchListener != null) {
                mSearchListener.onTextChanged(s, start, before, count);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mSearchInputClear != null) {
                if (s.length() > 0) {
                    mSearchInputClear.setVisibility(View.VISIBLE);
                } else {
                    mSearchInputClear.setVisibility(View.GONE);
                }
            }
            if (mSearchListener != null) {
                mSearchListener.afterTextChanged(s);
            }
        }
    };

    public TitleBar(Context context) {
        super(context);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBackView = (CoveredImageView) findViewById(R.id.uikit_title_bar_back);
        mBackTitleView = (TextView) findViewById(R.id.uikit_title_bar_back_title);
        mTitleView = (TextView) findViewById(R.id.uikit_title_bar_title);
        mBottomDivLine = findViewById(R.id.uikit_title_bar_div);
        mBottomProgressBar = (ProgressBar) findViewById(R.id.uikit_title_bar_progressbar);
        mActionGroup = (ViewGroup) findViewById(R.id.uikit_title_bar_action);
        mMainLayer = (ViewGroup) findViewById(R.id.uikit_title_bar_layer_main);
        mSearchLayer = (ViewGroup) findViewById(R.id.uikit_title_bar_layer_search);
        mSearchBtn = (ImageView) findViewById(R.id.uikit_title_bar_search);
        mSearchInput = (EditText) findViewById(R.id.uikit_title_bar_input);
        mSearchInputClear = findViewById(R.id.uikit_title_bar_input_clear);
        mSearchAction = (CoveredTextView) findViewById(R.id.uikit_title_bar_search_action);
        mTitleIndicator = (TitlePageIndicator) findViewById(R.id.uikit_title_bar_title_indicator);
        mShoppingCartLayout = (FrameLayout) findViewById(R.id.uikit_title_bar_shopping_cart);
        mRemindLayout = (FrameLayout) findViewById(R.id.uikit_title_bar_remind_layout);
        mRemindText = (TextView) findViewById(R.id.uikit_title_bar_remind);
        mShopCardImg = (ImageView) findViewById(R.id.uikit_title_bar_shopping_cart_img);
        mShopCardCount = (TextView) findViewById(R.id.uikit_title_bar_shopping_cart_count);
        mSearchInput.addTextChangedListener(mTextWatcher);
    }

    public TextView getBackTitleView() {
        return mBackTitleView;
    }

    public TextView getShopCardText() {
        return mShopCardCount;
    }

    public FrameLayout getShoppingCartLayout() {
        return mShoppingCartLayout;
    }

    public void setBackListener(OnClickListener clickListener) {
        if (mBackView != null) {
            mBackView.setOnClickListener(clickListener);
        }
    }

    public void setBackTitleClickListener(OnClickListener clickListener) {
        if (mBackTitleView != null) {
            mBackTitleView.setOnClickListener(clickListener);
        }
    }

    public void setBackTitle(String backTitle) {
        if (mBackTitleView != null) {
            mBackTitleView.setText(backTitle);
        }
    }

    public void setBackTitle(int backTitleId) {
        if (mBackTitleView != null) {
            mBackTitleView.setText(backTitleId);
        }
    }

    public void setSearchHint(String searchHint) {
        if (mSearchInput != null) {
            mSearchInput.setHint(searchHint);
        }
    }

    public void setSearchHint(int searchHintId) {
        if (mSearchInput != null) {
            mSearchInput.setHint(searchHintId);
        }
    }

    public String getSearchText() {
        if (mSearchInput != null) {
            return mSearchInput.getText().toString();
        }
        return "";
    }

    public void setSearchText(String searchText) {
        if (mSearchInput != null) {
            mSearchInput.setText(searchText);
        }
    }

    public void showLoading() {
        if (mBottomProgressBar != null) {
            mBottomProgressBar.setPadding(0, 0, 0, -10);
            mBottomProgressBar.setVisibility(VISIBLE);
        }
    }

    public void hideLoading() {
        if (mBottomProgressBar != null) {
            PropertyValuesHolder alphaToG = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
            mFadeOutAnimator = ObjectAnimator.ofPropertyValuesHolder(mBottomProgressBar, alphaToG);
            mFadeOutAnimator.setDuration(500);
            mFadeOutAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBottomProgressBar.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mFadeOutAnimator.start();
        }
    }

    public void setTitle(String title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    /**
     * 是否显示底部分割线条
     *
     * @param enable
     */
    public void enableBottomDiv(boolean enable) {
        if (mBottomDivLine != null) {
            mBottomDivLine.setVisibility(enable ? VISIBLE : GONE);
        }
    }

    /**
     * 是否使用返回
     *
     * @param enable
     */
    public void enableBack(boolean enable) {
        if (mBackView != null) {
            mBackView.setVisibility(enable ? VISIBLE : GONE);
        }
    }

    public void enableSearchAction(boolean enable) {
        if (mSearchAction != null) {
            mSearchAction.setVisibility(enable ? VISIBLE : GONE);
        }
    }

    public void enableShoppingCard(boolean enable) {
        if (mShoppingCartLayout != null) {
            mShoppingCartLayout.setVisibility(enable ? VISIBLE : GONE);
        }
    }

    public void enableRemind(boolean enable) {
        if (mRemindLayout != null) {
            mRemindLayout.setVisibility(enable ? VISIBLE : GONE);
        }
    }

    public void setRemindText(String text, OnClickListener onClickListener) {
        if (null != mRemindLayout && null != mRemindText) {
            mRemindText.setText(text);
            mRemindLayout.setOnClickListener(onClickListener);
        }
    }

    public void setBottomDivLineColor(int color) {
        if (mBottomDivLine != null) {
            mBottomDivLine.setBackgroundColor(color);
        }
    }

    public void addActionView(ActionView actionView) {
        if (mActionGroup != null && actionView != null
                && actionView.getParent() == null) {
            View view = actionView.getView();
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.gravity = Gravity.CENTER;
            lp.rightMargin = ScreenUtils.dip2px(getContext(), 10);
            view.setLayoutParams(lp);
            mActionGroup.addView(actionView.getView());
        }
    }

    public void addActionView(ActionView actionView, ViewGroup.LayoutParams layoutParams) {
        if (mActionGroup != null && actionView != null
                && actionView.getParent() == null) {
            View view = actionView.getView();
            view.setLayoutParams(layoutParams);

            mActionGroup.addView(actionView.getView());
        }
    }

    public void clearActions() {
        if (mActionGroup != null) {
            mActionGroup.removeAllViews();
        }
    }

    public TextView getTitleView() {
        return mTitleView;
    }

    /**
     * 设置返回按钮图片
     *
     * @param drawableId
     */
    public void setBackDrawable(int drawableId) {
        if (mBackView != null) {
            Drawable drawable = getResources().getDrawable(drawableId);
            if (drawable instanceof BitmapDrawable) {
                mBackView.setMultiplyColor(CoveredImageView.DEFAULT_MULTIPLY_COLOR);
            } else {
                mBackView.setMultiplyColor(CoveredImageView.CLEAR_MULTIPLY_COLOR);
            }
            mBackView.setImageResource(drawableId);
        }
    }

    /**
     * 设置餐车背景颜色
     *
     * @param drawableId
     */
    public void setShopCardImg(int drawableId) {
        if (mShopCardImg != null) {
            mShopCardImg.setImageResource(drawableId);
        }
    }

    public View getBackView() {
        return mBackView;
    }

    /**
     * 设置混合颜色
     *
     * @param color
     */
    public void setMultiplyColor(int color) {
        if (mBackView != null) {
            mBackView.setMultiplyColor(color);
        }
    }

    /**
     * 设置导航条
     *
     * @param viewPager
     */
    public void setViewPager(ViewPager viewPager, ViewPager.OnPageChangeListener pageChangeListener) {
        if (viewPager != null) {
            mTitleIndicator.setVisibility(VISIBLE);
            mTitleIndicator.setViewPager(viewPager);
            mTitleIndicator.setOnPageChangeListener(pageChangeListener);
        }
    }

    public boolean isInSearchMode() {
        return mSearchLayer.getVisibility() == VISIBLE;
    }

    public ViewGroup getSearchLayer() {
        return mSearchLayer;
    }

    public void setSearchLayer(boolean show) {
        if (show) {
            mSearchLayer.setVisibility(View.VISIBLE);
            mMainLayer.setVisibility(GONE);
            mSearchBtn.setVisibility(GONE);
            mSearchInput.setFocusable(true);
            mSearchInput.requestFocus();
            mSearchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
            mSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        if (mSearchListener != null) {
                            mSearchListener.onSearchClick();
                        }
                        return true;
                    }
                    return false;
                }
            });
            KeyboardUtils.openKeyboard(getContext(), this);
            if (mSearchListener != null) {
                mSearchListener.onSearchViewVisible(true);
            }
        } else {
            mSearchBtn.setVisibility(VISIBLE);
            mMainLayer.setVisibility(VISIBLE);
            mSearchLayer.setVisibility(GONE);
            KeyboardUtils.closeKeyboard(getContext(), this);
            if (mSearchListener != null) {
                mSearchListener.onSearchViewVisible(false);
                mSearchListener.onSearchFinishClick();
            }
        }
    }

    public void setSearchListener(final SearchListener listener) {
        setSearchListener(false, false, listener);
    }

    /**
     * 设置搜索监听
     *
     * @param animator
     * @param showSearchBtn
     * @param listener
     */
    public void setSearchListener(final boolean animator, boolean showSearchBtn, final SearchListener listener) {
        float searchBtnRightX = ScreenUtils.getViewX(mSearchBtn);
        float searchBtnLeftX = ScreenUtils.getViewX(mSearchInput);
        PropertyValuesHolder alphaToV = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        final PropertyValuesHolder alphaToG = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
        PropertyValuesHolder moveToLeft = PropertyValuesHolder.ofFloat("x", searchBtnRightX, searchBtnLeftX);

        mSearchListener = listener;
        mAnimatorList = new ArrayList<Animator>();
        final ObjectAnimator searchBtnAnimator = ObjectAnimator.ofPropertyValuesHolder(mSearchBtn, moveToLeft);
        mAnimatorList.add(searchBtnAnimator);
        mAnimatorList.add(ObjectAnimator.ofPropertyValuesHolder(mSearchLayer, alphaToV));
        mAnimatorList.add(ObjectAnimator.ofPropertyValuesHolder(mMainLayer, alphaToG));
        mSearchInputClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchInput.setText("");
                mSearchInputClear.setVisibility(View.GONE);
            }
        });

        mSearchBtn.setVisibility(showSearchBtn ? VISIBLE : GONE);
        mSearchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animator) {

                    searchBtnAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mSearchLayer.setVisibility(VISIBLE);
                            mMainLayer.setVisibility(GONE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            searchBtnAnimator.removeListener(this);
                            mSearchBtn.setVisibility(GONE);
                            mSearchInput.setFocusable(true);
                            mSearchInput.requestFocus();
                            KeyboardUtils.openKeyboard(getContext(), mSearchInput);
                            if (listener != null) {
                                listener.onSearchViewVisible(true);
                                listener.onSearchClick();
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            mSearchLayer.setVisibility(GONE);
                            mMainLayer.setVisibility(VISIBLE);
                            mSearchBtn.setVisibility(VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    for (Animator animator : mAnimatorList) {
                        ValueAnimator valueAnimator = (ValueAnimator) animator;
                        valueAnimator.start();
                    }
                } else {
                    setSearchLayer(true);
                }
            }
        });

        mSearchAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animator) {
                    searchBtnAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mSearchBtn.setVisibility(VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mMainLayer.setVisibility(View.VISIBLE);
                            mSearchLayer.setVisibility(GONE);
                            searchBtnAnimator.removeListener(this);
                            if (listener != null) {
                                listener.onSearchViewVisible(false);
                                listener.onSearchFinishClick();
                            }
                            KeyboardUtils.closeKeyboard(getContext(), mSearchInput);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            mSearchLayer.setVisibility(GONE);
                            mMainLayer.setVisibility(View.VISIBLE);
                            mSearchBtn.setVisibility(GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    for (Animator animator : mAnimatorList) {
                        ValueAnimator valueAnimator = (ValueAnimator) animator;
                        valueAnimator.reverse();
                    }
                } else {
                    setSearchLayer(false);
                }

            }
        });
    }

    public View getSearchButton() {
        return mSearchBtn;
    }

    public CoveredTextView getSearchAction() {
        return mSearchAction;
    }

    public EditText getSearchInput() {
        return mSearchInput;
    }

    public void setProgress(int progress) {
        if (mBottomProgressBar != null && progress <= 100 && progress > 0) {
            mBottomProgressBar.setProgress(progress);
        }
    }

    public static interface ActionView {
        public ViewParent getParent();

        public View getView();

        public void setOnClickListener(OnClickListener l);

        public void setVisibility(int visibility);
    }

    public static abstract class SearchListener implements TextWatcher {

        public void onSearchViewVisible(boolean visible) {
        }

        protected void onSearchFinishClick() {
        }

        protected void onSearchClick() {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public static class ActionLinearLayout extends LinearLayout implements ActionView {

        private int layout;

        public ActionLinearLayout(Context context, int layout) {
            super(context);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            LayoutInflater.from(getContext()).inflate(layout, this, true);
        }

        @Override
        public View getView() {
            return this;
        }
    }

    public static class ActionImageView extends ImageView implements ActionView {

        private int mResId;

        public ActionImageView(Context context, int resId) {
            super(context);
            mResId = resId;
        }

        @Override
        public View getView() {
            if (mResId != 0) {
                setImageResource(mResId);
            }
            return this;
        }
    }

    public static class ActionCoveredImageView extends CoveredImageView implements ActionView {

        private int mResId;

        public ActionCoveredImageView(Context context, int resId) {
            super(context);
            mResId = resId;
        }

        @Override
        public View getView() {
            if (mResId != 0) {
                setImageResource(mResId);
            }
            return this;
        }
    }

    public static class ActionTextView extends TextView implements ActionView {

        private String name;

        public ActionTextView(Context context, String name) {
            this(context, name, R.style.UIKit_Font);

        }

        public ActionTextView(Context context, String name, int defStyleAttr) {
            super(context, null, defStyleAttr);
            this.name = name;
        }

        @Override
        public View getView() {
            if (!StringUtils.isEmpty(name)) {
                setText(name);
            }
            setGravity(Gravity.CENTER);
            return this;
        }

    }

    public static class ActionCoveredTextView extends CoveredTextView implements ActionView {

        private String name;

        public ActionCoveredTextView(Context context, String name) {
            super(context, null, R.style.UIKit_Font_Orange);
            this.name = name;
        }

        public ActionCoveredTextView(Context context, String name, int style) {
            super(context, null, style);
            this.name = name;
        }

        @Override
        public View getView() {
            if (!StringUtils.isEmpty(name)) {
                setText(name);
            }
            setGravity(Gravity.CENTER);
            return this;
        }

    }
}
