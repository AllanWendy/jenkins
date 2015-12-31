package com.wecook.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.R;
import com.wecook.uikit.view.BaseView;


/**
 * 标题视图
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/16/14
 */
public class LabelView extends BaseView {

    private String mLabelName;

    private TextView mLabelView;

    private TextView mMoreView;

    private OnClickListener mMoreListener;

    public LabelView(Context context) {
        super(context);
    }

    public LabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LabelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initWithLabelName(String labelName) {
        mLabelName = labelName;

        loadLayout(R.layout.uikit_view_detail_label, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLabelView = (TextView) findViewById(R.id.app_food_detail_label);

        if (!StringUtils.isEmpty(mLabelName)) {
            mLabelView.setText(mLabelName);
        }

        mMoreView = (TextView) findViewById(R.id.app_food_detail_more);

        setOnMoreClickListener(mMoreListener);
    }

    public void setOnMoreClickListener(OnClickListener l) {
        mMoreListener = l;
        if (mMoreView != null) {
            mMoreView.setVisibility(l != null ? View.VISIBLE : View.GONE);
            mMoreView.setOnClickListener(l);
        }
    }
}
