package cn.wecook.app.main.kitchen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.asynchandler.SimpleSync;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.sdk.policy.KitchenWebInspireSearchPolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;
import com.wecook.uikit.widget.shape.HaloCircleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;

/**
 * 网络版厨房组菜
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/1/14
 */
public class KitchenWebGarnishFragment extends BaseListFragment {

    public static final String KEY_SHOW_TIPS = "show_tips";
    private ListView mListView;
    private ViewGroup mSelectedViewGroup;
    private View mDetailRecipeView;
    private TextView mDetailRecipeCount;
    private View mOperatorEmpty;
    private View mOperator;
    private View mGarnishEmpty;
    private EmptyView mCategorySearchEmpty;
    private View mGarnishTip1;
    private View mGarnishTip2;
    private TextView mGarnishSearchTips;
    private PullToRefreshListView mPullToRefreshListView;
    private CategoryBar mCategoryBar;

    private RecipeAdapter mRecipeAdapter;

    private List<ApiModelGroup<FoodIngredient>> mIngredientList = new ArrayList<ApiModelGroup<FoodIngredient>>();

    /**
     * 搜索食材列表监听
     */
    private KitchenWebInspireSearchPolicy.OnSearchKeywordListener mSearchListener
            = new KitchenWebInspireSearchPolicy.OnSearchKeywordListener() {
        @Override
        public void onStart() {
            showLoading();
        }

        @Override
        public void onResult(String keyword, List<FoodIngredient> foodIngredients) {
            if (!StringUtils.isEmpty(keyword)) {
                LogGather.onEventGarnishSearch(keyword,
                        KitchenWebInspireSearchPolicy.get().getSelectedKeys(),
                        foodIngredients != null && !foodIngredients.isEmpty());
                updateSpareIngredientList(foodIngredients);
            }
            showSearchTips();
        }
    };

