package cn.wecook.app.main.home.favorite;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.model.FavoriteFood;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.State;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.recommend.detail.food.FoodDetailFragment;

/**
 * 收藏的菜谱列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/17/14
 */
public class FavoriteFoodListFragment extends BaseListFragment {

    private List<FavoriteFood> mFoodList;
    private FavoriteFoodAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFoodList = new ArrayList<FavoriteFood>();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getRefreshListLayout().setMode(PullToRefreshBase.Mode.PULL_FROM_END);

        enableTitleBar(false);

        mAdapter = new FavoriteFoodAdapter(getContext(), mFoodList);
        setListAdapter(getListView(), mAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                FavoriteFood food = mAdapter.getItem(i - 1);
                if (food != null) {
                    bundle.putSerializable(FoodDetailFragment.EXTRA_FOOD, food.getFood());
                    bundle.putString(FoodDetailFragment.EXTRA_TITLE, food.getFood().title);
                    next(FoodDetailFragment.class, bundle);
                }
            }
        });
        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, Menu.FIRST + 1, 1, R.string.app_context_menu_remove);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == 0) {
            if (item.getItemId() == Menu.FIRST + 1) {
                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                if (menuInfo != null) {
                    requestFavoriteRemove(menuInfo.position - 1);
                }
            }
        }
        return super.onContextItemSelected(item);
    }

    private void requestFavoriteRemove(int position) {
        final FavoriteFood food = ListUtils.getItem(mFoodList, position);
        if (food != null) {
            FavoriteApi.favoriteRemove(FavoriteApi.TYPE_RECIPE,
                    StringUtils.parseInt(food.getFood().id), new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            if (result != null && result.available()) {
                                mAdapter.removeEntry(food);
                                if (mFoodList.isEmpty() && getContext() != null) {
                                    getEmptyView().setVisibility(View.VISIBLE);
                                    getEmptyView().setTitle(getString(R.string.app_empty_title_fav));
                                    getEmptyView().setSecondTitle(getString(R.string.app_empty_second_title_fav));
                                    getEmptyView().setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_fav);
                                }
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        if (mFoodList.isEmpty()) {
            showLoading();
            FavoriteApi.favoriteList(FavoriteApi.TYPE_RECIPE, 1, getLoadMore().getPageSize(),
                    new FavoriteFood(), new ApiCallback<ApiModelList<FavoriteFood>>() {
                        @Override
                        public void onResult(ApiModelList<FavoriteFood> result) {
                            mFoodList.addAll(result.getList());
                            UIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mFoodList.isEmpty() && getContext() != null) {
                                        getEmptyView().setVisibility(View.VISIBLE);
                                        getEmptyView().setTitle(getString(R.string.app_empty_title_fav));
                                        getEmptyView().setSecondTitle(getString(R.string.app_empty_second_title_fav));
                                        getEmptyView().setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_fav);
                                    } else {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                    hideLoading();
                                }
                            });
                        }
                    }
            );
        } else {
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {

        List list = (List) SyncHandler.sync(new SyncHandler.Sync() {
            Object list;
            boolean wait;

            @Override
            public void syncStart() {
                wait = true;
                FavoriteApi.favoriteList(FavoriteApi.TYPE_RECIPE, currentPage, pageSize,
                        new FavoriteFood(), new ApiCallback<ApiModelList<FavoriteFood>>() {
                            @Override
                            public void onResult(ApiModelList<FavoriteFood> result) {
                                list = result.getList();
                                wait = false;
                            }
                        }
                );
            }

            @Override
            public boolean waiting() {
                return wait;
            }

            @Override
            public Object syncEnd() {
                return list;
            }
        });

        return list;
    }

    /**
     * 收藏食物的适配
     */
    private class FavoriteFoodAdapter extends UIAdapter<FavoriteFood> {

        public FavoriteFoodAdapter(Context context, List<FavoriteFood> data) {
            super(context, R.layout.listview_item_my_favorite, data);
        }

        @Override
        public void updateView(int position, int viewType, final FavoriteFood data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            ImageView foodImage = (ImageView) findViewById(R.id.app_favorite_image);
            TextView foodName = (TextView) findViewById(R.id.app_favorite_name);
            TextView foodTags = (TextView) findViewById(R.id.app_favorite_desc);
            TextView favoriteTime = (TextView) findViewById(R.id.app_favorite_time);

            if (data.getFood() != null) {
                Food food = data.getFood();
                ImageFetcher.asInstance().load(food.image, foodImage);
                foodName.setText(food.title);
                foodTags.setText(food.tag);
            }

            String time = StringUtils.formatTime(Long.parseLong(data.getCreateTime()), "MM-dd");
            favoriteTime.setText("收藏于" + time);

        }
    }

}
