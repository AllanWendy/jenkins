package cn.wecook.app.main.recommend.list.food;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.adapter.FoodListAdapter;
import cn.wecook.app.features.publish.PublishFoodActivity;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 推荐菜谱
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/7/14
 */
public class RecommendFoodListAdapter extends FoodListAdapter {

    private BaseFragment fragment;

    public RecommendFoodListAdapter(BaseFragment fragment, List<ApiModelGroup<Food>> data) {
        super(fragment, data);
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        return count == 0 ? 0 : count + 1;
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType = super.getItemViewType(position);
        if (position == 0) {
            itemViewType = 1;//Label
        } else if (position == getCount() - 1) {
            itemViewType = 2;//底部界面
        }
        return itemViewType;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    protected View newView(int viewType) {
        View view = super.newView(viewType);
        if (viewType == 1) {
            view = View.inflate(getContext(), R.layout.listview_item_label_index, null);
        } else if (viewType == 2) {
            view = View.inflate(getContext(), R.layout.listview_footer_food, null);
        }

        if (view != null) {
            view.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));
        }
        return view;
    }

    @Override
    public void updateView(int position, int viewType, ApiModelGroup<Food> data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        if (viewType == 2) {

            findViewById(R.id.app_food_do).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserProperties.isLogin()) {
                        // 上传菜谱
                        LogGather.setLogMarker(LogGather.MARK.FROM, "精品菜谱");
                        LogGather.onEventPublishIn();
                        Intent intent = new Intent(getContext(), PublishFoodActivity.class);
                        fragment.startActivity(intent);
                    } else {
                        getContext().startActivity(new Intent(getContext(), UserLoginActivity.class));
                    }
                }
            });
            findViewById(R.id.app_food_more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MobclickAgent.onEvent(getContext(), LogConstant.UBS_HOME_MORE_TAP_COUNT);

                    fragment.setClickMarker(LogConstant.SOURCE_RECOMMEND_MORE);
                    Map<String, String> keys1 = new HashMap<String, String>();
                    keys1.put(LogConstant.KEY_SOURCE, LogConstant.SOURCE_RECOMMEND_MORE);
                    MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_OPEN_COUNT, keys1);

                    Bundle bundle = new Bundle();
                    bundle.putString(FoodListFragment.EXTRA_TITLE, fragment.getString(R.string.app_recommend_label_hot));
                    bundle.putInt(FoodListFragment.EXTRA_START_LINE, 2);
                    fragment.next(FoodListFragment.class, bundle);
                }
            });
        } else if (viewType == 1) {
            TextView name = (TextView) findViewById(R.id.app_list_item_index);
            name.setText(data.getGroupName());
            name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.app_ic_label_hot_food, 0, 0, 0);
            getItemView().setOnClickListener(null);
        } else {
            getItemView().setOnClickListener(null);
        }
    }

    @Override
    public void updateItem(View view, Food item, int position) {
        super.updateItem(view, item, position);
        view.setBackgroundDrawable(null);
    }
}
