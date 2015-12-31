package cn.wecook.app.main.recommend.list.party;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.legacy.TopicApi;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.policy.FavoritePolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.recommend.detail.party.PartyDetailFragment;

/**
 * 活动列表，吃货去哪儿
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/18/14
 */
public class PartyListFragment extends BaseListFragment{
    private PartyAdapter mPartyAdapter;
    private List<Party> mPartyList;

    private Runnable mUpdateList = new Runnable() {
        @Override
        public void run() {
            if (mPartyList.size() == 0) {
                showEmptyView();
            } else {
                mPartyAdapter.notifyDataSetChanged();
            }
            hideLoading();
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mPartyList = new ArrayList<Party>();
        mPartyAdapter = new PartyAdapter(getContext(), mPartyList);
        setListAdapter(getListView(), mPartyAdapter);
        getLoadMore().setPageSize(20);
        setTitle(R.string.app_title_party);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (mPartyList.size() == 0) {
            showLoading();

            TopicApi.getActivityList(1, getLoadMore().getPageSize(), new ApiCallback<ApiModelList<Party>>() {
                @Override
                public void onResult(ApiModelList<Party> result) {
                    mPartyList.addAll(result.getList());
                    UIHandler.post(mUpdateList);
                }
            });
        }
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {
        List list = (List)SyncHandler.sync(new SyncHandler.Sync() {
            List list = null;
            boolean wait = false;
            @Override
            public void syncStart() {
                wait = true;
                TopicApi.getActivityList(currentPage, pageSize, new ApiCallback<ApiModelList<Party>>() {
                    @Override
                    public void onResult(ApiModelList<Party> result) {
                        list = result.getList();
                        wait = false;
                    }
                });
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

    private class PartyAdapter extends UIAdapter<Party> {

        public PartyAdapter(Context context, List<Party> data) {
            super(context, data);
        }

        @Override
        protected View newView(int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_topic_article, null);
            if (view != null) {
                ScreenUtils.resizeView(view.findViewById(R.id.app_topic_image), ScreenUtils.getScreenWidthInt(), 9f / 16f);
                ScreenUtils.rePadding(view, 5,5,5,0);
            }
            return view;
        }

        @Override
        public void updateView(int position, int viewType, final Party data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            TextView title = (TextView) findViewById(R.id.app_topic_title);
            TextView subTitle = (TextView) findViewById(R.id.app_topic_sub_title);
            ImageView image = (ImageView) findViewById(R.id.app_topic_image);
            final ImageView favImage = (ImageView) findViewById(R.id.app_topic_fav);
            favImage.setVisibility(View.VISIBLE);
            if (data != null) {
                title.setText(data.getTitle());
                title.setTextColor(getResources().getColor(R.color.uikit_font_white));
                title.setBackgroundDrawable(null);
                String startTime = StringUtils.formatTime(Long.parseLong(data.getStartDate()) * 1000
                        , "MM.dd HH:mm");
                String endTime = StringUtils.formatTime(Long.parseLong(data.getEndDate()) * 1000
                        , "MM.dd HH:mm");
                boolean isExpireTime = System.currentTimeMillis() > Long.parseLong(data.getEndDate()) * 1000;
                subTitle.setText(Html.fromHtml(data.getCity()
                        + " | " + (isExpireTime ? "<font color='red'>已结束</font>" : (startTime + " - " +endTime))
                        + " | " + getString(R.string.app_want_join_party, data.getFavouriteCount())));

                subTitle.setBackgroundDrawable(null);
                subTitle.setTextColor(getResources().getColor(R.color.uikit_font_white));
                ImageFetcher.asInstance().load(data.getImage(), image, R.drawable.app_pic_default_party_list);

                FavoritePolicy.favoriteHelper(favImage, FavoriteApi.TYPE_PARTY,
                        StringUtils.parseInt(data.getId()), data, new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getContext(), UserLoginActivity.class));
                            }
                        }
                );

                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Map<String, String> keys = new HashMap<String, String>();
                        keys.put(LogConstant.KEY_NAME, data.getTitle());
                        MobclickAgent.onEvent(BaseApp.getApplication(), LogConstant.UBS_TOPIC_EVENTLIST_TAP_COUNT, keys);

                        Bundle bundle = new Bundle();
                        bundle.putString(PartyDetailFragment.EXTRA_TITLE, data.getTitle());
                        bundle.putSerializable(PartyDetailFragment.EXTRA_PARTY, data);
                        next(PartyDetailFragment.class, bundle);
                    }
                });
            }

        }
    }
}
