package cn.wecook.app.main.recommend.list.cookshow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.api.legacy.ReportApi;
import com.wecook.sdk.api.model.CookShow;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogConstant;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.loader.LoadMore;
import com.wecook.uikit.loader.LoadMoreImpl;
import com.wecook.uikit.loader.UILoader;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshBase;
import com.wecook.uikit.widget.pulltorefresh.PullToRefreshViewPager;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ActionDialog;
import cn.wecook.app.main.home.user.UserLoginActivity;
import cn.wecook.app.main.recommend.CookShowPublishFragment;
import cn.wecook.app.main.recommend.detail.cookshow.CookShowDetailFragment;

/**
 * 晒厨艺详情列表
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/4/15
 */
public class CookShowDetailListFragment extends BaseTitleFragment {

    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_CURRENT_PAGE = "extra_page";
    public static final String EXTRA_PAGE_SIZE = "extra_page_size";
    public static final String EXTRA_CURRENT_POS = "extra_current_position";
    public static final String EXTRA_LIST = "extra_list";

    private PullToRefreshViewPager pullToRefreshViewPager;
    private ViewPager viewPager;
    private ViewPageAdapter adapter;

    private int current;
    private String userId;
    private int page;
    private int pageSize;
    private List<CookShow> list = new ArrayList<CookShow>();
    private List<CookShow> extraList;

    private LoadMore<List<CookShow>> loadMore;
    private int mCurrentPosition;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getString(EXTRA_USER_ID);
            page = bundle.getInt(EXTRA_CURRENT_PAGE);
            pageSize = bundle.getInt(EXTRA_PAGE_SIZE);
            extraList = (List<CookShow>) bundle.getSerializable(EXTRA_LIST);
            current = bundle.getInt(EXTRA_CURRENT_POS);
            if (extraList != null) {
                list.addAll(extraList);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cook_show_detail_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar.ActionTextView moreView = new TitleBar.ActionTextView(getContext(), "更多");
        moreView.setTextColor(getResources().getColor(R.color.uikit_font_orange));
        moreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CookShow cookShow = ListUtils.getItem(list, mCurrentPosition);
                if (cookShow != null) {
                    showActionDialog(cookShow);
                }
            }
        });
        getTitleBar().addActionView(moreView);
        pullToRefreshViewPager = (PullToRefreshViewPager) view.findViewById(R.id.app_cook_show_detail_view_pager);
        pullToRefreshViewPager.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ViewPager>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ViewPager> refreshView) {
                //刷新
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ViewPager> refreshView) {
                //加载更多
                loadMore.doLoadMore(true);
            }
        });
        viewPager = pullToRefreshViewPager.getRefreshableView();

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                CookShow cookShow = ListUtils.getItem(list, position);
                if (cookShow != null) {
                    getTitleBar().setTitle(cookShow.getTitle());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        adapter = new ViewPageAdapter(getChildFragmentManager()) {
            private Fragment removeFragment;
            @Override
            public Fragment getItem(int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(CookShowDetailFragment.EXTRA_COOK_SHOW, list.get(position));
                bundle.putBoolean(CookShowDetailFragment.EXTRA_HIDE_TITLE_BAR, true);
                return BaseFragment.getInstance(CookShowDetailFragment.class, bundle);
            }

            @Override
            public int getItemPosition(Object object) {
                if (removeFragment == object) {
                    return POSITION_NONE;
                }
                return super.getItemPosition(object);
            }

            @Override
            public void remove(Object o) {
                super.remove(o);
                int removeIndex = list.indexOf(o);
                removeFragment = getItem(removeIndex);
                list.remove(o);
                if (extraList != null) {
                    extraList.remove(o);
                }
            }

            @Override
            public int getCount() {
                if (list != null) {
                    return list.size();
                }
                return 0;
            }
        };

        loadMore = new LoadMoreImpl<List<CookShow>>();
        loadMore.setCurrentPage(page);
        loadMore.setPageSize(pageSize);
        loadMore.setOnLoadMoreListener(new LoadMore.OnLoadMoreListener<List<CookShow>>() {
            @Override
            public void onLoaded(boolean success, List<CookShow> o) {
                if (success) {
                    list.addAll(o);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        loadMore.setMoreLoader(new UILoader<List<CookShow>>(this) {
            @Override
            public List<CookShow> runBackground() {
                return SyncHandler.sync(this);
            }

            @Override
            public void sync(final Callback<List<CookShow>> callback) {
                super.sync(callback);
                CookShowApi.getUserCookShowList(userId, loadMore.getCurrentPage(),
                        loadMore.getPageSize(), new ApiCallback<ApiModelList<CookShow>>() {
                            @Override
                            public void onResult(ApiModelList<CookShow> result) {
                                if (result.available() && !result.isEmpty()) {
                                    callback.callback(result.getList());
                                } else {
                                    callback.callback(null);
                                }
                            }
                        });
            }
        });

        loadMore.setListAdapter(adapter, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.setAdapter(adapter);
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(current, false);
                    }
                });
            }
        }, 300);
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 显示更多的操作
     */
    private void showActionDialog(final CookShow cookShow) {
        final ActionDialog dialog = new ActionDialog(getContext());
        boolean mIsSelfCookShow = false;
        User user = cookShow.getUser();
        if (user != null
                && UserProperties.isLogin()
                && user.getUid().equals(UserProperties.getUserId())) {
            mIsSelfCookShow = true;
        }

        if (mIsSelfCookShow) {
            dialog.addAction(R.string.app_cook_show_detail_edit_delete, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CookShowApi.deleteCookShow(cookShow.getId(), new ApiCallback<State>() {
                        @Override
                        public void onResult(State result) {
                            if (result.available()) {
                                MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_DELETE);
                                ToastAlarm.show(R.string.app_tip_delete_success);
                                adapter.remove(cookShow);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                    dialog.dismiss();
                }
            });
            dialog.addAction(R.string.app_cook_show_detail_edit_reset, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_EDIT);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(CookShowPublishFragment.EXTRA_PUBLISH_COOK_SHOW, cookShow);
                    next(CookShowPublishFragment.class, bundle);
                }
            });
        } else {
            dialog.addAction(R.string.app_cook_show_detail_edit_report, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UserProperties.isLogin()) {
                        ReportApi.reportIssue(ReportApi.TYPE_COOKING, cookShow.getId(), new ApiCallback<State>() {
                            @Override
                            public void onResult(State result) {
                                if (result.available()) {
                                    MobclickAgent.onEvent(getContext(), LogConstant.COOK_DETAIL_REPORT);
                                    ToastAlarm.show(R.string.app_tip_report_success);
                                } else {
                                    ToastAlarm.show(result.getErrorMsg());
                                }
                            }
                        });
                    } else {
                        startActivity(new Intent(getContext(), UserLoginActivity.class));
                    }

                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    private class ViewPageAdapter extends FragmentStatePagerAdapter implements Adapter {

        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int getCount() {
            return 0;
        }

        public void remove(Object o) {

        }
    }
}
