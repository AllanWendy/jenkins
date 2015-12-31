package cn.wecook.app.main.kitchen;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.policy.KitchenWebInspireSearchPolicy;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.main.recommend.list.food.FoodListFragment;

/**
 * 厨房组菜结果列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/13
 */
public class KitchenRecipeListFragment extends FoodListFragment {


    private List<String> mRecipeIds = new ArrayList<String>();
    private LoadMore<List<String>> mRecipeIdLoadMore = new LoadMoreImpl<List<String>>();

    private ApiCallback<ApiModelList<Food>> mWaitingCallback;
    @Override
    public void onStartUILoad() {
        showLoading();
        mRecipeIds.clear();
        mRecipeIdLoadMore.setMoreLoader(new UILoader<List<String>>(this) {
            @Override
            public List<String> runBackground() {
                return SyncHandler.sync(this);
            }

            @Override
            public void sync(final Callback<List<String>> callback) {
                super.sync(callback);
                KitchenWebInspireSearchPolicy.get().obtainRecipeIds(mRecipeIdLoadMore.getCurrentPage(),
                        mRecipeIdLoadMore.getPageSize(),
                        new KitchenWebInspireSearchPolicy.OnInspireSearchRecipeIdsListener() {
                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onResult(List<String> result) {
                                callback.callback(result);
                            }
                        });
            }
        });
        mRecipeIdLoadMore.setPageRange(0, 100);
        mRecipeIdLoadMore.setCurrentPage(0);
        mRecipeIdLoadMore.setPageSize(100);
        mRecipeIdLoadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener<List<String>>() {
            @Override
            public void onLoaded(boolean success, List<String> o) {
                if (success) {
                    mRecipeIds.addAll(o);

                    if (isDataLoaded()) {
                        LoadMore loadMore =  KitchenRecipeListFragment.super.getLoadMore();
                        requestFoodList(loadMore.getCurrentPage(), loadMore.getPageSize(), mWaitingCallback);
                    } else {
                        KitchenRecipeListFragment.super.onStartUILoad();
                    }
                } else {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                        }
                    });

                }
            }
        });

        mRecipeIdLoadMore.doLoadMore(true);
    }

    @Override
    protected void requestFoodList(int page, int pageSize, final ApiCallback<ApiModelList<Food>> callback) {

        List<String> subList = ListUtils.subList(mRecipeIds, (page - 1) * pageSize, page * pageSize);
        if (subList != null) {
            String ids = "";
            for (int i = 0; i < subList.size(); i++) {
                String id = subList.get(i);
                if (i == subList.size() - 1) {
                    ids += id;
                } else {
                    ids += id + ",";
                }
            }

            FoodApi.getFoodListByIds(ids, 1, pageSize, callback);
        } else {
            mRecipeIdLoadMore.doLoadMore(true);
            mWaitingCallback = callback;
        }
    }
}
