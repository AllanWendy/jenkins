package cn.wecook.app.main.recommend.list.cookshow;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.List;
import java.util.Random;

import cn.wecook.app.R;
import cn.wecook.app.features.pick.PickActivity;
import cn.wecook.app.main.home.user.UserPageFragment;
import cn.wecook.app.main.recommend.detail.cookshow.CookShowDetailFragment;

/**
 * 推荐厨艺列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/24/14
 */
public class RecommendCookShowAdapter extends UIAdapter<ApiModelGroup<CookShow>> {

    private final static int VIEW_TYPE_LABEL = 0;
    private final static int VIEW_TYPE_FOOT = 1;
    private final static int VIEW_TYPE_CONTENT = 2;
    private Random random = new Random();
    private int[] colors = {R.color.app_recipe_bg_red, R.color.app_recipe_bg_yellow};
    private BaseFragment mFragment;

    public RecommendCookShowAdapter(BaseFragment fragment, List<ApiModelGroup<CookShow>> data) {
        super(fragment.getContext(), data);

        mFragment = fragment;
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        return count == 0 ? 0 : count + 1;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_LABEL;//label
        } else if (position == getCount() - 1) {
            return VIEW_TYPE_FOOT;//foot
        } else {
            return VIEW_TYPE_CONTENT;//content
        }
    }

    @Override
    protected View newView(int viewType) {
        View view = null;
        if (viewType == 0) {//label
            view = View.inflate(getContext(), R.layout.listview_item_label_index, null);
        } else if (viewType == 1) {//foot
            view = View.inflate(getContext(), R.layout.listview_footer_cookshow, null);
        } else if (viewType == 2) {//content
            view = View.inflate(getContext(), R.layout.listview_item_cookshow_style_recommend, null);
            float space = getContext().getResources().getDimension(R.dimen.uikit_default_space_margin);
            int width = (int) ((StringUtils.parseInt(PhoneProperties.getScreenWidth()) - 3 * space) / 2);
            final ViewStub left = (ViewStub) view.findViewById(R.id.app_cook_show_list_item_left);
            final ViewStub right = (ViewStub) view.findViewById(R.id.app_cook_show_list_item_right);

            ScreenUtils.resizeView(left, width, 1);
            ScreenUtils.resizeView(right, width, 1);
            ScreenUtils.reMargin(left, (int) space, (int) space, (int) (space / 2), 0);
            ScreenUtils.reMargin(right, (int) (space / 2), (int) space, (int) space, 0);

        }

        if (view != null) {
            view.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));
        }
        return view;
    }

    private void updateNewView(View view) {
        View scoreBg = view.findViewById(R.id.app_cook_show_score_bg);
        TextView score = (TextView) view.findViewById(R.id.app_cook_show_score);

        if (Build.VERSION.SDK_INT <= 10) {
            float translateX = getContext().getResources().getDimension(R.dimen.app_recommend_cook_show_translation_x);
            float translateY = getContext().getResources().getDimension(R.dimen.app_recommend_cook_show_translation_y);

            ViewCompat.setRotation(scoreBg, 45);
            ViewCompat.setTranslationX(scoreBg, translateX);
            ViewCompat.setTranslationY(scoreBg, translateY);

            ViewCompat.setRotation(score, 45);
        }
    }

    @Override
    public void updateView(int position, int viewType, final ApiModelGroup<CookShow> data, Bundle extra) {
        super.updateView(position, viewType, data, extra);
        switch (viewType) {
            case VIEW_TYPE_LABEL: {
                TextView label = (TextView) findViewById(R.id.app_list_item_index);
                label.setText(data.getGroupName());
                label.setCompoundDrawablesWithIntrinsicBounds(R.drawable.app_ic_label_cook_show, 0, 0, 0);
                break;
            }
            case VIEW_TYPE_FOOT: {
                View cookshowDo = findViewById(R.id.app_cook_show_do);
                View cookshowMore = findViewById(R.id.app_cook_show_more);

                cookshowDo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        LogGather.setLogMarker(LogGather.MARK.FROM, "首页晒厨艺");
                        LogGather.onEventCookShowIn();
                        Intent intent = new Intent(getContext(), PickActivity.class);
                        intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_COOK_SHOW);
                        mFragment.startActivity(intent);
                    }
                });

                cookshowMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogGather.onEventCookShowMore();

                        Bundle bundle = new Bundle();
                        bundle.putInt(CookShowPageListFragment.EXTRA_FOCUS_TAB, CookShowPageListFragment.TAB_FOCUS_HOTTEST);
                        Intent actionIntent = new Intent(getContext(), CookShowPageActivity.class);
                        actionIntent.putExtras(bundle);
                        mFragment.startActivity(actionIntent);
                    }
                });
                break;
            }
            case VIEW_TYPE_CONTENT: {

                final ViewStub left = (ViewStub) findViewById(R.id.app_cook_show_list_item_left);
                final ViewStub right = (ViewStub) findViewById(R.id.app_cook_show_list_item_right);

                if (left != null) {
                    View leftSub = left.inflate();
                    updateNewView(leftSub);
                }
                if (right != null) {
                    View rightSub = right.inflate();
                    updateNewView(rightSub);
                }

                updateItem(findViewById(R.id.app_cook_show_list_item_left_inflated), data.getItem(0));
                updateItem(findViewById(R.id.app_cook_show_list_item_right_inflated), data.getItem(1));

                break;
            }
        }
    }

    public void updateItem(View view, final CookShow cookShow) {
        if (view == null || cookShow == null) {
            return;
        }
        ImageView cover = (ImageView) view.findViewById(R.id.app_cook_show_cover);
        ImageView avatar = (ImageView) view.findViewById(R.id.app_cook_show_avatar);
        View scoreBg = view.findViewById(R.id.app_cook_show_score_bg);
        TextView score = (TextView) view.findViewById(R.id.app_cook_show_score);
        TextView name = (TextView) view.findViewById(R.id.app_cook_show_name);
        View bottomBg = view.findViewById(R.id.app_cook_show_bottom_bg);

        int index = random.nextInt(2);
        if (!StringUtils.isEmpty(cookShow.getImage())) {
            ImageFetcher.asInstance().load(cookShow.getImage(), cover, colors[index]);
        } else {
            cover.setImageResource(R.drawable.app_pic_default_recipe_icon);
        }
        if (cookShow.getUser() != null) {
            ImageFetcher.asInstance().load(cookShow.getUser().getAvatar(), avatar);
            name.setText(cookShow.getUser().getNickname());
            name.setBackgroundDrawable(null);
            bottomBg.setVisibility(View.VISIBLE);
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(UserPageFragment.EXTRA_USER, cookShow.getUser());
                    mFragment.next(UserPageFragment.class, bundle);
                }
            });
        } else {
            avatar.setOnClickListener(null);
            avatar.setImageResource(R.drawable.app_pic_default_avatar);
        }

        if (Float.compare(StringUtils.parseFloat(cookShow.getPraiseScore()), 0f) != 0) {
            score.setVisibility(View.VISIBLE);
            scoreBg.setVisibility(View.VISIBLE);
            score.setText(getPraiseScore(cookShow));
            scoreBg.setBackgroundColor(colorScore(cookShow));
        } else {
            score.setVisibility(View.GONE);
            scoreBg.setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(CookShowDetailFragment.EXTRA_COOK_SHOW, cookShow);
                mFragment.next(CookShowDetailFragment.class, bundle);
            }
        });
    }

    public String getPraiseScore(CookShow cookShow) {
        if (StringUtils.isEmpty(cookShow.getPraiseScore())
                || "0".equals(cookShow.getPraiseScore())) {
            return "暂无分";
        }
        return cookShow.getPraiseScore() + "分";
    }

    public int colorScore(CookShow cookShow) {
        if (StringUtils.isEmpty(cookShow.getPraiseScore())
                || "0".equals(cookShow.getPraiseScore())) {
            return getContext().getResources().getColor(R.color.app_praise_no);
        }
        try {
            float score = Float.valueOf(cookShow.getPraiseScore());
            if (score >= 4) {
                return getContext().getResources().getColor(R.color.app_praise_high);
            } else {
                return getContext().getResources().getColor(R.color.app_praise_low);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return getContext().getResources().getColor(R.color.app_praise_no);
    }

}
