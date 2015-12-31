package cn.wecook.app.main.recommend.detail.party;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.model.User;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;
import com.wecook.uikit.widget.shape.CircleImageView;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserPageFragment;

/**
 * 活动参与列表视图
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/20/14
 */
public class PartyLikelyListView extends BaseView{

    private ViewGroup mUserHeadGroup;
    private TextView mUserDesc;
    private BaseFragment mFragment;

    public PartyLikelyListView(BaseFragment fragment) {
        super(fragment.getContext());
        mFragment = fragment;
    }

    public PartyLikelyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PartyLikelyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mUserHeadGroup = (ViewGroup) findViewById(R.id.app_likely_user_group);
        mUserDesc = (TextView) findViewById(R.id.app_likely_desc);

    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);
        if (obj != null && obj instanceof ApiModelList) {
            List<User> list = ((ApiModelList) obj).getList();
            mUserDesc.setText(getResources().getString(R.string.app_want_join_party,
                    ((ApiModelList) obj).getCountOfServer()));

            for (int i = 0; i < (list.size() < 10 ? list.size() : 10); i++) {
                final User user = list.get(i);
                CircleImageView userImage = new CircleImageView(getContext());
                ScreenUtils.resizeView(userImage, ScreenUtils.dip2px(getContext(), 43), 1f);
                ImageFetcher.asInstance().load(user.getAvatar(), userImage, -1);
                userImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(UserPageFragment.EXTRA_USER, user);
                        mFragment.next(UserPageFragment.class, bundle);
                    }
                });
                mUserHeadGroup.addView(userImage);
                ScreenUtils.rePadding(userImage, 5);
                ScreenUtils.reMargin(userImage, 8, 0, 8, 0, true);
            }
        }
    }
}
