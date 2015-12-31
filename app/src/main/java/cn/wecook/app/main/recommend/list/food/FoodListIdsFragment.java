package cn.wecook.app.main.recommend.list.food;

import android.app.Activity;
import android.os.Bundle;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.model.Food;

/**
 * 通过IDS获得菜谱列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/17/14
 */
public class FoodListIdsFragment extends FoodListFragment {

    public static final String EXTRA_IDS = "extra_recipe_ids";

    private String mRecipeIds;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mRecipeIds = bundle.getString(EXTRA_IDS);
        }
    }

    @Override
    public void requestFoodList(int page, int pageSize, ApiCallback<ApiModelList<Food>> callback) {
        FoodApi.getFoodListByIds(mRecipeIds, page, pageSize, callback);
    }
}