    private boolean mIsShowTips;
    private boolean mIsShowTips1;
    private boolean mIsShowTips2;
    private boolean mHasShowTips1;
    private boolean mNeedShowTips2;
    private boolean mInSearchMode;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setTitle(R.string.app_title_garnish_food);
        mIsShowTips = (Boolean) SharePreferenceProperties.get(KEY_SHOW_TIPS, true);
        setFixed(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kitchen_garnish, null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCategoryBar = (CategoryBar) view.findViewById(R.id.app_kitchen_garnish_category_bar);
        mCategoryBar.setOnSelectItemListener(new CategoryBar.OnSelectItemListener() {
            @Override
            public void onSelectItem(String itemName) {
                if ("全部".equals(itemName)) {
                    itemName = "";
                }
                KitchenWebInspireSearchPolicy.get().selectCategory(itemName);
            }
        });

        mGarnishSearchTips = (TextView) view.findViewById(R.id.app_kitchen_garnish_search_tip);

        mGarnishEmpty = view.findViewById(R.id.app_kitchen_garnish_empty);
        mCategorySearchEmpty = (EmptyView) view.findViewById(R.id.app_kitchen_category_search_empty);

        mGarnishTip1 = view.findViewById(R.id.app_kitchen_garnish_tip1);
        mGarnishTip1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGarnishTip1.setVisibility(View.GONE);
                mIsShowTips1 = false;
                mHasShowTips1 = true;
            }
        });
        mGarnishTip2 = view.findViewById(R.id.app_kitchen_garnish_tip2);
        mGarnishTip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGarnishTip2.setVisibility(View.GONE);
                mIsShowTips2 = false;
                mIsShowTips = false;
                SharePreferenceProperties.set(KEY_SHOW_TIPS, false);
            }
        });
        mDetailRecipeCount = (TextView) view.findViewById(R.id.app_kitchen_garnish_recipe_count);
        mSelectedViewGroup = (ViewGroup) view.findViewById(R.id.app_kitchen_garnish_operator_group);
        if (Build.VERSION.SDK_INT >= 11) {
            mSelectedViewGroup.getLayoutTransition().setDuration(200);
        }
        mDetailRecipeView = view.findViewById(R.id.app_kitchen_item_image);
        mOperator = view.findViewById(R.id.app_kitchen_garnish_operator);
        mOperatorEmpty = view.findViewById(R.id.app_kitchen_garnish_operator_empty);

        mDetailRecipeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsShowTips1 || mIsShowTips2) {
                    return;
                }
                KeyboardUtils.closeKeyboard(getContext(), getView());

                LogGather.onEventGarnishRecipe(KitchenWebInspireSearchPolicy.get().getSelectedKeys(),
                        mDetailRecipeCount.getText().toString());
                //跳转到菜谱列表
                next(KitchenRecipeListFragment.class, getString(R.string.app_title_garnish_food_recipe,
                        mDetailRecipeCount.getText().toString()));
            }
        });

        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.app_kitchen_recipe_list);

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                boolean result = performLoadMore();
                if (!result) {
                    UIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPullToRefreshListView.onRefreshComplete();
                        }
                    });
                }
            }
        });
        mListView = mPullToRefreshListView.getRefreshableView();
        mRecipeAdapter = new RecipeAdapter(getContext(), mIngredientList);
        mListView.setAdapter(mRecipeAdapter);

        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCategoryBar.collapse();
                KeyboardUtils.closeKeyboard(getContext(), getTitleBar().getSearchInput());
                return false;
            }
        });

        setListAdapter(mListView, mRecipeAdapter);
        getLoadMore().setPageSize(KitchenWebInspireSearchPolicy.MAX_PAGE_SIZE);
        getLoadMore().setOneItemWeight(3);
        getLoadMore().setOnLoadMoreListener(new LoadMore.OnLoadMoreListener() {
            @Override
            public void onLoaded(boolean success, Object o) {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshListView.onRefreshComplete();
                    }
                });
            }
        });


        getTitleBar().setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KitchenWebInspireSearchPolicy.get().release();
                finishFragment();
            }
        });
        getTitleBar().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getTitleBar().getViewTreeObserver().removeOnPreDrawListener(this);
                getTitleBar().setSearchHint(R.string.app_search_resource_hint);
                getTitleBar().setSearchListener(false, true, new TitleBar.SearchListener() {

                    @Override
                    public void onSearchViewVisible(boolean visible) {
                        if (visible) {
                            mGarnishSearchTips.setVisibility(View.VISIBLE);
                            getTitleBar().setSearchText("");
                            showSearchTips();
                            mCategoryBar.setVisibility(View.GONE);
                        } else {
                            KitchenWebInspireSearchPolicy.get().doInspireSearch(false);
                            mGarnishSearchTips.setVisibility(View.GONE);
                            mCategoryBar.setVisibility(View.VISIBLE);
                        }

                        mInSearchMode = visible;
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() == 0) {
                            KeyboardUtils.closeKeyboard(getContext(), getTitleBar().getSearchInput());
                        }
                        KitchenWebInspireSearchPolicy.get().searchFoodIngredients(s.toString(), 1, mSearchListener);
                    }
                });
                return false;
            }
        });
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {
        return SyncHandler.sync(new SimpleSync<List>() {
            @Override
            public void sync(final Callback<List> callback) {

                if (mInSearchMode) {
                    KitchenWebInspireSearchPolicy.get().searchFoodIngredients(getTitleBar().getSearchText(),
                            currentPage, pageSize, new KitchenWebInspireSearchPolicy.OnSearchKeywordListener() {
                                @Override
                                public void onStart() {
                                    UIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showLoading();
                                        }
                                    });
                                }

                                @Override
                                public void onResult(String key, List<FoodIngredient> foodIngredients) {
                                    ApiModelGroup<FoodIngredient> group = new ApiModelGroup<FoodIngredient>(3);
                                    callback.callback(group.loadChildrenFromList(foodIngredients));
                                    UIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoading();
                                        }
                                    });
                                }
                            });
                } else {
                    KitchenWebInspireSearchPolicy.get().obtainIngredientArray(currentPage, pageSize,
                            new KitchenWebInspireSearchPolicy.OnInspireSearchIngredientChangedListener() {
                                @Override
                                public void onStart() {
                                    UIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showLoading();
                                        }
                                    });
                                }

                                @Override
                                public void onResult(KitchenWebInspireSearchPolicy.InspireSearchIngredientsResult result) {
                                    ApiModelGroup<FoodIngredient> group = new ApiModelGroup<FoodIngredient>(3);
                                    callback.callback(group.loadChildrenFromList(result.result));
                                    UIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoading();
                                        }
                                    });
                                }

                                @Override
                                public void onSelectChange(boolean action, FoodIngredient ingredient, List<FoodIngredient> selected) {

                                }
                            });
                }
            }
        });
    }

    private void updateEmpty() {
        if (mInSearchMode) {
            mCategorySearchEmpty.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_search);

            Spannable reportMiss = StringUtils.getSpanClickable(getContext(), R.string.app_report_miss_ingredient,
                    new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            if (!StringUtils.isEmpty(getTitleBar().getSearchText())) {
                                KitchenWebInspireSearchPolicy.get().reportMissIngredient(getTitleBar().getSearchText());
                            }
                        }
                    }, R.color.uikit_font_orange);

            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append(reportMiss);
            sb.append("，味小库会尽快收录");
            StringUtils.addSpan(mCategorySearchEmpty.getSecondTitle(), sb);
            mCategorySearchEmpty.setTitle("没有找到该食材");
        } else {
            mCategorySearchEmpty.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_search);
            mCategorySearchEmpty.setTitle("未搜索到结果");
            mCategorySearchEmpty.setSecondTitle("该分类下无可搭配食材");
        }
    }

    private void showSearchTips() {
        List<FoodIngredient> selected = KitchenWebInspireSearchPolicy.get().getSelectedIngredients();
        String searchKey = getTitleBar().getSearchText();
        if (selected.isEmpty() && StringUtils.isEmpty(searchKey)) {
            mGarnishSearchTips.setText(R.string.app_kitchen_search_tip1);
        } else if (selected.isEmpty() && !StringUtils.isEmpty(searchKey)) {
            mGarnishSearchTips.setText(R.string.app_kitchen_search_tip2);
        } else if (!selected.isEmpty() && StringUtils.isEmpty(searchKey)) {
            mGarnishSearchTips.setText(R.string.app_kitchen_search_tip3);
        } else if (!selected.isEmpty() && !StringUtils.isEmpty(searchKey)) {
            mGarnishSearchTips.setText(R.string.app_kitchen_search_tip4);
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        KitchenWebInspireSearchPolicy.get().prepare(new KitchenWebInspireSearchPolicy.OnInspireSearchPreparedListener() {
            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onPreparedCategories(List<String> categories) {
                //分类数据更新
                mCategoryBar.updateItems(categories);
            }

            @Override
            public void onPreparedInspireSearch(KitchenWebInspireSearchPolicy.InspireSearchIngredientsResult result) {
                updateSpareIngredientList(result != null ? result.result : null);
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                    }
                });
            }
        });

        KitchenWebInspireSearchPolicy.get().setIngredientChangedListener(new KitchenWebInspireSearchPolicy.OnInspireSearchIngredientChangedListener() {

            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onResult(KitchenWebInspireSearchPolicy.InspireSearchIngredientsResult result) {
                int count = KitchenWebInspireSearchPolicy.get().getSelectedIngredients().size();
                if (count == 1
                        && mIsShowTips1 && !mHasShowTips1 && mIsShowTips) {
                    mGarnishTip1.setVisibility(View.VISIBLE);
                }

                if (count == 2 && mNeedShowTips2 && mIsShowTips) {
                    mGarnishTip2.setVisibility(View.VISIBLE);
                    mIsShowTips2 = true;
                    mHasShowTips1 = false;
                }
                updateRecipeList(result.recipeCount);
                updateSpareIngredientList(result.result);
                hideLoading();
            }

            @Override
            public void onSelectChange(boolean action, FoodIngredient ingredient, List<FoodIngredient> selected) {
                updateSelectedList(action, ingredient, selected);
                showSearchTips();
                mCategoryBar.collapse();
            }
        });

    }


    public int getColorPos(FoodIngredient ingredient) {
        int pos = 0;
        if ("roulei".equals(ingredient.getCategory())) {
            pos = 0;
        } else if ("shuguo".equals(ingredient.getCategory())) {
            pos = 1;
        } else if ("dannaidou".equals(ingredient.getCategory())) {
            pos = 3;
        } else {
            pos = 2;
        }
        return pos;
    }

    /**
     * 更新可做菜谱列表
     *
     * @param recipeCount
     */
    private void updateRecipeList(int recipeCount) {
        mDetailRecipeCount.setText(recipeCount + "");
    }

    /**
     * 更新备选食材列表
     *
     * @param filterList
     */
    private void updateSpareIngredientList(List<FoodIngredient> filterList) {
        getLoadMore().reset();
        if (filterList != null) {
            filterList.removeAll(KitchenWebInspireSearchPolicy.get().getSelectedIngredients());
        }
        mIngredientList.clear();
        ApiModelGroup<FoodIngredient> group = new ApiModelGroup<FoodIngredient>(3);
        mIngredientList.addAll(group.loadChildrenFromList(filterList));
        mRecipeAdapter.notifyDataSetChanged();
        mListView.setSelection(0);
        if (mIngredientList.isEmpty()) {
            updateEmpty();
            //在搜索(无选中食材，有搜索名)和分类模式下，空界面的展示方式
            if (mCategoryBar.isOpen() || (mInSearchMode
                    && KitchenWebInspireSearchPolicy.get().getSelectedIngredients().isEmpty()
                    && !StringUtils.isEmpty(getTitleBar().getSearchText()))) {
                mCategorySearchEmpty.setVisibility(View.VISIBLE);
            } else {
                mCategorySearchEmpty.setVisibility(View.GONE);
            }
            mGarnishEmpty.setVisibility(View.VISIBLE);
        } else {
            mGarnishEmpty.setVisibility(View.GONE);
            mCategorySearchEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * 更新已选择的列表
     *
     * @param isAdd
     * @param changedIngredient
     * @param addedList
     */
    private void updateSelectedList(boolean isAdd, FoodIngredient changedIngredient, List<FoodIngredient> addedList) {
        if (isAdd) {
            createIngredientView(changedIngredient);
        } else {
            removeIngredientView(changedIngredient);
        }

        if (addedList.isEmpty()) {
            mOperator.setVisibility(View.INVISIBLE);
            mOperatorEmpty.setVisibility(View.VISIBLE);
        } else {
            mOperator.setVisibility(View.VISIBLE);
            mOperatorEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * 创建食材图片
     *
     * @param ingredient
     * @return
     */
    private void createIngredientView(final FoodIngredient ingredient) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_resource_item_horizontal, null);
        TextView name = (TextView) view.findViewById(R.id.app_kitchen_item_name);
        name.setText(ingredient.getName());
        HaloCircleImageView image = (HaloCircleImageView) view.findViewById(R.id.app_kitchen_item_image);
        ImageFetcher.asInstance().load(ingredient.getImage(), image, R.color.uikit_grey_light);
        if (mSelectedViewGroup.getChildCount() == 0) {
            view.findViewById(R.id.app_kitchen_item_div).setVisibility(View.GONE);
        }
        view.setTag(R.id.app_view_tag_ingredient, ingredient);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsShowTips1 || mIsShowTips2) {
                    return;
                }
                int index = 0;
                for (int i = 0; i < mSelectedViewGroup.getChildCount(); i++) {
                    View child = mSelectedViewGroup.getChildAt(i);
                    FoodIngredient viewIngredient = (FoodIngredient) child.getTag(R.id.app_view_tag_ingredient);
                    if (viewIngredient == ingredient) {
                        index = i;
                        break;
                    }
                }

                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_NAME, ingredient.getName());
                keys.put(LogConstant.KEY_INDEX, index + "");
                MobclickAgent.onEvent(getContext(), LogConstant.UBS_KITCHEN_GUIDEDSEARCH_REMOVE_COUNT, keys);

                KitchenWebInspireSearchPolicy.get().inspireSearch(ingredient, false);
            }
        });
        mSelectedViewGroup.addView(view, 0);
    }

    /**
     * 移除
     *
     * @param ingredient
     */
    private void removeIngredientView(FoodIngredient ingredient) {
        int count = mSelectedViewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mSelectedViewGroup.getChildAt(i);
            FoodIngredient foodIngredient = (FoodIngredient) child.getTag(R.id.app_view_tag_ingredient);
            if (foodIngredient.equals(ingredient)) {
                if (i == count - 1 && count > 1) {
                    //清理最后一个，则前一个去掉DIV分割
                    View pre = mSelectedViewGroup.getChildAt(i - 1);
                    pre.findViewById(R.id.app_kitchen_item_div).setVisibility(View.GONE);
                }
                mSelectedViewGroup.removeView(child);
                break;
            }
        }
    }

    @Override
    public boolean back(Bundle data) {
        if (super.back(data)) {
            KitchenWebInspireSearchPolicy.get().release();
            return finishFragment(data);
        }
        return false;
    }

    /**
     * 列表食材数据
     */
    private class RecipeAdapter extends UIAdapter<ApiModelGroup<FoodIngredient>> {

        public RecipeAdapter(Context context, List<ApiModelGroup<FoodIngredient>> data) {
            super(context, R.layout.listview_item_kitchen, data);
        }

        @Override
        protected View newView(int viewType) {
            View view = super.newView(viewType);
            View left = view.findViewById(R.id.app_kitchen_item_left);
            View mid = view.findViewById(R.id.app_kitchen_item_mid);
            View right = view.findViewById(R.id.app_kitchen_item_right);

            float tbSpace = getContext().getResources().getDimension(R.dimen.app_kitchen_item_tb_space);
            float lrSpace = getContext().getResources().getDimension(R.dimen.app_kitchen_item_lr_space);
            ScreenUtils.reMargin(left, (int) lrSpace, (int) tbSpace, (int) (lrSpace), (int) tbSpace);
            ScreenUtils.reMargin(mid, (int) lrSpace, (int) tbSpace, (int) (lrSpace), (int) tbSpace);
            ScreenUtils.reMargin(right, (int) lrSpace, (int) tbSpace, (int) (lrSpace), (int) tbSpace);
            return view;
        }

        @Override
        public void updateView(int position, int viewType, ApiModelGroup<FoodIngredient> data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            View left = findViewById(R.id.app_kitchen_item_left);
            View middle = findViewById(R.id.app_kitchen_item_mid);
            View right = findViewById(R.id.app_kitchen_item_right);
            updateItem(left, data.getItem(0), position * 3 + 0);
            updateItem(middle, data.getItem(1), position * 3 + 1);
            updateItem(right, data.getItem(2), position * 3 + 2);
        }

        /**
         * 更新
         *
         * @param view
         * @param item
         */
        private void updateItem(View view, final FoodIngredient item, final int itemPos) {
            if (item == null) {
                view.setVisibility(View.INVISIBLE);
                view.setOnClickListener(null);
                return;
            }
            view.setVisibility(View.VISIBLE);
            TextView name = (TextView) view.findViewById(R.id.app_kitchen_item_name);
            HaloCircleImageView image = (HaloCircleImageView) view.findViewById(R.id.app_kitchen_item_image);
            ImageFetcher.asInstance().load(item.getImage(), image, R.color.uikit_grey_light);
            name.setText(item.getName());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mInSearchMode) {

                        LogGather.onEventGarnishSearchResult(getTitleBar().getSearchText(),
                                KitchenWebInspireSearchPolicy.get().getSelectedKeys(),
                                item.getName(), itemPos);

                        KitchenWebInspireSearchPolicy.get().inspireSearch(item, true);
                        getTitleBar().setSearchText("");
                        showSearchTips();
                    } else {
                        if (mIsShowTips) {
                            if (mIsShowTips1 || (!mHasShowTips1 && mIsShowTips2)) {
                                return;
                            }
                            if (!mHasShowTips1) {
                                mIsShowTips1 = true;
                            } else {
                                mNeedShowTips2 = true;
                            }
                        }

                        Map<String, String> keys = new HashMap<String, String>();
                        keys.put(LogConstant.KEY_FROM, LogConstant.FROM_INGREIDENT_LIST);
                        keys.put(LogConstant.KEY_NAME, item.getName());
                        keys.put(LogConstant.KEY_INDEX, itemPos + "");
                        MobclickAgent.onEvent(getContext(), LogConstant.UBS_KITCHEN_GUIDEDSEARCH_ADD_COUNT, keys);

                        KitchenWebInspireSearchPolicy.get().inspireSearch(item, true);
                        mCategoryBar.collapse();
                        mCategoryBar.selectItem("");
                    }

                }
            });
        }
    }

}
