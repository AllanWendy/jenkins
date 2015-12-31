package cn.wecook.app.main.home.user;

import android.os.Bundle;

import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

/**
 * 绑定手机界面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/20
 */
public class UserBindPhoneActivity extends BaseSwipeActivity {

    @Override
    protected BaseFragment onCreateFragment(Bundle savedInstanceState) {
        return BaseFragment.getInstance(UserBindPhoneFragment.class, savedInstanceState);
    }
}
