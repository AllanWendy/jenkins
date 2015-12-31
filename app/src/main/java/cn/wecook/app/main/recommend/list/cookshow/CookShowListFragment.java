package cn.wecook.app.main.recommend.list.cookshow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.CookShowListAdapter;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.pick.PickActivity;

/**
 * 晒厨艺列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/17/14
 */
public class CookShowListFragment extends BaseListFragment {

    public static final String EXTRA_FOOD_ID = "extra_food_id";
    public static final String EXTRA_FOOD_NAME = "extra_food_name";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_SORT_TYPE = "extra_sort_type";
    public static final String EXTRA_START_PAGE = "extra_start_page";

    /**
     * 相关菜谱的厨艺列表
     */
    public static final int TYPE_RECIPE_COOK_SHOW_LIST = 0;

    /**
     * 展示厨艺列表
     */
    public static final int TYPE_ONE_TAB_COOK_SHOW_LIST = 1;

    /**
     * 精选和最新厨艺列表
     */
    public static final int TYPE_TWO_TAB_COOK_SHOW_LIST = 2;

    /**
     * 精选
     */
    public static final int SORT_TYPE_HOTTEST = CookShowApi.SORT_HOT;

    /**
     * 最新
     */
    public static final int SORT_TYPE_NEWEST = CookShowApi.SORT_NEW;

    protected UIAdapter<CookShow> mAdapter;

    private List<CookShow> mListData;

    protected String mFoodId;
    protected String mFoodName;

    protected int mTabFocus;
    protected int mType = TYPE_RECIPE_COOK_SHOW_LIST;
    protected int mStartPage;
    protected int mListType;

    private LoadingDialog mLoadingDialog;

    private Runnable mUpdateUI = new Runnable() {
        @Override
        public void run() {
            if (getContext() != null) {
                if (mListData.isEmpty()) {
                    showEmptyView();
                } else {
                    hideEmptyView();
                    mAdapter.notifyDataSetChanged();
                }
                hideLoading();
            }
        }
    };

    @Override
    protected void onUpdateEmptyView() {
        super.onUpdateEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setTitle(getString(R.string.app_empty_title_cookshow));
            emptyView.setSecondTitle(getString(R.string.app_empty_second_title_cookshow));
            emptyView.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_cookshow);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListData = new ArrayList<CookShow>();

        mStartPage = 1;
        Bundle bundle = getArguments();
        if (bundle != null) {
            mType = bundle.getInt(EXTRA_TYPE);
            mTabFocus = bundle.getInt(EXTRA_SORT_TYPE, SORT_TYPE_HOTTEST);
            mStartPage = bundle.getInt(EXTRA_START_PAGE, 1);
            mFoodId = bundle.getString(EXTRA_FOOD_ID);
            mFoodName = bundle.getString(EXTRA_FOOD_NAME);
        }

        if (!StringUtils.isEmpty(mFoodName)) {
            String title = getString(R.string.app_title_cook_show_food, mFoodName);
            setTitle(title);
        } else {
            setTitle(R.string.app_title_cook_show_list);
        }
    }

    /**
     * 获得界面适配器
     *
     * @param fragment
     * @param data
     * @return
     */
    protected UIAdapter<CookShow> getAdapter(BaseFragment fragment, List<CookShow> data) {
        return new CookShowListAdapter(fragment, data);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mType == TYPE_TWO_TAB_COOK_SHOW_LIST) {
            enableTitleBar(false);
            if (mStartPage == mTabFocus) {
                mLoadingDialog = new LoadingDialog(getContext());
            }
        }

        TitleBar titleBar = getTitleBar();

        final TitleBar.ActionImageView cookShow = new TitleBar.ActionImageView(getContext(), R.drawable.app_ic_upload_food);
        cookShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PickActivity.class);
                intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_COOK_SHOW);
                startActivity(intent);
            }
        });
        titleBar.addActionView(cookShow);

        getRefreshListLayout().setMode(PullToRefreshBase.Mode.BOTH);

        mAdapter = getAdapter(this, mListData);
        setListAdapter(getListView(), mAdapter);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (preCheckLoadData()) {
            showLoading();

            requestListData(mType, mStartPage, getLoadMore().getPageSize(),
                    new ApiCallback<ApiModelList<CookShow>>() {
                        @Override
                        public void onResult(ApiModelList<CookShow> result) {
                            getRefreshListLayout().onRefreshComplete();
                            mListData.addAll(result.getList());
                            UIHandler.post(mUpdateUI);
                        }
                    }
            );

        }
    }

    @Override
    public void showLoading() {
        super.showLoading();
        if (mLoadingDialog != null) {
            mLoadingDialog.show();
        }
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    /**
     * 检查数据可用行
     *
     * @return
     */
    protected boolean preCheckLoadData() {
        return mListData.size() == 0;
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {

        List list = (List) SyncHandler.sync(new SyncHandler.Sync() {
            private boolean wait;
            private Object object;

            @Override
            public void syncStart() {
                requestListData(mType, currentPage, pageSize, new ApiCallback<ApiModelList<CookShow>>() {
                    @Override
                    public void onResult(ApiModelList<CookShow> result) {
                        object = result.getList();
                        wait = false;
                    }
                });
                wait = true;
            }

            @Override
            public boolean waiting() {
                return wait;
            }

            @Override
            public Object syncEnd() {
                return object;
            }
        });

        return list;
    }

    private void requestListData(int type, int currentPage, int pageSize, ApiCallback<ApiModelList<CookShow>> callback) {
        switch (type) {
            case TYPE_RECIPE_COOK_SHOW_LIST:
                requestListDataByFoodName(mFoodName, currentPage, pageSize, callback);
                break;
            case TYPE_ONE_TAB_COOK_SHOW_LIST:
            case TYPE_TWO_TAB_COOK_SHOW_LIST:
                requestListDataByType(mTabFocus, currentPage, pageSize, callback);
                break;
        }
    }

    /**
     * 通过菜谱名请求数据 当Type为{@link #TYPE_RECIPE_COOK_SHOW_LIST}
     *
     * @param foodName
     * @param currentPage
     * @param pageSize
     * @param callback
     */
    protected void requestListDataByFoodName(String foodName, int currentPage, int pageSize, ApiCallback<ApiModelList<CookShow>> callback) {
        CookShowApi.getCookShowList(CookShowApi.TYPE_RECIPE, foodName, currentPage, pageSize, callback);
    }

    /**
     * 通过类型请求数据 当Type为{@link #TYPE_ONE_TAB_COOK_SHOW_LIST,#TYPE_TWO_TAB_COOK_SHOW_LIST}
     *
     * @param type
     * @param currentPage
     * @param pageSize
     * @param callback
     */
    protected void requestListDataByType(int type, int currentPage, int pageSize, ApiCallback<ApiModelList<CookShow>> callback) {
        CookShowApi.getCookShowRecommendList(type, currentPage, pageSize, false, callback);
    }

    @Override
    protected boolean performRefresh() {
        return requestRefresh();
    }

    private boolean requestRefresh() {
        if (NetworkState.available()) {
            mListData.clear();
            Api.startNoCacheMode();
            onStartUILoad();
            Api.stopNoCacheMode();
            return true;
        }

        return false;
    }

    public List<CookShow> getListData() {
        return mListData;
    }
}
