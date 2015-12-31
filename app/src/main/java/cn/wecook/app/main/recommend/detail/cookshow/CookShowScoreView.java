package cn.wecook.app.main.recommend.detail.cookshow;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.YummyApi;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.CookShowScoreResult;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.fragment.BaseFragment;

import cn.wecook.app.R;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.home.user.UserPageFragment;

/**
 * 晒厨艺打分详情
 *
 * @author lenovo
 * @since 2014/12/31.
 */
public class CookShowScoreView extends LinearLayout {

    public static final int FROM_DETAIL = 1;
    public static final int FROM_LIST = 2;

    /** 作者 */
    private ImageView mAuthorAvatar;
    private TextView mAuthorName;
    private TextView mCreateTime;

    /** 中心图片和打分操作 */
    private ImageView mCoverImage;
    private ImageView mScoreBg;
    private TextView mScore;
    private ImageView mScoreState;
    private ViewGroup mScoreGroup;
    private ViewGroup mScoreStateGroup;
    private View[] mScoreChildren;

    /** 打分用户头像 */
    private ViewGroup mScoreUsersGroup;
    private View mScoreUserDiv;
    private ImageView[] mScoreUserAvatars;
    private TextView mScoreUserCount;

    /** 标题 */
    private TextView mTitle;
    private TextView mTags;
    private TextView mDescContent;

    /** 动作操作 */
    private View mActionShare;
    private TextView mActionComment;
    private View mActionDiv;

    private View mEndDiv;
    private BaseFragment mFragment;

    private boolean mIsInScore;
    private int mFrom;

    public CookShowScoreView(Context context) {
        super(context);
    }

    public CookShowScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAuthorAvatar = (ImageView) findViewById(R.id.app_cook_show_author_avatar);
        mAuthorName = (TextView) findViewById(R.id.app_cook_show_item_author_name);
        mCreateTime = (TextView) findViewById(R.id.app_cook_show_item_time);

        mCoverImage = (ImageView) findViewById(R.id.app_cook_show_item_image);
        mScoreBg = (ImageView) findViewById(R.id.app_cook_show_item_anim_start_image);
        mScore = (TextView) findViewById(R.id.app_cook_show_item_score_show);
        mScoreState = (ImageView) findViewById(R.id.app_cook_show_item_do_image);
        mScoreGroup = (ViewGroup) findViewById(R.id.app_cook_show_item_anim_group_layout);
        mScoreStateGroup = (ViewGroup) findViewById(R.id.app_cook_show_show_score_layout);

        int[] scoreChildIds = {
                R.id.app_cook_show_item_anim_score_one,
                R.id.app_cook_show_item_anim_score_two,
                R.id.app_cook_show_item_anim_score_three,
                R.id.app_cook_show_item_anim_score_four,
                R.id.app_cook_show_item_anim_score_five,
        };
        mScoreChildren = new View[scoreChildIds.length];
        for (int childIndex = 0; childIndex < mScoreChildren.length; childIndex++) {
            mScoreChildren[childIndex] = findViewById(scoreChildIds[childIndex]);
            //设置View代表的分值
            mScoreChildren[childIndex].setTag(childIndex + 1);
        }

        mScoreUserDiv = findViewById(R.id.app_cook_show_action_div1);
        mScoreUsersGroup = (ViewGroup) findViewById(R.id.app_cook_show_item_score_users);
        int[] scoreUserViewIds = {
                R.id.app_cook_show_score_avatar1,
                R.id.app_cook_show_score_avatar2,
                R.id.app_cook_show_score_avatar3,
                R.id.app_cook_show_score_avatar4,
                R.id.app_cook_show_score_avatar5,
                R.id.app_cook_show_score_avatar6,
                R.id.app_cook_show_score_avatar7,
                R.id.app_cook_show_score_avatar8,
                R.id.app_cook_show_score_avatar9,
                R.id.app_cook_show_score_avatar10,
        };
        mScoreUserAvatars = new ImageView[scoreUserViewIds.length];
        for (int childIndex = 0; childIndex < mScoreUserAvatars.length; childIndex++) {
            mScoreUserAvatars[childIndex] = (ImageView) findViewById(scoreUserViewIds[childIndex]);
        }
        mScoreUserCount = (TextView) findViewById(R.id.app_cook_show_score_number);


