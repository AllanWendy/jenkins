package cn.wecook.app.main.recommend.list.topic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.legacy.TopicApi;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.policy.FavoritePolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.recommend.detail.topic.TopicDetailFragment;

/**
 * 文章列表，吃货新鲜事
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/18/14
 */
public class TopicListFragment extends BaseListFragment {

    private TopicAdapter mTopicAdapter;
    private ApiModelList<Topic> mTopicList;

    private Runnable mUpdateList = new Runnable() {
        @Override
        public void run() {
            if (mTopicList.isEmpty()) {
                showEmptyView();
            } else {
                mTopicAdapter.notifyDataSetChanged();
            }
            hideLoading();
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getRefreshListLayout().setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mTopicList = new ApiModelList<Topic>(new Topic());
        mTopicAdapter = new TopicAdapter(getContext(), mTopicList.getList());
        setListAdapter(getListView(), mTopicAdapter);
        getLoadMore().setPageSize(20);
        setTitle(R.string.app_title_topic);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (mTopicList.getCountOfList() == 0) {
            showLoading();

            TopicApi.getArticleList(1, getLoadMore().getPageSize(), new ApiCallback<ApiModelList<Topic>>() {
                @Override
                public void onResult(ApiModelList<Topic> result) {
                    mTopicList.addAll(result);
                    UIHandler.post(mUpdateList);
                }
            });
        }
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {
        List list = (List) SyncHandler.sync(new SyncHandler.Sync() {
            List list = null;
            boolean wait = false;

            @Override
            public void syncStart() {
                wait = true;
                TopicApi.getArticleList(currentPage, pageSize, new ApiCallback<ApiModelList<Topic>>() {
                    @Override
                    public void onResult(ApiModelList<Topic> result) {
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

    private class TopicAdapter extends UIAdapter<Topic> {

        public TopicAdapter(Context context, List<Topic> data) {
            super(context, R.layout.listview_item_topic_article, data);
        }

        @Override
        protected View newView(int viewType) {
            View view = super.newView(viewType);
            if (view != null) {
                view.findViewById(R.id.app_topic_sub_title).setVisibility(View.GONE);
                ScreenUtils.resizeView(view.findViewById(R.id.app_topic_image), ScreenUtils.getScreenWidthInt(), 9f / 16f);
                ScreenUtils.rePadding(view, 5, 5, 5, 0);
            }
            return view;
        }

        @Override
        public void updateView(int position, int viewType, final Topic data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            TextView subTitle = (TextView) findViewById(R.id.app_topic_title);
            ImageView image = (ImageView) findViewById(R.id.app_topic_image);
            final ImageView favImage = (ImageView) findViewById(R.id.app_topic_fav);
            favImage.setVisibility(View.VISIBLE);
            if (data != null) {
                subTitle.setText(data.getTitle());
                subTitle.setTextColor(getResources().getColor(R.color.uikit_font_white));
                subTitle.setBackgroundDrawable(null);

                ImageFetcher.asInstance().load(data.getImage(), image, R.drawable.app_pic_default_artist);

                FavoritePolicy.favoriteHelper(favImage, FavoriteApi.TYPE_TOPIC,
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
                        MobclickAgent.onEvent(getContext(), LogConstant.UBS_TOPIC_NEWSLIST_TAP_COUNT, keys);

                        Bundle bundle = new Bundle();
                        bundle.putString(TopicDetailFragment.EXTRA_URL, "http://m.wecook.cn/topic/detail?inwecook=true&id=" + data.getId());
                        bundle.putString(TopicDetailFragment.EXTRA_TITLE, data.getTitle());
                        data.setUrl("http://m.wecook.cn/topic/detail?inwecook=true&id=" + data.getId());
                        bundle.putSerializable(TopicDetailFragment.EXTRA_DATA, data);
                        bundle.putInt(WebViewActivity.EXTRA_PAGE, WebViewActivity.PAGE_TOPIC_DETAIL);
                        startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                    }
                });
            }

        }
    }
}
