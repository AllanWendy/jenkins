package cn.wecook.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.DishTag;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.dish.DishActivity;
import cn.wecook.app.main.dish.DishDetailFragment;
import cn.wecook.app.main.dish.DishRecommendFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantDetailFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantFragment;

/**
 * 菜品列表数据
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/17
 */
public class DishAdapter extends UIAdapter<Dish> {
    protected BaseFragment mFragment;


    public DishAdapter(BaseFragment fragment, List<Dish> data) {
        this(fragment, R.layout.view_dish_big, data);
    }

    public DishAdapter(BaseFragment fragment, int itemLayoutResId, List<Dish> data) {
        super(fragment.getContext(), itemLayoutResId, data);
        mFragment = fragment;
    }

    public static void updateItem(final BaseFragment fragment, View container, int position,
                                  int viewType, Dish data, Bundle extra) {
        updateItem(fragment, container, true, position, viewType, data, extra);
    }

    public static void updateItem(final BaseFragment fragment, View container, boolean useBigImage,
                                  int position, int viewType, final Dish data, Bundle extra) {
        updateItem(fragment, container, useBigImage, true, position, viewType, data, extra);
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
                                  int position, int viewType, final Dish data, Bundle extra) {
        if (data != null) {
            ImageView cover = (ImageView) container.findViewById(R.id.app_dish_cover);
            TextView title = (TextView) container.findViewById(R.id.app_dish_name);
            TextView tip = (TextView) container.findViewById(R.id.app_dish_tip);
            TextView desc = (TextView) container.findViewById(R.id.app_dish_desc);
            ImageView descIcon = (ImageView) container.findViewById(R.id.app_dish_desc_icon);
            ViewGroup descGroup = (ViewGroup) container.findViewById(R.id.app_dish_desc_group);
            TextView price = (TextView) container.findViewById(R.id.app_dish_price);
            TextView priceNormal = (TextView) container.findViewById(R.id.app_dish_price_normal);
            ViewGroup tagsGroup = (ViewGroup) container.findViewById(R.id.app_dish_tags);
            ImageView restaurantImage = (ImageView) container.findViewById(R.id.app_dish_restaurant_image);
            ImageView restaurantIcon = (ImageView) container.findViewById(R.id.app_dish_restaurant_icon);
            TextView restaurantName = (TextView) container.findViewById(R.id.app_dish_restaurant_name);


            TextView saleCount = (TextView) container.findViewById(R.id.app_dish_name_end);
            ImageView btnAddToShopCard = (ImageView) container.findViewById(R.id.app_dish_btn);
            FrameLayout btnShopLayout = (FrameLayout) container.findViewById(R.id.app_dish_shop_btn_layout);


            List<DishTag> dishTags = data.getDishTags();
            tagsGroup.removeAllViews();
            if (dishTags != null && !dishTags.isEmpty() && tagsGroup != null) {
                for (DishTag tag : dishTags) {
                    View tagView = createTagView(container.getContext(), tag);
                    if (tagView != null) {
                        tagsGroup.addView(tagView);
                    }
                }
            }

            if (desc != null && descGroup != null) {
                if (StringUtils.isEmpty(data.getContent())) {
                    descGroup.setVisibility(View.GONE);
                } else {
                    desc.setText(data.getContent());
                    descIcon.setVisibility(View.GONE);
                    descGroup.setVisibility(View.VISIBLE);
                }
            }

            if (Dish.TYPE_RESTAURANT == data.getTemplateId()) {
                restaurantImage.setVisibility(View.GONE);
                final Restaurant restaurant = data.getRestaurant();

                if (restaurant != null) {
                    ImageFetcher.asInstance().load(restaurant.getImage(), cover);
                    if (title != null) {
                        title.setText(restaurant.getName());
                    }
                    if (tip != null) {
                        tip.setVisibility(View.VISIBLE);
                        tip.setText(restaurant.getTip());
                    }
                }
                if (restaurantIcon != null) {
                    restaurantIcon.setVisibility(View.VISIBLE);
                }

                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //跳转到餐厅详情
                        if (restaurant != null) {
                            Intent intent = new Intent(fragment.getContext(), DishActivity.class);
                            intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_RESTAURANT_DETAIL);
                            intent.putExtra(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, restaurant.getId());
                            fragment.startActivity(intent);
                        }
                    }
                });
            } else if (Dish.TYPE_DISH == data.getTemplateId()) {
                if (useBigImage) {
                    ImageFetcher.asInstance().load(data.getImageBig(), cover);
                } else {
                    ImageFetcher.asInstance().load(data.getImage(), cover);
                }

                if (title != null) {
                    title.setText(data.getTitle());
                }
                if (price != null) {
                    price.setText(data.getPrice());
                }


                if (priceNormal != null) {
                    priceNormal.getPaint().setAntiAlias(true);
                    priceNormal.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    priceNormal.setText(data.getPriceNormal());
                }

                final Restaurant restaurant = data.getRestaurant();

                if (restaurantImage != null) {
                    if (restaurant != null) {
                        ImageFetcher.asInstance().load(restaurant.getImage(), restaurantImage);
                        if (restaurantName != null) {
                            restaurantName.setText(restaurant.getName());
                        }
                    }

                    restaurantImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转到餐厅详情
                            if (fragment instanceof DishRecommendFragment) {
                                LogGather.onEventDishRecommendGoRestaurant();
                            } else if (fragment instanceof DishDetailFragment) {
                                LogGather.onEventDishDetailGoOtherDish();
                            }
                            if (restaurant != null) {
                                if (fragment.getActivity() instanceof DishActivity) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, restaurant.getId());
                                    fragment.next(DishRestaurantDetailFragment.class, bundle);
                                } else {
                                    Intent intent = new Intent(fragment.getContext(), DishActivity.class);
                                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_RESTAURANT_DETAIL);
                                    intent.putExtra(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, restaurant.getId());
                                    fragment.startActivity(intent);
                                }
                            }
                        }
                    });
                }

                if (restaurantIcon != null) {
                    restaurantIcon.setVisibility(View.GONE);
                }

                if (tip != null) {
                    tip.setVisibility(View.GONE);
                }

                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //跳转到菜品详情
                        if (fragment instanceof DishRecommendFragment) {
                            LogGather.onEventDishRecommendGoDish();
                        } else if (fragment instanceof DishRestaurantFragment) {
                            LogGather.onEventDishRestaurantListGoDish();
                        }
                        if (fragment.getActivity() instanceof DishActivity) {
                            Bundle bundle = new Bundle();
                            bundle.putString(DishDetailFragment.EXTRA_DISH_ID, data.getDishId());
                            bundle.putString(DishDetailFragment.EXTRA_TITLE, data.getTitle());
                            fragment.next(DishDetailFragment.class, bundle);
                        } else {
                            Intent intent = new Intent(fragment.getContext(), DishActivity.class);
                            intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_DISH_DETAIL);
                            intent.putExtra(DishDetailFragment.EXTRA_DISH_ID, data.getDishId());
                            intent.putExtra(DishDetailFragment.EXTRA_TITLE, data.getTitle());
                            fragment.startActivity(intent);
                        }
                    }
                });
                if ((Dish.STATE_OFF_SALE + "").equals(data.getState())) {
                    btnAddToShopCard.setImageResource(R.drawable.app_ic_btn_shop_card_normal);
                } else if ((Dish.STATE_ON_SALE + "").equals(data.getState())) {
                    btnAddToShopCard.setImageResource(R.drawable.app_ic_btn_shop_card_pressed);
                }
                btnShopLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ((Dish.STATE_OFF_SALE + "").equals(data.getState())) {
                            ToastAlarm.show("菜品已售罄");
                        } else if ((Dish.STATE_ON_SALE + "").equals(data.getState())) {
                            //加入购物车
                            DishPolicy.get().addDishToShopCart(data, new DishPolicy.OnShopCartUpdateListener() {
                                @Override
                                public void onResult(boolean success, String info) {
                                    if (success) {
                                        ToastAlarm.show("添加购物车成功");
                                    } else {
                                        ToastAlarm.show(info);
                                    }
                                }
                            });
                        }
                    }
                });
                btnAddToShopCard.setVisibility(addShopcart ? View.VISIBLE : View.GONE);
            }
        }
    }

    private static View createTagView(Context context, DishTag tag) {
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

    @Override
    protected View newView(int viewType) {
        View itemView = super.newView(viewType);
        int itemViewWidth = ScreenUtils.getScreenWidthInt() - 2 * ScreenUtils.dip2px(8);
        ScreenUtils.resizeView(itemView.findViewById(R.id.app_dish_cover_group), itemViewWidth, 9 / 16f);
        return itemView;
    }

    @Override
    public void updateView(int position, int viewType, Dish data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        updateItem(mFragment, getItemView(), position, viewType, data, extra);
    }


}
