package cn.wecook.app.main.recommend.detail.cookshow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SimpleSync;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.legacy.ReportApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.CommentListAdapter;
import cn.wecook.app.dialog.ActionDialog;
import cn.wecook.app.dialog.InputCommentDialog;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.recommend.CookShowPublishFragment;
import cn.wecook.app.main.recommend.list.food.FoodListSearchFragment;

/**
 * 晒厨艺详情
 *
 * @author kevin
 * @since 2014/12/31.
 */
public class CookShowDetailFragment extends BaseTitleFragment {

    public static final String EXTRA_COOK_SHOW = "extra_cook_show";
    public static final String EXTRA_COOK_SHOW_ID = "extra_cook_show_id";
    public static final String EXTRA_SHOW_COMMENT = "extra_show_comment";
    public static final String EXTRA_JUMP_TO_COMMENT = "extra_jump_to_comment";
    private CookShow mCookShow;
    private String mCookShowId;

    private CommentListAdapter mCommentAdapter;
    private CookShowScoreView mHeadView;
    private MergeAdapter mMergeAdapter;
    private List<Comment> mCommentList;

    private ListView mListView;
    private PullToRefreshListView mRefreshListLayout;
    private View mRelativeRecipeView;
    private View mCommentLabelView;
    private View mCommentInput;

