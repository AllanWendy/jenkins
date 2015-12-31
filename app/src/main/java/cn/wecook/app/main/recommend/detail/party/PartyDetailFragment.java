package cn.wecook.app.main.recommend.detail.party;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.legacy.TopicApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.Party;
import com.wecook.sdk.api.model.PartyDetail;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.FavoritePolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;
import com.wecook.uikit.widget.LabelView;
import com.wecook.uikit.widget.LineView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.zoom.PullToZoomListView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.CommentListAdapter;
import cn.wecook.app.adapter.PartyInfoAdapter;
import cn.wecook.app.dialog.InputCommentDialog;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 活动详情页面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/18/14
 */
public class PartyDetailFragment extends BaseListFragment {

    public static final String EXTRA_PARTY = "extra_party";
    public static final String EXTRA_JUMP_TO_COMMENT = "extra_jump_to_comment";

    private static final int DEFAULT_ANIMATION_TIME = 500;
    private static final int MAX_ALPHA = 50;

    private int mPartyId;
    private PartyDetail mPartyDetail;
    private Party mParty;
    private boolean mIsUpdatedInfo;
    private boolean mIsUpdatedFavoriteList;
    private boolean mIsUpdateComment;

    private MergeAdapter mAdapter;

    private PullToZoomListView mListView;
    private ImageView mHeadImageView;
    private TextView mHeadTitle;
    private TitleBar mTitleBar;
    private TitleBar.ActionImageView mTitleShare;

    private long mAnimatorDuration;
    private List<ValueAnimator> mAnimators = new ArrayList<ValueAnimator>();
    private PartyMiniMapView mMiniMapView;
    private PartyInfoAdapter mPartyInfoAdapter;
    private PartyMainPageView mPartyMainPageView;
    private PartyLikelyListView mPartyLikelyListView;
    private CommentListAdapter mPartyCommentAdapter;

    private ShapeDrawable mShareBgDrawable;
    private ShapeDrawable mBackBgDrawable;

