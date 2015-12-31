package cn.wecook.app.features.comment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.EmptyView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.CommentListAdapter;
import cn.wecook.app.dialog.InputCommentDialog;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 评论列表
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class CommentListFragment extends BaseListFragment {

    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_REQUEST_ID = "extra_request_id";

    private List<Comment> mComments;

    private String mType;
    private String mRequestId;
    private CommentListAdapter mAdapter;
    private InputCommentDialog mInputCommentDialog;
    private EmptyView mEmptyView;
    private Runnable mUpdateList = new Runnable() {
        @Override
        public void run() {
            if (mComments.isEmpty()) {
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setTitle(getString(R.string.app_empty_title_comment));
                mEmptyView.setSecondTitle(getString(R.string.app_empty_second_title_comment));
                mEmptyView.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_comment);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }
            hideLoading();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mType = bundle.getString(EXTRA_TYPE);
            mRequestId = bundle.getString(EXTRA_REQUEST_ID);
        }
        mComments = new ArrayList<Comment>();
        mAdapter = new CommentListAdapter(this, mComments, mType, mRequestId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = (EmptyView) view.findViewById(R.id.app_comment_empty_view);
        if (StringUtils.isEmpty(mRequestId) && StringUtils.isEmpty(mType)) {
            mEmptyView.setCanRefresh(false);
        }
        mEmptyView.setRefreshListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartUILoad();
            }
        });

        setListAdapter(getListView(), mAdapter);

        view.findViewById(R.id.app_comment_input).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserProperties.isLogin()) {
                    mInputCommentDialog = new InputCommentDialog(getContext(), mType, mRequestId);
                    mInputCommentDialog.setOnSendCommentListener(new InputCommentDialog.OnSendCommentListener() {
                        @Override
                        public void sendResult(Comment comment) {
                            if (comment != null) {
                                mComments.add(0, comment);
                                UIHandler.post(mUpdateList);
                            }
                            LogGather.onEventComment(mType, mRequestId, comment != null);
                        }
                    });
                    mInputCommentDialog.show();
                } else {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                }

            }
        });
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        if (!StringUtils.isEmpty(mRequestId)) {
            showLoading();
            CommentApi.getCommentList(mType, mRequestId, 1, getLoadMore().getPageSize(),
                    new ApiCallback<ApiModelList<Comment>>() {
                        @Override
                        public void onResult(ApiModelList<Comment> result) {
                            mComments.clear();
                            mComments.addAll(result.getList());
                            UIHandler.post(mUpdateList);
                        }
                    }
            );
        }
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, int currentPage, int pageSize) {

        List list = (List) SyncHandler.sync(new SyncHandler.Sync() {
            private boolean wait;
            private Object object;

            @Override
            public void syncStart() {
                ApiCallback<ApiModelList<Comment>> callback = new ApiCallback<ApiModelList<Comment>>() {
                    @Override
                    public void onResult(ApiModelList<Comment> result) {
                        object = result.getList();
                        wait = false;
                    }
                };

                CommentApi.getCommentList(mType, mRequestId, getLoadMore().getCurrentPage(),
                        getLoadMore().getPageSize(), callback);

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
}
