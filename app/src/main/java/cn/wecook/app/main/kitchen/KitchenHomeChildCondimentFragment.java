package cn.wecook.app.main.kitchen;

import android.os.Bundle;

import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.KitchenHomePolicy;

import java.util.List;

import cn.wecook.app.R;

/**
 * 调料
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public class KitchenHomeChildCondimentFragment extends KitchenHomeChildFragment {


    @Override
    protected List<FoodResource> getListData() {
        return KitchenHomePolicy.getInstance().getLocalListByType(KitchenApi.TYPE_CONDIMENT);
    }

    @Override
    protected void onAddActionClick() {
        Bundle bundle = new Bundle();
        bundle.putString(KitchenAddFragment.EXTRA_TYPE, KitchenApi.TYPE_CONDIMENT);
        next(KitchenAddFragment.class, bundle);
    }

    @Override
    protected String getFirstItemName() {
        return getString(R.string.app_kitchen_home_item_add_condiment_title);
    }

    @Override
    protected int getFirstItemDrawableId() {
        return R.drawable.app_ic_kitchen_add;
    }
}
