package cn.wecook.app.main.recommend;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.legacy.OperatorApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Banner;
import com.wecook.sdk.api.model.RecommendCard;
import com.wecook.sdk.api.model.RecommendInfo;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.WecookConfig;
import cn.wecook.app.dialog.AreaServerTipDialog;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.main.recommend.card.BannerMultiView;
import cn.wecook.app.main.recommend.card.RecommendCardView;
import cn.wecook.app.main.recommend.list.cookshow.CookShowPageActivity;
import cn.wecook.app.main.recommend.list.cookshow.CookShowPageListFragment;
import cn.wecook.app.main.search.SearchActivity;
import cn.wecook.app.server.LocalService;

/**
 * “首页”食谱推荐界面
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class RecommendFragment extends BaseListFragment {

    private final String mDefaultIndexInBeijing = RecommendCard.CARD_DISH + ","
            + RecommendCard.CARD_FOOD + ","
            + RecommendCard.CARD_COOK + ","
            + RecommendCard.CARD_PARTY + ","
            + RecommendCard.CARD_TOPIC;
    private final String mDefaultIndexInOther = RecommendCard.CARD_FOOD + ","
            + RecommendCard.CARD_COOK + ","
            + RecommendCard.CARD_PARTY + ","
            + RecommendCard.CARD_TOPIC + ","
            + RecommendCard.CARD_DISH;
    private PullToRefreshListView mPullToRefreshLayout;
    private ListView mListView;
    private MergeAdapter mAdapter;
    private BannerMultiView mBannerView;
    private RecommendCardView mDishView;
    private RecommendCardView mFoodView;
    private RecommendCardView mCookShowView;
    private RecommendCardView mPartyView;
    private RecommendCardView mTopicView;
    private View mFootManageCardView;
    private RecommendInfo mRecommend;
    private List<String> mCardTypes = new ArrayList<>();
    private boolean mIsRefreshRequest = true;
    private AreaServerTipDialog mAreaServerTipDialog;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.d("onReceive .... action : " + action);
            if (CookShowApi.ACTION_COOK_SHOW.equals(action)) {
                Bundle bundle = new Bundle();
                bundle.putInt(CookShowPageListFragment.EXTRA_FOCUS_TAB,
                        CookShowPageListFragment.TAB_FOCUS_NEWEST);
                Intent actionIntent = new Intent(getContext(), CookShowPageActivity.class);
                actionIntent.putExtras(bundle);
                startActivity(actionIntent);
            } else if (WebViewFragment.ACTION_INTENT_WEB_PAGE_LOADED.equals(action)) {
                String url = intent.getStringExtra(WebViewFragment.EXTRA_URL);

                if (!StringUtils.isEmpty(url)
                        && url.contains("maicaibangshou.cn")
                        && !hasShownAreaServerTip()
                        && !WecookConfig.getInstance().isOpenSell()) {
                    if (mAreaServerTipDialog == null) {
                        mAreaServerTipDialog = new AreaServerTipDialog(getContext());
                    }
                    mAreaServerTipDialog.show();
                }
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        IntentFilter filter = new IntentFilter();
        filter.addAction(CookShowApi.ACTION_COOK_SHOW);
        filter.addAction(WebViewFragment.ACTION_INTENT_WEB_PAGE_LOADED);
        LocalBroadcastManager.getInstance(activity).registerReceiver(mReceiver, filter);

        loadCardIndex();
    }

    /**
     * 加载卡片索引顺序
     */
    private void loadCardIndex() {
        boolean isInBeijing = (boolean) SharePreferenceProperties.get(LocalService.KEY_IS_IN_BEIJING, false);
        String defaultIndex = mDefaultIndexInBeijing;
        if (!isInBeijing) {
            defaultIndex = mDefaultIndexInOther;
        }
        String cardTypes = (String) SharePreferenceProperties.get("card_type", defaultIndex);
        String[] types = cardTypes.split(",");
        for (int i = 0; i < types.length; i++) {
            mCardTypes.add(types[i]);
        }
    }

    /**
     * 保存卡片索引顺序
     */
    private void saveCardIndex() {
        String cardTypes = "";
        for (int i = 0; i < mCardTypes.size(); i++) {
            String type = mCardTypes.get(i);
            if (i != mCardTypes.size() - 1) {
                cardTypes += type + ",";
            } else {
                cardTypes += type;
            }
        }
        SharePreferenceProperties.set("card_type", cardTypes);
    }

    /**
     * 对卡片进行排序
     */
    private void sortCard() {
        List<RecommendCard> cardList = mRecommend.getCardList();

        if (cardList != null) {
            for (RecommendCard card : cardList) {
                String type = card.getType();
                for (int i = 0; i < mCardTypes.size(); i++) {
                    String cardType = mCardTypes.get(i);
                    if (cardType.equals(type)) {
                        card.setIndex(i);
                        break;
                    }
                }

            }

            Collections.sort(cardList, new Comparator<RecommendCard>() {
                @Override
                public int compare(RecommendCard lhs, RecommendCard rhs) {
                    return lhs.getIndex() - rhs.getIndex();
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFootManageCardView = inflater.inflate(R.layout.listview_footer_card, null);
        return inflater.inflate(R.layout.fragment_food_recommend, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack(false);
        view.findViewById(R.id.app_food_search_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateListSelection();
            }
        });

        final View searchIcon = view.findViewById(R.id.app_food_bt_search);
        searchIcon.findViewById(R.id.app_food_bt_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.onEventSearchRecommend();

                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        mPullToRefreshLayout = (PullToRefreshListView) view.findViewById(R.id.app_food_content);
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                requestRefresh();
            }
        });

        mListView = mPullToRefreshLayout.getRefreshableView();
        mListView.addFooterView(mFootManageCardView);
        mFootManageCardView.findViewById(R.id.app_foot_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //管理卡片
                new CardManagerDialog(RecommendFragment.this).show();
            }
        });

    }

    private void requestRefresh() {
        if (NetworkState.available()) {
            mIsRefreshRequest = true;
            Api.startNoCacheMode();
            onStartUILoad();
            Api.stopNoCacheMode();
        } else {
            ToastAlarm.show("网络异常，请刷新重试");
            endLoading();
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void showEmptyView() {
        super.showEmptyView();
        EmptyView emptyView = getEmptyView();
        if (emptyView != null) {
            emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_cookshow);
            emptyView.setTitle("味小库数据为空");
            emptyView.setSecondTitle("请检查网络是否可用之后\n点击界面重试");
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        showLoading();
        //TODO 2.4.0测试数据
        try {
            mRecommend = new RecommendInfo();
            mRecommend.parseResult(FileUtils.readAssetsFile("empty.json"));
            mRecommend.setAvailable();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Address address = DishPolicy.get().getDishAddress();
        OperatorApi.getRecommendInfo(address.getLat(), address.getLon(),
                new ApiCallback<RecommendInfo>() {
                    @Override
                    public void onResult(RecommendInfo result) {

                        if (result.available() && result.hasCard()) {
                            mRecommend = result;

                            final MergeAdapter adapter = new MergeAdapter();

                            AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
                                @Override
                                public void run() {
                                    initViews(adapter);
                                }

                                @Override
                                public void postUi() {
                                    super.postUi();
                                    if (mListView != null && mIsRefreshRequest) {
                                        mIsRefreshRequest = false;
                                        mListView.setAdapter(adapter);
                                        mAdapter = adapter;
                                    }
                                    mAdapter.notifyDataSetChanged();
                                    endLoading();
                                }
                            });
                        } else {
                            ToastAlarm.show("网络异常，请刷新重试");
                            quickUpdate();
                            endLoading();
                        }
                    }
                });
    }

    private void quickUpdate() {
        final MergeAdapter adapter = new MergeAdapter();

        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                initViews(adapter);
            }

            @Override
            public void postUi() {
                super.postUi();
                if (mListView != null) {
                    mListView.setAdapter(adapter);
                    mAdapter = adapter;
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initViews(MergeAdapter adapter) {
        //定制化设置
        sortCard();

        if (mRecommend.hasBanner()) {
            initBannerView(adapter);
        }

        if (mRecommend.hasCard()) {
            List<RecommendCard> cardList = mRecommend.getCardList();
            for (int i = 0; i < cardList.size(); i++) {
                RecommendCard card = cardList.get(i);
                if (card != null) {
                    String type = card.getType();
                    if (RecommendCard.CARD_DISH.equals(type)) {
                        initDishList(adapter, card);
                    } else if (RecommendCard.CARD_FOOD.equals(type)) {
                        initFoodList(adapter, card);
                    } else if (RecommendCard.CARD_COOK.equals(type)) {
                        initCookShowList(adapter, card);
                    } else if (RecommendCard.CARD_PARTY.equals(type)) {
                        initPartyList(adapter, card);
                    } else if (RecommendCard.CARD_TOPIC.equals(type)) {
                        initTopicList(adapter, card);
                    }
                }
            }

        }

    }

//    private void updateViews() {
//        if (mRecommend.hasBanner()) {
//            updateBannerView();
//        }
//
//        if (mRecommend.hasCard()) {
//            List<RecommendCard> cardList = mRecommend.getCardList();
//            for (int i = 0; i < cardList.size(); i++) {
//                RecommendCard card = cardList.get(i);
//                if (card != null) {
//                    String type = card.getType();
//                    if (RecommendCard.CARD_DISH.equals(type)) {
//                        updateDishList();
//                    } else if (RecommendCard.CARD_FOOD.equals(type)) {
//                        updateFoodList();
//                    } else if (RecommendCard.CARD_COOK.equals(type)) {
//                        updateCookShowList();
//                    } else if (RecommendCard.CARD_PARTY.equals(type)) {
//                        updatePartyList();
//                    } else if (RecommendCard.CARD_TOPIC.equals(type)) {
//                        updateTopicList();
//                    }
//                }
//            }
//        }
//
//    }

    /**
     * 初始化Banner栏
     *
     * @param adapter
     */
    private void initBannerView(MergeAdapter adapter) {
        if (mRecommend != null) {
            List<Banner> result = mRecommend.getBannerList();
            if (result != null && !result.isEmpty()) {
                mBannerView = new BannerMultiView(this);
                mBannerView.loadLayout(R.layout.view_multi_banner, result);
                adapter.addView(mBannerView);
            }
        }
    }

//    /**
//     * 更新Banner栏
//     */
//    private void updateBannerView() {
//        UIHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (mRecommend != null) {
//                    List<Banner> result = mRecommend.getBannerList();
//                    if (result != null && !result.isEmpty()) {
//                        if (mBannerView != null) {
//                            mBannerView.setVisibility(View.VISIBLE);
//                            mBannerView.updateView(result);
//                        }
//                    } else {
//                        if (mBannerView != null) {
//                            mBannerView.setVisibility(View.GONE);
//                        }
//                    }
//                }
//            }
//        });
//    }

    /**
     * 初始化买菜列表
     *
     * @param adapter
     * @param card
     */
    private void initDishList(MergeAdapter adapter, RecommendCard card) {
        if (mRecommend != null && card != null) {
            mDishView = new RecommendCardView(this);
            mDishView.loadLayout(card);
            adapter.addView(mDishView);
        }
    }

    /**
     * 更新买菜列表
     */
    private void updateDishList() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRecommend != null) {
                    if (mDishView != null) {
                        mDishView.updateView();
                    }
                }
            }
        });
    }

    /**
     * 初始化菜谱列表
     *
     * @param adapter
     * @param card
     */
    private void initFoodList(MergeAdapter adapter, RecommendCard card) {
        if (mRecommend != null && card != null) {
            mFoodView = new RecommendCardView(this);
            mFoodView.loadLayout(card);
            adapter.addView(mFoodView);
        }
    }

