package cn.wecook.app.main.dish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.CommentApi;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.legacy.RestaurantApi;
import com.wecook.sdk.api.model.Comment;
import com.wecook.sdk.api.model.CommentCount;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.DishFeature;
import com.wecook.sdk.api.model.FoodIngredient;
import com.wecook.sdk.api.model.Image;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.api.model.Security;
import com.wecook.sdk.api.model.ShopCartDish;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.adapter.UIAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.indicator.CirclePageIndicator;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.DishAdapter;
import cn.wecook.app.adapter.FoodStepAdapter;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.picture.MultiPictureActivity;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.main.dish.order.PictureHelpView;
import cn.wecook.app.main.dish.restaurant.DishRestaurantCommentFragment;
import cn.wecook.app.main.dish.restaurant.DishRestaurantDetailFragment;
import cn.wecook.app.main.dish.shopcart.DishShopCartFragment;
import cn.wecook.app.main.home.user.UserPageFragment;
import cn.wecook.app.utils.ShoppingAnimationUtils;

/**
 * 菜品详情页面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/8/6
 */
public class DishDetailFragment extends BaseListFragment {

    public static final String EXTRA_DISH_ID = "extra_dish_id";

    private static final int TYPE_RESTAURANT = Dish.TYPE_RESTAURANT;//套餐
    private static final int TYPE_DISH = Dish.TYPE_DISH;//菜品

    private static final int TYPE_DISH_MORE = 2;//更多菜品页面
    private static final int TYPE_DISH_DETAIL = 1;//菜品详情页面


    private int currentDishType = 0;//当前套餐／菜品模式
    private int currentPullType = 0;


    private List hideViews = new ArrayList();//隐藏的view／adapter

    private ListView mListView;
    private View mHeaderView;
    private ViewPager mViewPager;
    private CirclePageIndicator mIndicator;
    private ImagePageAdapter mCoversAdapter;
    private View mIntoRestaurantView;
    private View mIntoShopcartView;
    private TextView mIntoShopcartViewNewMark;
    private TextView mAddtoShopcartView;
    private MergeAdapter mListAdapter;
    private Dish dish;
    private String dishId;
    private View mDishBriefView;//菜品／套餐简介
    private TitleBar titleBar;
    private TitleBar.ActionImageView share;
    private View mDishIngredientInfoView;//食材包
    private View mDishGroupInfoView;//包含菜品
    private DishGroupItemAdapter mDishGroupAdapter;//包含菜品adapter
    private View mDishFeatureInfoView;//菜品特色内容
    private View mDishFeatureInfoMoreView;//菜品特色更多内容
    private DishFeatureAdapter mDishFeatureAdapter;//特色菜品adapter
    private View mDishRestaurantInfoView;//餐厅更多菜品
    private FoodStepAdapter mCookStepAdapter;//制作步骤adapter
    private View mCookTipsView;//做法小贴士
    private EmptyView mEmptyView;
    private CommentCount mCommentCount;
    private List<Comment> mComments;
    private LoadingDialog mLoadingDialog;
    private DishPolicy.OnShopCartListener listener = new DishPolicy.OnShopCartListener() {
        @Override
        public void onUpdate(int count) {
            updateShopcartNewMark();
        }
    };
    private View rootView;
    private View cookStepHead;//烹饪步骤head
    private TextView titleBarTitle;
    private int color;
    private PullToRefreshListView mPullToRefreshListView;
    private View intervalView;//包含菜品的分割线
    private View dishComments;//评轮
    private View restaurantRecommedLayout;//商铺名片
    private boolean isCloseTitleBarAlpha;//titleBar变化的开关
    private LinearLayout recommendLayout;//更多推荐菜品的layout
    private LinearLayout restaurantRecommedCard;
    private View mDishDetailBottomLyout;
    private int statusBar;//状态栏高度
    private int windowHeight;//屏幕高度
    private ValueAnimator pullShowViewAnimator;//下拉／上啦的动画
    private float pullValue;
    private TextView cardMoreText;
    private boolean isShowFeature = false;
    private boolean isShowCookTips = false;
    private View mIntoRestaurantIconView;
    private View mIntoShopcartIconView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        if (bundle != null) {
            dishId = bundle.getString(EXTRA_DISH_ID);
        }
        mComments = new ArrayList<>();
        setTitle("菜品详情");
        mLoadingDialog = new LoadingDialog(getContext());
        DishPolicy.get().addOnShopCartListener(listener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        DishPolicy.get().removeOnShopCartListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null) {
            return rootView;
        }

        rootView = inflater.inflate(R.layout.fragment_dish_detail, null);

