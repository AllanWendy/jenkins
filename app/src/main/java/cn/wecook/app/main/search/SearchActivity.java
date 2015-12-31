package cn.wecook.app.main.search;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;

import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

/**
 * 搜索外部调用
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class SearchActivity extends BaseSwipeActivity {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onPreCreate() {
        super.onPreCreate();
        /**
         * 魅族SmartBar适配
         */
        if (Build.VERSION.SDK_INT >= 14) {
            final ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                if (!"meizu".equals(PhoneProperties.getChannel())) {
                    actionBar.hide();
                }
            }
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    @Override
    protected BaseFragment onCreateFragment(Bundle savedInstanceState) {
        return BaseFragment.getInstance(SearchFragment.class, savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!result) {
                finish();
            }
        }
        return result;
    }
}
