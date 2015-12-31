package cn.wecook.app.main.home.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelGroup;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SimpleSync;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.legacy.FoodApi;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.Food;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.adapter.BaseUIAdapter;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.EmptyView;
import com.wecook.uikit.widget.indicator.UnderlinePageIndicator;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshListView;
import com.wecook.uikit.widget.shape.HaloCircleImageView;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.adapter.FoodListAdapter;
import cn.wecook.app.features.picture.PictureActivity;

/**
 * 个人主页
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/14
 */
public class UserPageFragment extends BaseListFragment {


    public static final String EXTRA_USER = "extra_user";

    private static final int TAB_FOCUS_COOK_SHOW = 0;
    private static final int TAB_FOCUS_RECIPE = 1;

    private View mHeadView;
    private View mListHeadView;

    private int mCurrentPos = -1;

    private EmptyView mEmptyView;
    private PullToRefreshListView mRefreshListLayout;
    private ListView mListView;

    private FoodListAdapter mFoodListAdapter;
    private List<ApiModelGroup<Food>> mFoodList = new ArrayList<ApiModelGroup<Food>>();

    private UserPageCookShowAdapter mCookShowAdapter;
    private List<ApiModelGroup<CookShow>> mCookShowList = new ArrayList<ApiModelGroup<CookShow>>();
    private ArrayList<CookShow> mAllCookShowList = new ArrayList<CookShow>();

