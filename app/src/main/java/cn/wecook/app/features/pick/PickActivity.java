package cn.wecook.app.features.pick;

import android.os.Bundle;

import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

/**
 * 晒厨艺获取图片
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/23/14
 */
public class PickActivity extends BaseSwipeActivity {

    public static final int PICK_ONE = 1;
    public static final int PICK_MULTI = 2;
    public static final int PICK_COOK_SHOW = 3;

    public static final String EXTRA_PICK_TYPE = "extra_action_pick_type";
    public static final java.lang.String EXTRA_SELECTED_COUNT_MAX = "extra_selected_count_max";
    public static final String EXTRA_SELECTED = "extra_selected";

    private PickFragment mFragment;

    @Override
    protected BaseFragment onCreateFragment(Bundle savedInstanceState) {
        if (mFragment == null) {
            return mFragment = BaseFragment.getInstance(PickFragment.class, savedInstanceState);
        }
        return mFragment;
    }

}
