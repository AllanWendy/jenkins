package cn.wecook.app.main.dish;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.location.LocationServer;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.api.legacy.DishApi;
import com.wecook.sdk.api.model.Address;
import com.wecook.sdk.api.model.Banner;
import com.wecook.sdk.api.model.Category;
import com.wecook.sdk.api.model.DishRecommend;
import com.wecook.sdk.api.model.HomeFeedList;
import com.wecook.sdk.api.model.RecommendCustomCard;
import com.wecook.sdk.api.model.Restaurant;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.adapter.MergeAdapter;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.RestaurantAdapter;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.city.CityActivity;
import cn.wecook.app.features.deliver.WecookLink;
import cn.wecook.app.main.MainActivity;
import cn.wecook.app.main.dish.DishBarCode.DishBarCodeActivity;
import cn.wecook.app.main.dish.restaurant.DishRestaurantFragment;
import cn.wecook.app.main.recommend.card.BannerMultiView;
import cn.wecook.app.main.recommend.card.ClassifyView;
import cn.wecook.app.main.recommend.card.CustomCardView;
import cn.wecook.app.main.recommend.card.CustomDoubleLineCardView;
import cn.wecook.app.main.recommend.card.CustomFourGridCardView;
import cn.wecook.app.main.recommend.card.CustomSimpleImageCardView;
import cn.wecook.app.main.recommend.card.CustomThreeCardView;
import cn.wecook.app.main.search.SearchActivity;
import cn.wecook.app.main.search.SearchFragment;
import cn.wecook.app.utils.UrlUtils;

/**
 * 买菜帮手首页
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/16
 */
public class DishRecommendFragment extends BaseListFragment implements View.OnClickListener {
    /**
     * 加载完毕首页Feed流
     */
    public static final String ACTION_FINISH_LOAD_FEED = "cn.wecook.app_dish_finish_load_home_feed";

    private DishRecommend mDishRecommend;
    private List<Banner> banners;
    private List<Restaurant> food_feed;
    private ArrayList<Category> categories;
    private PullToRefreshListView mPullToRefreshLayout;

    private ListView mListView;
    private MergeAdapter adapter;

    private BannerMultiView mBannerView;//Banner
    private ClassifyView mClassifyView;//分类
    private RestaurantAdapter mRestaurantAdapter;//餐厅适配器
    private View mNoticeView;//为您推荐
    private View mBtn_loadMore;//加载更多
    private View null_View;//占位

    private RelativeLayout actionBarLayoutTop;
    private View actionBarLine_Top;
    private FrameLayout searchLayout_Top;
    private TextView tv_location_Top;//地理位置
    private FrameLayout framlayout_shop_Top;//购物车
    private ImageView img_shop_Top;//购物车
    private TextView tv_shop_count_Top;//购物车数量
    private RelativeLayout actionBarLayoutBottom;
    private View actionBarLine_Bottom;
    private FrameLayout searchLayout_Bottom;
    private TextView tv_location_Bottom;//地理位置
    //购物车
    private FrameLayout framlayout_shop_Bottom;//购物车
    private ImageView img_shop_Bottom;//购物车
    private TextView tv_shop_count_Bottom;//购物车数量
    //二维码
    private FrameLayout framlayout_zxing_layout;//二维码
    private ImageView img_zxing;//二维码


    //广播
    private BroadcastReceiver mReceiverSelectCity;
    private BroadcastReceiver mReceiverUpdAddress;
    /**
     * 是否刷新
     */
    private boolean isRef = false;
    private boolean isShowRefresh = false;
    private float alphaNow = 0.5f;

    private LoadingDialog dialog;

    private DishPolicy.OnShopCartListener listener = new DishPolicy.OnShopCartListener() {
        @Override
        public void onUpdate(int count) {
            updateShopCartCount(count);
        }
    };

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcasts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_dish_recommend, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new LoadingDialog(getContext());

        actionBarLayoutTop = (RelativeLayout) view.findViewById(R.id.dish_recommend_action_bar_top);
        tv_location_Top = (TextView) actionBarLayoutTop.findViewById(R.id.dish_recommend_location_btn);
        framlayout_shop_Top = (FrameLayout) actionBarLayoutTop.findViewById(R.id.dish_recommend_dishshop_layout);
        img_shop_Top = (ImageView) actionBarLayoutTop.findViewById(R.id.dish_recommend_shop_img);
        tv_shop_count_Top = (TextView) actionBarLayoutTop.findViewById(R.id.dish_recommend_shop_count);
        searchLayout_Top = (FrameLayout) actionBarLayoutTop.findViewById(R.id.dish_recommend_search_layout);
        actionBarLine_Top = actionBarLayoutTop.findViewById(R.id.app_dish_recommend_line);
        tv_location_Top = (TextView) actionBarLayoutTop.findViewById(R.id.dish_recommend_location_btn);