    private User mCurrentUser;
    private PagerAdapter mViewPageAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return new TextView(getContext());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentUser = (User) bundle.getSerializable(EXTRA_USER);
        }

        if (mCurrentUser == null) {
            mCurrentUser = new User();
        }

        setTitle(R.string.app_title_user_page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mListHeadView = inflater.inflate(R.layout.view_head_user_page, null);
        return inflater.inflate(R.layout.fragment_user_page, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHeadView = view.findViewById(R.id.app_user_page_head);
        mEmptyView = (EmptyView) view.findViewById(R.id.app_user_page_empty);
        mEmptyView.setImageViewResourceIdOfBottom(R.drawable.app_pic_empty_cookshow);
        mRefreshListLayout = (PullToRefreshListView) view.findViewById(R.id.app_user_page_list);
        if (mRefreshListLayout != null) {
            mRefreshListLayout.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

                @Override
                public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                    boolean result = performRefresh();
                    if (!result) {
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mRefreshListLayout.onRefreshComplete();
                            }
                        });
                    }
                }

                @Override
                public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                    boolean result = performLoadMore();
                    if (!result) {
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mRefreshListLayout.onRefreshComplete();
                            }
                        });
                    }
                }
            });
            mListView = mRefreshListLayout.getRefreshableView();
        }


        updateHeadView(mListHeadView);
        updateHeadView(mHeadView);

        mListView.addHeaderView(mListHeadView);


        mFoodListAdapter = new FoodListAdapter(this, mFoodList);
        mCookShowAdapter = new UserPageCookShowAdapter(this, mCookShowList);

        if (mCurrentPos == TAB_FOCUS_RECIPE) {
            updateCurrent(TAB_FOCUS_RECIPE);
        } else {
            updateCurrent(TAB_FOCUS_COOK_SHOW);
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();

        showLoading();

        UserApi.getInfo(mCurrentUser.getUid(), new ApiCallback<User>() {
            @Override
            public void onResult(User result) {
                if (result.available()) {
                    mCurrentUser.setEmail(result.getEmail());
                    mCurrentUser.setCity(result.getCity());
                    mCurrentUser.setPhone(result.getPhone());
                    mCurrentUser.setAvatar(result.getAvatar());
                    mCurrentUser.setBirthday(result.getBirthday());
                    mCurrentUser.setNickname(result.getNickname());
                    mCurrentUser.setGender(result.getGender());
                }
                onCardIn(null);
            }
        });

        CookShowApi.getUserCookShowList(mCurrentUser.getUid(), 1,
                getLoadMore().getPageSize(), new ApiCallback<ApiModelList<CookShow>>() {
                    @Override
                    public void onResult(ApiModelList<CookShow> result) {

                        if (result.available() && !result.isEmpty()) {
                            mCookShowList.clear();
                            ApiModelGroup<CookShow> group = new ApiModelGroup<CookShow>(3);
                            mCookShowList.addAll(group.loadChildrenFromList(result));
                            mAllCookShowList.addAll(result.getList());
                            updateTabCount(TAB_FOCUS_COOK_SHOW, result.getCountOfServer());

                            hideEmptyView();
                            if (mCurrentPos == TAB_FOCUS_COOK_SHOW) {
                                mCookShowAdapter.notifyDataSetChanged();
                            }
                        } else {
                            if (mCurrentPos == TAB_FOCUS_COOK_SHOW) {
                                showEmptyView();
                            }
                        }

                        if (mCurrentPos == TAB_FOCUS_COOK_SHOW) {
                            hideLoading();
                        }

                    }
                });

        FoodApi.getMyFoodRecipeList(mCurrentUser.getUid(), 1,
                getLoadMore().getPageSize(), new ApiCallback<ApiModelList<Food>>() {
                    @Override
                    public void onResult(ApiModelList<Food> result) {
                        if (result.available() && !result.isEmpty()) {
                            mFoodList.clear();
                            ApiModelGroup<Food> group = new ApiModelGroup<Food>(2);
                            mFoodList.addAll(group.loadChildrenFromList(result));

                            updateTabCount(TAB_FOCUS_RECIPE, result.getCountOfServer());

                            hideEmptyView();
                            if (mCurrentPos == TAB_FOCUS_RECIPE) {
                                mFoodListAdapter.notifyDataSetChanged();
                            }
                        } else {
                            if (mCurrentPos == TAB_FOCUS_RECIPE) {
                                showEmptyView();
                            }
                        }

                        if (mCurrentPos == TAB_FOCUS_RECIPE) {
                            hideLoading();
                        }
                    }
                });
    }

    private void updateTabCount(int tab, int size) {
        updateTabCount(mHeadView, tab, size);
        updateTabCount(mListHeadView, tab, size);
    }

    private void updateTabCount(View view, int tab, int size) {
        if (tab == TAB_FOCUS_COOK_SHOW) {
            TextView tabCookShow = (TextView) view.findViewById(R.id.app_user_page_cook_show);
            tabCookShow.setText(size + " 厨艺");
        } else {
            TextView tabRecipe = (TextView) view.findViewById(R.id.app_user_page_recipe);
            tabRecipe.setText(size + " 菜谱");
        }
    }

    @Override
    protected List onLoadMoreList(BaseUIAdapter adapter, final int currentPage, final int pageSize) {

        return SyncHandler.sync(new SimpleSync<List>() {
            @Override
            public void sync(final Callback<List> callback) {

                if (mCurrentPos == TAB_FOCUS_COOK_SHOW) {
                    CookShowApi.getUserCookShowList(mCurrentUser.getUid(), currentPage,
                            pageSize, new ApiCallback<ApiModelList<CookShow>>() {
                                @Override
                                public void onResult(ApiModelList<CookShow> result) {
                                    if (result.available() && !result.isEmpty()) {
                                        ApiModelGroup<CookShow> group = new ApiModelGroup<CookShow>(3);
                                        callback.callback(group.loadChildrenFromList(result));
                                        mAllCookShowList.addAll(result.getList());
                                    } else {
                                        callback.callback(null);
                                    }
                                }
                            });
                } else {
                    FoodApi.getMyFoodRecipeList(mCurrentUser.getUid(), currentPage, pageSize,
                            new ApiCallback<ApiModelList<Food>>() {
                                @Override
                                public void onResult(ApiModelList<Food> result) {
                                    if (result.available() && !result.isEmpty()) {
                                        ApiModelGroup<Food> group = new ApiModelGroup<Food>(2);
                                        callback.callback(group.loadChildrenFromList(result));
                                    } else {
                                        callback.callback(null);
                                    }
                                }
                            });
                }


            }
        });
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);

        if (mCurrentUser != null && mCurrentUser.getUid().equals(UserProperties.getUserId())) {
            mCurrentUser.setCity(UserProperties.getUserCity());
            mCurrentUser.setGender(UserProperties.getUserGender());
            mCurrentUser.setNickname(UserProperties.getUserName());
            mCurrentUser.setBirthday(UserProperties.getUserBirthday());
            mCurrentUser.setAvatar(UserProperties.getUserAvatar());
        }

        if (mHeadView != null) {
            updateHeadView(mHeadView);
        }

        if (mListHeadView != null) {
            updateHeadView(mListHeadView);
        }
    }

    private void updateHeadView(final View view) {
        if (mCurrentUser == null) {
            return;
        }

        HaloCircleImageView userAvatar = (HaloCircleImageView) view.findViewById(R.id.app_user_page_avatar);
        TextView userName = (TextView) view.findViewById(R.id.app_user_page_name);
        TextView userLocation = (TextView) view.findViewById(R.id.app_user_page_location);

        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentUser.getUid().equals(UserProperties.getUserId())) {
                    next(UserProfileFragment.class);
                } else {
                    //显示头像
                    Intent intent = new Intent(getContext(), PictureActivity.class);
                    intent.putExtra(PictureActivity.EXTRA_URL, mCurrentUser.getAvatar());
                    getActivity().startActivity(intent);
                }
            }
        });
        ImageFetcher.asInstance().load(mCurrentUser.getAvatar(), userAvatar, R.drawable.app_pic_default_avatar);
        userName.setText(mCurrentUser.getNickname());
        userLocation.setText(mCurrentUser.getCity());

        String gender = mCurrentUser.getGender();
        if (UserProperties.USER_GENDER_BOY.equals(gender)
                || UserProperties.USER_GENDER_BOY_CODE.equals(gender)) {
            userLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.app_ic_boy, 0);
        } else {
            userLocation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.app_ic_girl, 0);
        }

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.app_user_page_view_page);
        UnderlinePageIndicator indicator = (UnderlinePageIndicator) view.findViewById(R.id.app_user_page_tab_indicator);
        indicator.setFades(false);
        viewPager.setAdapter(mViewPageAdapter);
        indicator.setViewPager(viewPager, mCurrentPos);

        view.findViewById(R.id.app_user_page_cook_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickTab(TAB_FOCUS_COOK_SHOW);
            }
        });

        view.findViewById(R.id.app_user_page_recipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickTab(TAB_FOCUS_RECIPE);
            }
        });
    }

    public void updateCurrent(int position) {
        updateCurrent(mHeadView, position);
        updateCurrent(mListHeadView, position);
    }

    public void clickTab(int position) {
        clickTab(mHeadView, position);
        clickTab(mListHeadView, position);
    }

    public void clickTab(View view, int position) {
        updateCurrent(view, position);
        mCurrentPos = position;
        if (mListView.getAdapter() != null) {

            boolean empty = false;
            if (mCurrentPos == TAB_FOCUS_COOK_SHOW) {
                empty = mCookShowList.isEmpty();
            } else {
                empty = mFoodList.isEmpty();
            }

            if (empty) {
                showEmptyView();
            } else {
                hideEmptyView();
            }
        }
    }

    @Override
    protected void onListPageChanged(int currentPage, int pageSize) {
        super.onListPageChanged(currentPage, pageSize);
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                mRefreshListLayout.onRefreshComplete();
            }
        });
    }

    public void updateCurrent(View view, int position) {
        mCurrentPos = position;
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.app_user_page_view_page);
        if (viewPager != null) {
            viewPager.setCurrentItem(position, false);
        }
        if (mCurrentPos == TAB_FOCUS_COOK_SHOW) {
            view.findViewById(R.id.app_user_page_cook_show).setSelected(true);
            view.findViewById(R.id.app_user_page_recipe).setSelected(false);
            setListAdapter(mListView, mCookShowAdapter);
            mCookShowAdapter.notifyDataSetChanged();
            getLoadMore().setOneItemWeight(3);
            getLoadMore().setPageSize(21);
        } else {
            view.findViewById(R.id.app_user_page_cook_show).setSelected(false);
            view.findViewById(R.id.app_user_page_recipe).setSelected(true);
            setListAdapter(mListView, mFoodListAdapter);
            mFoodListAdapter.notifyDataSetChanged();
            getLoadMore().setOneItemWeight(2);
            getLoadMore().setPageSize(20);
        }
    }

    @Override
    protected void showEmptyView() {
        if (mCurrentPos == TAB_FOCUS_COOK_SHOW) {
            mEmptyView.setTitle("还没有晒过厨艺");
        } else {
            mEmptyView.setTitle("还没有上传过菜谱");
        }
        mEmptyView.setVisibility(View.VISIBLE);
        mHeadView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideEmptyView() {
        mEmptyView.setVisibility(View.GONE);
        mHeadView.setVisibility(View.GONE);
    }

    public String getCurrentUserId() {
        return mCurrentUser.getUid();
    }

    public ArrayList<CookShow> getCookShowListData() {
        return mAllCookShowList;
    }
}