        mPullToRefreshListView = (PullToRefreshListView) rootView.findViewById(R.id.app_dish_detail_list);
        mListView = mPullToRefreshListView.getRefreshableView();
        //底部布局
        mDishDetailBottomLyout = rootView.findViewById(R.id.app_dish_detail_bottom_layout);
        mIntoRestaurantView = rootView.findViewById(R.id.app_dish_detail_goto_restaurant);
        mIntoRestaurantIconView = rootView.findViewById(R.id.app_dish_detail_goto_restaurant_icon);
        mIntoShopcartView = rootView.findViewById(R.id.app_dish_detail_goto_shopcart);
        mIntoShopcartIconView = rootView.findViewById(R.id.app_dish_detail_goto_shopcart_icon);
        mIntoShopcartViewNewMark = (TextView) rootView.findViewById(R.id.app_dish_detail_goto_shopcart_mark);
        mAddtoShopcartView = (TextView) rootView.findViewById(R.id.app_dish_detail_add_to_shopcart);
        //大图展示布局
        mHeaderView = inflater.inflate(R.layout.view_multi_banner, null);
        mViewPager = (ViewPager) mHeaderView.findViewById(R.id.view_pager);
        mIndicator = (CirclePageIndicator) mHeaderView.findViewById(R.id.view_pager_indicator);
        ScreenUtils.resizeViewOnScreen(mViewPager, 1);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final double banner_height = ScreenUtils.getScreenWidth() * (9 / 16.0f);
        initData();
        titleBar = getTitleBar();
        color = getResources().getColor(R.color.uikit_white);
        titleBar.setBackgroundColor(0x00000000);
        ViewCompat.setAlpha(titleBar.getTitleView(), 0);
        titleBarTitle = getTitleBar().getTitleView();
        titleBar.enableBottomDiv(false);
        titleBar.setBackDrawable(R.drawable.uikit_bt_back);
        //分享
        if (share == null) {
            share = new TitleBar.ActionImageView(getContext(), R.drawable.app_bt_share);
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 分享
                    LogGather.onEventDishDetailTabShare();
                    ThirdPortDelivery.share(getContext(), dish);
                }
            });
            titleBar.addActionView(share);
        }

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //根据scroll设置title的变化
                if (firstVisibleItem <= 1) {
                    View child = mListView.getChildAt(0);
                    if (child == null) {
                        return;
                    }
                    int top = child.getTop();
                    int height = child.getHeight();
                    if (height != 0) {
                        float i = (float) ((float) Math.abs(top) / height);
                        setTitleBarAlpha(i);
                    }
                } else {
                    setTitleBarAlpha((float) 1);
                }
            }
        });

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                downPullAnimation();
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mPullToRefreshListView != null) {
                            mPullToRefreshListView.onRefreshComplete();
                        }
                    }
                });
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                upPullAnimation();
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mPullToRefreshListView != null) {
                            mPullToRefreshListView.onRefreshComplete();
                        }
                    }
                });
            }
        });

    }

    /**
     * 初始化数据
     */
    private void initData() {
        //屏幕高度
        windowHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        //状态栏高度
        Rect rect = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);///取得整个视图部分,注意，如果你要设置标题样式，这个必须出现在标题样式之后，否则会出错
        statusBar = rect.top;
        //初始化下拉／上啦动画
        initPullAnimation();
    }

    /**
     * 切换到更多菜品 应回到的位置y
     *
     * @return
     */
    private int getToTopValue() {
        int titleHeight = getTitleBar().getHeight();
        return titleHeight + statusBar + ScreenUtils.dip2px(10);
    }

    /**
     * 切换到菜品详情 时应回到的位置y
     *
     * @return
     */
    private int getToBottomValue() {
        int cardHeight = 0;
        int bottomHeight = 0;
        if (restaurantRecommedCard != null && mDishDetailBottomLyout != null) {
            cardHeight = restaurantRecommedCard.getHeight();
            bottomHeight = mDishDetailBottomLyout.getHeight();
            return (windowHeight - cardHeight - bottomHeight);
        }
        return 0;
    }

    /**
     * 滑动的动画
     */
    private void initPullAnimation() {
        pullShowViewAnimator = ValueAnimator.ofFloat(0f, 1f);
//        pullShowViewAnimator.setInterpolator(new DecelerateInterpolator(0f));
        pullShowViewAnimator.setDuration(500);
        pullShowViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                if (restaurantRecommedCard != null) {
                    int[] location = new int[2];
                    restaurantRecommedCard.getLocationInWindow(location);
                    int x = location[0];
                    int y = location[1];
                    switch (currentPullType) {
                        case TYPE_DISH_DETAIL:
                            if (y > getToTopValue()) {
                                pullValue = -(windowHeight - restaurantRecommedCard.getHeight() - titleBar.getHeight() - mDishDetailBottomLyout.getHeight() - statusBar) * value;
                            }
                            mListView.setTranslationY(pullValue);
                            break;
                        case TYPE_DISH_MORE:
                            if (y < getToBottomValue()) {
                                pullValue = (windowHeight - restaurantRecommedCard.getHeight() - titleBar.getHeight() - mDishDetailBottomLyout.getHeight() - statusBar) * value;
                            } else {
                            }
                            mListView.setTranslationY(pullValue);
                            break;
                    }
                    ;
                }
            }
        });
        pullShowViewAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                pullAnimationEnd();

            }

            private void pullAnimationEnd() {
                if (currentPullType == TYPE_DISH_DETAIL) {
                    showRestaurantMore(true);
                    mListView.setTranslationY(0);
                    mListView.setSelection(0);
                } else if (currentPullType == TYPE_DISH_MORE) {
                    showRestaurantMore(false);
                    mListView.setTranslationY(0);
                    mListView.setSelection(Integer.MAX_VALUE);

                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    /**
     * 上拉进入更多菜品
     */
    private void upPullAnimation() {
        if (pullShowViewAnimator != null) {
            if (pullShowViewAnimator.isRunning()) {
                return;
            }
            currentPullType = TYPE_DISH_DETAIL;
            pullShowViewAnimator.start();
        }
    }

    /**
     * 下拉进入菜品详情
     */
    private void downPullAnimation() {
        if (pullShowViewAnimator != null) {
            if (pullShowViewAnimator.isRunning()) {
                return;
            }
            currentPullType = TYPE_DISH_MORE;
            pullShowViewAnimator.start();
        }
    }


    /**
     * 显示更多推荐
     */
    private void showRestaurantMore(boolean isShowRestaurantMore) {
        if (isShowRestaurantMore) {
            mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            //显示商铺更多菜品推荐
            //公共部分
            mergeAdapterPendingActive(mHeaderView, false);
            mergeAdapterPendingActive(mDishBriefView, false);
            mergeAdapterPendingActive(mEmptyView, false);
            //简介
            mergeAdapterPendingActive(mDishFeatureInfoView, false);
            mergeAdapterPendingActive(mDishFeatureAdapter, false);
            mergeAdapterPendingActive(mDishFeatureInfoMoreView, false);

            //包含菜品
            mergeAdapterPendingActive(mDishGroupInfoView, false);
            mergeAdapterPendingActive(mDishGroupAdapter, false);
            mergeAdapterPendingActive(intervalView, false);
            //评论
            mergeAdapterPendingActive(dishComments, false);
            //商铺名片
            mergeAdapterPendingActive(restaurantRecommedLayout, true);
            if (restaurantRecommedLayout != null) {
                ScreenUtils.rePadding(restaurantRecommedLayout, 0, 62, 0, 0);
            }
            setRestaurantRecommedVisible(true);
            if (restaurantRecommedCard != null) {
                if (dish.getType().equals(Dish.TYPE_RESTAURANT + "")) {
                    ScreenUtils.reMargin(restaurantRecommedCard, ScreenUtils.dip2px(10), 0, ScreenUtils.dip2px(10), ScreenUtils.dip2px(0));
                } else {
                    ScreenUtils.reMargin(restaurantRecommedCard, ScreenUtils.dip2px(10), ScreenUtils.dip2px(0), ScreenUtils.dip2px(10), ScreenUtils.dip2px(0));
                }
            }


            //食材包
            mergeAdapterPendingActive(mDishIngredientInfoView, false);
            //烹饪步骤
            mergeAdapterPendingActive(cookStepHead, false);
            mergeAdapterPendingActive(mCookStepAdapter, false);
            //小贴士
            mergeAdapterPendingActive(mCookTipsView, false);

            setTitleBarAlpha(1);
            setTitleBarAlphaIsClose(true);
            if (titleBarTitle != null) {
                titleBarTitle.setText("更多菜品");
            }
            if (cardMoreText != null) {
                cardMoreText.setText("全部菜品，请进入餐厅查看");
            }

        } else {
            mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
            //显示主题内容

            //显示商铺更多菜品推荐
            mergeAdapterPendingActive(mHeaderView, true);
            mergeAdapterPendingActive(mDishBriefView, true);
            mergeAdapterPendingActive(mEmptyView, false);
            //简介
            mergeAdapterPendingActive(mDishFeatureInfoView, true);
            mergeAdapterPendingActive(mDishFeatureAdapter, isShowFeature);
            mergeAdapterPendingActive(mDishFeatureInfoMoreView, true);
            //评论
            mergeAdapterPendingActive(dishComments, true);

            //包含菜品
            mergeAdapterPendingActive(mDishGroupInfoView, currentDishType == TYPE_RESTAURANT);
            mergeAdapterPendingActive(mDishGroupAdapter, currentDishType == TYPE_RESTAURANT);
            mergeAdapterPendingActive(intervalView, currentDishType == TYPE_RESTAURANT);

            //商铺名片
            mergeAdapterPendingActive(restaurantRecommedLayout, true);
            if (restaurantRecommedLayout != null) {
                ScreenUtils.rePadding(restaurantRecommedLayout, 0, 0, 0, 0);

            }
            if (restaurantRecommedCard != null) {
                if (dish.getType().equals(Dish.TYPE_RESTAURANT + "")) {
                    ScreenUtils.reMargin(restaurantRecommedCard, ScreenUtils.dip2px(10), 0, ScreenUtils.dip2px(10), ScreenUtils.dip2px(10));
                } else {
                    ScreenUtils.reMargin(restaurantRecommedCard, ScreenUtils.dip2px(10), ScreenUtils.dip2px(10), ScreenUtils.dip2px(10), ScreenUtils.dip2px(10));
                }
            }
            setRestaurantRecommedVisible(false);

            //食材包
            mergeAdapterPendingActive(mDishIngredientInfoView, currentDishType == TYPE_DISH);
            //烹饪步骤
            mergeAdapterPendingActive(cookStepHead, currentDishType == TYPE_DISH);
            mergeAdapterPendingActive(mCookStepAdapter, currentDishType == TYPE_DISH);
            //小贴士
            mergeAdapterPendingActive(mCookTipsView, isShowCookTips);

            setTitleBarAlphaIsClose(false);
            if (titleBarTitle != null && dish != null) {
                titleBarTitle.setText(dish.getTitle());
            }
            if (cardMoreText != null) {
                cardMoreText.setText("上拉查看更多");
            }


        }
        if (mListAdapter != null) {
            mListAdapter.notifyDataSetChanged();
        }
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mPullToRefreshListView != null) {
                    mPullToRefreshListView.onRefreshComplete();
                }
            }
        });

    }

    /**
     * 设置是否显示更多菜品内容
     *
     * @param isVisible
     */
    private void setRestaurantRecommedVisible(boolean isVisible) {
        if (recommendLayout != null) {
            if (isVisible) {
                recommendLayout.setVisibility(View.VISIBLE);
            } else {
                recommendLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置mergeAdapter的activie
     *
     * @param view
     * @param b
     */
    private void mergeAdapterPendingActive(View view, boolean b) {
        if (view != null && mListAdapter != null) {
            if (b && hideViews.contains(view)) {
                return;
            }
            mListAdapter.pendingActive(view, b);
        }
    }

    /**
     * 设置mergeAdapter的activie
     *
     * @param b
     */
    private void mergeAdapterPendingActive(ListAdapter adapter, boolean b) {
        if (adapter != null && mListAdapter != null) {
            if (b && hideViews.contains(adapter)) {
                return;
            }
            mListAdapter.pendingActive(adapter, b);
        }

    }

    /**
     * 设置titleBar的变化
     *
     * @param isClose
     */
    private void setTitleBarAlphaIsClose(boolean isClose) {
        isCloseTitleBarAlpha = isClose;
    }

    /**
     * 设置顶部title变化
     *
     * @param i
     */
    private void setTitleBarAlpha(float i) {
        if (!isCloseTitleBarAlpha) {
            int a = (int) (i * 255);
            int newColor = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
            getTitleBar().setBackgroundColor(newColor);
            titleBarTitle.setAlpha(i);
            if (a > 240) {
                titleBarTitle.setAlpha(1);
                titleBar.setBackDrawable(R.drawable.uikit_bt_back_pressed);
                share.setImageResource(R.drawable.app_bt_share_highlight);
            } else {
                titleBar.setBackDrawable(R.drawable.uikit_bt_back);
                share.setImageResource(R.drawable.app_bt_share);
            }
        }
    }

    @Override
    public void showLoading() {
        super.showLoading();
        if (!NetworkState.available()) {
            ToastAlarm.show("网络异常，请检查后重新加载");
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (!StringUtils.isEmpty(dishId)) {
            showLoading();
            //请求菜品详情
            Logger.i("Simon", "开始解析－－－－－－－－－－－－－－－－－－－－－");
            DishApi.getDishDetail(dishId, new ApiCallback<Dish>() {
                @Override
                public void onResult(Dish result) {
                    if (result.available()) {
                        dish = result;
                        if (dish.getType().equals(Dish.TYPE_RESTAURANT + "")) {
                            currentDishType = TYPE_RESTAURANT;
                        } else {
                            currentDishType = TYPE_DISH;
                        }
                        //请求餐厅详情
                        RestaurantApi.getDishRestaurantDetail(dish.getRestaurantId(),
                                new ApiCallback<Restaurant>() {
                                    @Override
                                    public void onResult(Restaurant result) {
                                        if (result.available()) {
                                            dish.mergeRestaurant(result);
                                            //请求餐厅列表
                                            DishApi.getDishListFromRestaurant("", dish.getRestaurantId(),
                                                    "0", "0", 0, "", 1, 4, dishId, new ApiCallback<ApiModelList<Dish>>() {
                                                        @Override
                                                        public void onResult(ApiModelList<Dish> result) {
                                                            if (result.available()) {
                                                                dish.getRestaurant().setDishes(result.getList());
                                                            }
                                                            updateViews();
                                                            hideLoading();
                                                            mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                }
            });

            CommentApi.getCommentDishOverview(dishId,
                    new ApiCallback<CommentCount>() {
                        @Override
                        public void onResult(CommentCount result) {
                            if (result.available()) {
                                mCommentCount = result;
                            }
                        }
                    });

            CommentApi.getCommentListOfDish(dishId, 0, 1, 20,
                    new ApiCallback<ApiModelList<Comment>>() {
                        @Override
                        public void onResult(ApiModelList<Comment> result) {
                            if (result.available()) {
                                mComments.clear();
                                mComments.addAll(result.getList());
                            }
                        }
                    });

        }
    }

    /**
     * 更新所有试图
     */
    protected void updateViews() {
        if (dish != null) {
            setTitle(dish.getTitle());
            updateHead();
            updateBottomBar();
            updateDishBrief();
            updateDishTabInfo();
        }
    }

    /**
     * 更新底部数据
     */
    private void updateBottomBar() {
        if (dish != null) {
            //是否正常
            if (ShopCartDish.STATE_NORMAL.equals(dish.getState())) {
                mAddtoShopcartView.setEnabled(ShopCartDish.STATE_NORMAL.equals(dish.getState()));
                mAddtoShopcartView.setText("加入购物车");
            } else {
                mAddtoShopcartView.setEnabled(ShopCartDish.STATE_NORMAL.equals(dish.getState()));
                mAddtoShopcartView.setText("已售馨");
            }

            mIntoShopcartView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogGather.onEventDishDetailGoShopcart();
                    next(DishShopCartFragment.class);
                }
            });

            mIntoRestaurantView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoRestaurant();

                }
            });

            updateShopcartNewMark();

            mAddtoShopcartView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogGather.onEventDishDetailAddShopCart();
                    DishPolicy.get().addDishToShopCart(dish, new DishPolicy.OnShopCartUpdateListener() {
                        @Override
                        public void onResult(boolean success, String info) {
                            if (success) {
                                ShoppingAnimationUtils.setAnim(getActivity(), mAddtoShopcartView, mIntoShopcartIconView);
                            } else {
                                ToastAlarm.show(info);
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * 进入商铺
     */
    private void gotoRestaurant() {
        //进入餐厅
        LogGather.onEventDishDetailGoRestaurantFromBottom();
        Bundle bundle = new Bundle();
        bundle.putString(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, dish.getRestaurantId());
        next(DishRestaurantDetailFragment.class, bundle);
    }

    /**
     * 更新购物数据
     */
    private void updateShopcartNewMark() {
        if (mIntoShopcartViewNewMark != null) {
            int count = DishPolicy.get().getShopCartCount();
            if (count > 0) {
                mIntoShopcartViewNewMark.setVisibility(View.VISIBLE);
            } else {
                mIntoShopcartViewNewMark.setVisibility(View.GONE);
            }
            if (count > 99) {
                mIntoShopcartViewNewMark.setText("99+");
            } else {
                mIntoShopcartViewNewMark.setText("" + count);
            }
        }
    }

    private void updateHead() {
        if (mCoversAdapter == null) {
            mCoversAdapter = new ImagePageAdapter();
            mViewPager.setAdapter(mCoversAdapter);
        } else {
            mCoversAdapter.notifyDataSetChanged();
        }
        mIndicator.setViewPager(mViewPager);
        mIndicator.setPageCount(mCoversAdapter.getCount());

    }

    /**
     * 更新菜品简介
     */
    private void updateDishBrief() {
        if (null == getActivity()) return;
        mDishBriefView = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_detail_brief, null);

        TextView dishName = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_name);
        TextView dishSaleInMonth = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_sale);
        TextView dishEvaluate = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_evaluate);
        TextView dishDesc = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_desc);
        TextView dishPrice = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_price);
        TextView dishPriceNormal = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_price_normal);
        View dishPropertiesLayout = mDishBriefView.findViewById(R.id.app_dish_detail_properties);
        View dishPropertiesDiv = mDishBriefView.findViewById(R.id.app_dish_detail_properties_div_line);

        RatingBar dishDifficultyStar = (RatingBar) mDishBriefView.findViewById(R.id.app_dish_detail_difficulty_star);
        TextView dishCookTime = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_cook_time);
        //口味
        TextView dishFlavour = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_flavour);
        //优惠
        View dishPreferentialGroup = mDishBriefView.findViewById(R.id.app_dish_detail_preferential_layout);
        View dishPreferentialLayoutDivLine = mDishBriefView.findViewById(R.id.app_dish_detail_preferential_div_line);
        LinearLayout dishPreferentialLayout = (LinearLayout) mDishBriefView.findViewById(R.id.app_dish_detail_preferential_list);
        //配送信息
        LinearLayout dishDeliveryLayout = (LinearLayout) mDishBriefView.findViewById(R.id.app_dish_detail_delivery_list);
        TextView dishDelivery = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_delivery_label);
        View dishDeliveryDivLine = mDishBriefView.findViewById(R.id.app_dish_detail_delivery_div_line);
        //保障
        View securityGroup = mDishBriefView.findViewById(R.id.app_dish_detail_security_layout);
        LinearLayout securityLayout = (LinearLayout) mDishBriefView.findViewById(R.id.app_dish_detail_security_list);

        View spaceView = mDishBriefView.findViewById(R.id.app_dish_detail_space);
        //查看全部评价
        View mScroll2Comments = mDishBriefView.findViewById(R.id.app_dish_detail_scroll2comment);
        TextView mScroll2CommentsText = (TextView) mDishBriefView.findViewById(R.id.app_dish_detail_scroll2Comment_text);
        View mScroll2CommentsLine = mDishBriefView.findViewById(R.id.app_dish_detail_scroll2Comment_line);
        if (mCommentCount != null) {
            mScroll2CommentsText.setText("查看全部" + mCommentCount.getAll() + "条评价");
            if ("0".equals(mCommentCount.getAll())) {
                mScroll2CommentsLine.setVisibility(View.GONE);
                mScroll2Comments.setVisibility(View.GONE);
            } else {
                mScroll2CommentsLine.setVisibility(View.VISIBLE);
                mScroll2Comments.setVisibility(View.VISIBLE);
            }
            mScroll2Comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //跳转到菜品评价页面；
                    Bundle bundle = new Bundle();
                    bundle.putString(DishRestaurantCommentFragment.EXTRA_DISH_ID, dish.getDishId());
                    next(DishRestaurantCommentFragment.class, bundle);
                }
            });
            dishEvaluate.setText("评价" + mCommentCount.getAll());
        }
        if (dish != null) {
            dishName.setText(dish.getTitle());
            dishSaleInMonth.setText("月售" + dish.getSaleCount());

            if (!StringUtils.isEmpty(dish.getDescription())) {
                dishDesc.setVisibility(View.VISIBLE);
                dishDesc.setText(dish.getDescription());
            } else {
                dishDesc.setVisibility(View.GONE);
            }

            dishPrice.setText(dish.getPrice());
            if (!StringUtils.isEmpty(dish.getPriceNormal())) {
                dishPriceNormal.setVisibility(View.VISIBLE);
                dishPriceNormal.setText(dish.getPriceNormal());
                dishPriceNormal.getPaint().setAntiAlias(true);
                dishPriceNormal.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                dishPriceNormal.setVisibility(View.GONE);
            }

            if (dish.getType().equals(Dish.TYPE_RESTAURANT + "")) {
                dishPropertiesLayout.setVisibility(View.GONE);
                dishPropertiesDiv.setVisibility(View.GONE);
            } else {
                dishPropertiesLayout.setVisibility(View.VISIBLE);
                dishPropertiesDiv.setVisibility(View.VISIBLE);
                dishDifficultyStar.setRating(StringUtils.parseFloat(dish.getDifficulty()));
                if (StringUtils.isEmpty(dish.getSpend())) {

                    dishCookTime.setText("时间：- -");
                } else {

                    dishCookTime.setText("时间：" + dish.getSpend() + "分");
                }
                if (StringUtils.isEmpty(dish.getFlavour())) {
                    dishFlavour.setText("口味：- -");
                } else {
                    dishFlavour.setText("口味：" + dish.getFlavour());
                }
            }
            boolean isShowSpace = false;
            Restaurant restaurant = dish.getRestaurant();
            if (restaurant != null) {
                //设置优惠item
                List<Tags> preferential = restaurant.getPerferentials();
                if (preferential == null || preferential.isEmpty()) {
                    dishPreferentialGroup.setVisibility(View.GONE);
                    dishPreferentialLayoutDivLine.setVisibility(View.GONE);
                } else {
                    dishPreferentialGroup.setVisibility(View.VISIBLE);
                    dishPreferentialLayoutDivLine.setVisibility(View.VISIBLE);
                    isShowSpace = true;
                    for (Tags tags : preferential) {
                        createTagItem(tags, dishPreferentialLayout, false, 18, 15, 0);
                    }
                }
                //设置配送信息
                if (restaurant.getDeliveryTags() != null) {
                    List<Tags> deliveryTags = restaurant.getDeliveryTags();
                    for (int i = 0; i < deliveryTags.size(); i++) {
                        if (i == 0) {
                            createTagItem(deliveryTags.get(i), dishDeliveryLayout, false, 39, 15, 0);
                        } else {
                            createTagItem(deliveryTags.get(i), dishDeliveryLayout, false, 39, 15, 10);
                        }
                    }
                    dishDeliveryLayout.setVisibility(View.VISIBLE);
                    dishDelivery.setVisibility(View.VISIBLE);
                    dishDeliveryDivLine.setVisibility(View.VISIBLE);
                    isShowSpace = true;
                } else {
                    dishDeliveryLayout.setVisibility(View.GONE);
                    dishDelivery.setVisibility(View.GONE);
                    dishDeliveryDivLine.setVisibility(View.GONE);
                }
                //设置保障信息
                if (dish.getSecurity() != null && dish.getSecurity().getList() != null && dish.getSecurity().getList().size() > 0) {
                    List<Security> securities = dish.getSecurity().getList();
                    if (securities == null || securities.isEmpty()) {
                        securityGroup.setVisibility(View.GONE);
                    } else {
                        isShowSpace = true;
                        securityGroup.setVisibility(View.VISIBLE);
                        for (Security security : securities) {
                            createSecurityInfo(security, securityLayout, true, 18, 19);
                        }
                    }
                } else {
                    securityGroup.setVisibility(View.GONE);
                }
            }
            if (isShowSpace) {
                spaceView.setVisibility(View.VISIBLE);
            } else {
                spaceView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 创建保障信息item
     *
     * @param security
     */
    private void createSecurityInfo(Security security, LinearLayout layout, boolean isShowGoTo, int width, int height) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_preferential_small, null);
        final ImageView icon = (ImageView) view.findViewById(R.id.app_dish_preferential_icon);
        TextView name = (TextView) view.findViewById(R.id.app_dish_preferential_name);
        final TextView content = (TextView) view.findViewById(R.id.app_dish_detail_preferential_content);
        final ImageView gotoIcon = (ImageView) view.findViewById(R.id.app_dish_preferential_goto);
        ScreenUtils.resizeViewWithSpecial(icon, ScreenUtils.dip2px(width), ScreenUtils.dip2px(height));
        if (!StringUtils.isEmpty(security.getContent())) {
            content.setText(security.getContent());
        }
        content.setVisibility(View.GONE);
        if (security.getIcon() != null && !StringUtils.isEmpty(security.getIcon())) {
            ImageFetcher.asInstance().load(security.getIcon(), icon);
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }
        SpannableStringBuilder contentText = new SpannableStringBuilder(security.getName() + security.getDesc());
        contentText.setSpan(new RelativeSizeSpan(0.7f), security.getName().length(), contentText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        name.setText(contentText);
        if (isShowGoTo) {
            gotoIcon.setVisibility(View.VISIBLE);
        } else {
            gotoIcon.setVisibility(View.GONE);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示或隐藏详情数据
                //显示详情
                if (content.getVisibility() == View.GONE) {
                    content.setVisibility(View.VISIBLE);
                    gotoIcon.setImageResource(R.drawable.app_bt_up);
                } else {
                    content.setVisibility(View.GONE);
                    gotoIcon.setImageResource(R.drawable.app_bt_down);

                }
            }
        });
        layout.addView(view);
    }

    /**
     * 创建优惠信息的一个条目
     *
     * @param tags
     * @param layout
     * @param topMargin 单位Dp
     */
    private void createTagItem(Tags tags, LinearLayout layout, boolean isShowMark, int width, int height, int topMargin) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_preferential_small, null);
        if (topMargin > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, ScreenUtils.dip2px(getContext(), topMargin), 0, 0);
            view.setLayoutParams(params);
        }
        ImageView icon = (ImageView) view.findViewById(R.id.app_dish_preferential_icon);
        TextView name = (TextView) view.findViewById(R.id.app_dish_preferential_name);
        ImageView gotoIcon = (ImageView) view.findViewById(R.id.app_dish_preferential_goto);
        ScreenUtils.resizeViewWithSpecial(icon, ScreenUtils.dip2px(width), ScreenUtils.dip2px(height));
        if (!StringUtils.isEmpty(tags.getIcon())) {
            icon.setVisibility(View.VISIBLE);
            ImageFetcher.asInstance().load(tags.getIcon(), icon);
        } else {

            icon.setVisibility(View.GONE);
        }
        ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
        name.setText(tags.getName());
        if (isShowMark) {
            gotoIcon.setVisibility(View.VISIBLE);
        } else {
            gotoIcon.setVisibility(View.GONE);
        }
        layout.addView(view);
    }

    /**
     * 更新菜品功能
     */
    private void updateDishTabInfo() {
        if (null == getContext()) return;

        mEmptyView = new EmptyView(getContext());
        mEmptyView.onFinishInflate();

        //基础信息
        buildBaseInfoView();

        if (dish.getType().equals(Dish.TYPE_RESTAURANT + "")) {
            //套餐
            //菜品特色
            buildFeatureGroup();

            //套餐菜品列表
            buildDishGroup();

            //评论列表
            buildEvaluate();

            //餐厅推荐菜品名片
            buildRestaurantDishCard();
        } else {
            //菜品特色
            buildFeatureGroup();

            //食材包
            buildIngredientGroup();

            //评论列表
            buildEvaluate();

            //做法步骤列表
            buildCookStepList();

            //做法小贴士
            buildCookTips();

            //餐厅推荐菜品名片
            buildRestaurantDishCard();

        }


        mListView.setAdapter(mListAdapter);


    }


    /**
     * 商店推荐菜品名片
     */
    private void buildRestaurantDishCard() {

        //加载商铺推荐布局
        restaurantRecommedLayout = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_detail_restaurant_card, null);

        //初始化商铺推荐view
        restaurantRecommedCard = (LinearLayout) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_restaurant_card);
        recommendLayout = (LinearLayout) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_recommend_layout);
        LinearLayout recommendMarkLayout = (LinearLayout) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_mark);
        ImageView restaurantIcon = (ImageView) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_icon);//商家图标
        TextView restaurantName = (TextView) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_name);//商家名称
        TextView gotoRestaurant = (TextView) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_goto_restaurant);//进入店铺
        TextView scoreTextView = (TextView) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_score);//评分
        TextView monthlySalesTextView = (TextView) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_monthly_sales);//月售
        TextView inSaleDishesTextView = (TextView) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_in_sale_dishes);//在售菜品数量
        cardMoreText = (TextView) restaurantRecommedLayout.findViewById(R.id.app_dish_detail_recommend_text);


        //设置卡片顶部空间再不同type下的间距
        if (dish.getType().equals(Dish.TYPE_RESTAURANT + "")) {
            ScreenUtils.reMargin(restaurantRecommedCard, ScreenUtils.dip2px(10), 0, ScreenUtils.dip2px(10), ScreenUtils.dip2px(10));
        } else {
            ScreenUtils.reMargin(restaurantRecommedCard, ScreenUtils.dip2px(10), ScreenUtils.dip2px(10), ScreenUtils.dip2px(10), ScreenUtils.dip2px(10));
        }


        //设置商铺推荐卡片及内容数据
        if (dish != null) {
            ImageFetcher.asInstance().load(dish.getRestaurant().getImage(), restaurantIcon);
            restaurantName.setText(dish.getRestaurantName());
            scoreTextView.setText(dish.getRestaurant().getGrade() + "\n评分");
            monthlySalesTextView.setText(dish.getRestaurant().getSale() + "\n月售");
            int dishCount = dish.getRestaurant().getDishes_num();
            inSaleDishesTextView.setText(dishCount + "\n在售菜品");
            gotoRestaurant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //进入商铺
                    gotoRestaurant();
                }
            });


            //设置推荐菜品内容
            if (dish.getRestaurant() != null && dish.getRestaurant().getDishes() != null) {
                updateRestaurantRecommedDish(recommendLayout, dish.getRestaurant().getDishes());
            }
            recommendLayout.setVisibility(View.GONE);
            if (dish.getRestaurant() != null && dish.getRestaurant().getPerferentials() != null) {

                //添加商铺优惠mark
                addRecommedMark(recommendMarkLayout, dish.getRestaurant().getPerferentials());
            }
        }


        mListAdapter.addView(restaurantRecommedLayout);


    }

    /**
     * 商家名牌 添加优惠标签
     *
     * @param recommendMarkLayout
     * @param perferentials
     */
    private void addRecommedMark(LinearLayout recommendMarkLayout, List<Tags> perferentials) {
        if (perferentials != null && recommendMarkLayout != null) {
            for (Tags tag : perferentials) {
                addMarkItem(tag, recommendMarkLayout);
            }
        }

    }

    /**
     * 添加itemmark
     *
     * @param tag
     * @param recommendMarkLayout
     */
    private void addMarkItem(Tags tag, LinearLayout recommendMarkLayout) {
        if (tag != null && recommendMarkLayout != null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.view_restaurant_mark, null);
            ImageView mark = (ImageView) view.findViewById(R.id.app_restaurant_mark);
            ImageFetcher.asInstance().load(tag.getIcon(), mark);
            recommendMarkLayout.addView(view);
        }

    }

    /**
     * 推荐菜品内容
     *
     * @param recommendLayout
     * @param dishes
     */
    private void updateRestaurantRecommedDish(LinearLayout recommendLayout, List<Dish> dishes) {
        View recommendLayout_one = recommendLayout.findViewById(R.id.app_dish_detail_recommend_1);
        View recommendLayout_two = recommendLayout.findViewById(R.id.app_dish_detail_recommend_2);

        View[] views = new View[]{recommendLayout_one, recommendLayout_two};
        for (int i = 0; dishes.size() - 1 - i >= 0 && i / 2 < views.length; i += 2) {
            setItemRecommedDish(views[i / 2], ListUtils.getItem(dishes, i), ListUtils.getItem(dishes, i + 1), i);
        }

    }

    /**
     * 设置推荐菜品item数据
     */
    private void setItemRecommedDish(View itemView, Dish dataLeft, Dish dataRight, int position) {
        View itemLeft = itemView.findViewById(R.id.app_list_item_left);
        View itemRight = itemView.findViewById(R.id.app_list_item_right);

        setItemRecommedLayout(itemLeft, itemRight);
        setItemRecommedData(position, -1, dataLeft, dataRight, null, itemLeft, itemRight);

    }

    private void setItemRecommedLayout(View itemLeft, View itemRight) {
        int itemViewWidth = (ScreenUtils.getScreenWidthInt() - 3 * ScreenUtils.dip2px(8)) / 2;

        ScreenUtils.resizeView(itemLeft.findViewById(R.id.app_dish_cover_group), itemViewWidth, 1f);
        ScreenUtils.resizeViewOfWidth(itemLeft.findViewById(R.id.app_dish_title_group), itemViewWidth);

        ScreenUtils.resizeView(itemRight.findViewById(R.id.app_dish_cover_group), itemViewWidth, 1f);
        ScreenUtils.resizeViewOfWidth(itemRight.findViewById(R.id.app_dish_title_group), itemViewWidth);

        itemLeft.findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);
        itemRight.findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);

        ScreenUtils.reMargin(itemLeft.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 75, true);
        ScreenUtils.reMargin(itemRight.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 75, true);

    }

    /**
     * 评论模块
     */
    private void buildEvaluate() {
        dishComments = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_detail_comment, null);
        ViewGroup commentLayout = (ViewGroup) dishComments.findViewById(R.id.app_dish_detail_comment_list_layout);
        LinearLayout commentMore = (LinearLayout) dishComments.findViewById(R.id.app_dish_detail_comment_more);
        TextView textMore = (TextView) dishComments.findViewById(R.id.app_dish_detail_comment_more_text);
        //显示评论数据
        if (mComments != null && mComments.size() > 0) {
            //显示查看更多
            commentMore.setVisibility(View.VISIBLE);
            textMore.setText("查看全部评价");
            commentMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转到评论详情
                    Bundle bundle = new Bundle();
                    bundle.putString(DishRestaurantCommentFragment.EXTRA_DISH_ID, dish.getDishId());
                    next(DishRestaurantCommentFragment.class, bundle);
                }
            });
            for (int i = 0; i < mComments.size() && i < 3; i++) {
                setCommentData(commentLayout, mComments.get(i));
            }
            mListAdapter.addView(dishComments);
        }
