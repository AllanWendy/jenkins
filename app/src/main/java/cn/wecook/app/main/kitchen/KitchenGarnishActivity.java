package cn.wecook.app.main.kitchen;

import android.os.Bundle;

import com.wecook.common.modules.network.NetworkState;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

/**
 * 厨房组菜功能
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/11/12
 */
public class KitchenGarnishActivity extends BaseSwipeActivity {

    @Override
    protected BaseFragment onCreateFragment(Bundle intentBundle) {
        BaseFragment fragment = null;
        LogGather.setLogMarker(LogGather.MARK.FROM, "厨房");
        if (NetworkState.available()) {
            LogGather.onEventGarnishIn();
            fragment = BaseFragment.getInstance(KitchenWebGarnishFragment.class, intentBundle);
        } else {
            fragment = BaseFragment.getInstance(KitchenGarnishFragment.class, intentBundle);
        }

        return fragment;
    }
}
