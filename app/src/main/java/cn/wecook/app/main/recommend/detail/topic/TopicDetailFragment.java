package cn.wecook.app.main.recommend.detail.topic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.model.Topic;
import com.wecook.sdk.policy.FavoritePolicy;

import cn.wecook.app.R;
import cn.wecook.app.features.comment.CommentListFragment;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 专题详情界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/18/14
 */
public class TopicDetailFragment extends WebViewFragment {

    public static final String EXTRA_DATA = "topic_data";

    private Topic mTopic;
    private String mTopicId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTopic = (Topic) bundle.getSerializable(EXTRA_DATA);
            if (mTopic != null) {
                mTopicId = mTopic.getId();
            }
        }
    }

    @Override
    public View onCreateOperatorView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.view_topic_detail_oprator, parent, true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView fav = (TextView) view.findViewById(R.id.app_topic_detail_fav);
        TextView comment = (TextView) view.findViewById(R.id.app_topic_detail_comment);
        TextView share = (TextView) view.findViewById(R.id.app_topic_detail_share);

        FavoritePolicy.favoriteHelper(fav, FavoriteApi.TYPE_TOPIC,
                StringUtils.parseInt(mTopicId), mTopic, new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getContext(), UserLoginActivity.class));
                    }
                }
        );

        if (mTopic != null && mTopic.getCommentCount() != 0) {
            comment.setText("评论(" + mTopic.getCommentCount() + ")");
        } else {
            comment.setText("评论");
        }

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString(CommentListFragment.EXTRA_TITLE, "全部评论");
                bundle.putString(CommentListFragment.EXTRA_REQUEST_ID, mTopicId);
                bundle.putString(CommentListFragment.EXTRA_TYPE, CommentApi.TYPE_TOPIC);
                next(CommentListFragment.class, bundle);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThirdPortDelivery.share(getContext(), mTopic);
            }
        });
    }

}
