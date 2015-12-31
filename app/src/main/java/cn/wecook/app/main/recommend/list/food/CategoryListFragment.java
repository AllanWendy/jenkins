package cn.wecook.app.main.recommend.list.food;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CategoryApi;
import com.wecook.sdk.api.model.Category;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.EmptyView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.main.search.SearchActivity;

/**
 * 分类界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/23/14
 */
public class CategoryListFragment extends BaseTitleFragment {

    private ViewGroup mMenuViewGroup;
    private ListView mItemListView;

    private List<Category> mCategoryList;
    private Map<Category, List<CategoryItem>> mMapCategoryItemList;
    private List<CategoryItem> mCategoryItemList;

    private View mSearchBar;

    private MenuAdapter mMenuAdapter;
    private ItemAdapter mItemAdapter;

    private int mCurrentSelect = -1;

    private EmptyView mEmptyView;
    private Runnable mUpdateUi = new Runnable() {
        @Override
        public void run() {

            if (mCategoryList != null && mCategoryList.isEmpty()) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mMenuAdapter.notifyDataSetChanged();
                mMenuViewGroup.removeAllViews();
                for (int i = 0; i < mMenuAdapter.getCount(); i++) {
                    View child = mMenuAdapter.getView(i, null, null);
                    mMenuViewGroup.addView(child);
                }
            }

            setMenuSelect(0);

            finishAllLoaded(true);

            hideLoading();
        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCategoryList = new ArrayList<Category>();
        mMapCategoryItemList = new HashMap<Category, List<CategoryItem>>();
        mCategoryItemList = new ArrayList<CategoryItem>();
        mMenuAdapter = new MenuAdapter(getContext(), mCategoryList);
        mItemAdapter = new ItemAdapter(getContext(), mCategoryItemList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = (EmptyView) view.findViewById(R.id.uikit_empty);
        mEmptyView.setRefreshListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartUILoad();
            }
        });

        getTitleBar().setTitle(getString(R.string.app_title_category));
        mSearchBar = view.findViewById(R.id.app_category_search);
        mSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        mMenuViewGroup = (ViewGroup) view.findViewById(R.id.app_category_menu);
        mItemListView = (ListView) view.findViewById(R.id.app_category_content);

        mItemListView.setAdapter(mItemAdapter);

    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        showLoading();
        CategoryApi.getCategoryList(new ApiCallback<ApiModelList<Category>>() {
            @Override
            public void onResult(ApiModelList<Category> result) {
                mCategoryList.clear();
                //右侧的item的集合
                mCategoryList.addAll(result.getList());
                parseToCategoryItemList();
                UIHandler.post(mUpdateUi);
            }
        });
    }

    private void setMenuSelect(int selected) {
        if (mCurrentSelect != selected) {
            mCurrentSelect = selected;
            if (mCategoryList.size() > selected) {
                Category category = mCategoryList.get(selected);
                mCategoryItemList.clear();
                mCategoryItemList.addAll(mMapCategoryItemList.get(category));
                mItemAdapter.notifyDataSetChanged();
            }
            for (int i = 0; i < mMenuViewGroup.getChildCount(); i++) {
                View view = mMenuViewGroup.getChildAt(i);
                if (i == mCurrentSelect) {
                    view.setSelected(true);
                } else {
                    view.setSelected(false);
                }
            }
        }
    }


    private class MenuAdapter extends UIAdapter<Category> {

        public MenuAdapter(Context context, List<Category> data) {
            super(context, R.layout.view_category_menu, data);
        }

        @Override
        public void updateView(final int position, int viewType, final Category data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            ImageView icon = (ImageView) findViewById(R.id.app_category_menu_icon);
            TextView name = (TextView) findViewById(R.id.app_category_menu_name);

            ImageFetcher.asInstance().load(data.getImage(), icon);
            name.setText(data.getTitle());

            getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Map<String, String> keys = new HashMap<String, String>();
                    keys.put(LogConstant.KEY_NAME, data.getTitle());
                    MobclickAgent.onEvent(getContext(), LogConstant.UBS_CATEGORY_LEVEL1_TAP_COUNT, keys);

                    setMenuSelect(position);
                }
            });
        }
    }

    private void parseToCategoryItemList() {
        for (Category category : mCategoryList) {
            List<CategoryItem> itemList = new ArrayList<CategoryItem>();
            //把左侧的分类和右边的标题存放到map集合中   一一对应
            mMapCategoryItemList.put(category, itemList);
            for (Category subCategory : category.getSubCategory().getList()) {
                if (subCategory != null) {
                    CategoryItem item = new CategoryItem();
                    item.isIndex = true;
                    item.indexName = subCategory.getTitle();
                    item.categorys = new ArrayList<Category>();
                    //右边的标题的集合
                    itemList.add(item);

                    int index = 0;
                    CategoryItem subItem = null;
                    for (Category subsubCategory : subCategory.getSubCategory().getList()) {
                        if (index % 3 == 0) {
                            subItem = new CategoryItem();
                            subItem.isIndex = false;
                            subItem.categorys = new ArrayList<Category>();
                            subItem.categorys.add(subsubCategory);
                            //右边标题里存入子目录的对象  遍历所有的数据对象
                            itemList.add(subItem);
                        } else {
                            //在标题的集合里进行子目录的内容的存储
                            subItem.categorys.add(subsubCategory);
                        }
                        index++;
                    }

                }
            }
        }
    }

    private class CategoryItem {
        private List<Category> categorys;
        private String indexName;
        private boolean isIndex;
    }

    private class ItemAdapter extends UIAdapter<CategoryItem> {

        public ItemAdapter(Context context, List<CategoryItem> data) {
            super(context, data);
        }

        @Override
        protected View newView(int viewType) {
            if (viewType == 0) {
                //index
                return LayoutInflater.from(getContext()).inflate(R.layout.view_category_index, null);
            } else {
                return LayoutInflater.from(getContext()).inflate(R.layout.listview_item_category, null);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position).isIndex) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public void updateView(int position, int viewType, final CategoryItem data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            if (viewType == 0) {
                TextView title = (TextView) findViewById(R.id.app_category_index_name);
                title.setText(getString(R.string.app_category_index_name, data.indexName));
                if (StringUtils.isEmpty(data.indexName)
                        || "默认二级".equals(data.indexName)) {
                    title.setVisibility(View.GONE);
                } else {
                    title.setVisibility(View.VISIBLE);
                }
            } else {
                TextView left = (TextView) findViewById(R.id.app_category_item_left);
                TextView center = (TextView) findViewById(R.id.app_category_item_center);
                TextView right = (TextView) findViewById(R.id.app_category_item_right);

                updateItem(left, getSubItem(data.categorys, 0));
                updateItem(center, getSubItem(data.categorys, 1));
                updateItem(right, getSubItem(data.categorys, 2));

            }
        }

        private void updateItem(TextView view, final Category data) {
            if (data != null) {
                view.setText(data.getTitle());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, String> keys = new HashMap<String, String>();
                        keys.put(LogConstant.KEY_NAME, data.getTitle());
                        MobclickAgent.onEvent(getContext(), LogConstant.UBS_CATEGORY_LEVEL2_TAP_COUNT, keys);

                        setClickMarker(LogConstant.SOURCE_CATEGORY);
                        Map<String, String> keys1 = new HashMap<String, String>();
                        keys1.put(LogConstant.KEY_SOURCE, LogConstant.SOURCE_CATEGORY);
                        MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_OPEN_COUNT, keys1);

                        Category type = ListUtils.getItem(mCategoryList, mCurrentSelect);
                        if (type != null) {
                            LogGather.onEventTagSelect(type.getTitle(), data.getTitle());
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString(FoodListSearchFragment.EXTRA_TITLE,
                                data.getTitle());
                        bundle.putString(FoodListSearchFragment.EXTRA_KEYWORD,
                                data.getTitle());
                        next(FoodListSearchFragment.class, bundle);
                    }
                });
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.INVISIBLE);
            }
        }

        private Category getSubItem(List<Category> list, int pos) {
            if (list.size() > pos) {
                return list.get(pos);
            }
            return null;
        }
    }

}
