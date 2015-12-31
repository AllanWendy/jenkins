package cn.wecook.app.main.kitchen;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.core.internet.ApiResult;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.KitchenApi;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.api.model.FoodResourceCategory;
import com.wecook.sdk.policy.KitchenHomePolicy;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.AlphabetIndexBar;
import com.wecook.uikit.widget.AlphabetIndexView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.shape.CircleImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;

/**
 * 添加界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/15/14
 */
public class KitchenAddFragment extends BaseTitleFragment {

    public static final String EXTRA_TYPE = "type";
    private static final int REQUEST_CODE_SEARCH = 1;

    private int mCurrentSelect = -1;
    private ViewGroup mMenuViewGroup;
    private ListView mContentListView;
    private AlphabetIndexBar mIndexBar;

    private List<Menu> mMenuList;
    private MenuAdapter mMenuAdapter;

    private List<Item> mResourceList;
    private ItemAdapter mItemAdapter;
    String[] titles = {
            "推荐",
            "肉类",
            "蔬果",
            "蛋奶豆",
            "调料",
            "厨具",
    };
    int[] drawableIds = {
            R.drawable.app_ic_kitchen_add_label_recommend,
            R.drawable.app_ic_kitchen_add_label_meat,
            R.drawable.app_ic_kitchen_add_label_vegetable,
            R.drawable.app_ic_kitchen_add_label_milk,
            R.drawable.app_ic_kitchen_add_label_condiment,
            R.drawable.app_ic_kitchen_add_label_kitchenware,
    };
    private String type;

    private final List<String> mIndexData = new ArrayList<String>();
    private final Map<String, Integer> mIndexPositionMap = new HashMap<String, Integer>();
    private final Map<Integer, String> mIndexNamePositionMap = new HashMap<Integer, String>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMenuList = new ArrayList<Menu>();

        for (int i = 0; i < titles.length; i++) {
            Menu menu = new Menu();
            menu.title = titles[i];
            menu.drawableId = drawableIds[i];
            mMenuList.add(menu);
        }

        mResourceList = new ArrayList<Item>();

