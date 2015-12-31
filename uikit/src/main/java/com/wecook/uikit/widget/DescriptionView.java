package com.wecook.uikit.widget;

import android.content.Context;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.uikit.R;
import com.wecook.uikit.view.BaseView;

/**
 * 描述
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/16/14
 */
public class DescriptionView extends BaseView {
    public DescriptionView(Context context) {
        super(context);
    }

    private String description;

    private TextView textView;

    public void initDescription(String desc) {
        description = desc;

        loadLayout(R.layout.uikit_view_detail_desc, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        textView = (TextView) findViewById(R.id.uikit_detail_description);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);

        if (textView != null && !StringUtils.isEmpty(description)) {
            textView.setText(description);
        }
    }
}
