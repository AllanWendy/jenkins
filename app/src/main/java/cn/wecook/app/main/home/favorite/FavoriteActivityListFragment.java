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
import com.wecook.sdk.api.model.FavoriteParty;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.api.model.State;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.recommend.detail.party.PartyDetailFragment;

/**
 * 收藏的活动列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/17/14
 */
public class FavoriteActivityListFragment extends BaseListFragment {

    private List<FavoriteParty> mActivityList;
    private FavoriteActivityAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivityList = new ArrayList<FavoriteParty>();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        enableTitleBar(false);

        mAdapter = new FavoriteActivityAdapter(getContext(), mActivityList);
        setListAdapter(getListView(), mAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                FavoriteParty data = mAdapter.getItem(position - 1);
                if (data != null) {
                    bundle.putString(PartyDetailFragment.EXTRA_TITLE, data.getParty().getTitle());
                    bundle.putSerializable(PartyDetailFragment.EXTRA_PARTY, data.getParty());
                    next(PartyDetailFragment.class, bundle);
                }
            }
        });
        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(1, Menu.FIRST + 1, 1, R.string.app_context_menu_remove);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == 1) {
            if(item.getItemId() == Menu.FIRST + 1){
                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                requestFavoriteRemove(menuInfo.position - 1);
            }
        }
        return super.onContextItemSelected(item);
    }

    private void requestFavoriteRemove(int position) {
        final FavoriteParty party = ListUtils.getItem(mActivityList, position);
        if (party != null) {
            FavoriteApi.favoriteRemove(FavoriteApi.TYPE_PARTY,
                    StringUtils.parseInt(party.getParty().getId()),new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            if (result != null && result.available()) {
                                mAdapter.removeEntry(party);
                                if (mActivityList.isEmpty() && getContext() != null) {
                                    getEmptyView().setVisibility(View.VISIBLE);
                                    getEmptyView().setTitle(getString(R.string.app_empty_title_fav));
                                    getEmptyView().setSecondTitle(getString(R.string.app_empty_second_title_fav));
                                    getEmptyView().setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_fav);
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        if (mActivityList.isEmpty()) {
            showLoading();
            FavoriteApi.favoriteList(FavoriteApi.TYPE_PARTY, 1, getLoadMore().getPageSize(),
                    new FavoriteParty(), new ApiCallback<ApiModelList<FavoriteParty>>() {
                        @Override
                        public void onResult(ApiModelList<FavoriteParty> result) {
                            mActivityList.addAll(result.getList());
                            UIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mActivityList.isEmpty() && getContext() != null) {
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
                FavoriteApi.favoriteList(FavoriteApi.TYPE_PARTY, currentPage, pageSize,
                        new FavoriteParty(), new ApiCallback<ApiModelList<FavoriteParty>>() {
                            @Override
                            public void onResult(ApiModelList<FavoriteParty> result) {
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
     * 收藏活动适配
     */
    private class FavoriteActivityAdapter extends UIAdapter<FavoriteParty> {

        public FavoriteActivityAdapter(Context context, List<FavoriteParty> data) {
            super(context, R.layout.listview_item_my_favorite, data);
        }

        @Override
        public void updateView(int position, int viewType, final FavoriteParty data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            ImageView foodImage = (ImageView) findViewById(R.id.app_favorite_image);
            TextView foodName = (TextView) findViewById(R.id.app_favorite_name);
            TextView address = (TextView) findViewById(R.id.app_favorite_desc);
            TextView favoriteTime = (TextView) findViewById(R.id.app_favorite_time);

            if (data.getParty() != null) {
                Party party = data.getParty();
                ImageFetcher.asInstance().load(party.getImage(), foodImage);
                foodName.setText(party.getTitle());
                address.setText(party.getCity());
                String stime = StringUtils.formatTime(Long.parseLong(party.getStartDate()), "MM.dd");
                String etime = StringUtils.formatTime(Long.parseLong(party.getEndDate()), "MM.dd");
                String time = stime + "-" + etime;
                if (Long.parseLong(party.getEndDate()) * 1000 < System.currentTimeMillis()) {
                    time = "已结束";
                }
                favoriteTime.setText(time + " | " + party.getFavouriteCount() + "人想参加");
            }

        }
    }
}