        Bundle bundle = getArguments();
        if (bundle != null) {
            type = bundle.getString(EXTRA_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kitchen_add, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.app_title_kitchen_add));
        TitleBar.ActionCoveredTextView finish = new TitleBar.ActionCoveredTextView(getContext(),
                getString(R.string.app_button_title_finish));
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KitchenHomePolicy.getInstance().updateChangeList();
                back();
            }
        });
        titleBar.addActionView(finish);

        mMenuViewGroup = (ViewGroup) view.findViewById(R.id.app_kitchen_add_menu);
        mContentListView = (ListView) view.findViewById(R.id.app_kitchen_add_content);
        mIndexBar = (AlphabetIndexBar) view.findViewById(R.id.app_kitchen_add_index_bar);

        mMenuAdapter = new MenuAdapter(getContext(), mMenuList);
        mItemAdapter = new ItemAdapter(getContext(), mResourceList);

        mIndexBar.registerCallback(new AlphabetIndexBar.OnAlphabetIndexBar() {
            @Override
            public void onIndexBarTouch(AlphabetIndexView indexView, MotionEvent event) {
            }

            @Override
            public void onIndexChange(AlphabetIndexView indexView) {
                final int pos = mIndexPositionMap.get(indexView.getTagText());
                UIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mContentListView.setSelection(pos);
                    }
                }, 100);
            }
        });

        mContentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                String tag = mIndexNamePositionMap.get(firstVisibleItem);
                mIndexBar.setCurrentItem(tag);
            }
        });

        mContentListView.setAdapter(mItemAdapter);

        mMenuViewGroup.removeAllViews();
        for (int i = 0; i < mMenuAdapter.getCount(); i++) {
            View child = mMenuAdapter.getView(i, null, null);
            mMenuViewGroup.addView(child);
        }

        view.findViewById(R.id.app_kitchen_add_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next(KitchenAddSearchFragment.class);
            }
        });
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        int select = 0;
        if (StringUtils.isEmpty(type)) {
            select = 0;
        } else if (type.equals(KitchenApi.TYPE_INGREDIENT)) {
            select = 0;
        } else if (type.equals(KitchenApi.TYPE_CONDIMENT)) {
            select = 4;
        } else if (type.equals(KitchenApi.TYPE_KITCHENWARE)) {
            select = 5;
        }
        setMenuSelect(select);
    }

    ApiResult result;
    LoadingDialog loadingDialog;

    @Override
    public void showLoading() {
        super.showLoading();
        loadingDialog = new LoadingDialog(getContext());
        loadingDialog.setText(R.string.app_tip_refresh_loading);
        loadingDialog.cancelable(false);
        loadingDialog.show();
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        if (loadingDialog != null) {
            loadingDialog.cancel();
        }
    }

    private void setMenuSelect(int selected) {
        if (mCurrentSelect != selected) {
            mCurrentSelect = selected;
            for (int i = 0; i < mMenuViewGroup.getChildCount(); i++) {
                View view = mMenuViewGroup.getChildAt(i);
                if (i == mCurrentSelect) {
                    view.setSelected(true);
                } else {
                    view.setSelected(false);
                }
            }

            showLoading();

            if (result != null) {
                result.cancel();
            }

            if (selected == 0) {
                result = KitchenApi.getRecommendList(1, 20, new FoodResource(), new ApiCallback<ApiModelList<FoodResource>>() {
                    @Override
                    public void onResult(ApiModelList<FoodResource> result) {
                        final List<Item> list = new ArrayList<Item>();
                        for (FoodResource foodResource : result.getList()) {
                            Item item = new Item();
                            item.isIndex = false;
                            item.resource = foodResource;
                            item.indexName = "";
                            list.add(item);
                        }
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mResourceList.clear();
                                mResourceList.addAll(list);
                                mIndexBar.setVisibility(View.INVISIBLE);
                                mItemAdapter.notifyDataSetChanged();
                                UIHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mContentListView.setSelection(0);
                                    }
                                },200);

                                hideLoading();
                            }
                        });
                    }
                });
            } else {
                result = KitchenApi.getListByTag(titles[selected], new ApiCallback<ApiModelList<FoodResourceCategory>>() {
                    @Override
                    public void onResult(ApiModelList<FoodResourceCategory> result) {

                        List<FoodResourceCategory> categories = result.getList();
                        final List<Item> list = new ArrayList<Item>();

                        int indexPosition = 0;
                        int position = 0;
                        mIndexData.clear();
                        for (FoodResourceCategory category : categories) {
                            String indexName = category.getCategoryName();
                            mIndexData.add(indexName);
                            mIndexPositionMap.put(indexName, indexPosition);
                            Item index = new Item();
                            index.indexName = indexName;
                            index.resource = null;
                            index.isIndex = true;
                            list.add(index);
                            mIndexNamePositionMap.put(position, indexName);
                            position++;
                            if (category.getList() != null) {
                                if (category.getList().getList() != null) {
                                    for (FoodResource foodResource : category.getList().getList()) {
                                        Item item = new Item();
                                        item.isIndex = false;
                                        item.resource = foodResource;
                                        item.indexName = "";
                                        list.add(item);
                                    }
                                    indexPosition += category.getList().getList().size() + 1;
                                    for (FoodResource resource : category.getList().getList()) {
                                        mIndexNamePositionMap.put(position, indexName);
                                        position++;
                                    }
                                }
                            }

                        }
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mResourceList.clear();
                                mResourceList.addAll(list);
                                mIndexBar.setVisibility(View.VISIBLE);
                                mIndexBar.initialization(mIndexData);
                                mItemAdapter.notifyDataSetChanged();
                                UIHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mContentListView.setSelection(0);
                                    }
                                },200);
                                hideLoading();
                            }
                        });
                    }
                });
            }
        }
    }

    private class MenuAdapter extends UIAdapter<Menu> {

        public MenuAdapter(Context context, List<Menu> data) {
            super(context, R.layout.view_category_menu, data);
        }

        @Override
        public void updateView(final int position, int viewType, Menu data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            ImageView icon = (ImageView) findViewById(R.id.app_category_menu_icon);
            TextView name = (TextView) findViewById(R.id.app_category_menu_name);

            icon.setImageResource(data.drawableId);
            name.setText(data.title);

            getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMenuSelect(position);
                }
            });
        }
    }

    private class ItemAdapter extends UIAdapter<Item> {

        public ItemAdapter(Context context, List<Item> data) {
            super(context, R.layout.listview_item_kitchen_add, data);
        }

        @Override
        public int getItemViewType(int position) {
            Item item =  getItem(position);
            if (item.isIndex) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        protected View newView(int viewType) {
            if (viewType == 1) {
                return LayoutInflater.from(getContext()).inflate(R.layout.listview_item_index, null);
            }
            return super.newView(viewType);
        }

        @Override
        public void updateView(int position, int viewType, final Item data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            if (viewType == 1) {
                TextView indexName = (TextView) findViewById(R.id.app_list_item_index);
                indexName.setText(data.indexName);
            } else {
                CircleImageView image = (CircleImageView) findViewById(R.id.app_kitchen_add_image);
                TextView name = (TextView) findViewById(R.id.app_kitchen_add_name);
                View check = findViewById(R.id.app_kitchen_add_check);

                name.setText(data.resource.getName());
                ImageFetcher.asInstance().load(data.resource.getImage(), image);
                KitchenHomePolicy.getInstance().checkSelectState(getItemView(), check, data.resource);

                name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(KitchenResourceDetailFragment.EXTRA_DATA, data.resource);
                        next(KitchenResourceDetailFragment.class, bundle);
                    }
                });
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(KitchenResourceDetailFragment.EXTRA_DATA, data.resource);
                        next(KitchenResourceDetailFragment.class, bundle);
                    }
                });
            }
        }
    }

    static class Item {
        FoodResource resource;
        boolean isIndex;
        String indexName;
    }

    static class Menu {
        public String title;
        public int drawableId;
    }
}
