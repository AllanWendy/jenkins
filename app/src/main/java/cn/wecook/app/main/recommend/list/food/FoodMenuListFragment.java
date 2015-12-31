package cn.wecook.app.main.recommend.list.food;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.app.AppLink;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.legacy.SearchApi;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodTopMenu;
import com.wecook.sdk.api.model.FoodTopMenus;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.widget.EmptyView;

import java.util.HashMap;
import java.util.Map;

import cn.wecook.app.R;

/**
 * 带有顶部Menu菜谱列表
 *
 * @author shan
 * @version v1.0
 * @since 2014-10/8/14
 */
public class FoodMenuListFragment extends FoodListFragment {

    /**
     * 列表加载状态
     */
    public boolean listLoadingState = false;
    private String mSearchKey;
    private FoodTopMenuManager foodTopMenuManager;
    /**
     * 顶部Menu加载状态
     */
    private boolean topMenuLoadingState;
    private MergeAdapter mergeAdapter;
    private ViewGroup mTopMenu;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initMenuView(view);
        setMergeAdapter();
    }

    /**
     * 组合adapter
     */
    private void setMergeAdapter() {
        mergeAdapter = new MergeAdapter();
        mergeAdapter.addView(mTopMenu);
        mergeAdapter.addAdapter(getAdapter());
        ListView listView = getListView();
        if (listView != null) {
            listView.setAdapter(mergeAdapter);
        }

    }

    /**
     * 初始化MenuView
     *
     * @param view
     */
    private void initMenuView(View view) {
        //初始化顶部Menu
        mTopMenu = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.view_food_card_top_menu, null);
        initTopMenuView(mTopMenu);
        setLoadingListener(new LoadingListener() {
            @Override
            public void startLoading() {
                resetData();
                listLoadingState = true;
                loadingControl();
            }

            @Override
            public void endLoading() {
                listLoadingState = false;
                loadingControl();
            }
        });
    }

    /**
     * 重置数据
     */
    private void resetData() {
        setStartPage(1);
        listLoadingState = false;
        topMenuLoadingState = false;
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        requestFoodTopMenu();
    }

    /**
     * 请求顶部Menu数据
     */
    private void requestFoodTopMenu() {
        FoodApi.getTopMenu(PhoneProperties.getLon() + "", PhoneProperties.getLat() + "", new ApiCallback<FoodTopMenus>() {
            @Override
            public void onResult(FoodTopMenus result) {
                topMenuLoadingState = false;
                if (result != null && result.available()) {
                    foodTopMenuManager.setData(result);
                    loadingControl();
                }
            }
        });

    }

    /**
     * 加载进度他控制器
     */
    private void loadingControl() {
        if (!listLoadingState && !topMenuLoadingState) {
            hideLoading();
        } else {
            showLoading();
        }

    }

    /**
     * 初始化
     *
     * @param view
     */
    private void initTopMenuView(View view) {
        foodTopMenuManager = new FoodTopMenuManager(view);
        foodTopMenuManager.setTypeChangeListener(new FoodTopMenuManager.TypeChangeListener() {
            @Override
            public void typeChange(FoodTopMenu result) {
                String url = result.getUrl();
                Uri uri = Uri.parse(url);
                if (uri != null) {
                    String keywords = uri.getQueryParameter("keywords");
                    if (keywords != null && !("".equals(keywords))) {
                        setTitle(keywords);
                        mSearchKey = keywords;
                        onStartUILoad();
                    } else {
                        AppLink.sendLink(uri);
                    }

                }
            }
        });
    }

    @Override
    public void requestFoodList(int page, int pageSize, final ApiCallback<ApiModelList<Food>> callback) {
        if (mSearchKey != null && !("".equals(mSearchKey))) {
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
        } else {
            FoodApi.getRecommendFoodList(page, pageSize, callback);
        }
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


