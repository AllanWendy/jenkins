package cn.wecook.app.main.dish.list;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.List;

import cn.wecook.app.adapter.DishAdapter;
import cn.wecook.app.fragment.shopcard.ApiModeListShowCardFragment;

/**
 * 菜品列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/16
 */
public class DishListFragment extends ApiModeListShowCardFragment<Dish> {

    public static final String EXTRA_KEY_WORDS = "extra_key_words";

    private String mKeywords;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mKeywords = bundle.getString(EXTRA_KEY_WORDS);
        }

        if (StringUtils.isEmpty(mKeywords)) {
            mKeywords = "全部";
        }

        setTitle(mKeywords);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Dish>> callback) {
        Address address = DishPolicy.get().getDishAddress();
        String lat = address.getLocation().getLatitude();
        String lon = address.getLocation().getLongitude();
        DishApi.getDishList(mKeywords, lat, lon, DishApi.ORDER_TYPE_DEFAULT, "", page, pageSize, callback);
    }

    @Override
    protected UIAdapter<Dish> newAdapter(List<Dish> listData) {
        return new DishAdapter(this, listData);
    }
}
