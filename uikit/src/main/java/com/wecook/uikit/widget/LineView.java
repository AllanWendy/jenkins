package com.wecook.uikit.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.wecook.uikit.R;
import com.wecook.uikit.view.BaseView;


/**
 * 线
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/16/14
 */
public class LineView extends BaseView {
    public LineView(Context context) {
        super(context);
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initLayout() {
        loadLayout(R.layout.uikit_view_detail_line, null);
    }
}