    private EmptyView mEmptyView;
    private boolean mIsShowComment;
    private boolean mIsJumpToComment;
    private LoadMore<List<Comment>> mLoadMore;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCookShowId = bundle.getString(EXTRA_COOK_SHOW_ID);
            mCookShow = (CookShow) bundle.getSerializable(EXTRA_COOK_SHOW);
            if (mCookShow != null) {
                mCookShowId = mCookShow.getId();
            }
            mIsShowComment = bundle.getBoolean(EXTRA_SHOW_COMMENT);
            mIsJumpToComment = bundle.getBoolean(EXTRA_JUMP_TO_COMMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cook_show_detail, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        if (mCookShow != null) {
            titleBar.setTitle(mCookShow.getTitle());
        }
        TitleBar.ActionTextView moreView = new TitleBar.ActionTextView(getContext(), "更多");
        moreView.setTextColor(getResources().getColor(R.color.uikit_font_orange));
        moreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActionDialog();
            }
        });
        titleBar.addActionView(moreView);

        mRefreshListLayout = (PullToRefreshListView) view.findViewById(R.id.app_cook_show_listview);
        mEmptyView = (EmptyView) view.findViewById(R.id.app_cook_show_empty);
        mEmptyView.setTitle("无内容");
        mEmptyView.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_cookshow);
        mEmptyView.setRefreshListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartUILoad();
            }
        });
        if (StringUtils.isEmpty(mCookShowId)) {
            mEmptyView.setCanRefresh(false);
        }

        mListView = mRefreshListLayout.getRefreshableView();

        mMergeAdapter = new MergeAdapter();

        mHeadView = (CookShowScoreView) LayoutInflater.from(getContext()).inflate(R.layout.listview_item_cookshow_style_detail, null);
        mHeadView.updateFromDetail();
        mHeadView.whereFrom(CookShowScoreView.FROM_DETAIL);
        mRelativeRecipeView = LayoutInflater.from(getContext()).inflate(R.layout.view_cook_show_recipe_link, null);
        mRelativeRecipeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_RELATION);
                Bundle bundle = new Bundle();
                bundle.putString(FoodListSearchFragment.EXTRA_KEYWORD, mCookShow.getTitle());
                next(FoodListSearchFragment.class, bundle);
            }
        });

        mCommentLabelView = LayoutInflater.from(getContext()).inflate(R.layout.view_cook_show_comment_label, null);

        mCommentList = new ArrayList<Comment>();
        mCommentAdapter = new CommentListAdapter(this, mCommentList, CommentApi.TYPE_COOKING, mCookShowId);
        mCommentInput = view.findViewById(R.id.app_cook_show_comment);
        mCommentInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserProperties.isLogin()) {
                    showCommentInput();
                } else {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });

        mMergeAdapter.addView(mHeadView);
        mMergeAdapter.addView(mRelativeRecipeView);
        mMergeAdapter.addView(mCommentLabelView);
        mMergeAdapter.addAdapter(mCommentAdapter);

        mListView.setAdapter(mMergeAdapter);

        mLoadMore = new LoadMoreImpl<List<Comment>>();
        UILoader<List<Comment>> loader = new UILoader<List<Comment>>(this) {
            @Override
            public List<Comment> runBackground() {
                return syncRequestCommentList(mCookShow, mLoadMore.getCurrentPage(),
                        mLoadMore.getPageSize());
            }
        };
        loader.setRepeatCalled(true);
        mLoadMore.setMoreLoader(loader);
        mLoadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener<List<Comment>>() {
            @Override
            public void onLoaded(boolean success, List<Comment> o) {
                if (success) {
                    if (mCommentAdapter != null) {
                        mCommentAdapter.addEntrys(o);
                    }
                }
            }
        });
        mLoadMore.setListAdapter(mCommentAdapter, mListView);
    }

    private void showCommentInput() {
        InputCommentDialog commentDialog = new InputCommentDialog(getContext(), CommentApi.TYPE_COOKING, mCookShow.getId());
        commentDialog.setOnSendCommentListener(new InputCommentDialog.OnSendCommentListener() {
            @Override
            public void sendResult(Comment comment) {
                if (comment != null) {
                    mCommentLabelView.setVisibility(View.VISIBLE);
                    mCommentAdapter.addEntry(0, comment);
                }

                LogGather.onEventComment(CommentApi.TYPE_COOKING, mCookShow, comment != null);
            }
        });
        commentDialog.show();
    }

    /**
     * 显示更多的操作
     */
    private void showActionDialog() {
        if (mCookShow == null) {
            return;
        }

        final ActionDialog dialog = new ActionDialog(getContext());
        boolean mIsSelfCookShow = false;
        User user = mCookShow.getUser();
        if (user != null
                && UserProperties.isLogin()
                && user.getUid().equals(UserProperties.getUserId())) {
            mIsSelfCookShow = true;
        }

        if (mIsSelfCookShow) {
            dialog.addAction(R.string.app_cook_show_detail_edit_delete, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CookShowApi.deleteCookShow(mCookShow.getId(), new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            if (result.available()) {
                                MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_DELETE);
                                ToastAlarm.show(R.string.app_tip_delete_success);
                                finishFragment();
                            }
                        }
                    });
                    dialog.dismiss();
                }
            });
            dialog.addAction(R.string.app_cook_show_detail_edit_reset, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_EDIT);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(CookShowPublishFragment.EXTRA_PUBLISH_COOK_SHOW, mCookShow);
                    next(CookShowPublishFragment.class, bundle);
                }
            });
        } else {
            dialog.addAction(R.string.app_cook_show_detail_edit_report, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserProperties.isLogin()) {
                        ReportApi.reportIssue(ReportApi.TYPE_COOKING, mCookShow.getId(), new ApiCallback<State>() {
                            @Override
                            public void onResult(State result) {
                                if (result.available()) {
                                    MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_REPORT);
                                    ToastAlarm.show(R.string.app_tip_report_success);
                                } else {
                                    ToastAlarm.show(result.getErrorMsg());
                                }
                            }
                        });
                    } else {
                        startActivity(new Intent(getContext(), UserLoginActivity.class));
                    }

                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (!StringUtils.isEmpty(mCookShowId)) {
            showLoading();
            CookShowApi.getCookShowDetailInfo(mCookShowId, new ApiCallback<CookShow>() {
                @Override
                public void onResult(CookShow result) {
                    if (result.available()) {
                        mCookShow = result;
                        mHeadView.updateView(CookShowDetailFragment.this, mCookShow);
                        TitleBar titleBar = getTitleBar();
                        if (mCookShow != null) {
                            titleBar.setTitle(mCookShow.getTitle());
                        }
                        requestCommentList(mCookShow, mLoadMore.getCurrentPage(),
                                mLoadMore.getPageSize(), new ApiCallback<ApiModelList<Comment>>() {
                                    @Override
                                    public void onResult(ApiModelList<Comment> result) {
                                        if (result != null && result.available() && getContext() != null) {
                                            mCommentList.clear();
                                            mCommentList.addAll(result.getList());
                                            mCommentAdapter.notifyDataSetChanged();
                                        }

                                        UIHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mCommentList.isEmpty()) {
                                                    mCommentLabelView.setVisibility(View.GONE);
                                                } else {
                                                    mCommentLabelView.setVisibility(View.VISIBLE);
                                                    if (mIsJumpToComment) {
                                                        for (int i = 0; i < mMergeAdapter.getCount(); i++) {
                                                            Object o = mMergeAdapter.getItem(i);
                                                            if (o == mCommentLabelView) {
                                                                mListView.setSelection(Math.max(i, 0)
                                                                        + mListView.getHeaderViewsCount());
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                                hideLoading();
                                            }
                                        });

                                    }
                                });
                    } else {
                        showEmpty();
                    }
                }
            });
        } else {
            //显示空界面
            showEmpty();
        }

        if (mIsShowComment) {
            mIsShowComment = false;
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showCommentInput();
                }
            }, 300);
        }
    }

    /**
     * 显示空界面
     */
    private void showEmpty() {
        hideLoading();
        mEmptyView.setVisibility(View.VISIBLE);
    }

    /**
     * 请求评论列表
     *
     * @param resource
     */
    private void requestCommentList(final CookShow resource, int page, int pageSize, ApiCallback<ApiModelList<Comment>> callback) {
        CommentApi.getCommentList(CommentApi.TYPE_COOKING, resource.getId(), page, pageSize, callback);
    }

    /**
     * 同步请求
     *
     * @param resource
     * @param page
     * @param pageSize
     * @return
     */
    private List<Comment> syncRequestCommentList(final CookShow resource, final int page, final int pageSize) {
        return SyncHandler.sync(new SimpleSync<List<Comment>>() {
            @Override
            public void sync(final Callback<List<Comment>> callback) {
                requestCommentList(resource, page, pageSize, new ApiCallback<ApiModelList<Comment>>() {
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

    }

}
