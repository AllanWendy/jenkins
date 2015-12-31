package cn.wecook.app.main.dish;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nineoldandroids.animation.ValueAnimator;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Express;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;

/**
 * 排序列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/9
 */
public class DishOrderByView extends LinearLayout {
    private int mOrderType = DishApi.ORDER_TYPE_DEFAULT;
    private String mOrderDirect = DishApi.ORDER_DIRECT_UP;
    private String mOrderTimeType = DishApi.ORDER_TYPE_TIME_DEFAULT;
    private TextView mOrderTypeDefault;
    private TextView mOrderTypeSale;
    private TextView mOrderTypePrice;
    private TextView mOrderTypeArriveTime;
    private View mBottomLine;
    //Menu相关
    private FrameLayout mMenuLayout;
    private FrameLayout mOrderTypeArriveTimeLayout;
    private ImageView mSwitchLayoutl;
    private AdapterViewCompat.OnItemClickListener mItemClick;
    private DishOrderByView mRelationView;
    //监听菜单项目点击
    private OnMenuItemClick mOnMenuItemClick;
    //到达时间相关
    private PopupWindow mPopupWindow;
    private LinearLayout mPoplayout;
    private List<Express> mExpress = new ArrayList<>();
    private List<TextView> mExpressTexts = new ArrayList<>();
    private List<View> mExpressPressIcons = new ArrayList<>();
    /**
     * 进出的动画
     */
    private ValueAnimator valueAnimator;
    private boolean isShowPopWindow;

    public DishOrderByView(Context context) {
        super(context);
    }

