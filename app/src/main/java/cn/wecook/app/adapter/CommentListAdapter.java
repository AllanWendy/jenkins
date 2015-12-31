package cn.wecook.app.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.debug.DebugCenter;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.legacy.ReportApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.CommentPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.InputCommentDialog;
import cn.wecook.app.dialog.ListActionDialog;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.home.user.UserPageFragment;

/**
 * 评论列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class CommentListAdapter extends UIAdapter<Comment> {

    private BaseFragment mFragment;
    private String mCommentType;
    private String mForeignId;

    public CommentListAdapter(BaseFragment fragment, List<Comment> data, String type, String id) {
        super(fragment.getContext(), data);
        mFragment = fragment;
        mCommentType = type;
        mForeignId = id;
    }

    @Override
    protected View newView(int viewType) {
        return LayoutInflater.from(getContext()).inflate(R.layout.listview_item_comment, null);
    }

    @Override
    public void updateView(int position, int viewType, final Comment data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        ImageView userAvatar = (ImageView) findViewById(R.id.app_comment_avatar);
        TextView userName = (TextView) findViewById(R.id.app_comment_name);
        TextView yummyCount = (TextView) findViewById(R.id.app_comment_yummy_count);
        TextView content = (TextView) findViewById(R.id.app_comment_content);
        TextView time = (TextView) findViewById(R.id.app_comment_time);

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
                    mFragment.next(UserPageFragment.class, bundle);
                }
            });
        } else {
            userAvatar.setVisibility(View.GONE);
        }

        Spannable spannable = new SpannableString(data.getContent());
        if (data.getReplyto() != null) {
            spannable = new SpannableString("@" + data.getReplyto().getNickname() + ": " + data.getContent());
        }

        StringUtils.addForegroundColor(getContext(), spannable, R.color.uikit_orange, '@', ' ');
        StringUtils.addForegroundColor(getContext(), spannable, R.color.uikit_font_blue, '#', '#', ' ');
        content.setText(spannable);

        time.setText(StringUtils.formatTimeWithNearBy(Long.parseLong(data.getCreateTime()), "MM-dd"));

        CommentPolicy.praiseHelper(yummyCount, data.getId(), data,
                new Runnable() {
                    @Override
                    public void run() {
                        getContext().startActivity(new Intent(getContext(), UserLoginActivity.class));
                    }
                }
        );

        getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputComment(data.getAuthor(), mCommentType, mForeignId);
            }
        });

        getItemView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final User author = data.getAuthor();
                if (author.getUid().equals(UserProperties.getUserId()) && DebugCenter.isDebugable()) {
                    new ListActionDialog(getContext(), new String[]{"删除"}, new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                //删除
                                CommentApi.deleteComment(data.getId(), new ApiCallback<State>() {
                                    @Override
                                    public void onResult(State result) {
                                        ToastAlarm.show("删除成功");
                                        removeEntry(data);
                                    }
                                });
                            }
                        }
                    }).show();
                } else {
                    new ListActionDialog(getContext(), new String[]{"举报"}, new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                //举报
                                if (UserProperties.isLogin()) {
                                    ReportApi.reportIssue(ReportApi.TYPE_COMMENT, data.getId(), new ApiCallback<State>() {
                                        @Override
                                        public void onResult(State result) {
                                            if (result.available()) {
                                                ToastAlarm.show(R.string.app_tip_report_success);
                                            } else {
                                                ToastAlarm.show(result.getErrorMsg());
                                            }
                                        }
                                    });
                                } else {
                                    getContext().startActivity(new Intent(getContext(), UserLoginActivity.class));
                                }
                            }
                        }
                    }).show();
                }
                return false;
            }
        });
    }

    public void showInputComment(final User user, final String type, final String id) {

        if (UserProperties.isLogin()) {
            if (UserProperties.getUserId().equals(user.getUid())) {
                //不能回复自己
                return;
            }

            new InputCommentDialog(getContext(), user, type, id)
                    .setOnSendCommentListener(new InputCommentDialog.OnSendCommentListener() {
                        @Override
                        public void sendResult(Comment comment) {
                            if (comment != null) {
                                addEntry(0, comment);
                            }
                            LogGather.onEventComment(type, id, comment != null);
                        }
                    }).show();
        } else {
            getContext().startActivity(new Intent(getContext(), UserLoginActivity.class));
        }
    }
}
