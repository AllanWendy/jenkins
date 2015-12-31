package cn.wecook.app.features.publish;

import android.content.Intent;
import android.os.Bundle;

import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.fragment.BaseFragment;

/**
 * 上传菜谱功能
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/19/15
 */
public class PublishFoodActivity extends BaseSwipeActivity {

    public static final String EXTRA_FOOD_RECIPE = "extra_food_recipe";
    public static final String EXTRA_REEDIT_RECIPE = "extra_reedit_recipe";

    private BaseFragment mFragment;

    private boolean mIsReEdit;

    @Override
    protected void onPreCreate() {
        super.onPreCreate();
        Intent intent = getIntent();
        if (intent != null) {
            FoodRecipe recipe = (FoodRecipe) intent.getSerializableExtra(EXTRA_FOOD_RECIPE);
            mIsReEdit = intent.getBooleanExtra(EXTRA_REEDIT_RECIPE, false);
            if (recipe != null) {
                FoodDetailEditorPolicy.get().onRelease();
                FoodDetailEditorPolicy.get().onCreateFoodRecipe(recipe);
            }
        }
    }

    @Override
    protected BaseFragment onCreateFragment(Bundle savedInstanceState) {
        if (mFragment == null) {
            if (mIsReEdit) {
                if (savedInstanceState == null) {
                    savedInstanceState = new Bundle();
                }
                savedInstanceState.putBoolean(EditFoodDetailFragment.EXTRA_RE_EDIT, true);
            }
            mFragment = BaseFragment.getInstance(EditFoodDetailFragment.class, savedInstanceState);
        }
        return mFragment;
    }

}