//        else {
//            textMore.setText("暂无评论");
//            textMore.setCompoundDrawables(null, null, null, null);
//        }
    }


    /**
     * 设置评论模块数据
     */
    private void setCommentData(ViewGroup viewGroup, Comment data) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_comment_dish, null);
        ImageView userAvatar = (ImageView) view.findViewById(R.id.app_comment_avatar);
        TextView userName = (TextView) view.findViewById(R.id.app_comment_name);
        TextView content = (TextView) view.findViewById(R.id.app_comment_content);
        TextView time = (TextView) view.findViewById(R.id.app_comment_time);
        RatingBar star = (RatingBar) view.findViewById(R.id.app_comment_star);
        LinearLayout replayLayout = (LinearLayout) view.findViewById(R.id.app_comment_reply_layout);//回复
        TextView replayName = (TextView) replayLayout.findViewById(R.id.app_comment_reply_name);
        TextView replayContent = (TextView) replayLayout.findViewById(R.id.app_comment_reply_content);
        HorizontalScrollView pictrueLayout = (HorizontalScrollView) view.findViewById(R.id.app_comment_picture_layout);
        PictureHelpView pictrueView = (PictureHelpView) view.findViewById(R.id.app_comment_pictureview);
        //图片相关
        if (data.getImageData() == null) {
            ArrayList<Image> images = new ArrayList<>();
            if (data.getImages() != null && data.getImages_origin() != null && data.getImages().size() > 0 && data.getImages_origin().size() > 0) {
                for (int i = 0; i < data.getImages().size() && i < data.getImages_origin().size(); i++) {
                    Image image = new Image();
                    image.setImage(data.getImages().get(i));
                    image.setImage_origin(data.getImages_origin().get(i));
                    images.add(image);
                }

            }
            data.setImageData(images);
        }
        if (data.getImageData() != null && data.getImageData().size() > 0) {
            pictrueLayout.setVisibility(View.VISIBLE);
            pictrueView.setDataAndIsUpdatePicture(data.getImageData(), false);
            pictrueView.setPictureHelpCallBack(new PictureHelpView.PictureHelpCallBack() {
                @Override
                public void callback(int position, View parent, List<Image> data, boolean isUpdatePicture) {
                    if (!isUpdatePicture) {
                        // 跳转查看图片详情
                        Intent intent = new Intent(getContext(), MultiPictureActivity.class);
                        intent.putExtra(MultiPictureActivity.EXTRA_IMAGES, (Serializable) data);
                        intent.putExtra(MultiPictureActivity.EXTRA_FIRST_POS, position);
                        intent.putExtra(MultiPictureActivity.EXTRA_IS_SHOW_TITLE, false);
                        startActivity(intent);
                    }
                }
            });
        } else {
            pictrueLayout.setVisibility(View.GONE);
        }
        if (null != data.getReplyList() && null != data.getReplyList().getList() && data.getReplyList().getList().size() > 0) {
            //2.5.2暂定显示一条回复
            Comment replay = data.getReplyList().getList().get(0);
            String replayAuthorName = replay.getAuthor().getNickname();
            if (!replayAuthorName.contains("回复")) {
                replayAuthorName += "回复";
            }
            replayAuthorName += ":";
            replayName.setText(replayAuthorName);

            String replayContentStr = replay.getContent();
            replayContent.setText(replayContentStr);
            replayName.setVisibility(View.VISIBLE);
            replayContent.setVisibility(View.VISIBLE);
            replayLayout.setVisibility(View.VISIBLE);
        } else {
            replayLayout.setVisibility(View.GONE);
            replayName.setVisibility(View.GONE);
            replayContent.setVisibility(View.GONE);
        }
        if (data.getAuthor() != null) {
            final User author = data.getAuthor();
            userAvatar.setVisibility(View.VISIBLE);
            ImageFetcher.asInstance().load(author.getAvatar(), userAvatar, R.drawable.app_pic_default_avatar);
            userName.setText(author.getNickname());
            userAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(UserPageFragment.EXTRA_USER, author);
                    next(UserPageFragment.class, bundle);
                }
            });
        } else {
            userAvatar.setVisibility(View.GONE);
        }

        content.setText(data.getContent());
        time.setText(data.getCreateTime());
        star.setRating(StringUtils.parseFloat(data.getScore()));
        viewGroup.addView(view);
    }


    private void buildRestaurantInfoGroup() {
        if (mDishRestaurantInfoView == null) {
            mDishRestaurantInfoView = LayoutInflater.from(getContext()).inflate(
                    R.layout.view_dish_detail_restaurant_info, null);
            mListAdapter.addView(mDishRestaurantInfoView);
        }

        ImageView image = (ImageView) mDishRestaurantInfoView.findViewById(R.id.app_dish_detail_restaurant_image);
        TextView title = (TextView) mDishRestaurantInfoView.findViewById(R.id.app_dish_detail_restaurant_title);
        TextView subTitle = (TextView) mDishRestaurantInfoView.findViewById(R.id.app_dish_detail_restaurant_sub_title);
        TextView desc = (TextView) mDishRestaurantInfoView.findViewById(R.id.app_dish_detail_restaurant_desc);
        ViewGroup tags = (ViewGroup) mDishRestaurantInfoView.findViewById(R.id.app_dish_detail_restaurant_tags);
        View restaurantInfo = mDishRestaurantInfoView.findViewById(R.id.app_dish_detail_restaurant_info_layout);
        View restaurantDishMore = mDishRestaurantInfoView.findViewById(R.id.app_dish_detail_restaurant_dish_more);

        final Restaurant data = dish.getRestaurant();
        ImageFetcher.asInstance().load(data.getImage(), image);
        title.setText(data.getName());
        subTitle.setText(String.format("评价%s | 月销量%s", data.getGrade(), data.getSale()));
        desc.setText(data.getTip());
        restaurantInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.onEventDishDetailGoRestaurantFromInfo();
                Bundle bundle = new Bundle();
                bundle.putString(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, data.getId());
                next(DishRestaurantDetailFragment.class, bundle);
            }
        });
        createTagView(tags, data.getPerferentials());

        View dishGroup = mDishRestaurantInfoView.findViewById(R.id.app_dish_detail_restaurant_dish_group);
        if (data.getDishes() != null && !data.getDishes().isEmpty()) {
            dishGroup.setVisibility(View.VISIBLE);
            int[] viewIds = {R.id.app_dish_detail_restaurant_dish_1,
                    R.id.app_dish_detail_restaurant_dish_2,
                    R.id.app_dish_detail_restaurant_dish_3};
            for (int i = 0; i < viewIds.length; i++) {
                Dish dish = ListUtils.getItem(data.getDishes(), i);
                View view = mDishRestaurantInfoView.findViewById(viewIds[i]);
                view.setBackgroundColor(Color.rgb(230, 230, 230));
                view.setPadding(2, 2, 2, 2);
                if (dish != null) {
                    int imageWidth = ScreenUtils.getScreenWidthInt() - 2 * ScreenUtils.dip2px(15);
                    ScreenUtils.resizeView(view.findViewById(R.id.app_dish_cover_group), imageWidth, 9 / 16f);
                    view.setVisibility(View.VISIBLE);
                    DishAdapter.updateItem(this, view, true, true, 0, 0, dish, null);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
            restaurantDishMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogGather.onEventDishDetailMore();
                    Bundle bundle = new Bundle();
                    bundle.putString(DishRestaurantDetailFragment.EXTRA_RESTAURANT_ID, data.getId());
                    next(DishRestaurantDetailFragment.class, bundle);
                }
            });

        } else {
            dishGroup.setVisibility(View.GONE);
        }

    }

    private void createTagView(ViewGroup tags, List<Tags> dataTags) {
        if (tags != null) {
            tags.removeAllViews();
            if (dataTags != null && !dataTags.isEmpty()) {
                for (Tags tag : dataTags) {
                    ImageView imageView = new ImageView(getContext());
                    tags.addView(imageView);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    ScreenUtils.resizeView(imageView, 15, 1);
                    ScreenUtils.reMargin(imageView, 5, 0, 0, 0);
                    ImageFetcher.asInstance().load(tag.getIcon(), imageView);
                }
            }
        }
    }

    /**
     * 菜品特色
     */
    protected void buildFeatureGroup() {
        if (mDishFeatureInfoView == null) {
            mDishFeatureInfoView = LayoutInflater.from(getContext()).inflate(
                    R.layout.view_dish_detail_feature_info, null);
            mListAdapter.addView(mDishFeatureInfoView);
        }
        if (dish.getDishFeatures() != null && !dish.getDishFeatures().isEmpty()) {

            if (dish.getType().equals(Dish.TYPE_RESTAURANT + "")) {
                TextView featureLabel = (TextView) mDishFeatureInfoView.findViewById(R.id.app_dish_detail_feature_label);
                featureLabel.setText("套餐特色");
            }

            final View main = mDishFeatureInfoView.findViewById(R.id.app_dish_detail_feature_main);
            if (dish.getDishFeatures() != null && !dish.getDishFeatures().isEmpty()) {
                DishFeature dishFeature = dish.getDishFeatures().getItem(0);
                updateDishFeatureItem(main, dishFeature);

                if (mDishFeatureAdapter == null) {
                    mDishFeatureAdapter = new DishFeatureAdapter(getContext(), dish.getDishFeatures().getList());
                    mListAdapter.addAdapter(mDishFeatureAdapter);
                }
                main.setVisibility(View.VISIBLE);
                mListAdapter.setActive(mDishFeatureAdapter, false);
            }

            if (mDishFeatureInfoMoreView == null) {
                mDishFeatureInfoMoreView = LayoutInflater.from(getContext()).inflate(
                        R.layout.view_dish_detail_feature_info_more, null);
                mListAdapter.addView(mDishFeatureInfoMoreView);

            }

            View more = mDishFeatureInfoMoreView.findViewById(R.id.app_dish_detail_feature_more);
            final View arrow = mDishFeatureInfoMoreView.findViewById(R.id.app_dish_detail_feature_arrow);
            final TextView text = (TextView) mDishFeatureInfoMoreView.findViewById(R.id.app_dish_detail_feature_more_text);

            if (dish.getDishFeatures() != null && !dish.getDishFeatures().isEmpty()) {
                if (dish.getDishFeatures().getList().size() > 1) {
                    more.setVisibility(View.VISIBLE);
                    ViewCompat.setRotation(arrow, 90);
                    text.setText("点击展开全部");
                    more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean mainVisible = main.getVisibility() == View.VISIBLE;
                            if (mainVisible) {
                                isShowFeature = true;
                                ViewCompat.setRotation(arrow, -90);
                                main.setVisibility(View.GONE);
                                text.setText("点击收起");
                                mListAdapter.setActive(mDishFeatureAdapter, true);
                            } else {
                                isShowFeature = false;
                                ViewCompat.setRotation(arrow, 90);
                                main.setVisibility(View.VISIBLE);
                                text.setText("点击展开全部");
                                mListAdapter.setActive(mDishFeatureAdapter, false);
                            }
                        }
                    });
                } else {
                    more.setVisibility(View.GONE);
                }
            }
            mListAdapter.pendingActive(mDishFeatureInfoView, true);
        } else {

            mListAdapter.pendingActive(mDishFeatureInfoView, false);
            mListAdapter.pendingActive(mDishFeatureAdapter, false);
            mListAdapter.pendingActive(mDishFeatureInfoMoreView, false);
            if (!hideViews.contains(mDishFeatureAdapter)) hideViews.add(mDishFeatureAdapter);
            if (!hideViews.contains(mDishFeatureInfoView)) hideViews.add(mDishFeatureInfoView);
            if (!hideViews.contains(mDishFeatureInfoMoreView))
                hideViews.add(mDishFeatureInfoMoreView);
        }
    }

    private void buildDishGroup() {
        mDishGroupInfoView = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_detail_group_info, null);
        intervalView = LayoutInflater.from(getContext()).inflate(R.layout.view_interval, null);
        mListAdapter.addView(mDishGroupInfoView);

        if (dish.getDishGroupItems() != null) {
            mDishGroupAdapter = new DishGroupItemAdapter(getContext(), dish.getDishGroupItems().getList());
            mListAdapter.addAdapter(mDishGroupAdapter);
            mListAdapter.addView(intervalView);
        }
    }

    /**
     *
     */
    private void buildIngredientGroup() {
        if (mDishIngredientInfoView == null) {
            mDishIngredientInfoView = LayoutInflater.from(getContext()).inflate(
                    R.layout.view_dish_detail_ingredient_info, null);
            mListAdapter.addView(mDishIngredientInfoView);
        }
        //食材来源
        View ingredientSource = mDishIngredientInfoView.findViewById(R.id.app_order_detail_ingredient_source);

        if (StringUtils.isEmpty(dish.getDishSourceUrl())) {
            ingredientSource.setVisibility(View.GONE);
        } else {
            ingredientSource.setVisibility(View.VISIBLE);
            ingredientSource.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到餐厅介绍
                    Bundle bundle = new Bundle();
                    bundle.putString(WebViewFragment.EXTRA_URL, dish.getDishSourceUrl());
                    startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                }
            });
        }


        //主料
        View ingredientGroup = mDishIngredientInfoView.findViewById(R.id.app_order_detail_ingredient_group);
        LinearLayout ingredientList = (LinearLayout) mDishIngredientInfoView.findViewById(R.id.app_order_detail_ingredient_list);
        if (dish.getDishIngredients() != null && !dish.getDishIngredients().isEmpty()) {
            ingredientGroup.setVisibility(View.VISIBLE);
            List<FoodIngredient> ingredients = dish.getDishIngredients().getList();
            ApiModelGroup<FoodIngredient> group = new ApiModelGroup<>(2);
            List<ApiModelGroup<FoodIngredient>> groups = group.loadChildrenFromList(ingredients);
            ingredientList.removeAllViews();
            for (ApiModelGroup<FoodIngredient> item : groups) {
                createIngredientView(item, ingredientList);
            }
        } else {
            ingredientGroup.setVisibility(View.GONE);
        }
        //辅料
        View assistGroup = mDishIngredientInfoView.findViewById(R.id.app_order_detail_assist_group);
        TextView assists = (TextView) mDishIngredientInfoView.findViewById(R.id.app_order_detail_assists);
        if (dish.getDishAssistText() != null && !StringUtils.isEmpty(dish.getDishAssistText())) {
            assistGroup.setVisibility(View.VISIBLE);
            assists.setText(dish.getDishAssistText());

        } else {
            assistGroup.setVisibility(View.GONE);
        }

        //自备
        View selfPrepareGroup = mDishIngredientInfoView.findViewById(R.id.app_order_detail_self_prepare_group);
        TextView selfPrepare = (TextView) mDishIngredientInfoView.findViewById(R.id.app_order_detail_self_prepare);
        if (!StringUtils.isEmpty(dish.getPrepare())) {
            selfPrepareGroup.setVisibility(View.VISIBLE);
            selfPrepare.setText(dish.getPrepare());
        } else {
            selfPrepareGroup.setVisibility(View.GONE);
        }

    }

    private void updateDishFeatureItem(View main, DishFeature dishFeature) {
        ImageView image = (ImageView) main.findViewById(R.id.app_view_image);
        TextView text = (TextView) main.findViewById(R.id.app_view_text);
        if (!StringUtils.isEmpty(dishFeature.getImage())) {
            image.setVisibility(View.VISIBLE);
            int imageWidth = ScreenUtils.getScreenWidthInt() - 2 * ScreenUtils.dip2px(15);
            int imageHeight = imageWidth * 9 / 16;
            ScreenUtils.resizeViewOfHeight(image, imageHeight);
            ImageFetcher.asInstance().load(dishFeature.getImage(), image);
        } else {
            image.setVisibility(View.GONE);
        }

        if (!StringUtils.isEmpty(dishFeature.getText())) {
            text.setVisibility(View.VISIBLE);
            text.setText(dishFeature.getText().replace("", "").replace("【", "—").replace("】", "—").replace("—\r\n\r\n", "—\r\n"));
        } else {
            text.setVisibility(View.GONE);
        }
    }

    private void createIngredientView(ApiModelGroup<FoodIngredient> ingredient, LinearLayout list) {
        if (ingredient != null) {
            FoodIngredient left = ingredient.getItem(0);
            FoodIngredient right = ingredient.getItem(1);
            View itemGroupView = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_detail_ingredient_item_group, null);
            View leftView = itemGroupView.findViewById(R.id.app_dish_detail_ingredient_item_left);
            View rightView = itemGroupView.findViewById(R.id.app_dish_detail_ingredient_item_right);
            leftView.setVisibility(left != null ? View.VISIBLE : View.GONE);
            rightView.setVisibility(right != null ? View.VISIBLE : View.GONE);
            list.addView(itemGroupView);
            updateIngredientItem(left, leftView);
            updateIngredientItem(right, rightView);
        }
    }

    private void updateIngredientItem(FoodIngredient ingredient, View view) {
        if (ingredient != null) {
            TextView name = (TextView) view.findViewById(R.id.app_ingredient_name);
            TextView quality = (TextView) view.findViewById(R.id.app_ingredient_quality);
            name.setText(ingredient.getName());
            quality.setText(ingredient.getQuality());
        }
    }


    private void buildCookTips() {
        if (mCookTipsView == null) {
            mCookTipsView = LayoutInflater.from(getContext()).inflate(
                    R.layout.view_dish_detail_cook_tip, null);

            mListAdapter.addView(mCookTipsView);
        }

        if (!StringUtils.isEmpty(dish.getTips())) {
            isShowCookTips = true;
            mListAdapter.setActive(mCookTipsView, true);
            TextView tips = (TextView) mCookTipsView.findViewById(R.id.app_dish_detail_cook_tips);
            tips.setText(dish.getTips());
        } else {
            isShowCookTips = false;
            mListAdapter.setActive(mCookTipsView, false);
        }
    }

    private void buildCookStepList() {
        //烹饪head
        cookStepHead = LayoutInflater.from(getContext()).inflate(
                R.layout.view_dish_detail_step, null);
        mListAdapter.addView(cookStepHead);
        //烹饪步骤内容
        if (mCookStepAdapter == null && dish.getDishSteps() != null) {
            mCookStepAdapter = new FoodStepAdapter(getContext(), dish.getDishSteps().getList());
            mListAdapter.addAdapter(mCookStepAdapter);
        }
    }

    private void buildBaseInfoView() {
        mListAdapter = new MergeAdapter();
        mListAdapter.addView(mHeaderView);
        mListAdapter.addView(mDishBriefView);
        mListAdapter.addView(mEmptyView);
        mListAdapter.setActive(mEmptyView, false);
    }

    /**
     * 设置推荐菜品item数据
     *
     * @param position
     * @param viewType
     * @param dataLeft
     * @param dataRight
     * @param extra
     * @param itemLeft
     * @param itemRight
     */
    private void setItemRecommedData(int position, int viewType, Dish dataLeft, Dish dataRight, Bundle extra, View itemLeft, View itemRight) {
        if (dataLeft != null) {
            itemLeft.setVisibility(View.VISIBLE);
            DishAdapter.updateItem(DishDetailFragment.this, itemLeft, false, position, viewType, dataLeft, extra);
            ViewGroup descGroup = (ViewGroup) itemLeft.findViewById(R.id.app_dish_desc_group);
            if (descGroup != null) {
                descGroup.setVisibility(View.GONE);
            }
            FrameLayout leftBtnShopLayout = (FrameLayout) itemLeft.findViewById(R.id.app_dish_shop_btn_layout);
            setAddShoppingAnimation(leftBtnShopLayout, mIntoShopcartIconView, dataLeft);
        } else {
            itemLeft.setVisibility(View.INVISIBLE);
            ViewGroup descGroup = (ViewGroup) itemLeft.findViewById(R.id.app_dish_desc_group);
            if (descGroup != null) {
                descGroup.setVisibility(View.GONE);
            }
        }

        if (dataRight != null) {
            itemRight.setVisibility(View.VISIBLE);
            DishAdapter.updateItem(DishDetailFragment.this, itemRight, false, position, viewType, dataRight, extra);
            ViewGroup descGroup = (ViewGroup) itemRight.findViewById(R.id.app_dish_desc_group);
            if (descGroup != null) {
                descGroup.setVisibility(View.GONE);
            }
            FrameLayout rightBtnShopLayout = (FrameLayout) itemRight.findViewById(R.id.app_dish_shop_btn_layout);
            setAddShoppingAnimation(rightBtnShopLayout, mIntoShopcartIconView, dataRight);
        } else {
            itemRight.setVisibility(View.INVISIBLE);
            ViewGroup descGroup = (ViewGroup) itemRight.findViewById(R.id.app_dish_desc_group);
            if (descGroup != null) {
                descGroup.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 添加动画
     *
     * @param data
     */
    private void setAddShoppingAnimation(final View startView, final View endView, final Dish data) {
        startView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Dish.STATE_OFF_SALE + "").equals(data.getState())) {
                    ToastAlarm.show("菜品已售罄");
                } else if ((Dish.STATE_ON_SALE + "").equals(data.getState())) {
                    //加入购物车
                    DishPolicy.get().addDishToShopCart(data, new DishPolicy.OnShopCartUpdateListener() {
                        @Override
                        public void onResult(boolean success, String info) {
                            if (success) {
                                ShoppingAnimationUtils.setAnim(getActivity(), startView, endView);
                            } else {
                                ToastAlarm.show(info);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 头图适配
     */
    private class ImagePageAdapter extends PagerAdapter {

        private ApiModelList<Image> coversList;

        public ImagePageAdapter() {
            coversList = dish.getDishCovers();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.view_banner, null);
            ScreenUtils.resizeViewOnScreen(view, 1);
            ImageView imageView = (ImageView) view.findViewById(R.id.app_banner_image);
            if (coversList != null) {
                List<Image> covers = coversList.getList();
                Image cover = ListUtils.getItem(covers, position);
                if (cover != null) {
                    ImageFetcher.asInstance().load(cover.getUrl(), imageView);
                }

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 跳转查看图片详情
                        Intent intent = new Intent(getContext(), MultiPictureActivity.class);
                        ArrayList<Image> images = new ArrayList<Image>();
                        images.addAll(dish.getDishOriginCovers().getList());
                        intent.putExtra(MultiPictureActivity.EXTRA_IMAGES, images);
                        intent.putExtra(MultiPictureActivity.EXTRA_FIRST_POS, position);
                        intent.putExtra(MultiPictureActivity.EXTRA_IS_SHOW_TITLE, false);
                        startActivity(intent);
                    }
                });
            }
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            if (coversList != null) {
                return coversList.getCountOfList();
            }

            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    /**
     * 套餐包含的菜品列表
     */
    private class DishGroupItemAdapter extends UIAdapter<Dish> {

        public DishGroupItemAdapter(Context context, List<Dish> data) {
            super(context, R.layout.listview_item_dish_detail_group, data);
        }

        @Override
        public void updateView(int position, int viewType, final Dish data, Bundle extra) {
            super.updateView(position, viewType, data, extra);

            ImageView image = (ImageView) findViewById(R.id.app_dish_detail_group_image);
            TextView title = (TextView) findViewById(R.id.app_dish_detail_group_title);
            TextView price = (TextView) findViewById(R.id.app_dish_detail_group_price);

            ImageFetcher.asInstance().load(data.getImage(), image);
            title.setText(data.getTitle());
            SpannableStringBuilder priceText = new SpannableStringBuilder("单价 ");
            priceText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uikit_font_grep)), 0, priceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            priceText.append(StringUtils.getPriceText(data.getRawPrice()));
            price.setText(priceText);

            getItemView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogGather.onEventDishDetailGroupDish();
                    Bundle bundle = new Bundle();
                    bundle.putString(DishDetailFragment.EXTRA_DISH_ID, data.getDishId());
                    bundle.putString(DishDetailFragment.EXTRA_TITLE, data.getTitle());
                    next(DishDetailFragment.class, bundle);
                }
            });
        }


    }

    /**
     * 特色菜品adapter
     */
    private class DishFeatureAdapter extends UIAdapter<DishFeature> {

        public DishFeatureAdapter(Context context, List<DishFeature> data) {
            super(context, R.layout.view_text_image_layout, data);
        }

        @Override
        protected View newView(int viewType) {
            View view = super.newView(viewType);
            ScreenUtils.rePadding(view, 15, 0, 15, 7);
            return view;
        }

        @Override
        public void updateView(int position, int viewType, DishFeature data, Bundle extra) {
            super.updateView(position, viewType, data, extra);
            View line = getItemView().findViewById(R.id.app_view_line);
            line.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
            updateDishFeatureItem(getItemView(), data);
        }
    }
}