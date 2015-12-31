package cn.wecook.app.main.recommend.card;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.app.AppLink;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.model.Banner;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;

import java.util.HashMap;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.main.dish.DishRecommendFragment;
import cn.wecook.app.main.recommend.detail.party.PartyDetailFragment;
import cn.wecook.app.main.recommend.detail.topic.TopicDetailFragment;
import cn.wecook.app.main.recommend.list.food.FoodListSearchFragment;

/**
 * Banner栏
 *
 * @author kevin
 * @version v1.0
 * @since 2015-3/5/15
 */
public class BannerView extends BaseView<Banner> {

    private ImageView image;
    private BaseFragment fragment;

    public BannerView(BaseFragment context) {
        super(context.getContext());
        fragment = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        image = (ImageView) findViewById(R.id.app_banner_image);
        ScreenUtils.resizeViewOnScreen(image, 0.57f);
    }

    @Override
    public void updateView(final Banner banner) {
        super.updateView(banner);
        if (banner != null) {
            if (image != null) {
                ImageFetcher.asInstance().load(banner.getImage(), image);
            }
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> keys = new HashMap<String, String>();
                    keys.put(LogConstant.KEY_URI, banner.getUrl());
                    MobclickAgent.onEvent(getContext(), LogConstant.UBS_HOME_BANNER_TAP_COUNT, keys);

                    MobclickAgent.onEvent(getContext(), LogConstant.RECOMMEND_BANNER_CLICK);

                    if (fragment instanceof DishRecommendFragment) {
                        LogGather.onEventDishRecommendBanner(banner.getTitle());
                    }

                    if (Banner.TYPE_TOPIC.equals(banner.getType())) {
                        Bundle bundle = new Bundle();
                        bundle.putString(TopicDetailFragment.EXTRA_URL,
                                "http://m.wecook.cn/topic/detail?inwecook=true&id=" + banner.getForeignId());
                        bundle.putString(TopicDetailFragment.EXTRA_TITLE, banner.getTitle());
                        Topic topic = new Topic();
                        topic.setFav(banner.isFav());
                        topic.setCommentCount(banner.getCommentCount());
                        topic.setId(banner.getForeignId());
                        topic.setTitle(banner.getTitle());
                        topic.setUrl(banner.getUrl());
                        topic.setImage(banner.getImage());
                        bundle.putSerializable(TopicDetailFragment.EXTRA_DATA, topic);
                        bundle.putInt(WebViewActivity.EXTRA_PAGE, WebViewActivity.PAGE_TOPIC_DETAIL);
                        fragment.startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                    } else if (Banner.TYPE_PARTY.equals(banner.getType())) {
                        Bundle bundle = new Bundle();
                        bundle.putString(PartyDetailFragment.EXTRA_TITLE, banner.getTitle());
                        Party party = new Party();
                        party.setId(banner.getForeignId());
                        party.setCommentCount(banner.getCommentCount());
                        party.setFav(banner.isFav());
                        bundle.putSerializable(PartyDetailFragment.EXTRA_PARTY, party);
                        fragment.next(PartyDetailFragment.class, bundle);
                    } else {
                        String url = banner.getUrl();
                        if (URLUtil.isHttpUrl(url)) {
                            Bundle bundle = new Bundle();
                            bundle.putString(WebViewFragment.EXTRA_TITLE, banner.getTitle());
                            bundle.putString(WebViewFragment.EXTRA_URL, url);
                            fragment.startActivity(new Intent(fragment.getContext(), WebViewActivity.class), bundle);
                        } else if (AppLink.isAppUri(url)) {
                            int deliverNo = AppLink.sendLink(url);
                            switch (deliverNo) {
                                case AppLink.SEARCH:
                                    fragment.setClickMarker(LogConstant.SOURCE_BANNER);
                                    Map<String, String> keys1 = new HashMap<String, String>();
                                    keys1.put(LogConstant.KEY_SOURCE, LogConstant.SOURCE_BANNER);
                                    MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPELIST_OPEN_COUNT, keys1);

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FoodListSearchFragment.EXTRA_TITLE, banner.getTitle());
                                    Uri uri = Uri.parse(url);
                                    String searchKey = uri.getQueryParameter("key");
                                    bundle.putString(FoodListSearchFragment.EXTRA_KEYWORD, searchKey);
                                    fragment.next(FoodListSearchFragment.class, bundle);
                                    break;
                            }
                        }
                    }
                }
            });
        }
    }
}
