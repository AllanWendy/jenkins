package cn.wecook.app.main.recommend.detail.food;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.view.BaseView;

import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserPageFragment;

/**
 * 已晒的人列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/15/14
 */
public class FoodCookShareView extends BaseView {

    private ViewGroup mListView;
    private CookShareItemAdapter mAdapter;
    private List<CookShow> cookShowList;

    private BaseFragment mFragment;
    public FoodCookShareView(BaseFragment fragment) {
        super(fragment.getContext());
        mFragment = fragment;
    }

    public FoodCookShareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FoodCookShareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mListView = (ViewGroup) findViewById(R.id.app_food_detail_cook_share_list);
    }

    @Override
    public void updateView(Object obj) {
        super.updateView(obj);

        if (obj != null && obj instanceof List) {
            cookShowList = (List<CookShow>) obj;
            mAdapter = new CookShareItemAdapter(getContext(), cookShowList);
            mAdapter.notifyDataSetChanged();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                View child = mAdapter.getView(i, null, null);
                mListView.addView(child);
            }
        }
    }

    public void addCookShow(CookShow cookShow) {
        if (cookShow != null) {
            mAdapter.addEntry(0, cookShow);
            mListView.removeAllViews();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                View child = mAdapter.getView(i, null, null);
                mListView.addView(child);
            }
        }
    }

    private class CookShareItemAdapter extends UIAdapter<CookShow> {

        public CookShareItemAdapter(Context context, List<CookShow> data) {
            super(context, data);
        }

        @Override
        protected View newView(int viewType) {
            return LayoutInflater.from(getContext()).inflate(R.layout.listview_item_user, null);
        }

        @Override
        public void updateView(int position, int viewType, final CookShow data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            ImageView foodImage = (ImageView) findViewById(R.id.app_cook_share_food_image);
            ImageView userImage = (ImageView) findViewById(R.id.app_cook_share_user_avatar);
            TextView userName = (TextView) findViewById(R.id.app_cook_share_user_name);

            if (data.getUser() != null) {
                userName.setText(data.getUser().getNickname());
                ImageFetcher.asInstance().load(data.getUser().getAvatar(), userImage, R.drawable.app_pic_default_avatar);
                userImage.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(UserPageFragment.EXTRA_USER, data.getUser());
                        mFragment.next(UserPageFragment.class, bundle);
                    }
                });
            } else {
                userImage.setOnClickListener(null);
            }
            ImageFetcher.asInstance().load(data.getImage(), foodImage, R.color.uikit_grey);
        }
    }
}