    private LabelView mCommentLabel;
    private Runnable mUpdateUiRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPartyDetail != null && getContext() != null) {

                if (!mIsUpdatedInfo
                        && !"0".equals(mPartyDetail.getId())) {
                    mIsUpdatedInfo = true;
                    buildInfoDetail();
                }

                if (!mIsUpdateComment
                        && mPartyDetail.getCommentList() != null) {
                    mIsUpdateComment = true;
                    buildComment();
                }

                if (!mIsUpdatedFavoriteList
                        && mPartyDetail.getFavUsers() != null) {
                    mIsUpdatedFavoriteList = true;
                    buildFavoriteList();
                }

                if (mIsUpdateComment
                        && mIsUpdatedInfo
                        && mIsUpdatedFavoriteList) {

                    if (mListView.getAdapter() != null) {
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mListView.setAdapter(mAdapter);
                        createAnimation(mListView.getHeaderHeight() - mTitleBar.getHeight());
                        mListView.setOnListViewZoomListener(new PullToZoomListView.OnListViewZoomListener() {
                            @Override
                            public void onHeaderZoom(float f) {
                                updateZoomAnimator((long) f);
                            }
                        });
                    }

                    mShareBgDrawable.setAlpha(MAX_ALPHA);
                    mBackBgDrawable.setAlpha(MAX_ALPHA);

                    if (mIsJumpToComment) {
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            Object o = mAdapter.getItem(i);
                            if (o == mCommentLabel) {
                                mListView.setSelection(Math.max(i - 1, 1));
                                break;
                            }
                        }
                    }

                    hideLoading();
                }

            }
        }
    };
    private LoadMoreImpl<List<Comment>> mLoadMore;
    private boolean mIsJumpToComment;

    private void buildFavoriteList() {
        //想参与
        mAdapter.addView(mPartyLikelyListView);
        //线
        LineView lineView = new LineView(getContext());
        lineView.initLayout();
        mAdapter.addView(lineView);
    }

    private void buildInfoDetail() {
        // 头图
        ImageFetcher.asInstance().load(mPartyDetail.getImage(), mHeadImageView,
                R.drawable.app_pic_default_party);
        mHeadTitle.setText(mPartyDetail.getTitle());

        // 地图
        mAdapter.addView(mMiniMapView);
        //信息列表
        mAdapter.addAdapter(mPartyInfoAdapter);
        // 信息主页label
        LabelView labelView = new LabelView(getContext());
        labelView.initWithLabelName("活动详情");
        mAdapter.addView(labelView);
        //信息主页
        mAdapter.addView(mPartyMainPageView);
        //线
        LineView lineView = new LineView(getContext());
        lineView.initLayout();
        mAdapter.addView(lineView);
    }

    private void buildComment() {
        if (mPartyDetail.getCommentList().getCountOfList() != 0) {
            // 讨论label
            mCommentLabel = new LabelView(getContext());
            mCommentLabel.initWithLabelName("大家的讨论");
            mAdapter.addView(mCommentLabel);
            // 讨论列表
            mAdapter.addAdapter(mPartyCommentAdapter);

            mLoadMore = new LoadMoreImpl<List<Comment>>();
            mLoadMore.setMoreLoader(new UILoader<List<Comment>>(this) {
                @Override
                public List<Comment> runBackground() {
                    return SyncHandler.sync(this);
                }

                @Override
                public void sync(final Callback<List<Comment>> callback) {
                    super.sync(callback);
                    CommentApi.getCommentList(CommentApi.TYPE_EVENTS, "" + mPartyId, mLoadMore.getCurrentPage(),
                            mLoadMore.getPageSize(), new ApiCallback<ApiModelList<Comment>>() {
                                @Override
                                public void onResult(ApiModelList<Comment> result) {
                                    if (result != null && result.available() && getContext() != null) {
                                        callback.callback(result.getList());
                                    } else {
                                        callback.callback(null);
                                    }
                                }
                            });
                }
            });
            mLoadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener<List<Comment>>() {
                @Override
                public void onLoaded(boolean success, List<Comment> o) {
                    if (success) {
                        if (mPartyCommentAdapter != null) {
                            mPartyCommentAdapter.addEntrys(o);
                        }
                    }
                }
            });
            mLoadMore.setListAdapter(mPartyCommentAdapter, mListView);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mParty = (Party) bundle.getSerializable(EXTRA_PARTY);
            if (mParty != null) {
                mPartyId = StringUtils.parseInt(mParty.getId());
            }
            mIsJumpToComment = bundle.getBoolean(EXTRA_JUMP_TO_COMMENT);
        }

        mAdapter = new MergeAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getArguments() != null) {
            getArguments().clear();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_party_detail, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (PullToZoomListView) view.findViewById(R.id.app_party_detail_list);

        View headView = mListView.getHeaderView();

        mHeadImageView = (ImageView) headView.findViewById(R.id.app_list_head_image);

        mHeadTitle = (TextView) headView.findViewById(R.id.app_list_head_title);
        mHeadTitle.setSingleLine(false);
        mHeadTitle.setMaxLines(2);

        headView.findViewById(R.id.app_list_head_sub_title).setVisibility(View.GONE);

        headView.findViewById(R.id.app_list_head_fav).setVisibility(View.GONE);

        mTitleBar = getTitleBar();
        mTitleShare = new TitleBar.ActionImageView(getContext(), R.drawable.app_bt_share);
        mShareBgDrawable = new ShapeDrawable();
        mShareBgDrawable.setShape(new OvalShape());
        mShareBgDrawable.getPaint().setColor(getResources().getColor(R.color.uikit_black));
        mShareBgDrawable.setAlpha(0);
        mTitleShare.setBackgroundDrawable(mShareBgDrawable);
        mTitleShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThirdPortDelivery.share(getContext(), mPartyDetail);
            }
        });

        mTitleBar.addActionView(mTitleShare);
        mTitleBar.setBackDrawable(R.drawable.uikit_bt_back);
        mTitleBar.enableBottomDiv(false);

        view.findViewById(R.id.app_party_detail_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserProperties.isLogin()) {
                    new InputCommentDialog(getContext(), CommentApi.TYPE_EVENTS, mParty.getId())
                            .setOnSendCommentListener(new InputCommentDialog.OnSendCommentListener() {
                                @Override
                                public void sendResult(Comment comment) {
                                    if (comment != null) {
                                        LogGather.onEventComment(CommentApi.TYPE_EVENTS, mParty, true);
                                        if (mPartyCommentAdapter != null) {
                                            mPartyCommentAdapter.addEntry(0, comment);
                                        }
                                    } else {
                                        LogGather.onEventComment(CommentApi.TYPE_EVENTS, mParty, false);
                                    }
                                }
                            }).show();
                } else {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });

        View fav = view.findViewById(R.id.app_party_detail_like);
        FavoritePolicy.favoriteHelper(fav, FavoriteApi.TYPE_PARTY, mPartyId, mParty,
                new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getContext(), UserLoginActivity.class));
                    }
                }
        );

        View back = mTitleBar.getBackView();
        mBackBgDrawable = new ShapeDrawable();
        mBackBgDrawable.setShape(new OvalShape());
        mBackBgDrawable.getPaint().setColor(getResources().getColor(R.color.uikit_black));
        mBackBgDrawable.setAlpha(0);
        back.setBackgroundDrawable(mBackBgDrawable);

        if (mParty == null || mPartyId == 0) {
            getEmptyView().setCanRefresh(false);
        }
    }

    private void createAnimation(long duration) {
        if (duration < 300) {
            duration = DEFAULT_ANIMATION_TIME;
        }
        mAnimatorDuration = duration;
        TextView titleBarTextView = mTitleBar.getTitleView();
        PropertyValuesHolder alphaToV = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        PropertyValuesHolder alphaToG = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);

        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(mHeadTitle, alphaToG)
                .setDuration(duration));

        ValueAnimator titleVar = ObjectAnimator.ofFloat(0, 1f).setDuration(duration);
        titleVar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int color = getResources().getColor(R.color.uikit_grey);

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float alpha = (Float) animation.getAnimatedValue();
                int a = (int) (alpha * 256);
                int newColor = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
                mTitleBar.setBackgroundColor(newColor);
                int recurseAlpha = MAX_ALPHA - a;
                if (recurseAlpha < 0) {
                    recurseAlpha = 0;
                }
                mBackBgDrawable.setAlpha(recurseAlpha);
                mShareBgDrawable.setAlpha(recurseAlpha);
                if (a > 250) {
                    mTitleBar.setBackDrawable(R.drawable.uikit_bt_back_pressed);
                    mTitleShare.setImageResource(R.drawable.app_bt_share_highlight);
                } else {
                    mTitleBar.setBackDrawable(R.drawable.uikit_bt_back_normal);
                    mTitleShare.setImageResource(R.drawable.app_bt_share);
                }
            }
        });
        mAnimators.add(titleVar);
        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(titleBarTextView, alphaToV)
                .setDuration(duration));
    }

    private void updateZoomAnimator(long f) {
        if (f > mAnimatorDuration) {
            f = mAnimatorDuration - 1;
        }

        if (f >= 0) {
            for (ValueAnimator animator : mAnimators) {
                animator.setCurrentPlayTime(f);
            }
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (mPartyDetail == null) {
            showLoading();
            // 获得详情数据
            TopicApi.getActivityDetail(mPartyId, new ApiCallback<PartyDetail>() {

                @Override
                public void onResult(PartyDetail result) {
                    if (getContext() != null) {
                        mPartyDetail = result;
                        //图片
                        mMiniMapView = new PartyMiniMapView(getContext());
                        mMiniMapView.loadLayout(R.layout.view_mini_map, mPartyDetail);
                        mMiniMapView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString(WebViewFragment.EXTRA_URL, WebViewActivity.ACTIVITY_MAP_URL_PRE
                                        + mPartyDetail.getId());
                                startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                            }
                        });

                        //信息列表
                        mPartyInfoAdapter = new PartyInfoAdapter(PartyDetailFragment.this, mPartyDetail);

                        //信息主页
                        mPartyMainPageView = new PartyMainPageView(getContext());
                        mPartyMainPageView.loadLayout(R.layout.view_main_page, mPartyDetail.getContent());

                        if (StringUtils.isEmpty(mPartyDetail.getId())) {
                            showEmptyView();
                            hideLoading();
                            return;
                        }

                        UIHandler.post(mUpdateUiRunnable);

                        requestFavUser();
                    } else {
                        showEmptyView();
                        hideLoading();
                    }
                }
            });
        }
    }

    private void requestFavUser() {
        if (mPartyDetail.getFavUsers() == null) {
            FavoriteApi.getFavoriteUserList(FavoriteApi.TYPE_PARTY, mPartyId + "", 1, 20,
                    new ApiCallback<ApiModelList<User>>() {
                        @Override
                        public void onResult(ApiModelList<User> result) {
                            if (getContext() != null) {
                                mPartyDetail.setFavUsers(result);

                                //想参与
                                mPartyLikelyListView = new PartyLikelyListView(PartyDetailFragment.this);
                                mPartyLikelyListView.loadLayout(R.layout.view_likely_list, mPartyDetail.getFavUsers());
                                UIHandler.post(mUpdateUiRunnable);

                                requestComment();
                            }
                        }
                    }
            );
        }
    }

    private void requestComment() {
        if (mPartyDetail.getCommentList() == null) {
            // 获得评论数据
            CommentApi.getCommentList(CommentApi.TYPE_EVENTS, "" + mPartyId,
                    1, 10, new ApiCallback<ApiModelList<Comment>>() {
                        @Override
                        public void onResult(ApiModelList<Comment> result) {
                            if (getContext() != null) {
                                mPartyDetail.setCommentList(result);
                                mPartyCommentAdapter = new CommentListAdapter(PartyDetailFragment.this,
                                        result.getList(), CommentApi.TYPE_EVENTS, "" + mPartyId);
                                UIHandler.post(mUpdateUiRunnable);
                            }
                        }
                    }
            );
        }
    }
}