        actionBarLayoutBottom = (RelativeLayout) view.findViewById(R.id.dish_recommend_action_bar_bottom);
        tv_location_Bottom = (TextView) actionBarLayoutBottom.findViewById(R.id.dish_recommend_location_btn);
        framlayout_shop_Bottom = (FrameLayout) actionBarLayoutBottom.findViewById(R.id.dish_recommend_dishshop_layout);
        img_shop_Bottom = (ImageView) actionBarLayoutBottom.findViewById(R.id.dish_recommend_shop_img);
        tv_shop_count_Bottom = (TextView) actionBarLayoutBottom.findViewById(R.id.dish_recommend_shop_count);
        framlayout_zxing_layout = (FrameLayout) actionBarLayoutBottom.findViewById(R.id.dish_recommend_zxing_layout);
        img_zxing = (ImageView) actionBarLayoutBottom.findViewById(R.id.dish_recommend_zxing_img);


        searchLayout_Bottom = (FrameLayout) actionBarLayoutBottom.findViewById(R.id.dish_recommend_search_layout);
        actionBarLine_Bottom = actionBarLayoutBottom.findViewById(R.id.app_dish_recommend_line);
        tv_location_Bottom = (TextView) actionBarLayoutBottom.findViewById(R.id.dish_recommend_location_btn);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchLayout_Bottom.getLayoutParams();
        params.addRule(RelativeLayout.LEFT_OF, R.id.dish_recommend_zxing_layout);
        searchLayout_Bottom.setLayoutParams(params);

