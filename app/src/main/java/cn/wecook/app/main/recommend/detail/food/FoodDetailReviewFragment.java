package cn.wecook.app.main.recommend.detail.food;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.LabelView;
import com.wecook.uikit.widget.LineView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.zoom.PullToZoomListView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.FoodResourceAdapter;
import cn.wecook.app.adapter.FoodStepAdapter;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.LoadingDialog;

/**
 * 菜谱详情预览
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public class FoodDetailReviewFragment extends BaseTitleFragment {
    public static final String EXTRA_FOOD_RECIPE = "extra_food_recipe";

    private static final int DEFAULT_ANIMATION_TIME = 500;
    private PullToZoomListView mListView;
    private ImageView mHeadImageView;
    private TextView mHeadTitle;
    private TextView mHeadSubTitle;
    private List<ValueAnimator> mAnimators = new ArrayList<ValueAnimator>();
    private long mAnimatorDuration;

    private TitleBar mTitleBar;

    private MergeAdapter mAdapter;
    private FoodRecipe mFoodRecipe;

    private FoodLevelView mLevelView;
    private FoodStepAdapter mStepAdapter;
    private FoodResourceAdapter mRecipeAdapter;
    private FoodTipsView mTipsView;

    private Runnable mUpdateFoodDetail = new Runnable() {
        @Override
        public void run() {
            if (mFoodRecipe != null) {
                buildHeadViews();
                buildRecipeViews();

                if (mListView.getAdapter() != null) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mListView.setAdapter(mAdapter);
                    createAnimation(mListView.getHeaderHeight() - mTitleBar.getHeight());
                    mListView.setOnListViewZoomListener(new PullToZoomListView.OnListViewZoomListener() {
                        @Override
                        public void onHeaderZoom(float f) {
                            updateZoomAnimator((long) f);
                        }
                    });
                }
                finishAllLoaded(true);
                hideLoading();
            }
        }
    };

    private void buildHeadViews() {
        if (mFoodRecipe != null) {
            if (FileUtils.isExist(mFoodRecipe.getLocalImage())) {
                ImageFetcher.asInstance().load(mFoodRecipe.getLocalImage(),
                        mHeadImageView, R.drawable.app_pic_default_recipe);
            } else {
                ImageFetcher.asInstance().load(mFoodRecipe.getImage(),
                        mHeadImageView, R.drawable.app_pic_default_recipe);
            }
            mHeadTitle.setText(mFoodRecipe.getTitle());
            mHeadSubTitle.setText(mFoodRecipe.getTags());
        }

    }

    private void buildRecipeViews() {
        LineView lineView = null;
        LabelView labelView = null;

        //描述
        mAdapter.addView(mLevelView);

        //线
        lineView = new LineView(getContext());
        lineView.initLayout();
        mAdapter.addView(lineView);

        // 食材label
        labelView = new LabelView(getContext());
        labelView.initWithLabelName("食材");
        mAdapter.addView(labelView);

        // 食材列表
        mAdapter.addAdapter(mRecipeAdapter);

        // 线
        lineView = new LineView(getContext());
        lineView.initLayout();
        mAdapter.addView(lineView);

        // 步骤label
        labelView = new LabelView(getContext());
        labelView.initWithLabelName("步骤");
        mAdapter.addView(labelView);

        // 步骤列表
        mAdapter.addAdapter(mStepAdapter);

        // 线
        lineView = new LineView(getContext());
        lineView.initLayout();
        mAdapter.addView(lineView);

        if (!StringUtils.isEmpty(mFoodRecipe.getTips())) {
            // 小贴士label
            labelView = new LabelView(getContext());
            labelView.initWithLabelName("小贴士");
            mAdapter.addView(labelView);
            // 小贴士
            mAdapter.addView(mTipsView);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFoodRecipe = (FoodRecipe) bundle.getSerializable(EXTRA_FOOD_RECIPE);
            if (mFoodRecipe != null) {
                setTitle(mFoodRecipe.getTitle());
            }
        }
        mAdapter = new MergeAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_detail_review, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (PullToZoomListView) view.findViewById(R.id.app_food_detail_list);

        View headView = mListView.getHeaderView();

        mHeadImageView = (ImageView) headView.findViewById(R.id.app_list_head_image);

        mHeadTitle = (TextView) headView.findViewById(R.id.app_list_head_title);
        mHeadSubTitle = (TextView) headView.findViewById(R.id.app_list_head_sub_title);

        mTitleBar = getTitleBar();
        mTitleBar.setBackDrawable(R.drawable.uikit_bt_back);
        mTitleBar.enableBottomDiv(false);

        view.findViewById(R.id.app_food_detail_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //重新编辑
                back();
            }
        });
        view.findViewById(R.id.app_food_detail_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发布
                alarmPublish();
            }
        });
    }

    protected void alarmPublish() {
        int result = FoodDetailEditorPolicy.get().checkPublishValidate();
        if (result != FoodDetailEditorPolicy.STATE_PUBLISH_CHECK_SUCCESS) {
            int msgId = 0;
            switch (result) {
                case FoodDetailEditorPolicy.STATE_PUBLISH_CHECK_ERROR_EMPTY_TITLE:
                    msgId = R.string.app_tip_publish_invalidate_empty_title;
                    break;
                case FoodDetailEditorPolicy.STATE_PUBLISH_CHECK_ERROR_EMPTY_COVER:
                    msgId = R.string.app_tip_publish_invalidate_empty_cover;
                    break;
                case FoodDetailEditorPolicy.STATE_PUBLISH_CHECK_ERROR_EMPTY_INGREDIENTS:
                    msgId = R.string.app_tip_publish_invalidate_empty_ingredient;
                    break;
                case FoodDetailEditorPolicy.STATE_PUBLISH_CHECK_ERROR_EMPTY_STEPS:
                    msgId = R.string.app_tip_publish_invalidate_empty_step;
                    break;
                case FoodDetailEditorPolicy.STATE_PUBLISH_CHECK_ERROR_INVALID_STEP:
                    msgId = R.string.app_tip_publish_invalidate_foodstep;
                    break;
                case FoodDetailEditorPolicy.STATE_PUBLISH_CHECK_ERROR_INVALID_INGREDIENT:
                    msgId = R.string.app_tip_publish_invalidate_ingredient;
                    break;
                case FoodDetailEditorPolicy.STATE_PUBLISH_CHECK_ERROR_DESC_TIPS_TOO_LONG:
                    msgId = R.string.app_tip_publish_invalidate_desc_tips_too_long;
                    break;
                default:
                    msgId = R.string.app_tip_publish_invalidate;
                    break;
            }

            final ConfirmDialog confirmDialog = new ConfirmDialog(getContext(),
                    getString(msgId));
            confirmDialog.showCancel(false);
            confirmDialog.show();
            LogGather.onEventPublish(false, getString(msgId));
            return;
        }

        processPublish();
    }


    private void processPublish() {
        final LoadingDialog dialog = new LoadingDialog(getContext());
        dialog.setText(R.string.app_alarm_publish_long_time);
        dialog.cancelable(false);
        dialog.show();
        // 发布
        FoodDetailEditorPolicy.get().publish(new FoodDetailEditorPolicy.PublishListener() {
            @Override
            public void onPublish(boolean success, String message) {
                dialog.dismiss();
                LogGather.onEventPublish(success, message);
                if (success) {
                    ToastAlarm.show(R.string.app_food_publish_success);
                    Bundle bundle = new Bundle();
                    FoodRecipe recipe = FoodDetailEditorPolicy.get().getFoodRecipe();
                    Food food = new Food();
                    food.id = recipe.getId();
                    food.title = recipe.getTitle();
                    food.createTime = recipe.getCreateTime();
                    food.image = recipe.getImage();
                    food.isFavourite = "0";
                    bundle.putSerializable(FoodDetailOfSelfFragment.EXTRA_FOOD, food);
                    bundle.putString(EXTRA_TITLE, recipe.getTitle());
                    bundle.putBoolean(EXTRA_FIXED_VIEW, true);
                    next(FoodDetailOfSelfFragment.class, bundle);
                    FoodDetailEditorPolicy.get().onRelease();
                } else {
                    if (NetworkState.available()) {
                        new ConfirmDialog(getContext(), R.string.app_food_publish_fail).showCancel(false).show();
                    } else {
                        new ConfirmDialog(getContext(), R.string.app_error_network).showCancel(false).show();
                    }
                }
            }
        });
    }

    private void createAnimation(long duration) {
        if (duration < 300) {
            duration = DEFAULT_ANIMATION_TIME;
        }
        mAnimatorDuration = duration;
        TextView titleBarTextView = mTitleBar.getTitleView();
        PropertyValuesHolder alphaToV = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        PropertyValuesHolder alphaToG = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);

        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(mHeadTitle, alphaToG)
                .setDuration(duration));
        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(mHeadSubTitle, alphaToG)
                .setDuration(duration));

        ValueAnimator titleVar = ObjectAnimator.ofFloat(0, 1f).setDuration(duration);
        titleVar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int color = getResources().getColor(R.color.uikit_grey);

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float alpha = (Float) animation.getAnimatedValue();
                int a = (int) (alpha * 256);
                int newColor = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
                mTitleBar.setBackgroundColor(newColor);
                if (a > 250) {
                    mTitleBar.setBackDrawable(R.drawable.uikit_bt_back_pressed);
                } else {
                    mTitleBar.setBackDrawable(R.drawable.uikit_bt_back);
                }
            }
        });
        mAnimators.add(titleVar);
        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(titleBarTextView, alphaToV)
                .setDuration(duration));
    }

    private void updateZoomAnimator(long f) {
        if (f > mAnimatorDuration) {
            f = mAnimatorDuration - 1;
        }

        if (f >= 0) {
            for (ValueAnimator animator : mAnimators) {
                animator.setCurrentPlayTime(f);
            }
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        showLoading();
        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                mLevelView = new FoodLevelView(getContext());
                mLevelView.loadLayout(R.layout.view_detail_level, mFoodRecipe);

                mTipsView = new FoodTipsView(getContext());
                mTipsView.loadLayout(R.layout.view_detail_tips, mFoodRecipe);

                if (mFoodRecipe.getStepList() != null) {
                    mStepAdapter = new FoodStepAdapter(getActivity(), mFoodRecipe
                            .getStepList().getList());
                }

                List<FoodResource> foodResourceList = new ArrayList<FoodResource>();
                if (mFoodRecipe.getIngredientsList() != null) {
                    foodResourceList.addAll(mFoodRecipe.getIngredientsList().getList());
                }

                if (mFoodRecipe.getAssistList() != null) {
                    foodResourceList.addAll(mFoodRecipe.getAssistList().getList());
                }

                mRecipeAdapter = new FoodResourceAdapter(getActivity(), foodResourceList);

                UIHandler.post(mUpdateFoodDetail);
            }

        });
    }

}
