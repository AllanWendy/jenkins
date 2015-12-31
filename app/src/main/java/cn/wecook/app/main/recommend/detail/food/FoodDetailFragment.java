package cn.wecook.app.main.recommend.detail.food;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.legacy.FavoriteApi;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.FoodDetail;
import com.wecook.sdk.api.model.FoodResource;
import com.wecook.sdk.policy.FavoritePolicy;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.LabelView;
import com.wecook.uikit.widget.LineView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.zoom.PullToZoomListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wecook.app.R;
import cn.wecook.app.adapter.CommentListAdapter;
import cn.wecook.app.adapter.FoodResourceAdapter;
import cn.wecook.app.adapter.FoodStepAdapter;
import cn.wecook.app.dialog.InputCommentDialog;
import cn.wecook.app.features.pick.PickActivity;
import cn.wecook.app.features.pick.PickFragment;
import cn.wecook.app.features.picture.PictureActivity;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.recommend.list.cookshow.CookShowListFragment;

/**
 * 菜谱详情
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public class FoodDetailFragment extends BaseTitleFragment {
    public static final String EXTRA_FOOD = "extra_food";
    public static final String EXTRA_JUMP_TO_COMMENT = "extra_jump_to_comment";
    public static final int MAX_ALPHA = 50;
    private static final int DEFAULT_ANIMATION_TIME = 500;
    private Food mFood;
    private PullToZoomListView mListView;
    private ImageView mHeadImageView;
    private TextView mHeadTitle;
    private TextView mHeadSubTitle;
    private ImageView mHeadFavView;
    private List<ValueAnimator> mAnimators = new ArrayList<ValueAnimator>();
    private long mAnimatorDuration;

    private TitleBar mTitleBar;
    private TitleBar.ActionImageView mTitleShare;
    private TitleBar.ActionImageView mTitleFav;
    private ShapeDrawable mShareBgDrawable;
    private ShapeDrawable mBackBgDrawable;

    private MergeAdapter mAdapter;
    private FoodDetail mDetail;
    private boolean isUpdatedRecipe;
    private boolean isUpdatedComment;
    private boolean isUpdatedCookShare;

    private FoodDescriptionView mFoodDescriptionView;
    private FoodPriceView mFoodPriceView;
    private FoodStepAdapter mStepAdapter;
    private FoodResourceAdapter mRecipeAdapter;
    private FoodTipsView mTipsView;
    private CommentListAdapter mCommentAdapter;
    private FoodCookShareView mCookShareView;

    private EmptyView mEmptyView;
    private LoadMoreImpl<List<Comment>> mLoadMore;
    private boolean mIsJumpToComment;
    private LabelView mCommentLabel;
    private Runnable mUpdateFoodDetail = new Runnable() {
        @Override
        public void run() {
            if (mDetail.getFoodRecipe() != null) {
                if (getActivity() == null) return;
                //update food recipe
                if (!isUpdatedRecipe) {
                    isUpdatedRecipe = true;

                    buildHeadViews();
                    buildRecipeViews();
                }

                if (mDetail.getCommentList() != null
                        && !isUpdatedComment) {
                    //update food comment
                    isUpdatedComment = true;
                    buildCommentViews();
                }

                if (mDetail.getCookShareList() != null
                        && !isUpdatedCookShare) {
                    //update food cook share
                    isUpdatedCookShare = true;
                    buildCookShareViews();
                }

                if (isUpdatedRecipe
                        && isUpdatedComment
                        && isUpdatedCookShare) {
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

                    mShareBgDrawable.setAlpha(MAX_ALPHA);
                    mBackBgDrawable.setAlpha(MAX_ALPHA);

                    if (mIsJumpToComment) {
                        for (int i = 0; i < mAdapter.getCount(); i++) {
                            Object o = mAdapter.getItem(i);
                            if (o == mCommentLabel) {
                                mListView.setSelection(Math.max(i - 1, 0));
                                break;
                            }
                        }
                    }

                    hideLoading();
                }
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CookShowApi.ACTION_COOK_SHOW.equals(intent.getAction())) {
                CookShow cookShow = (CookShow) intent.getSerializableExtra(CookShowApi.PARAM_COOK_SHOW);
                if (cookShow != null) {
                    if (mCookShareView != null) {
                        mCookShareView.addCookShow(cookShow);
                    }
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString(CookShowListFragment.EXTRA_FOOD_ID, mFood.id);
                    bundle.putString(CookShowListFragment.EXTRA_FOOD_NAME, mFood.title);
                    bundle.putInt(CookShowListFragment.EXTRA_TYPE, CookShowListFragment.TYPE_RECIPE_COOK_SHOW_LIST);
                    next(CookShowListFragment.class, bundle);
                }
            }
        }
    };


    private void buildHeadViews() {
        if (mDetail.getFoodRecipe() != null) {
            mHeadImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mHeadImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    ImageFetcher.asInstance().load(mDetail.getFoodRecipe().getImage(),
                            mHeadImageView, R.color.uikit_grey,
                            mHeadImageView.getMeasuredWidth(), mHeadImageView.getMeasuredHeight());
                    return false;
                }
            });
            mHeadImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), PictureActivity.class);
                    intent.putExtra(PictureActivity.EXTRA_ANIMATOR_STYLE, PictureActivity.ANIMATOR_STYLE_UP_TO_CENTER);
                    intent.putExtra(PictureActivity.EXTRA_URL, mDetail.getFoodRecipe().getImage());
                    intent.putExtra(PictureActivity.EXTRA_ORIGINAL_URL, mDetail.getFoodRecipe().getImageOriginal());
                    getActivity().startActivity(intent);
                }
            });
            mHeadTitle.setText(mDetail.getFoodRecipe().getTitle());
            mHeadSubTitle.setText(mDetail.getFoodRecipe().getTags());
        }

    }

    private void buildCommentViews() {
        if (mDetail.getCommentList().getCountOfList() > 0) {
            // 讨论label
            mCommentLabel = new LabelView(getContext());
            mCommentLabel.initWithLabelName("大家的讨论");
            mAdapter.addView(mCommentLabel);
            // 讨论列表
            mAdapter.addAdapter(mCommentAdapter);

            mLoadMore = new LoadMoreImpl<List<Comment>>();
            mLoadMore.setMoreLoader(new UILoader<List<Comment>>(this) {
                @Override
                public List<Comment> runBackground() {
                    return SyncHandler.sync(this);
                }

                @Override
                public void sync(final Callback<List<Comment>> callback) {
                    super.sync(callback);

                    requestCommentList(mFood.id, mLoadMore.getCurrentPage(),
                            mLoadMore.getPageSize(), new ApiCallback<ApiModelList<Comment>>() {
                                @Override
                                public void onResult(ApiModelList<Comment> result) {
                                    if (result != null && result.available() && getContext() != null) {
                                        callback.callback(result.getList());
                                    } else {
                                        callback.callback(null);
                                    }
                                }
                            });
                }
            });
            mLoadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener<List<Comment>>() {
                @Override
                public void onLoaded(boolean success, List<Comment> o) {
                    if (success) {
                        if (mCommentAdapter != null) {
                            mCommentAdapter.addEntrys(o);
                        }
                    }
                }
            });
            mLoadMore.setListAdapter(mCommentAdapter, mListView);
        }
    }

    private void buildCookShareViews() {
        // 朋友已晒label
        ApiModelList<CookShow> list = mDetail.getCookShareList();
        if (list.getCountOfList() > 0) {
            LabelView labelView = new LabelView(getContext());
            labelView.initWithLabelName(list.getCountOfServer() + "个朋友做过这道菜");
            labelView.setOnMoreClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(CookShowListFragment.EXTRA_FOOD_ID, mFood.id);
                    bundle.putString(CookShowListFragment.EXTRA_FOOD_NAME, mFood.title);
                    bundle.putInt(CookShowListFragment.EXTRA_TYPE, CookShowListFragment.TYPE_RECIPE_COOK_SHOW_LIST);
                    next(CookShowListFragment.class, bundle);
//                    next(CookShowHomeFragment.class);
                }
            });
            mAdapter.addView(labelView);

            // 横向列表
            mAdapter.addView(mCookShareView);

            //线
            LineView lineView = new LineView(getContext());
            lineView.initLayout();
            mAdapter.addView(lineView);
        }
    }

    private void buildRecipeViews() {
        LineView lineView = null;
        LabelView labelView = null;

        //描述
        mAdapter.addView(mFoodDescriptionView);
        //线
        lineView = new LineView(getContext());
        lineView.initLayout();
        mAdapter.addView(lineView);
//        if (!StringUtils.isEmpty(mDetail.getMinPrice())
//                || !StringUtils.isEmpty(mDetail.getMaxPrice())) {
//            //价格
//            mAdapter.addView(mFoodPriceView);
//
//            //线
//            lineView = new LineView(getContext());
//            lineView.initLayout();
//            mAdapter.addView(lineView);
//        }

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

        if (!StringUtils.isEmpty(mDetail.getFoodRecipe().getTips())) {
            // 小贴士label
            labelView = new LabelView(getContext());
            labelView.initWithLabelName("小贴士");
            mAdapter.addView(labelView);
            // 小贴士
            mAdapter.addView(mTipsView);
            // 线
            lineView = new LineView(getContext());
            lineView.initLayout();
            mAdapter.addView(lineView);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFood = (Food) bundle.getSerializable(EXTRA_FOOD);
            if (mFood != null) {
                setTitle(mFood.title);
            }
            mIsJumpToComment = bundle.getBoolean(EXTRA_JUMP_TO_COMMENT);
        }
        mDetail = new FoodDetail();
        mAdapter = new MergeAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_food_detail, null);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmptyView = (EmptyView) view.findViewById(R.id.uikit_empty);
        mEmptyView.setRefreshListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartUILoad();
            }
        });

        if (mFood == null || StringUtils.isEmpty(mFood.id)) {
            mEmptyView.setCanRefresh(false);
        }

        mListView = (PullToZoomListView) view.findViewById(R.id.app_food_detail_list);

        View headView = mListView.getHeaderView();

        mHeadImageView = (ImageView) headView.findViewById(R.id.app_list_head_image);

        mHeadTitle = (TextView) headView.findViewById(R.id.app_list_head_title);
        mHeadSubTitle = (TextView) headView.findViewById(R.id.app_list_head_sub_title);
        mHeadFavView = (ImageView) headView.findViewById(R.id.app_list_head_fav);

        mTitleBar = getTitleBar();
        mTitleShare = new TitleBar.ActionImageView(getContext(), R.drawable.app_bt_share);
        mShareBgDrawable = new ShapeDrawable();
        mShareBgDrawable.setShape(new OvalShape());
        mShareBgDrawable.getPaint().setColor(getResources().getColor(R.color.uikit_black));
        mShareBgDrawable.setAlpha(0);
        mTitleShare.setBackgroundDrawable(mShareBgDrawable);
        mTitleShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> keys = new HashMap<String, String>();
                keys.put(LogConstant.KEY_KEY, mFood.title);
                MobclickAgent.onEvent(getContext(), LogConstant.UBS_RECIPE_SHARE_COUNT, keys);

                ThirdPortDelivery.share(getContext(), mDetail);
            }
        });

        mTitleFav = new TitleBar.ActionImageView(getContext(), R.drawable.app_bt_fav);
        FavoritePolicy.favoriteHelper(new View[]{mHeadFavView, mTitleFav}, FavoriteApi.TYPE_RECIPE,
                StringUtils.parseInt(mFood.id), mFood, new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getContext(), UserLoginActivity.class));
                    }
                }
        );

        mTitleBar.addActionView(mTitleFav);
        mTitleBar.addActionView(mTitleShare);
        mTitleBar.setBackDrawable(R.drawable.uikit_bt_back);
        mTitleBar.enableBottomDiv(false);
        if (isFixed) {
            mTitleBar.setBackListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAll();
                }
            });
        }

        View back = mTitleBar.getBackView();
        mBackBgDrawable = new ShapeDrawable();
        mBackBgDrawable.setShape(new OvalShape());
        mBackBgDrawable.getPaint().setColor(getResources().getColor(R.color.uikit_black));
        mBackBgDrawable.setAlpha(0);
        back.setBackgroundDrawable(mBackBgDrawable);

        view.findViewById(R.id.app_food_detail_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserProperties.isLogin()) {
                    InputCommentDialog dialog;
                    if (Build.BRAND.equals("vivo")) {
                        dialog = new InputCommentDialog(getContext(), null, CommentApi.TYPE_RECIPE, mFood.id, false);
                    } else {
                        dialog = new InputCommentDialog(getContext(), CommentApi.TYPE_RECIPE, mFood.id);
                    }
                    dialog.setOnSendCommentListener(new InputCommentDialog.OnSendCommentListener() {
                        @Override
                        public void sendResult(Comment comment) {
                            if (comment != null) {
                                if (mCommentAdapter != null) {
                                    mCommentAdapter.addEntry(0, comment);
                                }
                            }
                        }
                    }).show();
                } else {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                }
            }
        });
        View cookShow = view.findViewById(R.id.app_food_detail_upload_food);
        cookShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserProperties.isLogin()) {
                    Intent intent = new Intent(getContext(), PickActivity.class);
                    intent.putExtra(PickFragment.EXTRA_FOOD, mFood);
                    intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_COOK_SHOW);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                }

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(CookShowApi.ACTION_COOK_SHOW);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    private void createAnimation(long duration) {
        if (duration < 300) {
            duration = DEFAULT_ANIMATION_TIME;
        }
        mAnimatorDuration = duration;
        TextView titleBarTextView = mTitleBar.getTitleView();
//        float textWidth = ScreenUtils.getTextViewWordWidth(titleBarTextView);
//        int toX = (int) (ScreenUtils.getScreenWidth() / 2 - textWidth);
//        PropertyValuesHolder titleMove = PropertyValuesHolder.ofFloat("x", 0, toX);
//        PropertyValuesHolder favMove = PropertyValuesHolder.ofFloat("x",
//                ScreenUtils.getViewX(mHeadFavView),ScreenUtils.getViewX(mTitleFav));
        PropertyValuesHolder alphaToV = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
        PropertyValuesHolder alphaToG = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);

        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(mHeadTitle, alphaToG)
                .setDuration(duration));
        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(mHeadSubTitle, alphaToG)
                .setDuration(duration));

        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(mHeadFavView, alphaToG)
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
                int recurseAlpha = MAX_ALPHA - a;
                if (recurseAlpha < 0) {
                    recurseAlpha = 0;
                }
                mBackBgDrawable.setAlpha(recurseAlpha);
                mShareBgDrawable.setAlpha(recurseAlpha);
                if (a > 250) {
                    mTitleBar.setBackDrawable(R.drawable.uikit_bt_back_pressed);
                    mTitleShare.setImageResource(R.drawable.app_bt_share_highlight);
                    mTitleFav.setImageResource(R.drawable.app_bt_fav_highlight);
                    mTitleFav.setEnabled(true);
                } else {
                    mTitleBar.setBackDrawable(R.drawable.uikit_bt_back);
                    mTitleShare.setImageResource(R.drawable.app_bt_share);
                    mTitleFav.setImageResource(R.drawable.app_bt_fav);
                    mTitleFav.setEnabled(false);
                }
            }
        });
        mAnimators.add(titleVar);
        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(titleBarTextView, alphaToV)
                .setDuration(duration));
        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(mTitleFav, alphaToV)
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
        if (mDetail.getFoodRecipe() == null) {
            showLoading();

            requestFoodDetail(mFood.id, new ApiCallback<FoodDetail>() {
                @Override
                public void onResult(FoodDetail result) {
                    if (result != null && result.getFoodRecipe() != null && getContext() != null) {
                        mDetail = result;
                        mDetail.setFoodRecipe(result.getFoodRecipe());

                        mTitleBar.setTitle(mDetail.getFoodRecipe().getTitle());

                        mFoodDescriptionView = new FoodDescriptionView(FoodDetailFragment.this);
                        mFoodDescriptionView.loadLayout(R.layout.view_detail_description, result);

                        mFoodPriceView = new FoodPriceView(getContext());
                        mFoodPriceView.loadLayout(R.layout.view_detail_price, result);

                        mTipsView = new FoodTipsView(getContext());
                        mTipsView.loadLayout(R.layout.view_detail_tips, result.getFoodRecipe());

                        if (mDetail.getFoodRecipe().getStepList() != null) {
                            mStepAdapter = new FoodStepAdapter(getActivity(), mDetail.getFoodRecipe()
                                    .getStepList().getList(), FoodStepAdapter.TYPE_RECIPE);
                        }

                        List<FoodResource> foodResourceList = new ArrayList<FoodResource>();
                        if (result.getFoodRecipe().getIngredientsList() != null) {
                            foodResourceList.addAll(mDetail.getFoodRecipe().getIngredientsList().getList());
                        }

                        if (result.getFoodRecipe().getAssistList() != null) {
                            foodResourceList.addAll(mDetail.getFoodRecipe().getAssistList().getList());
                        }

                        mRecipeAdapter = new FoodResourceAdapter(getActivity(), foodResourceList);

                        UIHandler.post(mUpdateFoodDetail);

                        loadCookShareList();
                    } else {
                        showEmpty();
                        hideLoading();
                    }
                }
            });
        }
    }

    public void showEmpty() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    protected void requestFoodDetail(String id, ApiCallback<FoodDetail> apiCallback) {
        FoodApi.getFoodDetail(id, apiCallback);
    }

    private void loadCookShareList() {
        if (mDetail.getCookShareList() == null || mDetail.getCookShareList().getCountOfList() == 0) {
            // 添加相关已晒列表
            CookShowApi.getCookShowList(CookShowApi.TYPE_RECIPE, mFood.title, 1, 10, new ApiCallback<ApiModelList<CookShow>>() {
                @Override
                public void onResult(ApiModelList<CookShow> result) {
                    if (result != null && getActivity() != null) {
                        mDetail.setCookShareList(result);
                        mCookShareView = new FoodCookShareView(FoodDetailFragment.this);
                        mCookShareView.loadLayout(R.layout.view_detail_cook_share, result.getList());
                        UIHandler.post(mUpdateFoodDetail);
                        loadComments();
                    }
                }
            });
        }
    }

    private void loadComments() {
        if (mDetail.getCommentList() == null || mDetail.getCommentList().getCountOfList() == 0) {
            //添加评论列表
            CommentApi.getCommentList(CommentApi.TYPE_RECIPE, mFood.id, 1, 10, new ApiCallback<ApiModelList<Comment>>() {
                @Override
                public void onResult(ApiModelList<Comment> result) {
                    if (result != null && getActivity() != null) {
                        mDetail.setCommentList(result);
                        mCommentAdapter = new CommentListAdapter(FoodDetailFragment.this,
                                result.getList(), CommentApi.TYPE_RECIPE, mFood.id);
                        UIHandler.post(mUpdateFoodDetail);
                    }
                }
            });
        }
    }

    /**
     * 请求评论列表
     */
    private void requestCommentList(final String resourceId, int page, int pageSize, ApiCallback<ApiModelList<Comment>> callback) {
        CommentApi.getCommentList(CommentApi.TYPE_RECIPE, resourceId, page, pageSize, callback);
    }

}
