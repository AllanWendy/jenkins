package cn.wecook.app.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.GradientView;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.dish.DishActivity;
import cn.wecook.app.main.dish.restaurant.DishRestaurantDetailFragment;

/**
 * 菜品列表数据
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/17
 */
public class RestaurantAdapter extends UIAdapter<Restaurant> {

    protected BaseFragment mFragment;

    public RestaurantAdapter(BaseFragment fragment, List<Restaurant> data) {
        this(fragment, R.layout.listview_item_restaurant_with_food, data);
    }

    public RestaurantAdapter(BaseFragment fragment, int itemLayoutResId, List<Restaurant> data) {
        super(fragment.getContext(), itemLayoutResId, data);
        mFragment = fragment;
    }

    public static void updateItem(final BaseFragment fragment, View container, int position,
                                  int viewType, Restaurant data, Bundle extra) {
        updateItem(fragment, container, true, position, viewType, data, extra);
    }

    public static void updateItem(final BaseFragment fragment, View container, boolean useBigImage,
                                  int position, int viewType, final Restaurant data, Bundle extra) {
        updateItem(fragment, container, useBigImage, false, position, viewType, data, extra);
    }

    /**
     * Item的通用更新方法
     *
     * @param container
     * @param useBigImage 是否使用大图片
     * @param addShopcart 是否显示添加购物车
     * @param position    位置
     * @param viewType    item类型
     * @param data        数据
     * @param extra       额外数据
     */
    public static void updateItem(final BaseFragment fragment, View container, boolean useBigImage, boolean addShopcart,
                                  int position, int viewType, final Restaurant data, Bundle extra) {
        if (data != null) {
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(fragment.getContext(), DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_RESTAURANT_DETAIL);
                    intent.putExtra(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, data.getId());
                    fragment.startActivity(intent);
                }
            });
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(fragment.getContext(), DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_RESTAURANT_DETAIL);
                    intent.putExtra(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, data.getId());
                    fragment.startActivity(intent);
                }
            });
            ImageView cover = (ImageView) container.findViewById(R.id.app_item_resaurant_food_img);
            TextView resaurant_name = (TextView) container.findViewById(R.id.app_item_resaurant_name);
            TextView food_name = (TextView) container.findViewById(R.id.app_item_resaurant_food_name);
            TextView price = (TextView) container.findViewById(R.id.app_item_resaurant_food_price);
            final GradientView gradientView = (GradientView) container.findViewById(R.id.item_resaurant_food_bgview);//蒙版
            //处理白线
            container.findViewById(R.id.line).setVisibility((position == 0) ? View.GONE : View.VISIBLE);
            //设置图片
            gradientView.setVisibility(View.GONE);
            ImageFetcher.asInstance().load(data.getDish().getImage(), cover, new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                    gradientView.setVisibility(View.VISIBLE);
                    return false;
                }
            });
            //设置餐厅名称
            resaurant_name.setText(data.getName());
            //设置食物名称
            food_name.setText(data.getDish().getTitle());
            //设置食物价格
            price.setText(data.getDish().getPrice());
        }
    }

    @Override
    protected View newView(int viewType) {
        View itemView = super.newView(viewType);
        int itemViewWidth = ScreenUtils.getScreenWidthInt();
        ScreenUtils.resizeView(itemView.findViewById(R.id.app_item_resaurant_food_img), itemViewWidth, 9 / 16f);
        return itemView;
    }

    @Override
    public void updateView(int position, int viewType, Restaurant data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        updateItem(mFragment, getItemView(), position, viewType, data, extra);
    }

}