    public DishOrderByView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DishOrderByView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSwitchImg(int drawableId) {
        if (null != mOrderTypeArriveTimeLayout && mOrderTypeArriveTimeLayout.getVisibility() != View.GONE) {
            mOrderTypeArriveTimeLayout.setVisibility(GONE);
        }
        if (mMenuLayout.getVisibility() != View.VISIBLE) {
            mMenuLayout.setVisibility(VISIBLE);
        }
        if (null != mSwitchLayoutl) {
            mSwitchLayoutl.setImageResource(drawableId);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mOrderTypeDefault = (TextView) findViewById(R.id.app_order_by_default);
        mOrderTypePrice = (TextView) findViewById(R.id.app_order_by_price);
        mOrderTypeSale = (TextView) findViewById(R.id.app_order_by_sale);
        mMenuLayout = (FrameLayout) findViewById(R.id.app_menu_layout);
        mBottomLine = findViewById(R.id.order_by_view_bottom_line);
        mSwitchLayoutl = (ImageView) mMenuLayout.findViewById(R.id.app_switch_layout_mode);
        mOrderTypeArriveTime = (TextView) findViewById(R.id.app_order_by_choose_time);
        mOrderTypeArriveTimeLayout = (FrameLayout) findViewById(R.id.app_order_by_choose_time_layout);
        mOrderTypeDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideChooseTimeWindow();
                if (mOrderType != DishApi.ORDER_TYPE_DEFAULT) {
                    mOrderType = DishApi.ORDER_TYPE_DEFAULT;
                    updateSelectTab();
                    if (mItemClick != null) {
                        mItemClick.onItemClick(null, v, 0, 0);
                    }
                }
                if (mRelationView != null) {
                    mRelationView.mOrderTypeDefault.performClick();
                }
            }
        });

        mOrderTypePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideChooseTimeWindow();
                if (mOrderType != DishApi.ORDER_TYPE_PRICE) {
                    mOrderType = DishApi.ORDER_TYPE_PRICE;
                    mOrderDirect = DishApi.ORDER_DIRECT_UP;
                } else {
                    if (DishApi.ORDER_DIRECT_DOWN.equals(mOrderDirect)) {
                        mOrderDirect = DishApi.ORDER_DIRECT_UP;
                    } else {
                        mOrderDirect = DishApi.ORDER_DIRECT_DOWN;
                    }
                }
                updateSelectTab();
                if (mItemClick != null) {
                    mItemClick.onItemClick(null, v, 1, 0);
                }

                if (mRelationView != null) {
                    mRelationView.mOrderTypePrice.performClick();
                }
            }
        });

        mOrderTypeSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideChooseTimeWindow();
                if (mOrderType != DishApi.ORDER_TYPE_SALE) {
                    mOrderType = DishApi.ORDER_TYPE_SALE;
                    mOrderDirect = DishApi.ORDER_DIRECT_DOWN;
                    updateSelectTab();
                    if (mItemClick != null) {
                        mItemClick.onItemClick(null, v, 2, 0);
                    }
                }
                if (mRelationView != null) {
                    mRelationView.mOrderTypeSale.performClick();
                }
            }
        });
        mMenuLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideChooseTimeWindow();
                if (null != mOnMenuItemClick) {
                    mOnMenuItemClick.OnClick(v);
                    if (mItemClick != null) {
                        mItemClick.onItemClick(null, v, 3, 0);
                    }
                }
            }
        });
        mOrderTypeArriveTimeLayout.setEnabled(false);
        initAnimation();
        updateSelectTab();
        loadExpressTypeList();
    }

    /**
     * 初始化选择配送时间布局
     */
    private void initChooseTimeLayout(List<Express> expressTypeLists) {
        mExpress.clear();
        mExpressTexts.clear();
        mExpressPressIcons.clear();
        mExpress.addAll(expressTypeLists);
        //到达时间选择相关
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.view_popwindow_choose_time,
                null);
        mPoplayout = (LinearLayout) layout.findViewById(R.id.choose_time_popwindow_content);
        LinearLayout contentLayout = (LinearLayout) layout.findViewById(R.id.choose_time_popwindow_content);
        contentLayout.removeAllViews();
        final int index = 0;
        for (final Express express : expressTypeLists) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_choose_dish_arrive_time, null);
            TextView expressText = (TextView) itemView.findViewById(R.id.choose_time_text);
            ImageView expressPressedIcon = (ImageView) itemView.findViewById(R.id.choose_time_state_img);
            expressText.setText(express.getText());
            ImageFetcher.asInstance().load(express.getIcon(), (ImageView) itemView.findViewById(R.id.choose_time_img));

            mExpressTexts.add(expressText);
            mExpressPressIcons.add(expressPressedIcon);
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOrderTimeType != express.getType()) {
                        mOrderTimeType = express.getType();
                        if (mItemClick != null) {
                            mItemClick.onItemClick(null, v, 4 + index, 0);
                        }
                        updateSelectTab();
                    }
                    hideChooseTimeWindow();
                }
            });
            contentLayout.addView(itemView);
        }
        mPopupWindow = new PopupWindow(layout, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(R.style.popwindow_anim_style);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(90, 0, 0, 0)));
        mOrderTypeArriveTimeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowPopWindow = !mPopupWindow.isShowing();
                startPopAnimation();
                if (mPopupWindow.isShowing()) {
                    hideChooseTimeWindow();
                } else {
                    mPopupWindow.showAsDropDown(mBottomLine);
                }

            }
        });
        //空View
        layout.findViewById(R.id.choose_time_null_view).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideChooseTimeWindow();
                }
                return true;
            }
        });
    }

    public void updateStatus(int mOrderType, String mOrderDirect, String mOrderTimeType) {
        this.mOrderType = mOrderType;
        this.mOrderDirect = mOrderDirect;
        this.mOrderTimeType = mOrderTimeType;
        updateSelectTab();
    }


    private void updateSelectTab() {
        mOrderTypeDefault.setSelected(mOrderType == DishApi.ORDER_TYPE_DEFAULT);
        mOrderTypePrice.setSelected(mOrderType == DishApi.ORDER_TYPE_PRICE);
        mOrderTypeSale.setSelected(mOrderType == DishApi.ORDER_TYPE_SALE);
        mOrderTypeArriveTime.setSelected(!mOrderTimeType.equals(DishApi.ORDER_TYPE_TIME_DEFAULT));

        Drawable drawable = mOrderTypePrice.getCompoundDrawables()[2];
        int level = 0;
        if (mOrderType == DishApi.ORDER_TYPE_PRICE) {
            if (DishApi.ORDER_DIRECT_UP.equals(mOrderDirect)) {
                level = 2;
            } else if (DishApi.ORDER_DIRECT_DOWN.equals(mOrderDirect)) {
                level = 1;
            }
        }
        drawable.setLevel(level);
        if (mRelationView != null) {
            mRelationView.updateSelectTab();
        }

        //配送时间Popwindow
        for (int i = 0; i < mExpressTexts.size(); i++) {
            mExpressTexts.get(i).setTextColor(getResources().getColor(R.color.uikit_777));
        }
        for (int i = 0; i < mExpressPressIcons.size(); i++) {
            mExpressPressIcons.get(i).setVisibility(View.GONE);
        }
        Drawable timeDrawable = mOrderTypeArriveTime.getCompoundDrawables()[2];
        int timeDrawableLevel = 0;
        if (!DishApi.ORDER_TYPE_TIME_DEFAULT.equals(mOrderTimeType)) {
            for (int i = 0; i < mExpress.size(); i++) {
                Express express = mExpress.get(i);
                if (mOrderTimeType.equals(express.getType())) {
                    mExpressTexts.get(i).setTextColor(getResources().getColor(R.color.uikit_orange));
                    mExpressPressIcons.get(i).setVisibility(View.VISIBLE);
                    timeDrawableLevel = 1;
                    mOrderTypeArriveTime.setText(express.getTitle());
                }
            }
        } else {
            mOrderTypeArriveTime.setText("配送");
        }
        timeDrawable.setLevel(timeDrawableLevel);
    }

    public int getOrderType() {
        return mOrderType;
    }

    public String getOrderDirect() {
        return mOrderDirect;
    }

    public String getOrderTimeType() {
        return mOrderTimeType;
    }

    public void setOnItemClick(AdapterViewCompat.OnItemClickListener onItemClick) {
        mItemClick = onItemClick;
        if (mRelationView != null) {
            mRelationView.setOnItemClick(onItemClick);
        }
    }

    public void relationWith(DishOrderByView relationView) {
        mRelationView = relationView;
    }


    /**
     * 菜单点击监听
     *
     * @param onClick
     */
    public void setOnMenuItemClick(OnMenuItemClick onClick) {
        this.mOnMenuItemClick = onClick;
    }

    /**
     * 初始化页面进入退出动画
     */
    private void initAnimation() {
        valueAnimator = ValueAnimator.ofFloat(1f, 0f);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float animatedValue = (Float) animation.getAnimatedValue();
                if (!isShowPopWindow) {
                    animatedValue = 1 - animatedValue;
                }
                if (null != mPoplayout) {
                    final Float finalAnimatedValue = animatedValue;
                    mPoplayout.setTranslationY(-(finalAnimatedValue * (mPoplayout.getMeasuredHeight())));
                }
            }
        });
    }

    private void startPopAnimation() {
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        valueAnimator.start();
    }

    /**
     * 关闭选择到达时间对话框
     */
    public void hideChooseTimeWindow() {
        if (null != mPopupWindow && mPopupWindow.isShowing()) {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPopupWindow.dismiss();
                }
            });
        }
    }

    /**
     * 加载配送列表
     */
    private void loadExpressTypeList() {
        DishApi.getExpressTypeList(new ApiCallback<ApiModelList<Express>>() {
            @Override
            public void onResult(ApiModelList<Express> result) {
                if (result.available()) {
                    initChooseTimeLayout(result.getList());
                    mOrderTypeArriveTimeLayout.setEnabled(true);
                } else {
                    mOrderTypeArriveTimeLayout.setEnabled(false);
                }
            }
        });
    }

    public interface OnMenuItemClick {
        void OnClick(View v);
    }

}
