package cn.wecook.app.main.recommend.list.food;

import android.app.Activity;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.SearchApi;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.widget.EmptyView;

import java.util.HashMap;
import java.util.Map;

import cn.wecook.app.R;

/**
 * 通过关键词获得菜谱列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/17/14
 */
public class FoodListSearchFragment extends FoodListFragment {

    public static final String EXTRA_KEYWORD = "extra_app_uri";

    private String mSearchKey;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mSearchKey = bundle.getString(EXTRA_KEYWORD);
        }

        if (!StringUtils.isEmpty(mSearchKey)) {
            setTitle(mSearchKey);
        }
    }

    @Override
    public void requestFoodList(int page, int pageSize, final ApiCallback<ApiModelList<Food>> callback) {
        SearchApi.search(mSearchKey, page, pageSize, new ApiCallback<ApiModelList<Food>>() {
            @Override
            public void onResult(ApiModelList<Food> result) {

                if (result != null && result.available() && result.isEmpty()) {
                    Map<String, String> keys = new HashMap<String, String>();
                    keys.put(LogConstant.KEY_KEY, mSearchKey);
                    MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPESEARCH_RESULT_EMPTY_COUNT, keys);
                }

                callback.onResult(result);
            }
        });
    }

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView view = super.getEmptyView();
        if (getActivity() != null) {
            view.setTitle(getString(R.string.app_empty_title_search));
            view.setSecondTitle(getString(R.string.app_empty_second_title_search));
            view.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_search);
        }
    }
}
