package cn.wecook.app.main.dish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.CommentCount;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.main.home.user.UserPageFragment;

/**
 * 菜品评价
 * Created by simon on 15/9/18.
 */
public class DishCommentFragment extends BaseListFragment {
    public static final String EXTRA_DISH_ID = DishDetailFragment.EXTRA_DISH_ID;


    private static final int COMMENT_TYPE_ALL = 0;
    private static final int COMMENT_TYPE_GOOD = 1;
    private static final int COMMENT_TYPE_MIDDLE = 2;
    private static final int COMMENT_TYPE_BAD = 3;


    private TitleBar titleBar;
    private CommentCount mCommentCount;
    private View mDishEvaluateInfoView;
    private List<Comment> mComments = new ArrayList<Comment>();
    private DishEvaluateAdapter mDishEvaluateAdapter;//评论adapter
    private int mCurrentCommentType;
    private String dishId;
    private MergeAdapter mListAdapter;
    private ListView mListView;
    private PullToRefreshListView mRefreshListLayout;
    private int page = 1;
    private int pageSize = 20;
    private LoadMore mLoadMore;
    private LoadingDialog dialog;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle arguments = getArguments();
        if (arguments != null) {
            dishId = arguments.getString(EXTRA_DISH_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_dish_comment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new LoadingDialog(getContext());
        dialog.cancelable(false);

        titleBar = getTitleBar();
        titleBar.setTitle("菜品评价");
        titleBar.enableBack(true);
        mRefreshListLayout = (PullToRefreshListView) view.findViewById(R.id.app_dish_detail_comment_list);
        mRefreshListLayout.setMode(PullToRefreshBase.Mode.BOTH);
        mListView = mRefreshListLayout.getRefreshableView();
        mDishEvaluateInfoView = view.findViewById(R.id.app_dish_detail_comment_tabs);
        updateViews();
        mRefreshListLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                UIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshListLayout.onRefreshComplete();
                    }
                }, 300);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                boolean result = performLoadMore();
                if (!result) {
                    mRefreshListLayout.onRefreshComplete();
                }
            }
        });

        setListAdapter(mListView, mDishEvaluateAdapter);
        mLoadMore = getLoadMore();
        mLoadMore.setCurrentPage(page);
        mLoadMore.setAutoLoadCount(Integer.MAX_VALUE);
    }

    @Override
    protected void onUpdateListMore(Object data, boolean hasMore) {
        super.onUpdateListMore(data, hasMore);
        if (mDishEvaluateAdapter != null) {
            mDishEvaluateAdapter.notifyDataSetChanged();
        }
        mRefreshListLayout.onRefreshComplete();
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {

        List list = (List) SyncHandler.sync(new SyncHandler.Sync() {
            private boolean wait;
            private Object object;

            @Override
            public void syncStart() {
                ApiCallback<ApiModelList<Comment>> apiCallback = new ApiCallback<ApiModelList<Comment>>() {
                    @Override
                    public void onResult(ApiModelList<Comment> result) {
                        if (result.available()) {
                            object = result.getList();
                        }
                        wait = false;
                    }
                };
                CommentApi.getCommentListOfDish(dishId, mCurrentCommentType, currentPage, pageSize, apiCallback);
                wait = true;
            }

            @Override
            public boolean waiting() {
                return wait;
            }

            @Override
            public Object syncEnd() {
                return object;
            }
        });

        return list;
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        requestCommentList(page, pageSize);

    }


    private void requestCommentList(int currentPage, int pageSize) {
        showLoadingDialog();
        CommentApi.getCommentDishOverview(dishId,
                new ApiCallback<CommentCount>() {
                    @Override
                    public void onResult(CommentCount result) {
                        if (result.available()) {
                            mCommentCount = result;
                            updateTabViews();
                        }
                    }
                });
        CommentApi.getCommentListOfDish(dishId, mCurrentCommentType, currentPage, pageSize,
                new ApiCallback<ApiModelList<Comment>>() {
                    @Override
                    public void onResult(ApiModelList<Comment> result) {
                        mComments.clear();
                        if (result.available()) {
                            mComments.addAll(result.getList());
                        }
                        if (mDishEvaluateAdapter != null) {
                            mDishEvaluateAdapter.notifyDataSetChanged();
                        }
                        hideLoadingDialog();
                    }
                });
    }

    public void showLoadingDialog() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void hideLoadingDialog() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 更新UI
     */
    private void updateViews() {
        if (mComments != null) {
            if (mListAdapter != null) {
                mListAdapter = null;
            }
            mListAdapter = new MergeAdapter();
            addComments();
            mListView.setAdapter(mListAdapter);
        }
    }


    /**
     * 添加评论数据
     */
    private void addComments() {
        if (mDishEvaluateAdapter == null) {
            mDishEvaluateAdapter = new DishEvaluateAdapter(getContext(), mComments);
            mListAdapter.addAdapter(mDishEvaluateAdapter);
        }
    }

    /**
     * 更新数据顶部tab数据
     */
    private void updateTabViews() {
        if (mCommentCount != null) {
            final TextView all = (TextView) mDishEvaluateInfoView.findViewById(R.id.app_restaurant_comment_all);
            final TextView good = (TextView) mDishEvaluateInfoView.findViewById(R.id.app_restaurant_comment_good);
            final TextView middle = (TextView) mDishEvaluateInfoView.findViewById(R.id.app_restaurant_comment_middle);
            final TextView bad = (TextView) mDishEvaluateInfoView.findViewById(R.id.app_restaurant_comment_bad);

            all.setText("全部 " + mCommentCount.getAll());
            good.setText("好评 " + mCommentCount.getGood());
            middle.setText("中评 " + mCommentCount.getMiddle());
            bad.setText("差评 " + mCommentCount.getBad());

            updateCommentTypeSelect(all, good, middle, bad);

            all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentCommentType = COMMENT_TYPE_ALL;
                    updateCommentTypeSelect(all, good, middle, bad);
                    resetData();
                    onStartUILoad();
                }
            });
            good.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentCommentType = COMMENT_TYPE_GOOD;
                    updateCommentTypeSelect(all, good, middle, bad);
                    resetData();
                    onStartUILoad();
                }
            });
            middle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentCommentType = COMMENT_TYPE_MIDDLE;
                    updateCommentTypeSelect(all, good, middle, bad);
                    resetData();
                    onStartUILoad();
                }
            });
            bad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentCommentType = COMMENT_TYPE_BAD;
                    updateCommentTypeSelect(all, good, middle, bad);
                    resetData();
                    onStartUILoad();
                }
            });
        }
    }

    /**
     * 还原数据
     */
    private void resetData() {
        if (mLoadMore != null) {
            mLoadMore.setCurrentPage(page);
        }
        mComments.clear();
        if (mDishEvaluateAdapter != null) {
            mDishEvaluateAdapter.notifyDataSetChanged();
        }
    }

    private void updateCommentTypeSelect(TextView all, TextView good, TextView middle, TextView bad) {
        all.setSelected(mCurrentCommentType == COMMENT_TYPE_ALL);
        good.setSelected(mCurrentCommentType == COMMENT_TYPE_GOOD);
        middle.setSelected(mCurrentCommentType == COMMENT_TYPE_MIDDLE);
        bad.setSelected(mCurrentCommentType == COMMENT_TYPE_BAD);
    }

    /**
     * 评论
     */
    private class DishEvaluateAdapter extends UIAdapter<Comment> {

        public DishEvaluateAdapter(Context context, List<Comment> data) {
            super(context, R.layout.listview_item_comment_dish, data);
        }

        @Override
        public void updateView(int position, int viewType, Comment data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            ImageView userAvatar = (ImageView) findViewById(R.id.app_comment_avatar);
            TextView userName = (TextView) findViewById(R.id.app_comment_name);
            TextView content = (TextView) findViewById(R.id.app_comment_content);
            TextView time = (TextView) findViewById(R.id.app_comment_time);
            RatingBar star = (RatingBar) findViewById(R.id.app_comment_star);

            if (data.getAuthor() != null) {
                final User author = data.getAuthor();
                userAvatar.setVisibility(View.VISIBLE);
                ImageFetcher.asInstance().load(author.getAvatar(), userAvatar, R.drawable.app_pic_default_avatar);
                userName.setText(author.getNickname());
                userAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(UserPageFragment.EXTRA_USER, author);
                        next(UserPageFragment.class, bundle);
                    }
                });
            } else {
                userAvatar.setVisibility(View.GONE);
            }

            content.setText(data.getContent());
            time.setText(data.getCreateTime());
            star.setRating(StringUtils.parseFloat(data.getScore()));
        }
    }

}
