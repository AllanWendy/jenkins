package cn.wecook.app.main.dish.list;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.EmptyView;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.DishAdapter;

/**
 * 今日特价
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/14
 */
public class DishSpecialListFragment extends DishListFragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle("今日特价");
        setUseShopCartPolice(false);
    }

    @Override
    protected UIAdapter<Dish> newAdapter(List<Dish> listData) {
        return new DishSpecialAdapter(this, listData);
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Dish>> callback) {
        Address address = DishPolicy.get().getDishAddress();
        String lat = address.getLocation().getLatitude();
        String lon = address.getLocation().getLongitude();
        DishApi.getSpecialDishList(lat, lon, page, pageSize, callback);
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_search);
            emptyView.setTitle("");
            emptyView.setSecondTitle("今天特价菜卖光了～");
        }
    }

    private class DishSpecialAdapter extends DishAdapter {

        public DishSpecialAdapter(BaseFragment fragment, List<Dish> data) {
            super(fragment, R.layout.view_dish_big, data);
        }

        @Override
        protected View newView(int viewType) {
            View itemView = super.newView(viewType);
            ScreenUtils.reMargin(itemView.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 40, true);
            return itemView;
        }

        @Override
        public void updateView(int position, int viewType, Dish data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
        }
    }
}
