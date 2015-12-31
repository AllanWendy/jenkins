package cn.wecook.app.main.kitchen;

import android.content.Intent;

import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.KitchenHomePolicy;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.barcode.BarCodeActivity;

/**
 * 扫码
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public class KitchenHomeChildBarcodeFragment extends KitchenHomeChildFragment {

    @Override
    protected List<FoodResource> getListData() {
        return KitchenHomePolicy.getInstance().getLocalListByType(KitchenApi.TYPE_BARCODE);
    }

    @Override
    protected void onAddActionClick() {
        Intent intent = new Intent(getContext(), BarCodeActivity.class);
        startActivity(intent);
    }

    @Override
    protected String getFirstItemName() {
        return getString(R.string.app_kitchen_home_item_add_barcode_title);
    }

    @Override
    protected int getFirstItemDrawableId() {
        return R.drawable.app_ic_kitchen_barcode;
    }
}
