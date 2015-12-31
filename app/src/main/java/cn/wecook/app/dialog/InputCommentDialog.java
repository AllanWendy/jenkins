package cn.wecook.app.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.DialogAlarm;
import com.wecook.uikit.alarm.ToastAlarm;

import java.util.HashMap;
import java.util.Map;

import cn.wecook.app.R;

/**
 * 评论书写对话框
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/29/14
 */
public class InputCommentDialog extends DialogAlarm {

    private View mCloseView;
    private View mSendView;

    private TextView mTitle;
    private TextView mContentCount;
    private EditText mEditText;

    /**
     * 为哪个类型评论
     */
    private String mCommentType;
    /**
     * 类型的ID
     */
    private String mCommentId;

    private User mReplayTo;

    private boolean isSending;
    private OnSendCommentListener mListener;
    private static Map<String, String> mContentMap = new HashMap<String, String>();

    private String mContent;
    private String mContentKey;

    private LoadingDialog mLoadingDialog;

    public InputCommentDialog(Context context, String type, String id) {
        this(context, null, type, id);
    }

    public InputCommentDialog(Context context, User replayTo, String type, String id, boolean noAnim) {
        super(context, R.style.UIKit_Dialog_NO_ANIMATION);
        mCommentType = type;
        mCommentId = id;
        mReplayTo = replayTo;

        mContentKey = mCommentType + ":" + mCommentId;
        mContent = mContentMap.get(mContentKey);

        mLoadingDialog = new LoadingDialog(context);
    }

    public InputCommentDialog(Context context, User replayTo, String type, String id) {
        super(context, R.style.UIKit_Dialog_Fixed);
        mCommentType = type;
        mCommentId = id;
        mReplayTo = replayTo;

        mContentKey = mCommentType + ":" + mCommentId;
        mContent = mContentMap.get(mContentKey);

        mLoadingDialog = new LoadingDialog(context);
    }

    @Override
    public View getView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.dialog_comment_input, parent, true);
    }

    @Override
    public void onViewCreated(View view) {
        mEditText = (EditText) view.findViewById(R.id.app_comment_edit);
        mContentCount = (TextView) view.findViewById(R.id.app_comment_content_count);
        mTitle = (TextView) view.findViewById(R.id.app_comment_title);
        mCloseView = view.findViewById(R.id.app_comment_close);

        if (mReplayTo != null) {
            mEditText.setHint("@" + mReplayTo.getNickname() + ": ");
            mTitle.setText(R.string.app_title_replay_comment);
        }

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = (int) StringUtils.chineseLength(s.toString());
                if (length > 120) {
                    mContentCount.setVisibility(View.VISIBLE);
                    mContentCount.setText(length + "");
                } else {
                    mContentCount.setVisibility(View.GONE);
                }

                mContentMap.put(mContentKey, s.toString());
            }
        });

        if (!StringUtils.isEmpty(mContent)) {
            mEditText.setText(mContent);
            mEditText.setSelection(mContent.length());
        }
        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mSendView = view.findViewById(R.id.app_comment_send);
        mSendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSendComment();
            }
        });
    }

    @Override
    protected void onDialogShowed() {
        super.onDialogShowed();
        KeyboardUtils.openKeyboard(getContext(), mEditText);
    }

    /**
     * 请求发送评论
     */
    private void requestSendComment() {
        if (checkCommentValidate()) {
            mLoadingDialog.show();
            isSending = true;
            CommentApi.createComment(UserProperties.getUserId(), mCommentType, mCommentId,
                    mReplayTo != null ? mReplayTo.getUid() : "",
                    mEditText.getText().toString(), new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            Comment comment = null;
                            if (result != null && result.available()) {
                                ToastAlarm.show(R.string.app_tip_comment_success);
                                comment = new Comment();
                                comment.setId("0");
                                comment.setContent(mEditText.getText().toString());
                                comment.setCreateTime(System.currentTimeMillis() + "");
                                comment.setAuthor(UserProperties.getUser());
                                comment.setReplyto(mReplayTo);
                            }
                            mContentMap.remove(mContentKey);
                            if (mListener != null) {
                                mListener.sendResult(comment);
                            }
                            isSending = false;
                            mLoadingDialog.dismiss();
                            dismiss();
                        }
                    }
            );
        }
    }

    /**
     * 检查评论内容是否合规
     *
     * @return
     */
    private boolean checkCommentValidate() {
        if (isSending) {
            ToastAlarm.show(R.string.app_error_comment_sending);
            return false;
        }

        if (!NetworkState.available()) {
            return false;
        }

        String commentContent = mEditText.getText().toString();

        if (StringUtils.isEmpty(commentContent.trim())) {
            ToastAlarm.show(R.string.app_error_comment_empty);
            mEditText.setText("");
            return false;
        }

        if (StringUtils.chineseLength(commentContent) > 120) {
            ToastAlarm.show(R.string.app_error_comment_too_long);
            return false;
        }


        return true;
    }


    /**
     * 设置监听
     *
     * @param listener
     * @return
     */
    public InputCommentDialog setOnSendCommentListener(OnSendCommentListener listener) {
        mListener = listener;
        return this;
    }

    public interface OnSendCommentListener {
        /**
         * 发送结果
         *
         * @param comment
         */
        void sendResult(Comment comment);
    }
}
