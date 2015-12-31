package cn.wecook.app.main.kitchen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.KitchenHomePolicy;
import com.wecook.sdk.policy.LogConstant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.main.recommend.list.food.FoodListGarnishFragment;

/**
 * 食材
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public class KitchenHomeChildIngredientFragment extends KitchenHomeChildFragment {

    private View mFooterView;
    private TextView mTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFooterView = inflater.inflate(R.layout.view_resource_item_footer, null);
        mFooterView.setVisibility(View.GONE);
        ViewGroup viewGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        viewGroup.addView(mFooterView);
        return viewGroup;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = (TextView) mFooterView.findViewById(R.id.app_resource_footer_name);
        mTextView.setText(R.string.app_button_title_garnish);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<FoodResource> list = getListData();
                String resourceNames = "";
                for (FoodResource resource : list) {
                    resourceNames += resource.getName() + " ";
                }

                setClickMarker(LogConstant.SOURCE_GARNISH);
                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_SOURCE, LogConstant.SOURCE_GARNISH);
                MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_OPEN_COUNT, keys);

                MobclickAgent.onEvent(getContext(), LogConstant.UBS_KITCHEN_COOKSEARCH_COUNT);
                Bundle bundle = new Bundle();
                bundle.putString(FoodListGarnishFragment.EXTRA_TITLE, getString(R.string.app_title_garnish_recipe_list));
                bundle.putString(FoodListGarnishFragment.EXTRA_NAMES, resourceNames.trim());
                next(FoodListGarnishFragment.class, bundle);
            }
        });
    }

    @Override
    protected List<FoodResource> getListData() {

        final List<FoodResource> list = KitchenHomePolicy.getInstance().getLocalListByType(KitchenApi.TYPE_INGREDIENT);

        if (mFooterView != null) {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (list.size() <= 1 || KitchenHomePolicy.getInstance().isInEditor()) {
                        mFooterView.setVisibility(View.GONE);
                    } else {
                        mFooterView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        return list;
    }

    @Override
    protected void onAddActionClick() {
        next(KitchenAddFragment.class);
    }

    @Override
    protected String getFirstItemName() {
        return getString(R.string.app_kitchen_home_item_add_ingredient_title);
    }

    @Override
    protected int getFirstItemDrawableId() {
        return R.drawable.app_ic_kitchen_add;
    }
}
