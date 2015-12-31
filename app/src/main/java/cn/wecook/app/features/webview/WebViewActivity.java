package cn.wecook.app.features.webview;

import android.os.Bundle;

import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

import cn.wecook.app.main.recommend.detail.topic.TopicDetailFragment;

/**
 * WebView相关的页面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/11/12
 */
public class WebViewActivity extends BaseSwipeActivity {

    public static final int PAGE_TOPIC_DETAIL = 1;

    public static final String EXTRA_PAGE = "webview_page";
    public static final String ACTIVITY_MAP_URL_PRE = "http://m.wecook.cn/activity/detail_map?inwecook=true&id=";

    private int mPage;

    @Override
    protected BaseFragment onCreateFragment(Bundle intentBundle) {
        if (intentBundle != null) {
            mPage = intentBundle.getInt(EXTRA_PAGE);
        }
        BaseFragment fragment = null;
        switch (mPage) {
            case PAGE_TOPIC_DETAIL:
                fragment = BaseFragment.getInstance(TopicDetailFragment.class, intentBundle);
                break;
            default:
                fragment = BaseFragment.getInstance(WebViewFragment.class, intentBundle);
        }
        return fragment;
    }
}
