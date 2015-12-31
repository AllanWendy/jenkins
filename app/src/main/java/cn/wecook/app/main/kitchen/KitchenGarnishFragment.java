package cn.wecook.app.main.kitchen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.policy.KitchenInspireSearchPolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;
import com.wecook.uikit.widget.shape.HaloCircleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.main.recommend.list.food.FoodListIdsFragment;

/**
 * 厨房组菜
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/1/14
 */
public class KitchenGarnishFragment extends BaseListFragment {

    public static final String KEY_SHOW_TIPS = "show_tips";
    private ListView mListView;
    private ViewGroup mSelectedViewGroup;
    private View mDetailRecipeView;
    private TextView mDetailRecipeCount;
    private View mOperatorEmpty;
    private View mOperator;
    private View mGarnishEmpty;
    private View mGarnishTip1;
    private View mGarnishTip2;
    private TextView mGarnishSearchTips;
    private PullToRefreshListView mPullToRefreshListView;
    private CategoryBar mCategoryBar;

    private RecipeAdapter mRecipeAdapter;

    private List<ApiModelGroup<FoodIngredient>> mIngredientList = new ArrayList<ApiModelGroup<FoodIngredient>>();
    private List<FoodRecipe> mRecipeList;

    int[][] colors = new int[][]{
            {R.color.app_halo_cycle_in_red, R.color.app_halo_cycle_out_red},
            {R.color.app_halo_cycle_in_green, R.color.app_halo_cycle_out_green},
            {R.color.app_halo_cycle_in_blue, R.color.app_halo_cycle_out_blue},
            {R.color.app_halo_cycle_in_yellow, R.color.app_halo_cycle_out_yellow},
    };

