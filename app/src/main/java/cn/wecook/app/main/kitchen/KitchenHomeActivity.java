package cn.wecook.app.main.kitchen;

import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * 我的食材
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/11/12
 */
public class KitchenHomeActivity extends BaseSwipeActivity {
    @Override
    protected BaseFragment onCreateFragment(Bundle intentBundle) {
        Map<String, String> keys = new HashMap<String, String>();
        keys.put(LogConstant.KEY_TO, LogConstant.TO_MY_INGREDIENTS);
        MobclickAgent.onEvent(getContext(), LogConstant.UBS_KITCHEN_ENTER_COUNT, keys);

        return BaseFragment.getInstance(KitchenHomeFragment.class, intentBundle);
    }
}
