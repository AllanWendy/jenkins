package cn.wecook.app.main.recommend.list.cookshow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wecook.sdk.api.legacy.CookShowApi;
import com.wecook.sdk.policy.LogGather;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;
import com.wecook.uikit.widget.indicator.UnderlinePageIndicator;

import cn.wecook.app.R;
import cn.wecook.app.features.pick.PickActivity;

/**
 * 晒厨艺列表
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/17/14
 */
public class CookShowPageListFragment extends BaseTitleFragment {

    public static final String EXTRA_FOCUS_TAB = "extra_focus_tab";
    public static final String EXTRA_START_PAGE = "extra_start_page";

    /** 精选 */
    public static final int TAB_FOCUS_HOTTEST = CookShowApi.SORT_HOT;

    /** 最新 */
    public static final int TAB_FOCUS_NEWEST = CookShowApi.SORT_NEW;

    private ViewPager mViewPager;
    private UnderlinePageIndicator mIndicator;
    private FragmentPagerAdapter mAdapter;

    private int mCurrentPos;
    private int mFocusTab = TAB_FOCUS_HOTTEST;
    private int mStartPage = 1;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CookShowApi.ACTION_COOK_SHOW.equals(action)) {
                setCurrent(1);
            }
        }
    };
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFocusTab = bundle.getInt(EXTRA_FOCUS_TAB, TAB_FOCUS_HOTTEST);
            mStartPage = bundle.getInt(EXTRA_START_PAGE, 1);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(CookShowApi.ACTION_COOK_SHOW);
        LocalBroadcastManager.getInstance(activity).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_cook_show_list, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.app_title_cook_show_list));
        titleBar.enableBottomDiv(true);
        titleBar.setBottomDivLineColor(getResources().getColor(R.color.uikit_grey));
        titleBar.setBackDrawable(R.drawable.uikit_bt_back_pressed);

        final TitleBar.ActionImageView cookShow = new TitleBar.ActionImageView(getContext(), R.drawable.app_ic_upload_food);
        cookShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogGather.setLogMarker(LogGather.MARK.FROM, "晒厨艺列表");
                LogGather.onEventCookShowIn();

                Intent intent = new Intent(getContext(), PickActivity.class);
                intent.putExtra(PickActivity.EXTRA_PICK_TYPE, PickActivity.PICK_COOK_SHOW);
                startActivity(intent);
            }
        });
        titleBar.addActionView(cookShow);

        mViewPager = (ViewPager) view.findViewById(R.id.app_cook_show_view_page);
        mIndicator = (UnderlinePageIndicator) view.findViewById(R.id.app_cook_show_tag_indicator);
        mIndicator.setFades(false);
        mAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Bundle bundleTo = new Bundle();
                bundleTo.putInt(CookShowListFragment.EXTRA_TYPE, CookShowListFragment.TYPE_TWO_TAB_COOK_SHOW_LIST);
                bundleTo.putInt(CookShowListFragment.EXTRA_START_PAGE, mStartPage);
                switch (position) {
                    case 0:
                        bundleTo.putInt(CookShowListFragment.EXTRA_SORT_TYPE, CookShowListFragment.SORT_TYPE_HOTTEST);
                        break;
                    case 1:
                        bundleTo.putInt(CookShowListFragment.EXTRA_SORT_TYPE, CookShowListFragment.SORT_TYPE_NEWEST);
                        break;
                }
                return BaseFragment.getInstance(CookShowListFragment.class, bundleTo);
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        mViewPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mViewPager, 0);
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateCurrent(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        view.findViewById(R.id.app_cook_show_tag_hot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrent(0);
            }
        });

        view.findViewById(R.id.app_cook_show_tag_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrent(1);
            }
        });

        if (mFocusTab == TAB_FOCUS_NEWEST) {
            setCurrent(1);
        } else {
            setCurrent(0);
        }
    }

    public void setCurrent(int position) {
        if (mCurrentPos != position) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(position, false);
            }
        }
    }

    public void updateCurrent(int position) {
        mCurrentPos = position;
    }
}