//    /**
//     * 更新菜谱列表
//     */
//    private void updateFoodList() {
//        UIHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (mRecommend != null) {
//                    if (mFoodView != null) {
//                        mFoodView.updateView();
//                    }
//                }
//            }
//        });
//    }

    /**
     * 初始化晒厨艺
     *
     * @param adapter
     * @param card
     */
    private void initCookShowList(MergeAdapter adapter, RecommendCard card) {
        if (mRecommend != null && card != null) {
            mCookShowView = new RecommendCardView(this);
            mCookShowView.loadLayout(card);
            adapter.addView(mCookShowView);
        }
    }

//    /**
//     * 更新晒厨艺列表
//     */
//    private void updateCookShowList() {
//        UIHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (mRecommend != null) {
//                    if (mCookShowView != null) {
//                        mCookShowView.updateView();
//                    }
//                }
//            }
//        });
//
//    }

    /**
     * 初始化活动列表
     *
     * @param adapter
     * @param card
     */
    private void initPartyList(MergeAdapter adapter, RecommendCard card) {
        if (mRecommend != null && card != null) {
            mPartyView = new RecommendCardView(this);
            mPartyView.loadLayout(card);
            adapter.addView(mPartyView);
        }
    }

//    /**
//     * 更新活动列表
//     */
//    private void updatePartyList() {
//
//        UIHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (mRecommend != null) {
//                    if (mPartyView != null) {
//                        mPartyView.updateView();
//                    }
//                }
//            }
//        });
//    }

    /**
     * 初始化文章列表
     *
     * @param adapter
     * @param card
     */
    private void initTopicList(MergeAdapter adapter, RecommendCard card) {
        if (mRecommend != null && card != null) {
            mTopicView = new RecommendCardView(this);
            mTopicView.loadLayout(card);
            adapter.addView(mTopicView);
        }
    }

//    /**
//     * 更新活动列表
//     */
//    private void updateTopicList() {
//        UIHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (mRecommend != null) {
//                    if (mTopicView != null) {
//                        mTopicView.updateView();
//                    }
//                }
//            }
//        });
//    }

    private void updateListSelection() {
        if (mListView != null) {
            mListView.setSelection(0);
        }
    }

    private void endLoading() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                hideLoading();
                getTitleBar().setBottomDivLineColor(getResources().getColor(R.color.uikit_orange));
                if (mPullToRefreshLayout != null) {
                    mPullToRefreshLayout.onRefreshComplete();
                }
            }
        });

    }

    @Override
    public void showLoading() {
        getTitleBar().setBottomDivLineColor(getResources().getColor(R.color.uikit_grey));
        super.showLoading();
    }

    private boolean hasShownAreaServerTip() {
        return (boolean) SharePreferenceProperties.get(AreaServerTipDialog.IS_SHOW, false);
    }

    public List<String> getCardTypes() {
        return mCardTypes;
    }

    public void updateSortCard() {
        saveCardIndex();
        sortCard();
        quickUpdate();
    }


    public List<RecommendCard> getRecommendCards() {
        return mRecommend.getCardList();
    }
}
