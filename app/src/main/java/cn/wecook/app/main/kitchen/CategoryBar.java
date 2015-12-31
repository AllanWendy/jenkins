package cn.wecook.app.main.kitchen;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.utils.ScreenUtils;

import java.util.List;

import cn.wecook.app.R;

/**
 * 分类侧栏
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/11
 */
public class CategoryBar extends ScrollView {

    private ViewGroup mCategoryBarGroup;
    private boolean mIsOpened;
    private float mTranslationX;

    private float mLastX;
    private int mTouchSpot;
    private boolean mIsMoved;
    private OnStateChangeListener mStateChangeListener;
    private OnSelectItemListener mSelecteItemListener;

    public CategoryBar(Context context) {
        super(context);
    }

    public CategoryBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoryBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCategoryBarGroup = (ViewGroup) findViewById(R.id.app_kitchen_garnish_category_bar_group);
        mTouchSpot = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(getContext()));
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Logger.d("category-bar", "action : " + event.getAction());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIsMoved = false;
                        mLastX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float offsetX = event.getX() - mLastX;
                        if (Math.abs(offsetX) > mTouchSpot && !mIsMoved) {
                            mIsMoved = true;
                            if (offsetX > 0) {
                                collapse();
                            } else {
                                expand();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!mIsMoved) {
                            if (mIsOpened) {
                                collapse();
                            } else {
                                expand();
                            }
                        }
                        break;
                }
                return false;
            }
        });

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                int width = getMeasuredWidth();
                mTranslationX = width - ScreenUtils.dip2px(getContext(), 28);
                ViewCompat.setTranslationX(CategoryBar.this, mTranslationX);
                return false;
            }
        });

    }

    /**
     * 打开
     */
    public void expand() {
        if (mIsOpened) {
            return;
        }
        float startX = mTranslationX;
        float endX = 0;
        PropertyValuesHolder move = PropertyValuesHolder.ofFloat("translationX", startX, endX);
        ObjectAnimator.ofPropertyValuesHolder(this, move).setDuration(300).start();
        setBackgroundResource(R.drawable.app_bg_garnish_bar_expand);
        mIsOpened = true;
        if (mStateChangeListener != null) {
            mStateChangeListener.onStateChange(mIsOpened);
        }
    }

    /**
     * 收起
     */
    public void collapse() {
        if (!mIsOpened) {
            return;
        }
        float startX = 0;
        float endX = mTranslationX;
        PropertyValuesHolder move = PropertyValuesHolder.ofFloat("translationX", startX, endX);
        ObjectAnimator.ofPropertyValuesHolder(this, move).setDuration(300).start();
        setBackgroundResource(R.drawable.app_bg_garnish_bar_collapse);
        mIsOpened = false;
        if (mStateChangeListener != null) {
            mStateChangeListener.onStateChange(mIsOpened);
        }
    }

    public boolean isOpen() {
        return mIsOpened;
    }

    /**
     * 更新item
     * @param categories
     */
    public void updateItems(List<String> categories) {

        if (categories != null) {
            mCategoryBarGroup.removeAllViews();
            for (final String name : categories) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.view_garnish_category_item, null);
                final TextView textView = (TextView) view.findViewById(R.id.app_kitchen_garnish_category_item_name);
                textView.setText(name);
                if ("全部".equals(name)) {
                    textView.setSelected(true);
                }
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        selectItem(textView.getText().toString());

                    }

                });

                mCategoryBarGroup.addView(view);
            }
        }
    }

    public void selectItem(String itemName) {
        if (mSelecteItemListener != null) {
            mSelecteItemListener.onSelectItem(itemName);
        }

        if ("".equals(itemName)) {
            itemName = "全部";
        }

        for (int i = 0; i < mCategoryBarGroup.getChildCount(); i++) {
            View child = mCategoryBarGroup.getChildAt(i);
            final TextView name = (TextView) child.findViewById(R.id.app_kitchen_garnish_category_item_name);
            String childName = name.getText().toString();

            if (childName.equals(itemName)) {
                name.setSelected(true);
            } else {
                name.setSelected(false);
            }
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        mStateChangeListener = listener;
    }

    public void setOnSelectItemListener(OnSelectItemListener listener) {
        mSelecteItemListener = listener;
    }

    public interface OnStateChangeListener {
        void onStateChange(boolean isopen);
    }

    public interface OnSelectItemListener {
        void onSelectItem(String itemName);
    }
}
