package cn.wecook.app.main.home.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodDetail;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.ApiModelListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.features.publish.PublishFoodActivity;
import cn.wecook.app.main.recommend.detail.food.FoodDetailOfSelfFragment;

/**
 * 我的菜谱
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/31/15
 */
public class MyRecipeFragment extends ApiModelListFragment<Food> {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_my_recipe, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.app_title_my_recipe);

        TitleBar.ActionCoveredImageView add = new TitleBar.ActionCoveredImageView(getContext(), R.drawable.app_bt_action_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.setLogMarker(LogGather.MARK.FROM, "我的菜谱");
                LogGather.onEventPublishIn();
                Intent intent = new Intent(getContext(), PublishFoodActivity.class);
                startActivity(intent);
            }
        });

        getTitleBar().addActionView(add);

        view.findViewById(R.id.app_my_recipe_draft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.onEventMyDraft();
                next(RecipeDraftFragment.class);
            }
        });

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //跳转到详情
                int pos = position - getListView().getHeaderViewsCount();
                Food data = getItem(pos);
                if (data != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(FoodDetailOfSelfFragment.EXTRA_FOOD, data);
                    bundle.putString(FoodDetailOfSelfFragment.EXTRA_TITLE, data.title);
                    next(FoodDetailOfSelfFragment.class, bundle);
                }
            }
        });

        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setTitle(getString(R.string.app_empty_title_my_recipe));
            emptyView.setSecondTitle(getString(R.string.app_empty_second_title_my_recipe));
            emptyView.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_cookshow);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, Menu.FIRST + 1, 1, R.string.app_context_menu_edit);
        menu.add(0, Menu.FIRST + 2, 2, R.string.app_context_menu_remove);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = menuInfo.position - getListView().getHeaderViewsCount();
        final Food food = getItem(position);

        if (item.getItemId() == Menu.FIRST + 1) {
            //编辑
            FoodApi.getMyFoodRecipeDetail(food.id, new ApiCallback<FoodDetail>() {
                @Override
                public void onResult(FoodDetail result) {
                    if (result.available()) {
                        MobclickAgent.onEvent(getContext(), LogConstant.NEW_RECIPE_MYRECIPE_EDIT_AGAIN);
                        FoodRecipe recipe = result.getFoodRecipe();
                        Intent intent = new Intent(getContext(), PublishFoodActivity.class);
                        intent.putExtra(PublishFoodActivity.EXTRA_FOOD_RECIPE, recipe);
                        intent.putExtra(PublishFoodActivity.EXTRA_REEDIT_RECIPE, true);
                        startActivity(intent);
                    } else {
                        ToastAlarm.show(R.string.app_error_get_food_detail_fail);
                    }
                }
            });

        } else if (item.getItemId() == Menu.FIRST + 2) {
            new ConfirmDialog(getContext(), R.string.app_alarm_delete).setConfirm(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //删除
                    FoodApi.deleteMyFoodRecipe(food.id, new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            if (result.available()) {
                                MobclickAgent.onEvent(getContext(), LogConstant.NEW_RECIPE_MYRECIPE_DELETE);
                                removeItem(food);
                                ToastAlarm.show(R.string.app_tip_delete_success);
                            }
                        }
                    });
                }
            }).show();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void requestList(int page, int pageSize, ApiCallback<ApiModelList<Food>> callback) {
        FoodApi.getMyFoodRecipeList(page, pageSize, callback);
    }

    @Override
    protected void updateItemView(View view, int position, int viewType, Food data, Bundle extra) {
        ImageView image = (ImageView) view.findViewById(R.id.app_recipe_image);
        TextView name = (TextView) view.findViewById(R.id.app_recipe_name);
        TextView tags = (TextView) view.findViewById(R.id.app_recipe_tags);
        TextView time = (TextView) view.findViewById(R.id.app_recipe_time);
        TextView viewCount = (TextView) view.findViewById(R.id.app_recipe_view);
        TextView favCount = (TextView) view.findViewById(R.id.app_recipe_fav);
        TextView cookCount = (TextView) view.findViewById(R.id.app_recipe_cook);

        ImageFetcher.asInstance().load(data.image, image);

        name.setText(data.title);
        tags.setText(data.getTags());
        time.setText(getTime(data));
        viewCount.setText(data.view);
        favCount.setText(data.favourite);
        cookCount.setText(data.works);
    }

    private String getTime(Food data) {
        String time = "";
        try {
            long createTime = Long.parseLong(data.createTime);
            time = StringUtils.formatTimeWithNearBy(createTime, "yyyy-MM-dd");
        } catch (Exception e) {
        }
        return time;
    }


    @Override
    protected int getItemLayoutId() {
        return R.layout.listview_item_recipe;
    }

}