        String selectedCity = LocationServer.asInstance().getSelectedCity();
        tv_location_Top.setText((selectedCity == null || "".equals(selectedCity)) ? "北京" : selectedCity);
        tv_location_Bottom.setText((selectedCity == null || "".equals(selectedCity)) ? "北京" : selectedCity);
        mPullToRefreshLayout = (PullToRefreshListView) view.findViewById(R.id.dish_recommend_list);
        mPullToRefreshLayout.setOnPullEventListener(new PullToRefreshBase.OnPullEventListener<ListView>() {
            @Override
            public void onPullEvent(PullToRefreshBase<ListView> refreshView, PullToRefreshBase.State state, PullToRefreshBase.Mode direction) {
                AlphaAnimation alphaAnimation = null;
                boolean isShow = true;
                if (state == PullToRefreshBase.State.PULL_TO_REFRESH) {
                    alphaAnimation = new AlphaAnimation(1, 0);
                    alphaAnimation.setDuration(200);//设置动画持续时间
                    alphaAnimation.setFillAfter(true);
                    setActionBarAboutBtnState(false);
                    isShowRefresh = true;
                } else if (state == PullToRefreshBase.State.RESET) {
                    alphaAnimation = new AlphaAnimation(0, 1);
                    alphaAnimation.setDuration(200);//设置动画持续时间
                    alphaAnimation.setFillAfter(true);
                    setActionBarAboutBtnState(true);
                    isShowRefresh = false;
                }
                if (null != alphaAnimation && null != actionBarLayoutBottom && actionBarLayoutBottom.getVisibility() == View.VISIBLE)
                    actionBarLayoutBottom.setAnimation(alphaAnimation);
            }
        });
        mPullToRefreshLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                requestRefresh();
            }
        });
        mListView = mPullToRefreshLayout.getRefreshableView();
        final double banner_height = ScreenUtils.getScreenWidth() * (9 / 16.0f);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, final int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int ScrollY = (int) (getScrollY() - banner_height);
                        if (firstVisibleItem == 1 && null != mBannerView) {
                            float alpha = (float) (ScrollY / (banner_height - getResources().getDimensionPixelSize(R.dimen.uikit_action_bar_default_height)));
                            setAlpha(alpha);
                        } else if (null != mBannerView && firstVisibleItem == 0) {
                            setAlpha(0);
                        } else {
                            setAlpha(1);
                        }
                    }
                });

            }

            public int getScrollY() {
                View c = mListView.getChildAt(0);
                if (c == null) {
                    return 0;
                }
                int firstVisiblePosition = mListView.getFirstVisiblePosition();
                int top = c.getTop();
                return -top + firstVisiblePosition * c.getHeight();
            }
        });
        framlayout_zxing_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterToZxing();
            }
        });
        //添加购物车策略
        DishPolicy.get().addOnShopCartListener(listener);
        DishPolicy.get().update();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mReceiverSelectCity) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiverSelectCity);
        }
        if (null != mReceiverUpdAddress) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiverUpdAddress);
        }

        if (listener != null) {
            DishPolicy.get().removeOnShopCartListener(listener);
        }
    }

    /**
     * 注册广播
     */
    private void registerBroadcasts() {
        //注册选择城市的广播
        registerSelecteCityBroadcast();
        //注册地址改变广播
        registerUpdAddressBroadcast();
    }

    /**
     *  选择城市的广播
     */
    private void registerSelecteCityBroadcast() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(LocationServer.SEND_BROADCAST_SELECTED_CITY);
        //注册广播
        if (null == mReceiverSelectCity) {
            mReceiverSelectCity = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent != null) {
                        String selectCity = intent.getStringExtra(LocationServer.SELECTED_CITY);
                        tv_location_Top.setText(selectCity);
                        tv_location_Bottom.setText(selectCity);
                        requestRefresh();
                    }
                }
            };
        }
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiverSelectCity, myIntentFilter);
    }

    /**
     *  注册更新地址广播
     */
    private void registerUpdAddressBroadcast() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(DishRestaurantFragment.ACTION_UPD_ADDRESS);
        //注册广播
        if (null == mReceiverUpdAddress) {
            mReceiverUpdAddress = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent != null) {
                        requestRefresh();
                    }
                }
            };
        }
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiverUpdAddress, myIntentFilter);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (NetworkState.available()) {
            hideEmptyView();
            showLoading();
        } else {
            ToastAlarm.show("请检查手机网络");
            showEmptyView();
            return;
        }
        if (!isRef) {
            dialog.show();
        }

        Address address = DishPolicy.get().getDishAddress();
        final String lat = address.getLocation().getLatitude();
        final String lon = address.getLocation().getLongitude();
        String city = LocationServer.asInstance().getIndexCity();
        final String mCity = city;
        DishApi.getDishRecommendInfo(lon, lat, mCity, new ApiCallback<DishRecommend>() {
            @Override
            public void onResult(DishRecommend result) {
                mDishRecommend = result;
                adapter = new MergeAdapter();
                initView();
                setClick();
                //加载feed流
                loadHomeFeed(lat, lon, mCity);
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
    protected boolean performRefresh() {
        if (getLoadMore() != null) {
            getLoadMore().reset();
        }
        return super.performRefresh();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        if (mDishRecommend != null && null != getActivity()) {
            banners = mDishRecommend.getBannerList();
            if (banners != null && !banners.isEmpty()) {
                mBannerView = new BannerMultiView(this);
                mBannerView.setStatAutoRun(true);
                mBannerView.loadLayout(R.layout.view_multi_banner, banners);
                adapter.addView(mBannerView);
//            } else {
//                mBannerView = new BannerMultiView(this);
//                mBannerView.setStatAutoRun(true);
//                banners = new ArrayList<>();
//                Banner banner = new Banner();
//                banner.setImage("http://u1.wecook.cn/images/20150825/55dc0ad845248.jpg-p540");
//                banners.add(banner);
//                mBannerView.loadLayout(R.layout.view_multi_banner, banners);
//                adapter.addView(mBannerView);

            }
            categories = (ArrayList<Category>) mDishRecommend.getCategoryList();
            if (null != categories && !categories.isEmpty()) {
                mClassifyView = new ClassifyView(this);
                mClassifyView.loadLayout(categories);
                mClassifyView.setOnLayoutItemClick(new ClassifyView.OnLayoutItemClick() {
                    @Override
                    public void OnClickListener(int position, Category category) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(DishClassifyFragment.PARAMS_CATEGORIES, categories);
                        bundle.putInt(DishClassifyFragment.PARAMS_FIRSTSELECTITEM, position);
                        bundle.putString(DishClassifyFragment.PARAMS_KEYWORDS, category.getUrlKeyWords());
                        next(DishClassifyFragment.class, bundle);
                    }
                });
                adapter.addView(mClassifyView);
            }
            List<RecommendCustomCard> cards = mDishRecommend.getCardLists();
            if (null != cards && !cards.isEmpty()) {
                for (int i = 0; i < cards.size(); i++) {
                    RecommendCustomCard mRecommendCustomCard = cards.get(i);
                    CustomCardView mCustomCardView;
                    switch (mRecommendCustomCard.getType()) {
                        case RecommendCustomCard.TYPE_SIMPLE_IMAGE:
                            mCustomCardView = new CustomSimpleImageCardView(getContext());
                            break;
                        case RecommendCustomCard.TYPE_DOUBLE_LINE:
                            mCustomCardView = new CustomDoubleLineCardView(getContext());
                            break;
                        case RecommendCustomCard.TYPE_THREE_CARD:
                            mCustomCardView = new CustomThreeCardView(getContext());
                            break;
                        case RecommendCustomCard.TYPE_FOUR_GRID:
                            mCustomCardView = new CustomFourGridCardView(getContext());
                            break;
                        default:
                            mCustomCardView = null;
                            break;
                    }
                    if (null != mCustomCardView) {
                        mCustomCardView.loadLayout(mRecommendCustomCard);
                        adapter.addView(mCustomCardView);
                    }
                }
            }
        }

    }

    /**
     * 设置Alpha
     *
     * @param alpha
     */
    private void setAlpha(float alpha) {
        if (alphaNow == alpha) return;
        if (alpha < 0.01) alpha = 0.01f;
        if (alpha > 1.0) alpha = 1.0f;
        alphaNow = alpha;
        //Alpha值
        actionBarLayoutBottom.setBackgroundColor(Color.argb((int) (255 * alpha), 255, 255, 255));
        //线
        actionBarLine_Bottom.setVisibility(alpha > 0.9 ? View.VISIBLE : View.GONE);
        //文字
        tv_location_Bottom.setTextColor(alpha > 0.99 ? getResources().getColor(R.color.uikit_orange) : getResources().getColor(R.color.uikit_white));
        Drawable drawable = (alpha > 0.99) ? getResources().getDrawable(R.drawable.app_ic_down_arrow_home)
                : getResources().getDrawable(R.drawable.app_ic_down_arrow_home_white);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv_location_Bottom.setCompoundDrawablesWithIntrinsicBounds(null, null,
                drawable, null);
        //购物车
        img_shop_Bottom.setImageResource((alpha > 0.99) ? R.drawable.app_ic_shop_cart_nobg
                : R.drawable.app_ic_shop_cart_nobg_white);
        //二维码
        img_zxing.setImageResource((alpha > 0.99) ? R.drawable.app_ic_zxing_home_red
                : R.drawable.app_ic_zxing_home_white);
    }

    private void endLoading() {
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                hideLoading();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (mPullToRefreshLayout != null) {
                    mPullToRefreshLayout.onRefreshComplete();
                }
            }
        });
    }

    private void requestRefresh() {
        isRef = true;
        if (NetworkState.available()) {
            Api.startNoCacheMode();
            onStartUILoad();
            Api.stopNoCacheMode();
        } else {
            ToastAlarm.show("网络异常，请刷新重试");
            isRef = false;
            endLoading();
        }
    }

    /**
     * 加载Feed流
     *
     * @param lat
     * @param lon
     * @param city
     */
    private void loadHomeFeed(String lat, String lon, String city) {
        if (isRef) {
            Api.startNoCacheMode();
        }
        DishApi.getHomeFeed(lat, lon, city, new ApiCallback<HomeFeedList<Restaurant>>() {
                    @Override
                    public void onResult(HomeFeedList<Restaurant> result) {
                        if (result.available()) {
                            final String title = result.getMoreTitle();
                            final String url = UrlUtils.addPTitle(result.getMoreUrl(), title);
                            food_feed = result.getList();
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).setFood_feed(food_feed);
                                //发送广播
                                Intent intent = new Intent();
                                intent.setAction(ACTION_FINISH_LOAD_FEED);
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                            }
                            if (null == mNoticeView) {
                                mNoticeView = LayoutInflater.from(getContext()).inflate(R.layout.view_feed_notice, null);
                            }
                            adapter.addView(mNoticeView);
                            mRestaurantAdapter = new RestaurantAdapter(DishRecommendFragment.this, food_feed);
                            adapter.addAdapter(mRestaurantAdapter);

                            mBtn_loadMore = LayoutInflater.from(getContext()).inflate(R.layout.app_home_feed_btn, null);
                            mBtn_loadMore.findViewById(R.id.app_home_feed_more_layout).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    WecookLink.sendLink(url);
                                }
                            });
                            mBtn_loadMore.findViewById(R.id.app_home_feed_button).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    WecookLink.sendLink(url);
                                }
                            });
                            adapter.addView(mBtn_loadMore);
                            if (null == null_View) {
                                null_View = new View(getContext());
                                null_View.setMinimumHeight(ScreenUtils.dip2px(20f));
                            }
                            adapter.addView(null_View);
                        }
                        loadUi(true);
                    }
                }

        );
        if (isRef) {
            Api.stopNoCacheMode();
        }
    }

    /**
     * 加载UI
     *
     * @param reSetAdapter 从新设置适配器
     */
    private void loadUi(boolean reSetAdapter) {
        isRef = false;
        if (null == banners || banners.size() == 0) {
            actionBarLayoutTop.setVisibility(View.VISIBLE);
            actionBarLayoutBottom.setVisibility(View.GONE);
        } else {
            actionBarLayoutTop.setVisibility(View.GONE);
            actionBarLayoutBottom.setVisibility(View.VISIBLE);
        }
        if (null != actionBarLayoutTop && null != actionBarLayoutBottom &&
                actionBarLayoutTop.getVisibility() == View.VISIBLE && actionBarLayoutBottom.getVisibility() == View.VISIBLE) {
            actionBarLayoutBottom.setVisibility(View.GONE);
        }

        if (null == adapter || adapter.getCount() == 0) {
            showEmptyView();
            endLoading();
            return;
        }
        if (mListView.getAdapter() == null || reSetAdapter) {
            mListView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        mListView.setBackgroundResource(R.drawable.app_bg);
        if (null != mDishRecommend.getBannerList() && mDishRecommend.getBannerList().size() > 0) {
            setAlpha(0);
        } else {
            setAlpha(1);
        }
        endLoading();
    }

    /**
     * 绑定监听
     */
    private void setClick() {
        if (null != actionBarLayoutTop) actionBarLayoutTop.setClickable(true);
        if (null != actionBarLayoutBottom) actionBarLayoutBottom.setClickable(true);
        setActionBarAboutBtnState(true);
    }

    /**
     * 设置ActionBar相关按钮的点击是否可用
     */
    private void setActionBarAboutBtnState(boolean available) {
        if (null != tv_location_Top) {
            tv_location_Top.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CityActivity.class);
                    getActivity().startActivity(intent);
                }
            });
            tv_location_Top.setClickable(available);
        }
        if (null != tv_location_Bottom) {
            tv_location_Bottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CityActivity.class);
                    if (isShowRefresh) return;
                    getActivity().startActivity(intent);
                }
            });
            tv_location_Bottom.setClickable(available);
        }
        if (null != searchLayout_Top) {
            searchLayout_Top.setOnClickListener(this);
            searchLayout_Top.setClickable(available);
        }
        if (null != searchLayout_Bottom) {
            searchLayout_Bottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShowRefresh) return;
                    LogGather.onEventSearchRecommend();
                    Intent intent = new Intent(getContext(), SearchActivity.class);
                    intent.putExtra(SearchFragment.EXTRA_TYPE, SearchFragment.TYPE_DISH);
                    startActivity(intent);
                }
            });
            searchLayout_Bottom.setClickable(available);
        }
        if (null != framlayout_shop_Top) {
            framlayout_shop_Top.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转到购物车
                    Intent intent = new Intent(getContext(), DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_SHOP_CART);
                    startActivity(intent);
                }
            });
            framlayout_shop_Top.setClickable(available);
        }
        if (null != framlayout_shop_Bottom) {
            framlayout_shop_Bottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShowRefresh) return;
                    //跳转到购物车
                    Intent intent = new Intent(getContext(), DishActivity.class);
                    intent.putExtra(DishActivity.EXTRA_PAGE, DishActivity.PAGE_SHOP_CART);
                    startActivity(intent);
                }
            });
            framlayout_shop_Bottom.setClickable(available);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dish_recommend_search_layout:
                LogGather.onEventSearchRecommend();
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra(SearchFragment.EXTRA_TYPE, SearchFragment.TYPE_DISH);
                startActivity(intent);
                break;
        }
    }

    private void updateShopCartCount(int count) {
        if (count == 0) {
            tv_shop_count_Top.setVisibility(View.GONE);
            tv_shop_count_Bottom.setVisibility(View.GONE);
        } else {
            if (count > 99) {
                tv_shop_count_Top.setText("99+");
                tv_shop_count_Bottom.setText("99+");
            } else {
                tv_shop_count_Top.setText("" + count);
                tv_shop_count_Bottom.setText("" + count);
            }
            tv_shop_count_Top.setVisibility(View.VISIBLE);
            tv_shop_count_Bottom.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 进入扫一扫
     */
    private void enterToZxing() {
        startActivity(new Intent(getContext(), DishBarCodeActivity.class));
    }

}
