package com.wecook.sdk.policy;

import android.view.View;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.sdk.api.legacy.YummyApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 评论策略
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/30/14
 */
public class CommentPolicy {

    /**
     * 点赞
     *
     * @param view
     * @param commentId
     * @param comment
     * @param showLoginDialog
     */
    public static void praiseHelper(final TextView view, final String commentId,
                                    final Comment comment, final Runnable showLoginDialog) {

        if (view != null && comment != null) {
            view.setText(comment.getPraiseCount() + "");
            view.setSelected(comment.isPraised());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserProperties.isLogin()) {
                        if (comment.isPraised() == view.isSelected()) {
                            if (comment.isPraised()) {
                                view.setText(comment.decreasePraiseCount() + "");
                            } else {
                                view.setText(comment.increasePraiseCount() + "");
                            }
                            view.setSelected(!comment.isPraised());
                            praiseToggle(commentId, comment.isPraised(), new ApiCallback<State>() {
                                @Override
                                public void onResult(State result) {
                                    if (result != null && result.available()) {
                                        comment.setPraise(!comment.isPraised());
                                    } else {
                                        //还原数据
                                        if (comment.isPraised()) {
                                            view.setText(comment.increasePraiseCount() + "");
                                        } else {
                                            view.setText(comment.decreasePraiseCount() + "");
                                        }
                                        view.setSelected(comment.isPraised());
                                    }
                                }
                            });
                        }
                    } else {
                        if (showLoginDialog != null) {
                            showLoginDialog.run();
                        }
                    }
                }
            });
        }
    }

    /**
     * 点赞开关
     */
    private static void praiseToggle(String commentId, boolean hasPraise, ApiCallback<State> callback) {
        if (hasPraise) {
            YummyApi.removePraise(YummyApi.TYPE_COMMENT, commentId, callback);
        } else {
            YummyApi.addPraise(YummyApi.TYPE_COMMENT, commentId, callback);
        }
    }
}
