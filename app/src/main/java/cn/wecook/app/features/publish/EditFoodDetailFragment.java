package cn.wecook.app.features.publish;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.MediaStorePicker;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.sdk.api.model.FoodRecipe;
import com.wecook.sdk.api.model.FoodStep;
import com.wecook.sdk.policy.FoodDetailEditorPolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.dragsort.DragSortGridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.dialog.ListActionDialog;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.message.MessageEventReceiver;
import cn.wecook.app.features.pick.PickActivity;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.recommend.detail.food.FoodDetailOfSelfFragment;
import cn.wecook.app.main.recommend.detail.food.FoodDetailReviewFragment;

/**
 * 编辑菜谱详情页面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/19/15
 */
public class EditFoodDetailFragment extends BaseTitleFragment {
    public static final String EXTRA_RE_EDIT = "extra_re_edit";
    private EditFoodStepAdapter mFoodStepAdapter;

    private View mGridHeadView;
    private DragSortGridView mGridView;
    private View mBackTitleView;

    private boolean mHasRegisterReceiver;
    private boolean mIsReEdit;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MessageEventReceiver.ACTION_PICK_MULTI_PIC.equals(intent.getAction())) {
                Logger.i("message", "EditFoodDetailFragment .. onReceive.");
                ArrayList<MediaStorePicker.MediaImage> images =
                        intent.getParcelableArrayListExtra(MessageEventReceiver.PARAM_LIST_BITMAP);
                if (images != null && !images.isEmpty()) {

                    Map<String, FoodStep> uploadImageMap = new HashMap<String, FoodStep>();

                    for (MediaStorePicker.MediaImage image : images) {
                        FoodStep step = new FoodStep();
                        step.setLocalImage(image.path);
                        int findIndex = Collections.binarySearch(FoodDetailEditorPolicy.get().getStepList(), step,
                                new Comparator<FoodStep>() {
                                    @Override
                                    public int compare(FoodStep lhs, FoodStep rhs) {
                                        return (StringUtils.isEmpty(lhs.getLocalImage()) ? "" : lhs.getLocalImage())
                                                .compareTo(rhs.getLocalImage());
                                    }
                                });
                        if (findIndex < 0) {
                            FoodDetailEditorPolicy.get().addStep(step);
                            uploadImageMap.put(image.path, step);
                        }
                    }

                    if (!uploadImageMap.isEmpty()) {
                        if (mFoodStepAdapter != null) {
                            mFoodStepAdapter.notifyDataSetChanged();
                        }
                        FoodDetailEditorPolicy.get().uploadImages(uploadImageMap);
                    }
                }

                unregisterReceiver();
            }

        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mIsReEdit = bundle.getBoolean(EXTRA_RE_EDIT);
        }

        if (!mIsReEdit) {
            FoodDetailEditorPolicy.get().onCreateFoodRecipe();
        }
    }

    private void registerReceiver(Context context) {
        mHasRegisterReceiver = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageEventReceiver.ACTION_PICK_MULTI_PIC);
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver() {
        if (mHasRegisterReceiver) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int space = ScreenUtils.dimen2px(getContext(), R.dimen.uikit_default_padding);
        int cardWidth = (PhoneProperties.getScreenWidthInt() - 4 * space) / 3;
        int titleWidth = 2 * cardWidth + space;
        int titleHeight = ScreenUtils.dimen2px(getContext(), R.dimen.app_food_publish_group_height);

        mGridHeadView = inflater.inflate(R.layout.listview_head_edit_food_detail, null);
        View titleGroup = mGridHeadView.findViewById(R.id.app_food_publish_add_title_group);
        View ingredientsGroup = mGridHeadView.findViewById(R.id.app_food_publish_add_ingredients_group);

        ScreenUtils.resizeViewWithSpecial(titleGroup, titleWidth, titleHeight);
        ScreenUtils.resizeViewWithSpecial(ingredientsGroup, cardWidth, titleHeight);
        ScreenUtils.reMargin(titleGroup, 0, space, space / 2, 0);
        ScreenUtils.reMargin(ingredientsGroup, space / 2, space, 0, 0);

        ingredientsGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加食材
                Bundle bundle = new Bundle();
                bundle.putInt(EditFoodStepListFragment.EXTRA_STEP_TYPE, EditFoodStepListFragment.STEP_INGREDIENT);
                next(EditFoodStepListFragment.class, bundle);
            }
        });

        titleGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加标题
                Bundle bundle = new Bundle();
                bundle.putInt(EditFoodStepListFragment.EXTRA_STEP_TYPE, EditFoodStepListFragment.STEP_TITLE);
                next(EditFoodStepListFragment.class, bundle);
            }
        });
        return inflater.inflate(R.layout.fragment_edit_food_detail, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFixed(true);
        TitleBar titleBar = getTitleBar();
        mBackTitleView = titleBar.getBackTitleView();
        if (FoodDetailEditorPolicy.get().hasChanged()) {
            mBackTitleView.setVisibility(View.VISIBLE);
        } else {
            mBackTitleView.setVisibility(View.GONE);
        }

        titleBar.setTitle(getString(R.string.app_title_edit_food_detail));
        titleBar.setBackTitle(R.string.app_button_title_draft_box);

        FoodDetailEditorPolicy.get().setOnChangedListener(new FoodDetailEditorPolicy.OnEditorChangedListener() {
            @Override
            public void onChanged(boolean changed) {
                if (mBackTitleView != null) {
                    mBackTitleView.setVisibility(changed ? View.VISIBLE : View.GONE);
                }
            }
        });

        View.OnClickListener backListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        };

        titleBar.setBackListener(backListener);
        titleBar.setBackTitleClickListener(backListener);

        TitleBar.ActionCoveredTextView publish = new TitleBar.ActionCoveredTextView(getContext(),
                getString(mIsReEdit ? R.string.app_button_title_republish : R.string.app_button_title_publish));
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmPublish();
            }
        });
        titleBar.addActionView(publish);

        mGridView = (DragSortGridView) view.findViewById(R.id.app_food_publish_drag_grid);
        mGridView.addHeaderView(mGridHeadView);
        mFoodStepAdapter = new EditFoodStepAdapter(getContext(), FoodDetailEditorPolicy.get().getStepList());
        mGridView.setAdapter(mFoodStepAdapter);
        mGridView.setOnReorderingListener(new DragSortGridView.OnReorderingListener() {
            @Override
            public void onReordering(int fromPosition, int toPosition) {
                FoodDetailEditorPolicy.get().onChangePosition(fromPosition, toPosition);
                mFoodStepAdapter.notifyDataSetChanged();
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = position - mGridView.getHeaderViewCount() * mGridView.getNumColumns();
                if (pos < 0) {
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putInt(EditFoodStepListFragment.EXTRA_STEP_TYPE, EditFoodStepListFragment.STEP_ONE_OPTION);
                bundle.putInt(EditFoodStepListFragment.EXTRA_STEP_NO, pos);
                next(EditFoodStepListFragment.class, bundle);
            }
        });

        view.findViewById(R.id.app_food_publish_add_step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new ListActionDialog(getContext(), new String[]{
                        "图片(可同时添加多图)",
                        "文本"
                }, new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        switch (position) {
                            case 0:
                                MobclickAgent.onEvent(getContext(), LogConstant.NEW_RECIPE_EDIT_DETAIL_ADD_IMAGE_STEP);
                                registerReceiver(getContext());
                                Intent intent = new Intent(getContext(), PickActivity.class);
                                intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_MULTI);
                                startActivity(intent);
                                break;
                            case 1:
                                MobclickAgent.onEvent(getContext(), LogConstant.NEW_RECIPE_EDIT_DETAIL_ADD_TEXT_STEP);
                                Bundle bundle = new Bundle();
                                bundle.putInt(EditFoodStepListFragment.EXTRA_STEP_TYPE, EditFoodStepListFragment.STEP_ONE_OPTION_TEXT);
                                next(EditFoodStepListFragment.class, bundle);
                                break;
                        }
                    }
                }).show();

            }
        });

        view.findViewById(R.id.app_food_publish_review).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //预览
                LogGather.onEventPublishReview();

                Bundle bundle = new Bundle();
                bundle.putSerializable(FoodDetailReviewFragment.EXTRA_FOOD_RECIPE,
                        FoodDetailEditorPolicy.get().getReviewFoodRecipe());
                next(FoodDetailReviewFragment.class, bundle);
            }
        });

        updateViews();
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

        new PublishTagDialog(getContext())
                .setOnPublishListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processPublish();
                    }
                }).show();

    }

    private void processPublish() {
        final LoadingDialog dialog = new LoadingDialog(getContext());
        dialog.setText(R.string.app_alarm_publish_long_time);
        dialog.cancel();
        dialog.show();
        // 发布
        FoodDetailEditorPolicy.get().publish(new FoodDetailEditorPolicy.PublishListener() {
            @Override
            public void onPublish(boolean success, String message) {
                dialog.dismiss();
                LogGather.onEventPublish(success, message);
                if (success) {
                    MobclickAgent.onEvent(getContext(), LogConstant.NEW_RECIPE_EDIT_DETAIL_PUBLISH);
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
                    bundle.putString(FoodDetailOfSelfFragment.EXTRA_TITLE, recipe.getTitle());
                    bundle.putBoolean(FoodDetailOfSelfFragment.EXTRA_FIXED_VIEW, true);
                    next(FoodDetailOfSelfFragment.class, bundle);
                    FoodDetailEditorPolicy.get().onRelease();

                    ThirdPortDelivery.share(getContext(), recipe,
                            getString(R.string.app_recipe_publish_success));
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

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        setFixed(true);
        if (getContext() != null) {
            updateViews();
        }
    }

    protected void saveToDraft() {
        showLoading();
        FoodDetailEditorPolicy.get().saveToDraft(new FoodDetailEditorPolicy.SaveListener() {
            @Override
            public void onSave(boolean local, boolean remote) {
                FoodDetailEditorPolicy.get().onRelease();
                hideLoading();
                if (local) {
                    MobclickAgent.onEvent(getContext(), LogConstant.NEW_RECIPE_EDIT_DETAIL_DRAFT);
                    ToastAlarm.show(R.string.app_tip_save_to_draft_success);
                }
                finishAll();
            }
        });

    }

    @Override
    public boolean back(Bundle data) {
        if (FoodDetailEditorPolicy.get().hasChanged()) {
            saveToDraft();
        } else {
            finishFragment();
        }
        return true;
    }

    private void updateViews() {
        updateTitleView();
        updateIngredientView();
        updateStepsView();
    }

    private void updateTitleView() {
        if (mGridHeadView != null) {
            TextView title = (TextView) mGridHeadView.findViewById(R.id.app_food_publish_add_title);
            TextView name = (TextView) mGridHeadView.findViewById(R.id.app_food_publish_title_show);
            ImageView cover = (ImageView) mGridHeadView.findViewById(R.id.app_food_publish_cover);
            ImageView icon = (ImageView) mGridHeadView.findViewById(R.id.app_food_publish_bt_photo);
            name.setText(FoodDetailEditorPolicy.get().getTitle());
            String url = FoodDetailEditorPolicy.get().getImage();
            String path = FoodDetailEditorPolicy.get().getLocalImage();

            if (FileUtils.isExist(path)) {
                ImageFetcher.asInstance().loadWithoutMemoryCache(path, cover);
            } else if (!StringUtils.isEmpty(url)) {
                ImageFetcher.asInstance().load(url, cover);
            }

            if (StringUtils.isEmpty(url) && !FileUtils.isExist(path)) {
                ViewCompat.setAlpha(name, 1);
                ViewCompat.setAlpha(icon, 1);
                cover.setImageBitmap(null);
                title.setVisibility(View.VISIBLE);
            } else {
                ViewCompat.setAlpha(name, 0.8f);
                ViewCompat.setAlpha(icon, 0.8f);
                title.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void updateIngredientView() {
        if (mGridHeadView != null) {
            ViewGroup ingredients = (ViewGroup) mGridHeadView.findViewById(R.id.app_food_publish_ingredients);
            TextView ingredientsTitle = (TextView) mGridHeadView.findViewById(R.id.app_food_publish_add_ingredients_title);
            ImageView ingredientsIcon = (ImageView) mGridHeadView.findViewById(R.id.app_food_publish_add_ingredients_icon);
            if (FoodDetailEditorPolicy.get().getIngredientsList().isEmpty()) {
                ingredientsTitle.setText(R.string.app_food_publish_add_ingredients);
                ingredientsIcon.setVisibility(View.VISIBLE);
            } else {
                ingredientsTitle.setText(R.string.app_food_publish_add_ingredients_title);
                ingredientsIcon.setVisibility(View.GONE);
            }
            ingredients.removeAllViews();
            FoodDetailEditorPolicy.get().updateValidateIngredientList();
            for (FoodIngredient ingredient : FoodDetailEditorPolicy.get().getIngredientsList()) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_ingredient_show, null);
                TextView name = (TextView) view.findViewById(R.id.app_food_publish_ingredients_name);
                name.setText(ingredient.getName());
                TextView quality = (TextView) view.findViewById(R.id.app_food_publish_ingredients_quality);
                quality.setText(ingredient.getQuality());
                ingredients.addView(view);
            }
        }
    }

    private void updateStepsView() {
        if (mGridHeadView != null) {
            View div = mGridHeadView.findViewById(R.id.app_food_publish_div_group);
            if (FoodDetailEditorPolicy.get().getStepList().isEmpty()) {
                div.setVisibility(View.GONE);
            } else {
                div.setVisibility(View.VISIBLE);
            }

            if (mFoodStepAdapter != null) {
                mFoodStepAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 编辑菜谱步骤
     *
     * @author kevin
     * @version v1.0
     * @since 2015-1/20/15
     */
    public class EditFoodStepAdapter extends UIAdapter<FoodStep> {

        public EditFoodStepAdapter(Context context, List<FoodStep> data) {
            super(context, R.layout.view_food_step_card, data);
        }

        @Override
        protected View newView(int viewType, int position) {
            View view = super.newView(viewType);
            View image = view.findViewById(R.id.app_food_step_image);

            int space = ScreenUtils.dimen2px(getContext(), R.dimen.uikit_default_padding);
            int cardWidth = (PhoneProperties.getScreenWidthInt() - 4 * space) / 3;

            ScreenUtils.resizeViewWithSpecial(image, cardWidth, cardWidth);
            return view;
        }

        @Override
        public void updateView(final int position, int viewType, FoodStep data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            ImageView image = (ImageView) findViewById(R.id.app_food_step_image);
            TextView order = (TextView) findViewById(R.id.app_food_step_order);
            TextView desc = (TextView) findViewById(R.id.app_food_step_desc);
            TextView descFull = (TextView) findViewById(R.id.app_food_step_desc_full);
            if (data.isOnlyText()) {
                image.setVisibility(View.INVISIBLE);
                descFull.setVisibility(View.VISIBLE);
                descFull.setText(data.getText());
                image.setImageBitmap(null);
            } else {
                image.setVisibility(View.VISIBLE);
                descFull.setVisibility(View.GONE);
                descFull.setText("");
                if (FileUtils.isExist(data.getLocalImage())) {
                    ImageFetcher.asInstance().loadWithoutMemoryCache(data.getLocalImage(), image);
                } else if (!StringUtils.isEmpty(data.getOnlineImageUrl())) {
                    ImageFetcher.asInstance().load(data.getOnlineImageUrl(), image);
                }
            }
            order.setText("" + (position + 1));
            desc.setText(data.getText());
        }
    }

}
