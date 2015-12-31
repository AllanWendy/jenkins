package cn.wecook.app.main.recommend.detail.food;

import android.os.Bundle;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.model.FoodDetail;

/**
 * 我de菜谱详情
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public class FoodDetailOfSelfFragment extends FoodDetailFragment {

    @Override
    protected void requestFoodDetail(String id, ApiCallback<FoodDetail> apiCallback) {
        FoodApi.getMyFoodRecipeDetail(id, apiCallback);
    }

    @Override
    public boolean back(Bundle data) {
        finishAll();
        return true;
    }
}
