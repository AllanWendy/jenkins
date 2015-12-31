package cn.wecook.app.main.search;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.core.internet.ApiResult;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.legacy.SearchApi;
import com.wecook.sdk.api.model.SearchSuggestion;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.tools.SmartBarUtils;
import com.wecook.uikit.widget.TitleBar;

import java.util.HashMap;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.main.dish.search.DishSearchResultFragment;
import cn.wecook.app.main.recommend.list.food.FoodListSearchFragment;

/**
 * 搜索界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public class SearchFragment extends BaseTitleFragment {

    public static final String TYPE_RECIPE = "type_recipe";
    public static final String TYPE_DISH = "type_dish";
    public static final String SEARCH_KEY = "search_key";
    public static final int SUG_LIMIT = 20;
    public static String EXTRA_TYPE = "extra_type";
    private SearchHistoryView mHistoryView;
    private SearchSuggestionView mSuggestionView;

    private EditText mInputView;
    private String mInputString;
    private String mSearchType = TYPE_RECIPE;

    private Runnable mRequestSugRunnable = new Runnable() {
        @Override
        public void run() {
            requestSuggestion();
        }
    };
    private String mSearchKey;
    private TitleBar titleBar;
    private ApiResult mSuggestApiResult;

    public String getSearchType() {
        return mSearchType;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /**
         * 魅族SmartBar适配
         */
        if (Build.VERSION.SDK_INT >= 14) {
            final ActionBar actionBar = activity.getActionBar();
            if (actionBar != null && "meizu".equals(PhoneProperties.getChannel())) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayOptions(0);
                final ActionBar.Tab searchTab = actionBar.newTab();
                ActionBar.TabListener mTabListener = new ActionBar.TabListener() {

                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

                    }

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                        if (tab == searchTab) {
                            LogGather.onEventSearchInput(mInputView.getText().toString());
                            search(mInputView.getText().toString());
                        }
                    }
                };
                actionBar.addTab(searchTab.setIcon(R.drawable.uikit_ic_food_search).setTabListener(mTabListener));
                SmartBarUtils.setActionBarTabsShowAtBottom(actionBar, true);
                SmartBarUtils.setActionBarViewCollapsable(actionBar, true);
            }
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSearchType = bundle.getString(EXTRA_TYPE);
            mSearchKey = bundle.getString(SEARCH_KEY);
        }
    }

    @Override
    public void onCardIn(Bundle bundle) {
        super.onCardIn(bundle);
        if (bundle != null) {
            String tempType = bundle.getString(EXTRA_TYPE);
            if (tempType != null && !"".equals(tempType)) {
                mSearchType = tempType;
            }
            mSearchKey = bundle.getString(SEARCH_KEY);
        } else {
            mSearchKey = "";
        }
        setTitleSearchText();
        if (titleBar != null && titleBar.getSearchInput() != null)
            KeyboardUtils.openKeyboard(getContext(), mInputView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_home, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleBar = getTitleBar();
        titleBar.getSearchLayer().setPadding(ScreenUtils.dip2px(15), 0, ScreenUtils.dip2px(10), 0);
        titleBar.enableBack(false);
        titleBar.setSearchLayer(true);
        titleBar.setSearchListener(new TitleBar.SearchListener() {

            @Override
            protected void onSearchFinishClick() {
                super.onSearchFinishClick();
            }

            @Override
            protected void onSearchClick() {
                super.onSearchClick();
                LogGather.onEventSearchInput(mInputView.getText().toString());
                search(mInputView.getText().toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                if (count > 0) {
                    UIHandler.postOnceDelayed(mRequestSugRunnable, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if (mSuggestionView != null && mHistoryView != null) {
                    if (s.length() > 0) {
                        mSuggestionView.setVisibility(View.VISIBLE);
                        mHistoryView.setVisibility(View.GONE);
                    } else {
                        mSuggestionView.setVisibility(View.GONE);
                        mSuggestionView.clearSuggestion();
                        mHistoryView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        titleBar.getSearchAction().setVisibility(View.VISIBLE);
        titleBar.getSearchAction().setText("取消");
        titleBar.getSearchAction().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        setTitleSearchText();

        mInputView = titleBar.getSearchInput();

        mSuggestionView = (SearchSuggestionView) view.findViewById(R.id.app_search_layer_suggestion);
        mHistoryView = (SearchHistoryView) view.findViewById(R.id.app_search_layer_history);

        mSuggestionView.setSearchFragment(this);
        mHistoryView.setSearchFragment(this);

        KeyboardUtils.openKeyboard(getContext(), mInputView);
    }

    private void setTitleSearchText() {
        if (titleBar != null) {
            if (mSearchKey == null) {
                if (TYPE_DISH.equals(mSearchType)) {
                    titleBar.setSearchHint("搜索想吃的菜");
                } else if (TYPE_RECIPE.equals(mSearchType)) {
                    titleBar.setSearchHint("搜索菜谱");
                }
            } else {
                titleBar.setSearchText(mSearchKey);
                titleBar.getSearchInput().setSelection(mSearchKey != null ? mSearchKey.length() : 0);
            }
        }
    }

    /**
     * 请求suggestion
     */
    private void requestSuggestion() {
        String current = mInputView.getText().toString();
        if (!StringUtils.isEmpty(current)
                && !current.equals(mInputString)) {
            mInputString = current;

            if (mSuggestApiResult != null) {
                mSuggestApiResult.cancel();
            }

            ApiCallback<ApiModelList<SearchSuggestion>> callback =
                    new ApiCallback<ApiModelList<SearchSuggestion>>() {

                        @Override
                        public void onResult(ApiModelList<SearchSuggestion> result) {
                            if (result != null && result.available()) {
                                mSuggestionView.updateView(result.getList());
                            }
                        }
                    };
            if (TYPE_DISH.equals(mSearchType)) {
                mSuggestApiResult = DishApi.dishSuggestion(mInputString, SUG_LIMIT, callback);
            } else if (TYPE_RECIPE.equals(mSearchType)) {
                mSuggestApiResult = SearchApi.suggestion(mInputString, SUG_LIMIT, callback);
            }
        }

    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        if (TYPE_DISH.equals(mSearchType)) {
            DishApi.dishSearchHotTag(new ApiCallback<ApiModelList<Tags>>() {
                @Override
                public void onResult(ApiModelList<Tags> result) {
                    if (result != null) {
                        mHistoryView.updateHotSearchTag(result.getList());
                    } else {
                        mHistoryView.updateHotSearchTag(null);
                    }
                }
            });
        } else if (TYPE_RECIPE.equals(mSearchType)) {
            SearchApi.searchHotTag(new ApiCallback<ApiModelList<Tags>>() {
                @Override
                public void onResult(ApiModelList<Tags> result) {
                    if (result != null) {
                        mHistoryView.updateHotSearchTag(result.getList());
                    } else {
                        mHistoryView.updateHotSearchTag(null);
                    }
                }
            });
        }

    }

    /**
     * 搜索
     *
     * @param searchKey
     */
    public void search(String searchKey) {

        if (!StringUtils.isEmpty(searchKey) && !StringUtils.containWith(searchKey, ";")) {
            mHistoryView.addHistory(searchKey);
            KeyboardUtils.closeKeyboard(getContext(), mInputView);

            if (TYPE_RECIPE.equals(mSearchType)) {
                setClickMarker(LogConstant.SOURCE_SEARCH);
                Map<String, String> keys1 = new HashMap<String, String>();
                keys1.put(LogConstant.KEY_SOURCE, LogConstant.SOURCE_SEARCH);
                MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_OPEN_COUNT, keys1);

                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_KEY, searchKey);
                MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPESEARCH_TO_RECIPE_COUNT, keys);

                Bundle bundle = new Bundle();
                bundle.putString(FoodListSearchFragment.EXTRA_KEYWORD, searchKey);
                next(FoodListSearchFragment.class, bundle);
            } else if (TYPE_DISH.equals(mSearchType)) {
                Bundle bundle = new Bundle();
                bundle.putString(DishSearchResultFragment.EXTRA_KEYWORD, searchKey);
                next(DishSearchResultFragment.class, bundle);
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KeyboardUtils.closeKeyboard(getContext(), mInputView);
    }
}
