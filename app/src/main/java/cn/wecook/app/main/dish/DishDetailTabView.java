package cn.wecook.app.main.dish;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.wecook.app.R;

/**
 * 菜品详情页面的选择TAB
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/10
 */
public class DishDetailTabView extends LinearLayout {

    public static final int TAB_INFO = 1;
    public static final int TAB_EVALUATE = 2;
    public static final int TAB_COOK = 3;

    private View mTabInfo;
    private View mTabEvaluate;
    private View mTabCook;

    private DishDetailTabView relativeTabView;
    private OnTabSelectListener mTabSelectListener;

    public DishDetailTabView(Context context) {
        super(context);
    }

    public DishDetailTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTabInfo = findViewById(R.id.app_dish_detail_tab_info);
        mTabEvaluate = findViewById(R.id.app_dish_detail_tab_evaluate);
        mTabCook = findViewById(R.id.app_dish_detail_tab_cook);

        String[] names = {"菜品", "评价", "做法"};
        View[] views = {mTabInfo, mTabEvaluate, mTabCook};
        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            TextView text = (TextView) view.findViewById(R.id.app_text_title);
            text.setText(names[i]);
        }

        mTabInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(TAB_INFO, true);
            }
        });

        mTabEvaluate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(TAB_EVALUATE, true);
            }
        });

        mTabCook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(TAB_COOK, true);
            }
        });
    }

    public void setRelativeTabView(DishDetailTabView tabView) {
        relativeTabView = tabView;
    }

    public void selectTab(int currentTab) {
        selectTab(currentTab, false);
    }

    private void selectTab(int currentTab, boolean user) {

        mTabInfo.setSelected(currentTab == TAB_INFO);
        mTabEvaluate.setSelected(currentTab == TAB_EVALUATE);
        mTabCook.setSelected(currentTab == TAB_COOK);

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