        mTitle = (TextView) findViewById(R.id.app_cook_show_item_recipe_name);
        mTags = (TextView) findViewById(R.id.app_cook_show_item_recipe_tags);
        mDescContent = (TextView) findViewById(R.id.app_cook_show_item_recipe_desc);

        mActionShare = findViewById(R.id.app_cook_show_item_share);
        mActionComment = (TextView) findViewById(R.id.app_cook_show_item_comment);
        mActionDiv = findViewById(R.id.app_cook_show_action_div2);

        mEndDiv = findViewById(R.id.app_cook_show_end_div);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ScreenUtils.resizeView(mCoverImage, PhoneProperties.getScreenWidthInt(), 1);
        ScreenUtils.resizeView(mScoreGroup, PhoneProperties.getScreenWidthInt(), 1);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void updateView(BaseFragment fragment, CookShow cookShow) {
        mFragment = fragment;

        if (cookShow != null) {
            updateAuthorView(cookShow);

            updateCenterImage(cookShow);

            updateScoreUser(cookShow);

            updateDesc(cookShow);

            updateAction(cookShow);
        }
    }

    /**
     * 厨艺动作区
     *
     * @param cookShow
     */
    private void updateAction(final CookShow cookShow) {
        if (cookShow.getCommentCount() != 0) {
            mActionComment.setText("" + cookShow.getCommentCount());
        } else {
            mActionComment.setText(R.string.app_comment_action_empty);
        }
        mActionComment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserProperties.isLogin()) {
                    if (mFrom == FROM_DETAIL) {
                        MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_COMMENT);
                    } else if (mFrom == FROM_LIST) {
                        MobclickAgent.onEvent(getContext(), LogConstant.COOK_LIST_COMMENT);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(CookShowDetailFragment.EXTRA_COOK_SHOW, cookShow);
                    if (cookShow.getCommentCount() == 0) {
                        bundle.putBoolean(CookShowDetailFragment.EXTRA_SHOW_COMMENT, true);
                    }
                    mFragment.next(CookShowDetailFragment.class, bundle);
                } else {
                    getContext().startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });

        mActionShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFrom == FROM_DETAIL) {
                    MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_SHARE);
                } else if (mFrom == FROM_LIST) {
                    MobclickAgent.onEvent(getContext(), LogConstant.COOK_LIST_SHARE);
                }
                ThirdPortDelivery.share(getContext(), cookShow);
            }
        });
    }

    /**
     * 厨艺描述
     *
     * @param cookShow
     */
    private void updateDesc(CookShow cookShow) {

        mTitle.setText(cookShow.getTitle());
        String tags = getTags(cookShow);
        if (!StringUtils.isEmpty(tags)) {
            mTags.setText(tags);
        }
        if (StringUtils.isEmpty(cookShow.getDescription())) {
            mDescContent.setVisibility(GONE);
        } else {
            mDescContent.setVisibility(VISIBLE);
            mDescContent.setText(cookShow.getDescription());
        }
    }

    /**
     * 获得标签字符串
     *
     * @param cookShow
     * @return
     */
    private String getTags(CookShow cookShow) {
        ApiModelList<Tags> tags = cookShow.getTags();

        String tagsString = "";
        for (Tags tag : tags.getList()) {
            if (!StringUtils.isEmpty(tag.getName())) {
                tagsString += "#" + tag.getName() + " ";
            }
        }
        return tagsString;
    }

    /**
     * 打分人列表
     *
     * @param cookShow
     */
    private void updateScoreUser(CookShow cookShow) {
        ApiModelList<User> scoreUsers = cookShow.getPraiseList();
        if (scoreUsers != null && !scoreUsers.isEmpty()) {
            mScoreUsersGroup.setVisibility(VISIBLE);
            mScoreUserDiv.setVisibility(VISIBLE);
            mScoreUserCount.setText(getResources().getString(R.string.app_cook_show_praise_number,
                    cookShow.getPraiseCount()));

            for (int index = 0; index < mScoreUserAvatars.length; index++) {
                final User user = ListUtils.getItem(scoreUsers.getList(), index);
                if (user != null) {
                    mScoreUserAvatars[index].setVisibility(VISIBLE);
                    ImageFetcher.asInstance().load(user.getAvatar(), mScoreUserAvatars[index], R.drawable.app_pic_default_avatar);

                    mScoreUserAvatars[index].setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(UserPageFragment.EXTRA_USER, user);
                            mFragment.next(UserPageFragment.class, bundle);
                        }
                    });

                } else {
                    mScoreUserAvatars[index].setOnClickListener(null);
                    mScoreUserAvatars[index].setVisibility(GONE);
                }
            }

        } else {
            mScoreUserDiv.setVisibility(GONE);
            mScoreUsersGroup.setVisibility(GONE);
        }
    }

    /**
     * 更新中间图片
     *
     * @param cookShow
     */
    private void updateCenterImage(final CookShow cookShow) {

        ImageFetcher.asInstance().load(cookShow.getImage(), mCoverImage);
        updateScoreState(cookShow);

        OnClickListener scoreClk = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cookShow.isPraised() && checkLogin()) {
                    showScorePanel();
                }
            }
        };

        mScore.setOnClickListener(scoreClk);
        mScoreBg.setOnClickListener(scoreClk);
        mScoreState.setOnClickListener(scoreClk);

        OnClickListener doScoreClk = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cookShow.isPraised()) {
                    return;
                }
                final Integer score = (Integer) v.getTag();
                final String preScore = cookShow.getPraiseScore();
                //计算平均打分值
                cookShow.setPraiseScore(calcLocalScore(cookShow, score));
                cookShow.setPraise(true);

                if (cookShow.getPraiseList() == null) {
                    cookShow.setPraiseList(new ApiModelList<User>(new User()));
                }
                final User selfUser = UserProperties.getUser();
                cookShow.getPraiseList().getList().add(0, selfUser);
                cookShow.increasePraiseCount();
                updateScoreState(cookShow);
                updateScoreUser(cookShow);

                hideScorePanel();

                YummyApi.updateScore(YummyApi.TYPE_COOKING, cookShow.getId(), score + "",
                        new ApiCallback<CookShowScoreResult>() {
                    @Override
                    public void onResult(CookShowScoreResult result) {
                        if (result.available()) {
                            if (mFrom == FROM_DETAIL) {
                                MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_SCORE);
                            } else if(mFrom == FROM_LIST) {
                                MobclickAgent.onEvent(getContext(), LogConstant.COOK_LIST_SCORE);
                            }
                            cookShow.setPraiseScore(result.getScore());
                        }
                        LogGather.onEventCookShowScore(cookShow.getTitle(), score, result.available(), result.getErrorMsg());
                    }
                });
            }
        };

        for (View child : mScoreChildren) {
            child.setOnClickListener(doScoreClk);
        }

        mScoreGroup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideScorePanel();
            }
        });

        mScoreGroup.setVisibility(GONE);
        if (mIsInScore) {
            ViewCompat.setTranslationY(mScoreStateGroup, 0);
            ViewCompat.setAlpha(mScoreStateGroup, 1);
        }
        mIsInScore = false;
    }

    private void showScorePanel() {
        Animation animation = AnimationUtils.loadAnimation(getContext(),
                R.anim.anim_score_fade_in);
        LayoutAnimationController controller = new LayoutAnimationController(animation, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mScoreGroup.setLayoutAnimation(controller);
        mScoreGroup.setLayoutAnimationListener(null);
        //显示打分区域
        mScoreGroup.setVisibility(VISIBLE);
        ViewCompat.animate(mScoreStateGroup)
                .alpha(0)
                .translationY(mScoreStateGroup.getHeight())
                .setDuration(300)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        ViewCompat.setTranslationY(mScoreStateGroup, mScoreStateGroup.getHeight());
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        ViewCompat.setTranslationY(mScoreStateGroup, mScoreStateGroup.getHeight());
                    }
                })
                .start();
        mIsInScore = true;
    }

    private void hideScorePanel() {
        Animation animation = AnimationUtils.loadAnimation(getContext(),
                R.anim.anim_score_fade_out);
        animation.setFillAfter(true);
        LayoutAnimationController controller = new LayoutAnimationController(animation, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        mScoreGroup.setLayoutAnimation(controller);
        mScoreGroup.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mScoreGroup.setVisibility(GONE);
                mIsInScore = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mScoreGroup.startLayoutAnimation();
        ViewCompat.animate(mScoreStateGroup)
                .alpha(1)
                .translationY(0)
                .setDuration(300)
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        ViewCompat.setTranslationY(mScoreStateGroup, 0);
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        ViewCompat.setTranslationY(mScoreStateGroup, 0);
                    }
                })
                .start();
    }

    private boolean checkLogin() {
        if (UserProperties.isLogin()) {
            return true;
        }
        getContext().startActivity(new Intent(getContext(), UserLoginActivity.class));
        return false;
    }

    private void updateScoreState(CookShow cookShow) {
        mScore.setText(getPraiseScore(cookShow));
        mScoreBg.setImageDrawable(new ColorDrawable(colorScore(cookShow)));
        if (cookShow.isPraised()) {
            mScoreState.setImageResource(R.drawable.app_ic_score_done);
        } else {
            mScoreState.setImageResource(R.drawable.app_ic_score_more);
        }
    }

    /**
     * 本地计算平均值
     *
     * @param cookShow
     * @param score
     * @return
     */
    private String calcLocalScore(CookShow cookShow, int score) {
        int count = cookShow.getPraiseCount();
        float averageScore = Float.valueOf(cookShow.getPraiseScore());
        float result = (averageScore * count + score) / (count + 1);
        int integerNumber = (int) result * 10;
        int roundedIntegerNumber = Math.round(result * 10);
        if (roundedIntegerNumber == integerNumber) {
            return integerNumber / 10 + "";
        }
        return (float)Math.round(result * 10) / 10 + "";
    }

    /**
     * 更新作者栏
     *
     * @param cookShow
     */
    private void updateAuthorView(CookShow cookShow) {
        final User author = cookShow.getUser();
        if (author != null) {
            ImageFetcher.asInstance().load(author.getAvatar(), mAuthorAvatar);
            mAuthorName.setText(author.getNickname());

            mAuthorAvatar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(UserPageFragment.EXTRA_USER, author);
                    mFragment.next(UserPageFragment.class, bundle);
                }
            });
        } else {
            mAuthorAvatar.setOnClickListener(null);
        }

        long createTime = Long.valueOf(cookShow.getCreateTime());
        mCreateTime.setText(StringUtils.formatTimeWithNearBy(createTime, "MM-dd"));
    }


    /**
     * 分数
     *
     * @param cookShow
     * @return
     */
    public String getPraiseScore(CookShow cookShow) {
        if (StringUtils.isEmpty(cookShow.getPraiseScore())
                || "0".equals(cookShow.getPraiseScore())) {
            return "暂无\n评分";
        }
        return cookShow.getPraiseScore() + "分";
    }

    /**
     * 分数状态颜色
     *
     * @param cookShow
     * @return
     */
    public int colorScore(CookShow cookShow) {
        if (StringUtils.isEmpty(cookShow.getPraiseScore())
                || "0".equals(cookShow.getPraiseScore())) {
            return getContext().getResources().getColor(R.color.app_praise_null_alpha);
        }
        return getContext().getResources().getColor(R.color.app_praise_high_alpha);
    }

    /**
     * 是否正在打分
     *
     * @return
     */
    public boolean isInScore() {
        return mIsInScore;
    }

    public void updateFromDetail(){
        mActionDiv.setVisibility(GONE);
        mActionComment.setVisibility(GONE);
        mEndDiv.setVisibility(GONE);
        mDescContent.setMaxLines(Integer.MAX_VALUE);
    }

    public void whereFrom(int from) {
        mFrom = from;
    }
}
