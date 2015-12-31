package cn.wecook.app.main.dish.list;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.DishTag;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.EmptyView;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.DishAdapter;

/**
 * 热门套餐
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/29
 */
public class DishGroupListFragment extends DishListFragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        String title = null;
        if (bundle != null) {
            title = bundle.getString(EXTRA_TITLE);
        }
        setTitle(StringUtils.isEmpty(title) ? "套餐" : title);
        setUseShopCartPolice(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected UIAdapter<Dish> newAdapter(List<Dish> listData) {
        return new DishGroupAdapter(this, listData);
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Dish>> callback) {
        Address address = DishPolicy.get().getDishAddress();
        String lat = address.getLocation().getLatitude();
        String lon = address.getLocation().getLongitude();
        DishApi.getDishGroupList(lat, lon, page, pageSize, callback);
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_search);
            emptyView.setTitle("");
            emptyView.setSecondTitle("附近没发现可买套餐");
        }
    }

    private class DishGroupAdapter extends DishAdapter {

        public DishGroupAdapter(BaseFragment fragment, List<Dish> data) {
            super(fragment, R.layout.view_dish_big, data);
        }

        @Override
        protected View newView(int viewType) {
            View itemView = super.newView(viewType);
            itemView.findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.app_dish_shop_btn_layout).setVisibility(View.GONE);
            itemView.findViewById(R.id.app_dish_restaurant_group).setVisibility(View.GONE);

//            ScreenUtils.reMargin(itemView.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 60, true);
            return itemView;
        }

        @Override
        public void updateView(int position, int viewType, final Dish data, Bundle extra) {
            updateItem(DishGroupListFragment.this, getItemView(), true, false, position, viewType, data, extra);
            if (data != null) {
                View restaurantView = getItemView().findViewById(R.id.app_dish_restaurant_tag_view);
                if (data.getRestaurant() != null) {
                    restaurantView.setVisibility(View.VISIBLE);
                    TextView restaurantName = (TextView) restaurantView.findViewById(R.id.app_dish_restaurant_tag_name);
                    restaurantName.setText(data.getRestaurantName());
//                    restaurantName.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            //跳转到餐厅详情
//                            if (data.getRestaurant().getId() != null) {
//                                Bundle bundle = new Bundle();
//                                bundle.putString(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, data.getRestaurant().getId());
//                                next(DishRestaurantDetailFragment.class, bundle);
//                            }
//                        }
//                    });
                } else {
                    restaurantView.setVisibility(View.GONE);

                }


                ViewGroup tagsGroup = (ViewGroup) getItemView().findViewById(R.id.app_dish_tags);
                List<DishTag> dishTags = data.getDishTags();
                tagsGroup.removeAllViews();
                if (dishTags != null && !dishTags.isEmpty() && tagsGroup != null) {

                    for (DishTag tag : dishTags) {
                        if ("套餐".equals(tag.getName())) {
                            continue;
                        }
                        View tagView = createTagView(getItemView().getContext(), tag);
                        if (tagView != null) {
                            tagsGroup.addView(tagView);
                        }
                    }
                }


            }
        }

        private View createTagView(Context context, DishTag tag) {
            if (tag != null) {
                View tagView = LayoutInflater.from(context).inflate(R.layout.view_dish_tag, null);
                TextView dishTagName = (TextView) tagView.findViewById(R.id.app_dish_tag_name);
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadii(new float[]{0, 0, 0, 0, ScreenUtils.dip2px(5), ScreenUtils.dip2px(5), ScreenUtils.dip2px(5), ScreenUtils.dip2px(5)});
                bg.setColor(tag.getBgColor());
                dishTagName.setBackgroundDrawable(bg);
                dishTagName.setText(tag.getName());
                dishTagName.setTextColor(tag.getColor());
                return tagView;
            }
            return null;
        }
    }

}
