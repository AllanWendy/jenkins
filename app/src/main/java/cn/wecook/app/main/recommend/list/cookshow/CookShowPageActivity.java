package cn.wecook.app.main.recommend.list.cookshow;

import android.os.Bundle;

import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

/**
 * 晒厨艺滚动页面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/5/20
 */
public class CookShowPageActivity extends BaseSwipeActivity {

    @Override
    protected BaseFragment onCreateFragment(Bundle savedInstanceState) {
        return BaseFragment.getInstance(CookShowPageListFragment.class, savedInstanceState);
    }
}
