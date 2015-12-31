package cn.wecook.app.main.dish.restaurant;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.ListUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.legacy.RestaurantApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Dish;
import com.wecook.sdk.api.model.Notice;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.api.model.Tags;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.zoom.PullToZoomListView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.DishAdapter;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.dialog.NoticeListDialog;
import cn.wecook.app.fragment.shopcard.BaseTitleShopCardFragment;
import cn.wecook.app.main.dish.DishCategoryView;
import cn.wecook.app.main.dish.DishOrderByView;
import cn.wecook.app.main.dish.list.DishHotSaleListFragment;
import cn.wecook.app.utils.ShoppingAnimationUtils;

/**
 * 餐厅详情页面
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/8
 */
public class DishRestaurantDetailFragment extends BaseTitleShopCardFragment {

    private static final String LOOP_PREFERENTIAL_NOTICE = "loop_preferential_notice";
    public static String EXTRA_RESTAURANT_ID = "extra_restaurant_id";
    public static String EXTRA_RESTAURANT = "extra_restaurant";
    private int defaultAnimationTime = 500;
    /**
     * 默认数据
     */
    private String default_dish_mode = DishDetailAdapter.DISH_MODE_DOUBLE;

    private MergeAdapter mAdapter;
    private PullToZoomListView mPullToZoomListView;
    private View mHeadView;
    private ImageView mHeadImageView;
    private ImageView mRestaurantImageView;
    private TextView mScoreCountView;
    private TextView mScoreDescView;
    private TextView mSaleCountView;
    private FrameLayout mNoticeView;

    private LinearLayout mPreferentialViewGroup;

    private View mRestaurantNoticeView;
    private TextView mRestaurantNoticeContentView;

    private DishCategoryView mTitleBarCategoryLayout;//TitleBar下面的分类视图
    private DishCategoryView mCategoryLayout;//列表中间的分类视图

    private DishDetailAdapter mDishAdapter;
    private List<Dish> mDishListData = new ArrayList<Dish>();
    private String mRestaurantId;
    private Restaurant mRestaurant;
    private EmptyView emptyView;

    private LoadMoreImpl<List<Dish>> mDishLoadMore;

    private List<ValueAnimator> mAnimators = new ArrayList<ValueAnimator>();
    private long mAnimatorDuration;
    private int mLoopTime;
    private Notice mRestaurantNotice;
    private boolean firstSet = true;

    private AnimatorSet mLoopAnimator;
    private LoadingDialog mLoadingDialog;
    private UIHandler.LoopRunnable runnable = new UIHandler.LoopRunnable() {
        @Override
        public void loopEnd() {
        }

        @Override
        public void run() {
            if (null == mLoopAnimator) {
                return;
            }
            mLoopAnimator.start();
        }
    };


    /**
     * 正在加载页面的TAG
     */
    private boolean mIsRequesting;
    private View view;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setUseShopCartPolice(true);
        mLoadingDialog = new LoadingDialog(activity);
        mLoadingDialog.cancelable(false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mRestaurantId = bundle.getString(EXTRA_RESTAURANT_ID);
            mRestaurant = (Restaurant) bundle.getSerializable(EXTRA_RESTAURANT);
            if (mRestaurant != null) {
                mRestaurantId = mRestaurant.getId();
            }
        }

