package cn.wecook.app.main.dish.order;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.wecook.app.R;

/**
 * 订单页面的选择TAB
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/10
 */
public class DishOrderTabView extends LinearLayout {

    public static final int TAB_ORDER_STATE = 1;
    public static final int TAB_ORDER_DETAIL = 2;

    private View mTabOrderState;
    private View mTabOrderDetail;

    private DishOrderTabView relativeTabView;
    private OnTabSelectListener mTabSelectListener;

    public DishOrderTabView(Context context) {
        super(context);
    }

    public DishOrderTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTabOrderState = findViewById(R.id.app_dish_order_tab_state);
        mTabOrderDetail = findViewById(R.id.app_dish_order_tab_detail);

        String[] names = {"订单状态", "订单详情"};
        View[] views = {mTabOrderState, mTabOrderDetail};
        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            TextView text = (TextView) view.findViewById(R.id.app_text_title);
            text.setText(names[i]);
        }

        mTabOrderState.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(TAB_ORDER_STATE, true);
            }
        });

        mTabOrderDetail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(TAB_ORDER_DETAIL, true);
            }
        });

    }

    public void setRelativeTabView(DishOrderTabView tabView) {
        relativeTabView = tabView;
    }

    public void selectTab(int currentTab) {
        selectTab(currentTab, false);
    }

    private void selectTab(int currentTab, boolean user) {

        mTabOrderState.setSelected(currentTab == TAB_ORDER_STATE);
        mTabOrderDetail.setSelected(currentTab == TAB_ORDER_DETAIL);

        if (mTabSelectListener != null) {
            mTabSelectListener.onSelect(currentTab, user);
        }
        if (relativeTabView != null) {
            relativeTabView.selectTab(currentTab, user);
        }

    }

    public void setOnTabSelectListener(OnTabSelectListener listener) {
        mTabSelectListener = listener;
    }

    public static interface OnTabSelectListener {
        void onSelect(int tab, boolean user);
    }
}