    /**
     * 搜索食材列表监听
     */
    private KitchenInspireSearchPolicy.OnSearchKeywordListener mSearchListener
            = new KitchenInspireSearchPolicy.OnSearchKeywordListener() {
        @Override
        public void onResult(String keyword, List<FoodIngredient> foodIngredients) {
            if (foodIngredients != null) {
                mIngredientList.clear();
                ApiModelGroup<FoodIngredient> group = new ApiModelGroup<FoodIngredient>(3);
                mIngredientList.addAll(group.loadChildrenFromList(foodIngredients));
                mRecipeAdapter.notifyDataSetChanged();
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
        mCategoryBar.setVisibility(View.GONE);
        mGarnishSearchTips = (TextView) view.findViewById(R.id.app_kitchen_garnish_search_tip);

        mGarnishEmpty = view.findViewById(R.id.app_kitchen_garnish_empty);
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
                List<FoodIngredient> select = KitchenInspireSearchPolicy.getInstance().getSelectedIngredients();
                String titleName = "";
                for (FoodIngredient ingredient : select) {
                    titleName += ingredient.getName() + " ";
                }
                titleName = titleName.trim();
                KeyboardUtils.closeKeyboard(getContext(), getView());

                setClickMarker(LogConstant.SOURCE_INSPIRE_SEARCH);
                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_SOURCE, LogConstant.SOURCE_INSPIRE_SEARCH);
                MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_OPEN_COUNT, keys);

                Map<String, String> keys1 = new HashMap<String, String>();
                keys1.put(LogConstant.KEY_SELECTED, KitchenInspireSearchPolicy.getInstance().getSelectedIds());
                keys1.put(LogConstant.KEY_RECIPE_COUNT, mDetailRecipeCount.getText().toString());
                MobclickAgent.onEvent(getContext(), LogConstant.UBS_KITCHEN_GUIDEDSEARCH_GO_COUNT, keys1);

                //跳转到菜谱列表
                Bundle bundle = new Bundle();
                bundle.putString(FoodListIdsFragment.EXTRA_IDS, KitchenInspireSearchPolicy.getInstance().getSelectedIds());
                bundle.putString(FoodListIdsFragment.EXTRA_TITLE, titleName);
                next(FoodListIdsFragment.class, bundle);
            }
        });

        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.app_kitchen_recipe_list);
        mListView = mPullToRefreshListView.getRefreshableView();
        mRecipeAdapter = new RecipeAdapter(getContext(), mIngredientList);
        mListView.setAdapter(mRecipeAdapter);
        getTitleBar().setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        } else {
                            mGarnishSearchTips.setVisibility(View.GONE);
                        }

                        mInSearchMode = visible;
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        KitchenInspireSearchPolicy.getInstance().searchFoodIngredients(s.toString(), mSearchListener);
                    }
                });
                return false;
            }
        });
    }

    private void showSearchTips() {
        List<FoodIngredient> selected = KitchenInspireSearchPolicy.getInstance().getSelectedIngredients();
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
        if (!KitchenInspireSearchPolicy.getInstance().isPrepared()) {
            showLoading();
        }

        KitchenInspireSearchPolicy.getInstance().prepare(getContext(), new KitchenInspireSearchPolicy.OnPrepareDataListener() {

            @Override
            public void onPrepared(boolean result, List<FoodIngredient> all) {
                if (result) {
                    updateSpareIngredientList(all);
                }
                hideLoading();
            }
        });

        KitchenInspireSearchPolicy.getInstance().setOnRecipeChangedListener(new KitchenInspireSearchPolicy.OnRecipeChangedListener() {

            @Override
            public void onRecipeResult(final List<FoodRecipe> recipeList) {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateRecipeList(recipeList);
                    }
                });

            }

            @Override
            public void onSelected(final boolean isAdd, final FoodIngredient changedIngredient, final List<FoodIngredient> addedList) {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (addedList.size() == 1 && mIsShowTips1 && !mHasShowTips1 && mIsShowTips) {
                            mGarnishTip1.setVisibility(View.VISIBLE);
                        }
                        if (addedList.size() == 2 && mNeedShowTips2 && mIsShowTips) {
                            mGarnishTip2.setVisibility(View.VISIBLE);
                            mIsShowTips2 = true;
                            mHasShowTips1 = false;
                        }
                        updateSelectedList(isAdd, changedIngredient, addedList);
                        showSearchTips();
                    }
                });

            }

            @Override
            public void onFiltered(final List<FoodIngredient> filterList) {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateSpareIngredientList(filterList);
                    }
                });

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
     * @param recipeList
     */
    private void updateRecipeList(List<FoodRecipe> recipeList) {
        mRecipeList = recipeList;
        if (mRecipeList != null) {
            mDetailRecipeCount.setText(mRecipeList.size() + "");
        }
    }

    /**
     * 更新备选食材列表
     *
     * @param filterList
     */
    private void updateSpareIngredientList(List<FoodIngredient> filterList) {
        mIngredientList.clear();
        ApiModelGroup<FoodIngredient> group = new ApiModelGroup<FoodIngredient>(3);
        mIngredientList.addAll(group.loadChildrenFromList(filterList));
        mRecipeAdapter.notifyDataSetChanged();

        if (mIngredientList.isEmpty()) {
            mGarnishEmpty.setVisibility(View.VISIBLE);
        } else {
            mGarnishEmpty.setVisibility(View.GONE);
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
        image.setInColor(getResources().getColor(colors[getColorPos(ingredient)][0]));
        image.setOutColor(getResources().getColor(colors[getColorPos(ingredient)][1]));
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

                KitchenInspireSearchPolicy.getInstance().inspireSearch(ingredient, false);
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
    public void onDestroy() {
        super.onDestroy();
        KitchenInspireSearchPolicy.getInstance().release();
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
            image.setInColor(getResources().getColor(colors[getColorPos(item)][0]));
            image.setOutColor(getResources().getColor(colors[getColorPos(item)][1]));
            ImageFetcher.asInstance().load(item.getImage(), image, R.color.uikit_grey_light);
            name.setText(item.getName());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mInSearchMode) {
                        Map<String, String> keys = new HashMap<String, String>();
                        keys.put(LogConstant.KEY_FROM, LogConstant.FROM_INGREIDENT_SEARCH);
                        keys.put(LogConstant.KEY_NAME, item.getName());
                        keys.put(LogConstant.KEY_INDEX, itemPos + "");
                        MobclickAgent.onEvent(getContext(), LogConstant.UBS_KITCHEN_GUIDEDSEARCH_ADD_COUNT, keys);

                        KitchenInspireSearchPolicy.getInstance().inspireSearch(item, true);
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

                        KitchenInspireSearchPolicy.getInstance().inspireSearch(item, true);
                    }

                }
            });
        }
    }

    @Override
    public boolean back(Bundle data) {
        if (super.back(data)) {
            return finishFragment(data);
        }
        return false;
    }
}