        mAdapter = new MergeAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_restaurant_detail, null);

        mPullToZoomListView = (PullToZoomListView) view.findViewById(R.id.app_restaurant_detail_list);
        mPullToZoomListView.setHeaderViewSize(ScreenUtils.getScreenWidthInt(),
                (int) (210f * ScreenUtils.getScreenWidthInt() / 375f));
        mPullToZoomListView.setOnListViewZoomListener(new PullToZoomListView.OnListViewZoomListener() {
            @Override
            public void onHeaderZoom(float f) {
                Rect categorySize = ScreenUtils.getViewSize(mCategoryLayout);
                Rect categoryTitlebarSize = ScreenUtils.getViewSize(mTitleBarCategoryLayout);

                if (categoryTitlebarSize.top > 0 && categorySize.top > 0) {
                    if (mAnimators.isEmpty()) {
                        mAnimatorDuration = categorySize.top - categoryTitlebarSize.top;
                        defaultAnimationTime = (int) mAnimatorDuration;
                        createAnimation(mAnimatorDuration);
                    }

                    int offset = categorySize.top - categoryTitlebarSize.top;
                    if (offset <= 0) {
                        //显示
                        mTitleBarCategoryLayout.setVisibility(View.VISIBLE);
                        mCategoryLayout.relationWith(null);
                        mTitleBarCategoryLayout.relationWith(mCategoryLayout);
                        getTitleBar().enableBottomDiv(true);
                    } else {
                        mTitleBarCategoryLayout.setVisibility(View.GONE);
                        mTitleBarCategoryLayout.relationWith(null);
                        mCategoryLayout.relationWith(mTitleBarCategoryLayout);
                        getTitleBar().enableBottomDiv(false);
                    }
                    updateZoomAnimator(mAnimatorDuration - offset);
                }

            }
        });
        mHeadView = mPullToZoomListView.getHeaderView();
        mHeadImageView = (ImageView) mHeadView.findViewById(R.id.app_list_head_image);
        mHeadImageView.setImageResource(R.drawable.app_bg_restaurant);

        mNoticeView = (FrameLayout) mHeadView.findViewById(R.id.app_list_head_notice);

        mRestaurantImageView = (ImageView) mHeadView.findViewById(R.id.app_restaurant_detail_image);
        mScoreCountView = (TextView) mHeadView.findViewById(R.id.app_restaurant_detail_score_title);
        mScoreDescView = (TextView) mHeadView.findViewById(R.id.app_restaurant_detail_score_desc);
        mHeadView.findViewById(R.id.app_restaurant_detail_score).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 用户评论
                LogGather.onEventDishRestaurantDetailGoEvaluate();
                Bundle bundle = new Bundle();
                bundle.putString(DishRestaurantCommentFragment.EXTRA_RESTAURANT_ID, mRestaurantId);
                next(DishRestaurantCommentFragment.class, bundle);
            }
        });
        mSaleCountView = (TextView) mHeadView.findViewById(R.id.app_restaurant_detail_sale_title);
        mHeadView.findViewById(R.id.app_restaurant_detail_sale).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //热门销量
                LogGather.onEventDishRestaurantDetailGoSale();
                Bundle bundle = new Bundle();
                bundle.putString(DishHotSaleListFragment.EXTRA_RESTAURANT_ID, mRestaurantId);
                next(DishHotSaleListFragment.class, bundle);
            }
        });

        mRestaurantNoticeView = view.findViewById(R.id.app_restaurant_detail_notice);
        mRestaurantNoticeContentView = (TextView) view.findViewById(R.id.app_restaurant_detail_notice_content);

        mTitleBarCategoryLayout = (DishCategoryView) view.findViewById(R.id.app_restaurant_detail_anchor);
        mCategoryLayout = (DishCategoryView) inflater.inflate(R.layout.view_category_order, null);
        mCategoryLayout.relationWith(mTitleBarCategoryLayout);
        mCategoryLayout.setHandler(new DishCategoryView.OnCategoryOrderHandler() {
            @Override
            public void onOrderItemClick(int position) {
                requestDishList();
            }

            @Override
            public void onCategoryItemClick(Tags tag) {
                requestDishList();
            }
        });
        int default_menu_icon = default_dish_mode.equals(DishDetailAdapter.DISH_MODE_SINGLE) ? R.drawable.app_ic_layout_mode_small : R.drawable.app_ic_layout_mode_big;
        mTitleBarCategoryLayout.getmOrderByChooseView().setSwitchImg(default_menu_icon);
        mCategoryLayout.getmOrderByChooseView().setSwitchImg(default_menu_icon);
        mTitleBarCategoryLayout.getmOrderByChooseView().setOnMenuItemClick(new DishOrderByView.OnMenuItemClick() {
            @Override
            public void OnClick(View v) {
                changeAdapterMode(v);
            }
        });
        mCategoryLayout.getmOrderByChooseView().setOnMenuItemClick(new DishOrderByView.OnMenuItemClick() {
            @Override
            public void OnClick(View v) {
                changeAdapterMode(v);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setBackDrawable(R.drawable.uikit_bt_back);
        titleBar.getTitleView().setTextColor(getResources().getColor(R.color.uikit_white));
        titleBar.enableShoppingCard(true);
        titleBar.setShopCardImg(R.drawable.uikit_ic_shop_cart_white);
        titleBar.enableBottomDiv(false);
        mDishLoadMore = new LoadMoreImpl<>();
        mDishLoadMore.setListAdapter(mDishAdapter, mPullToZoomListView);
        mDishLoadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener<List<Dish>>() {
            @Override
            public void onLoaded(boolean success, List<Dish> o) {
                if (success) {
                    if (mDishAdapter != null) {
                        mDishAdapter.addEntrys(o);
                    }
                }
            }
        });
        mDishLoadMore.setMoreLoader(new UILoader<List<Dish>>(this) {
            @Override
            public void sync(final Callback<List<Dish>> callback) {
                super.sync(callback);
                requestDishList(mDishLoadMore.getCurrentPage(), mDishLoadMore.getPageSize(),
                        new ApiCallback<ApiModelList<Dish>>() {
                            @Override
                            public void onResult(ApiModelList<Dish> result) {
                                if (result.available()) {
                                    callback.callback(result.getList());
                                } else {
                                    callback.callback(null);
                                }
                            }
                        });
            }
        });
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

        if (!StringUtils.isEmpty(mRestaurantId)) {
            showLoading();
            RestaurantApi.getDishRestaurantDetail(mRestaurantId, new ApiCallback<Restaurant>() {
                @Override
                public void onResult(Restaurant result) {
                    if (result.available()) {
                        mRestaurant = result;
                    } else {
                        ToastAlarm.show(result.getErrorMsg());
                    }

                    if (mRestaurant != null && null != getContext()) {
                        updateInfo();
                        updatePreferential();
                        updateCategoryAndOrder();
                        requestDishList();
                        startNoticeLoop();
                    }
                }
            });

            DishApi.getDishNotice(DishApi.NOTICE_TYPE_RESTAURANT, mRestaurantId,
                    new ApiCallback<Notice>() {
                        @Override
                        public void onResult(Notice result) {
                            String title = null;
                            if (result.available()) {
                                title = result.getTitle();
                            }
                            if (!StringUtils.isEmpty(title)) {
                                mRestaurantNoticeView.setVisibility(View.VISIBLE);
                                mRestaurantNoticeContentView.setText(title);
                                mRestaurantNoticeView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (mRestaurantNotice != null) {
                                            LogGather.onEventDishRestaurantDetailGoNotice();
                                            new NoticeListDialog(getContext(), mRestaurantNotice)
                                                    .setCloseClick(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {

                                                        }
                                                    })
                                                    .setDialogIcon(R.drawable.app_ic_dialog_notice)
                                                    .show();
                                        }
                                    }
                                });
                                mRestaurantNotice = result;
                                mRestaurantNotice.setImage("");
                            } else {
                                mRestaurantNoticeView.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    /**
     * 开始广告轮播
     */
    private void startNoticeLoop() {
        long frequency = 3000;
        if (mLoopAnimator != null) {
            //轮播
            UIHandler.loop(LOOP_PREFERENTIAL_NOTICE, runnable, frequency);
        }
    }

    /**
     * 停止广告轮播
     */
    private void stopNoticeLoop() {
        UIHandler.stopLoop(LOOP_PREFERENTIAL_NOTICE);
    }


    private void createAnimation(long duration) {
        if (duration < defaultAnimationTime) {
            duration = defaultAnimationTime;
        }
//        TextView titleBarTitle = getTitleBar().getTitleView();
//        PropertyValuesHolder alphaToV = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
//        PropertyValuesHolder alphaToG = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);


        ValueAnimator background = ObjectAnimator.ofFloat(0, 1f).setDuration(duration);
        background.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int color = getResources().getColor(R.color.uikit_white);

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float alpha = (Float) animation.getAnimatedValue();
                int a = (int) (alpha * 255);
                int newColor = Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
                getTitleBar().setBackgroundColor(newColor);
                getTitleBar().setBackDrawable(a > 253 ? R.drawable.uikit_bt_back_pressed : R.drawable.uikit_bt_back);
                getTitleBar().getTitleView().setTextColor(getResources().getColor(a > 253 ? R.color.uikit_black : R.color.uikit_white));
                getTitleBar().setShopCardImg(a > 253 ? R.drawable.uikit_ic_shop_cart_orange : R.drawable.uikit_ic_shop_cart_white);
            }
        });
        mAnimators.add(background);
//        mAnimators.add(ObjectAnimator.ofPropertyValuesHolder(titleBarTitle, alphaToV)
//                .setDuration(duration));
    }

    private void updateZoomAnimator(float f) {
        if (f > mAnimatorDuration) {
            f = mAnimatorDuration - 1;
        }

        if (f >= 0) {
            for (ValueAnimator animator : mAnimators) {
                animator.setCurrentPlayTime((long) f);
            }
        }
    }

    private void requestDishList() {
        if (mIsRequesting || null == getContext()) {
            return;
        }
        mIsRequesting = true;
        mLoadingDialog.show();
        showLoading();
        mDishLoadMore.reset();
        requestDishList(mDishLoadMore.getCurrentPage(), mDishLoadMore.getPageSize(),
                new ApiCallback<ApiModelList<Dish>>() {
                    @Override
                    public void onResult(ApiModelList<Dish> result) {
                        mDishListData.clear();
                        if (result.available()) {
                            mDishListData.addAll(result.getList());
                        }
                        if (mDishAdapter == null) {
                            if (firstSet) {
                                if (null != mDishListData && mDishListData.size() > 0 && null != mDishListData.get(mDishListData.size() - 1) && null != mDishListData.get(mDishListData.size() - 1).getImage()) {
                                    int position = Math.min((mDishListData.size() - 1), 4);
                                    ImageFetcher.asInstance().load(mDishListData.get(position).getImage(), mHeadImageView);
                                }

                                emptyView = initEmptyView();

                                mAdapter.addView(emptyView);
                                emptyView.setBackgroundColor(getResources().getColor(R.color.uikit_pulltorefresh_bg));
                            } else {
                                firstSet = false;
                            }
                            mDishAdapter = new DishDetailAdapter(DishRestaurantDetailFragment.this, mDishListData, default_dish_mode);
                            mAdapter.addAdapter(mDishAdapter);
                        }


                        if (mPullToZoomListView.getAdapter() == null) {
                            mPullToZoomListView.setAdapter(mAdapter);
                        } else {
                            mAdapter.notifyDataSetChanged();
                        }
                        mDishAdapter.notifyDataSetChanged();
                        //控制是否显示空View
                        boolean flagShowEmpty = (mDishAdapter.getCount() == 0);
                        mAdapter.setActive(emptyView, flagShowEmpty);

                        hideLoading();
                        mLoadingDialog.dismiss();
                        mIsRequesting = false;
                    }
                });
    }

    /**
     * 请求餐厅菜品列表
     */
    private void requestDishList(int page, int pageSize, ApiCallback<ApiModelList<Dish>> callback) {
        Address address = DishPolicy.get().getDishAddress();
        String lat = address.getLocation().getLatitude();
        String lon = address.getLocation().getLongitude();
        DishApi.getDishListFromRestaurant(mCategoryLayout.getCurrentTag(), mRestaurantId, lat, lon,
                mCategoryLayout.getOrderType(), mCategoryLayout.getOrderDirect(),
                page, pageSize, callback);
    }

    /**
     * 更新分类和排序
     */
    private void updateCategoryAndOrder() {

        final List<Tags> categorys = mRestaurant.getCategorys();
        if (!ListUtils.isEmpty(categorys)) {
            mCategoryLayout.setCurrentTag("全部");
            mCategoryLayout.updateView(categorys);
            mAdapter.addView(mCategoryLayout);
        }
    }

    /**
     * 修改适配器模式
     */
    private void changeAdapterMode(View v) {
        if (null != mPullToZoomListView) {
        }
        if (null != mDishAdapter) {
            boolean isSingle = mDishAdapter.getMode().equals(DishDetailAdapter.DISH_MODE_SINGLE);
            mDishAdapter.setMode(isSingle ? DishDetailAdapter.DISH_MODE_DOUBLE : DishDetailAdapter.DISH_MODE_SINGLE);
            mTitleBarCategoryLayout.getmOrderByChooseView().setSwitchImg(isSingle ? R.drawable.app_ic_layout_mode_big : R.drawable.app_ic_layout_mode_small);
            mCategoryLayout.getmOrderByChooseView().setSwitchImg(isSingle ? R.drawable.app_ic_layout_mode_big : R.drawable.app_ic_layout_mode_small);
            mDishAdapter.notifyDataSetChanged();
            mAdapter.notifyDataSetChanged();
            if (null != mPullToZoomListView && mPullToZoomListView.getAdapter() != null) {
                if (mDishAdapter.getMode().equals(DishDetailAdapter.DISH_MODE_SINGLE)) {
                    int scrollToPosition = Math.max(0, (mPullToZoomListView.getFirstVisiblePosition() + mPullToZoomListView.getFirstVisiblePosition() % 2) * 2 - 2);
                    mPullToZoomListView.smoothScrollToPosition(Math.min(scrollToPosition, mPullToZoomListView.getCount() - 1));
                } else {
                    int scrollToPosition = Math.max(0, (mPullToZoomListView.getFirstVisiblePosition() + mPullToZoomListView.getFirstVisiblePosition() % 2) / 2);
                    mPullToZoomListView.smoothScrollToPosition(Math.min(scrollToPosition, mPullToZoomListView.getCount() - 1));
                }
            }
        }
    }

    /**
     * 更新优惠信息
     */
    private void updatePreferential() {
        final Notice preferentialNotice = new Notice();
        List<Notice.Note> notes = new ArrayList<>();
        preferentialNotice.setNotes(notes);
        //配送信息
        Tags deliveryTag = mRestaurant.getDeliveryTag();

        //优惠列表
        final List<Tags> preferentialList = mRestaurant.getPerferentials();
        mPreferentialViewGroup = new LinearLayout(getContext());
        mPreferentialViewGroup.setOrientation(LinearLayout.VERTICAL);

        //设置优惠信息
        if (!ListUtils.isEmpty(preferentialList)) {
            Notice.Note note = new Notice.Note();
            notes.add(note);
            note.title = "优惠信息";
            note.items = new ArrayList<>();

            for (Tags tag : preferentialList) {
                Notice.NoteItem item = new Notice.NoteItem();
                item.name = tag.getName();
                item.icon = tag.getIcon();
                note.items.add(item);
            }
            Tags tags = ListUtils.getItem(preferentialList, 0);
            if (tags != null) {
                final View item = View.inflate(getContext(), R.layout.view_dish_preferential, null);
                mPreferentialViewGroup.addView(item);
                final ImageView icon = (ImageView) item.findViewById(R.id.app_dish_preferential_icon);
                final TextView name = (TextView) item.findViewById(R.id.app_dish_preferential_name);
                name.setText(tags.getName());
                ImageFetcher.asInstance().load(tags.getIcon(), icon, R.drawable.app_bt_address_icon_default);
                if (deliveryTag != null) {
                    preferentialList.add(deliveryTag);
                }
                if (preferentialList.size() > 1) {
                    mLoopTime = 0;
                    final PropertyValuesHolder rotationTo = PropertyValuesHolder.ofFloat("rotationX", 0f, 90f);
                    final PropertyValuesHolder rotationBack = PropertyValuesHolder.ofFloat("rotationX", 90f, 0f);
                    ObjectAnimator rotationToAnimator = ObjectAnimator.ofPropertyValuesHolder(name, rotationTo).setDuration(300);
                    ObjectAnimator rotationBackAnimator = ObjectAnimator.ofPropertyValuesHolder(name, rotationBack).setDuration(300);
                    mLoopAnimator = new AnimatorSet();
                    mLoopAnimator.playSequentially(rotationToAnimator, rotationBackAnimator);
                    rotationToAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float rotationX = (float) valueAnimator.getAnimatedValue();
                            if (Float.compare(rotationX, 90) == 0) {
                                Tags current = preferentialList.get(mLoopTime++ % preferentialList.size());
                                name.setText(current.getName());
                                ImageFetcher.asInstance().load(current.getIcon(), icon);
                            }
                        }
                    });
                }
            }
        }

        //设置配送信息
        if (deliveryTag != null) {
            Notice.Note note = new Notice.Note();
            notes.add(note);
            note.title = "配送信息";
            note.items = new ArrayList<>();
            Notice.NoteItem noteItem = new Notice.NoteItem();
            noteItem.name = deliveryTag.getName();
            noteItem.icon = deliveryTag.getIcon();
            note.items.add(noteItem);

//            View item = View.inflate(getContext(), R.layout.view_dish_preferential, null);
//            mPreferentialViewGroup.addView(item);
//            final ImageView icon = (ImageView) item.findViewById(R.id.app_dish_preferential_icon);
//            final TextView name = (TextView) item.findViewById(R.id.app_dish_preferential_name);
//            name.setText(deliveryTag.getName());
//            ImageFetcher.asInstance().load(deliveryTag.getIcon(), icon);
        }


        if (mPreferentialViewGroup.getChildCount() > 0) {
            if (null != mNoticeView) {
                mNoticeView.removeAllViews();
                mNoticeView.addView(mPreferentialViewGroup);
            }
//            mAdapter.addView(mPreferentialViewGroup);
            mPreferentialViewGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new NoticeListDialog(getContext(), preferentialNotice)
                            .setCloseClick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            })
                            .setDialogIcon(R.drawable.app_ic_dialog_preferential)
                            .show();
                }
            });

