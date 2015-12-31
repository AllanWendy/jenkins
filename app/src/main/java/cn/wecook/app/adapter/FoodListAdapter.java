package cn.wecook.app.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.policy.FavoritePolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.recommend.detail.food.FoodDetailFragment;

/**
 * 菜谱
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/7/14
 */
public class FoodListAdapter extends UIAdapter<ApiModelGroup<Food>> {

    private BaseFragment fragment;
    private Random random = new Random();
    private int[] colors = {R.color.app_recipe_bg_red, R.color.app_recipe_bg_yellow};

    public FoodListAdapter(BaseFragment fragment, List<ApiModelGroup<Food>> data) {
        super(fragment.getActivity(), data);
        this.fragment = fragment;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    protected View newView(int viewType) {
        if (viewType == 0) {
            View view = View.inflate(getContext(), R.layout.listview_item_food, null);
            float space = getContext().getResources().getDimension(R.dimen.uikit_default_space_margin);
            int width = (int) ((StringUtils.parseInt(PhoneProperties.getScreenWidth()) - 3 * space) / 2);
            View left = view.findViewById(R.id.app_food_list_item_left);
            View right = view.findViewById(R.id.app_food_list_item_right);

            ScreenUtils.resizeViewOfWidth(left.findViewById(R.id.app_food_operator_group), width);
            ScreenUtils.resizeViewOfWidth(right.findViewById(R.id.app_food_operator_group), width);

            ScreenUtils.resizeView(left.findViewById(R.id.app_food_cover_group), width, 1);
            ScreenUtils.resizeView(right.findViewById(R.id.app_food_cover_group), width, 1);

            ScreenUtils.rePadding(left.findViewById(R.id.app_food_cover), 1);
            ScreenUtils.rePadding(right.findViewById(R.id.app_food_cover), 1);

            ScreenUtils.reMargin(left, (int) space, (int) space, (int) (space / 2), 0);
            ScreenUtils.reMargin(right, (int) (space / 2), (int) space, (int) space, 0);
            return view;
        }
        return null;
    }


    @Override
    public void updateView(int position, int viewType, ApiModelGroup<Food> data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        if (viewType == 0) {
            View left = findViewById(R.id.app_food_list_item_left);
            View right = findViewById(R.id.app_food_list_item_right);
            if (left != null) {
                updateItem(left, data.getItem(0), position * 2);
            }
            if (right != null) {
                updateItem(right, data.getItem(1), position * 2 + 1);
            }
        }
    }

    protected void updateItem(View view, final Food item, final int pos) {
        if (item == null) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);

            ImageView cover = (ImageView) view.findViewById(R.id.app_food_cover);
            final ImageView fav = (ImageView) view.findViewById(R.id.app_food_fav);
            fav.setVisibility(View.GONE);
            TextView price = (TextView) view.findViewById(R.id.app_food_price);
            TextView name = (TextView) view.findViewById(R.id.app_food_name);
            TextView lookCount = (TextView) view.findViewById(R.id.app_food_look_count);
            TextView favCount = (TextView) view.findViewById(R.id.app_food_fav_count);
            TextView cookCount = (TextView) view.findViewById(R.id.app_food_cook_count);
            View countGroup = view.findViewById(R.id.app_food_count_group);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Map<String, String> keys = new HashMap<String, String>();
                    keys.put(LogConstant.KEY_NAME, item.title);
                    keys.put(LogConstant.KEY_SOURCE, fragment.getClickMarker());
                    MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_TAP_COUNT, keys);

                    String event = (String) LogGather.getLogMarker(LogGather.MARK.DURATION_EVENT);
                    if (LogGather.LOG_ZHUGE.EV_TAG_RECIPE.equals(event)) {
                        LogGather.onEventTagRecipe(item.title);
                    } else if (LogGather.LOG_ZHUGE.EV_SEARCH_DO.equals(event)) {
                        LogGather.onEventSearchResult(item.title, pos);
                    }

                    Bundle bundle = new Bundle();
                    bundle.putSerializable(FoodDetailFragment.EXTRA_FOOD, item);
                    bundle.putString(FoodDetailFragment.EXTRA_TITLE, item.title);
                    fragment.next(FoodDetailFragment.class, bundle);
                }
            });
            int index = random.nextInt(2);

            if (!StringUtils.isEmpty(item.image)) {
                ImageFetcher.asInstance().load(item.image, cover, colors[index]);
            } else {
                cover.setImageResource(R.drawable.app_pic_default_recipe_icon);
            }

            FavoritePolicy.favoriteHelper(fav, FavoriteApi.TYPE_RECIPE,
                    StringUtils.parseInt(item.id), item, new Runnable() {
                        @Override
                        public void run() {
                            getContext().startActivity(new Intent(getContext(), UserLoginActivity.class));
                        }
                    }
            );

            if (!StringUtils.isEmpty(item.title)) {
                name.setText(item.title);
                name.setBackgroundDrawable(null);
            }

            if (!StringUtils.isEmpty(item.view)
                    || !StringUtils.isEmpty(item.favourite)
                    || !StringUtils.isEmpty(item.works)) {

                lookCount.setVisibility(View.VISIBLE);
                lookCount.setText(item.view);

                favCount.setVisibility(View.VISIBLE);
                favCount.setText(item.favourite);

                cookCount.setVisibility(View.VISIBLE);
                cookCount.setText(item.works);

                countGroup.setBackgroundDrawable(null);
            }
        }
    }
}
