package cn.wecook.app.main.recommend.detail.party;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.uikit.view.BaseView;

import cn.wecook.app.R;

/**
 * 活动主页
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/20/14
 */
public class PartyMainPageView extends BaseView{

    private TextView mContentView;
    private ImageView mIndicator;
    private WebView mWebContentView;

    private boolean isExtend;

    public PartyMainPageView(Context context) {
        super(context);
    }

    public PartyMainPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PartyMainPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = (TextView) findViewById(R.id.app_main_page_content);
        mWebContentView = (WebView) findViewById(R.id.app_main_page_web_content);
        mIndicator = (ImageView) findViewById(R.id.app_main_page_extend_more);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);
        if (obj != null && obj instanceof String) {
            mContentView.setText(Html.fromHtml((String) obj));
            mContentView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doExtend();
                }
            });
            mWebContentView.loadDataWithBaseURL(null, (String) obj,
                    "text/html", "utf-8", null);
            mIndicator.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doExtend();
                }
            });
        }
    }

    private void doExtend() {
        isExtend = !isExtend;
        if (isExtend) {
            mWebContentView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
            mIndicator.setImageResource(R.drawable.app_bt_extend_close_more);
        } else {
            mWebContentView.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
            mIndicator.setImageResource(R.drawable.app_bt_extend_open_more);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWebContentView.destroy();
    }
}