////            增加底部的灰色间隔
//            View view = new View(getContext());
//            view.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_grey_light));
//            ScreenUtils.resizeViewWithSpecial(view, ScreenUtils.getScreenWidthInt(), ScreenUtils.dip2px(8));
//            mPreferentialViewGroup.addView(view);
        }
    }

    /**
     * 更新餐厅信息
     */
    private void updateInfo() {
        ImageFetcher.asInstance().loadRoundedCorner(mRestaurant.getImage(), mRestaurantImageView, ScreenUtils.dip2px(1));
        mScoreCountView.setText(mRestaurant.getGrade());
        mSaleCountView.setText(mRestaurant.getSale());
        setTitle(mRestaurant.getName());
    }

    @Override
    public void onCardOut() {
        super.onCardOut();
        stopNoticeLoop();
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        startNoticeLoop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopNoticeLoop();
    }

    private EmptyView initEmptyView() {
        if (null == getContext()) {
            return null;
        }
        EmptyView emptyView = new EmptyView(getContext());
        emptyView.onFinishInflate();
        emptyView.setImageViewResourceIdOfTop(R.drawable.app_pic_empty_search);
        emptyView.setTitle("未找到该菜品");
        emptyView.setSecondTitle("");
        return emptyView;
    }

    private class DishDetailAdapter extends DishAdapter {
        /**
         * 单图显示
         *
         * @see #setMode
         */
        public static final String DISH_MODE_SINGLE = "single";
        /**
         * 双图显示
         *
         * @see #setMode
         */
        public static final String DISH_MODE_DOUBLE = "double";
        /**
         * 单图显示
         */
        public static final int ITEM_TYPE_ONE = 1;
        /**
         * 双图显示
         */
        public static final int ITEM_TYPE_TWO = 2;
        private String mode = DISH_MODE_SINGLE;


        /**
         * 构造
         *
         * @param fragment
         * @param data
         * @param mode     mode {@link #DISH_MODE_SINGLE} or
         *                 {@link #DISH_MODE_DOUBLE}
         */
        public DishDetailAdapter(BaseFragment fragment, List<Dish> data, String mode) {
            super(fragment, data);
            this.mode = mode;
        }

        @Override
        public int getCount() {
            if (mode.equals(DISH_MODE_DOUBLE)) {
                return (super.getCount() + super.getCount() % 2) / 2;
            } else {
                return (super.getCount());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mode.equals(DISH_MODE_SINGLE)) {
                return ITEM_TYPE_ONE;
            } else {
                return ITEM_TYPE_TWO;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        public String getMode() {
            return mode;
        }

        /**
         * 设置显示模式
         *
         * @param mode {@link #DISH_MODE_SINGLE} or
         *             {@link #DISH_MODE_DOUBLE}
         */
        public void setMode(String mode) {
            this.mode = mode;
        }

        @Override
        protected View newView(int viewType) {
            switch (viewType) {
                case ITEM_TYPE_TWO:
                    View view_double = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_dish, null);
                    View itemLeft = view_double.findViewById(R.id.app_list_item_left);
                    View itemRight = view_double.findViewById(R.id.app_list_item_right);
                    setViewStatues(itemLeft);
                    setViewStatues(itemRight);
                    int itemViewWidth = (ScreenUtils.getScreenWidthInt() - 3 * ScreenUtils.dip2px(8)) / 2;
                    ScreenUtils.resizeView(itemLeft.findViewById(R.id.app_dish_cover_group), itemViewWidth, 1f);
                    ScreenUtils.resizeViewOfWidth(itemLeft.findViewById(R.id.app_dish_title_group), itemViewWidth);

                    ScreenUtils.resizeView(itemRight.findViewById(R.id.app_dish_cover_group), itemViewWidth, 1f);
                    ScreenUtils.resizeViewOfWidth(itemRight.findViewById(R.id.app_dish_title_group), itemViewWidth);

                    ScreenUtils.reMargin(itemLeft.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 75, true);
                    ScreenUtils.reMargin(itemRight.findViewById(R.id.app_dish_restaurant_group), 0, 0, 0, 75, true);

                    View coverL = itemLeft.findViewById(R.id.app_dish_content);
                    ScreenUtils.resizeView(coverL, itemViewWidth, 251.5f / 172.5f);

                    View coverR = itemRight.findViewById(R.id.app_dish_content);
                    ScreenUtils.resizeView(coverR, itemViewWidth, 251.5f / 172.5f);
                    return view_double;
                case ITEM_TYPE_ONE:
                default:
                    View view_single = LayoutInflater.from(getContext()).inflate(R.layout.view_dish_big, null);
                    setViewStatues(view_single);
                    int itemViewWidthBig = ScreenUtils.getScreenWidthInt() - 2 * ScreenUtils.dip2px(8);
                    View content = view_single.findViewById(R.id.app_dish_content);
                    ScreenUtils.resizeView(content, itemViewWidthBig, 257 / 355f);
                    return view_single;
            }
        }


        @Override
        public void updateView(int position, int viewType, final Dish data, Bundle extra) {
            switch (viewType) {
                case ITEM_TYPE_ONE:
                    getItemView().findViewById(R.id.app_dish_content).findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);
                    super.updateItem(mFragment, getItemView(), true, true, position, viewType, data, extra);
                    FrameLayout btnShopLayout = (FrameLayout) getItemView().findViewById(R.id.app_dish_shop_btn_layout);
                    setAddShoppingAnimation(btnShopLayout, getTitleBar().getShoppingCartLayout(), data);
                    break;
                case ITEM_TYPE_TWO:
                    View viewLeft = getItemView().findViewById(R.id.app_list_item_left);
                    View viewRight = getItemView().findViewById(R.id.app_list_item_right);
                    int positionLeft = position * 2;
                    int positionRight = positionLeft + 1;
                    Dish dishLeft = ((positionLeft) >= mDishListData.size()) ? null : mDishListData.get(positionLeft);
                    Dish dishRight = ((positionRight) >= mDishListData.size()) ? null : mDishListData.get(positionRight);
                    if (null != dishLeft) {
                        viewLeft.setVisibility(View.VISIBLE);
                        super.updateItem(mFragment, viewLeft, false, true, positionLeft, viewType, dishLeft, extra);
                        FrameLayout leftBtnShopLayout = (FrameLayout) viewLeft.findViewById(R.id.app_dish_shop_btn_layout);
                        setAddShoppingAnimation(leftBtnShopLayout, getTitleBar().getShoppingCartLayout(), dishLeft);
                    } else {
                        viewLeft.setVisibility(View.INVISIBLE);
                    }
                    if (null != dishRight) {
                        viewRight.setVisibility(View.VISIBLE);
                        super.updateItem(mFragment, viewRight, false, true, positionRight, viewType, dishRight, extra);
                        FrameLayout rightBtnShopLayout = (FrameLayout) viewRight.findViewById(R.id.app_dish_shop_btn_layout);
                        setAddShoppingAnimation(rightBtnShopLayout, getTitleBar().getShoppingCartLayout(), dishRight);
                    } else {
                        viewRight.setVisibility(View.INVISIBLE);
                    }
                    viewRight.findViewById(R.id.app_dish_content).findViewById(R.id.app_dish_desc_group).setVisibility(View.GONE);
                    viewLeft.findViewById(R.id.app_dish_content).findViewById(R.id.app_dish_desc_group).setVisibility(View.GONE);
                    break;
            }

        }

        /**
         * 设置按钮状态
         */
        private void setViewStatues(View view) {
            view.findViewById(R.id.app_dish_restaurant_group).setVisibility(View.GONE);
            view.findViewById(R.id.app_dish_desc_group).setVisibility(View.VISIBLE);
            view.findViewById(R.id.app_dish_name_end).setVisibility(View.GONE);
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
    }
}
