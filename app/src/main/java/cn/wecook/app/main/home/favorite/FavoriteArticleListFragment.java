package cn.wecook.app.main.home.favorite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.wecook.sdk.api.model.FavoriteArtist;
import com.wecook.sdk.api.model.State;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.main.recommend.detail.topic.TopicDetailFragment;

/**
 * 收藏的文章列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/17/14
 */
public class FavoriteArticleListFragment extends BaseListFragment{

    private List<FavoriteArtist> mArtistList;
    private FavoriteArtistAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mArtistList = new ArrayList<FavoriteArtist>();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        enableTitleBar(false);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mAdapter = new FavoriteArtistAdapter(getContext(), mArtistList);
        setListAdapter(getListView(), mAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();

                FavoriteArtist data = mAdapter.getItem(position - 1);
                if (data != null) {
                    bundle.putString(TopicDetailFragment.EXTRA_URL,
                            "http://m.wecook.cn/topic/detail?inwecook=true&id=" + data.getTopic().getId());
                    bundle.putSerializable(TopicDetailFragment.EXTRA_DATA, data.getTopic());
                    data.getTopic().setUrl("http://m.wecook.cn/topic/detail/id/" + data.getTopic().getId() + ".html");
                    bundle.putString(TopicDetailFragment.EXTRA_TITLE, data.getTopic().getTitle());
                    bundle.putInt(WebViewActivity.EXTRA_PAGE, WebViewActivity.PAGE_TOPIC_DETAIL);
                    startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                }

            }
        });
        getListView().setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(2, Menu.FIRST + 1, 1, R.string.app_context_menu_remove);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == 2) {
            if (item.getItemId() == Menu.FIRST + 1) {
                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                requestFavoriteRemove(menuInfo.position - 1);
            }
        }
        return super.onContextItemSelected(item);
    }

    private void requestFavoriteRemove(int position) {
        final FavoriteArtist artist = ListUtils.getItem(mArtistList, position);
        if (artist != null) {
            FavoriteApi.favoriteRemove(FavoriteApi.TYPE_TOPIC,
                    StringUtils.parseInt(artist.getTopic().getId()),new ApiCallback<State>() {
                @Override
                public void onResult(State result) {
                    if (result != null && result.available()) {
                        mAdapter.removeEntry(artist);
                        if (mArtistList.isEmpty() && getContext() != null) {
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

        if (mArtistList.isEmpty()) {
            showLoading();
            FavoriteApi.favoriteList(FavoriteApi.TYPE_TOPIC, 1, getLoadMore().getPageSize(),
                    new FavoriteArtist(), new ApiCallback<ApiModelList<FavoriteArtist>>() {
                        @Override
                        public void onResult(ApiModelList<FavoriteArtist> result) {
                            mArtistList.addAll(result.getList());
                            UIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mArtistList.isEmpty() && getContext() != null) {
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
                FavoriteApi.favoriteList(FavoriteApi.TYPE_TOPIC, currentPage, pageSize,
                        new FavoriteArtist(), new ApiCallback<ApiModelList<FavoriteArtist>>() {
                            @Override
                            public void onResult(ApiModelList<FavoriteArtist> result) {
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
    private class FavoriteArtistAdapter extends UIAdapter<FavoriteArtist> {

        public FavoriteArtistAdapter(Context context, List<FavoriteArtist> data) {
            super(context, R.layout.listview_item_my_favorite, data);
        }

        @Override
        public void updateView(int position, int viewType, final FavoriteArtist data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            ImageView foodImage = (ImageView) findViewById(R.id.app_favorite_image);
            TextView foodName = (TextView) findViewById(R.id.app_favorite_name);
            TextView desc = (TextView) findViewById(R.id.app_favorite_desc);
            TextView favoriteTime = (TextView) findViewById(R.id.app_favorite_time);

            if (data.getTopic() != null) {
                ImageFetcher.asInstance().load(data.getTopic().getImage(), foodImage);
                foodName.setText(data.getTopic().getTitle());
                desc.setText(data.getTopic().getDescription());
            }

            String time = StringUtils.formatTime(Long.parseLong(data.getCreateTime()), "MM-dd");
            favoriteTime.setText("收藏于" + time);
        }
    }
}
