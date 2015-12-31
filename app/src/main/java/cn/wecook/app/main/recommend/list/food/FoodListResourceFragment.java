package cn.wecook.app.main.recommend.list.food;

import android.app.Activity;
import android.os.Bundle;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodResource;

/**
 * 通过原材料获得菜谱列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/17/14
 */
public class FoodListResourceFragment extends FoodListFragment {

    public static final String EXTRA_FOOD_RESOURCE = "extra_food_resource";
    private FoodResource mFoodResource;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFoodResource = (FoodResource) bundle.getSerializable(EXTRA_FOOD_RESOURCE);
        }
    }

    @Override
    public void requestFoodList(int page, int pageSize, ApiCallback<ApiModelList<Food>> callback) {
        FoodApi.getFoodListByResource(mFoodResource.getType(), mFoodResource.getName(), page, pageSize, callback);
    }
}
