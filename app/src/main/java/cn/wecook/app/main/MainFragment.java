package cn.wecook.app.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.ListUtils;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.MessageQueuePolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import cn.wecook.app.R;
import cn.wecook.app.main.dish.DishRecommendFragment;
import cn.wecook.app.main.home.HomeFragment;
import cn.wecook.app.main.kitchen.KitchenFragment;

/**
 * 主界面
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class MainFragment extends BaseFragment implements View.OnClickListener {

    public static final int TAB_FOOD = 0;
    public static final int TAB_KITCHEN = 1;
    public static final int TAB_HOME = 2;
    private KitchenFragment kitchenFragment;
    private List<Fragment> fragmentList = new ArrayList<Fragment>();
    private DishRecommendFragment dishRecommendFragment;
    private HomeFragment homeFragment;
    private ViewPager mContentViewPager;
    private int mCurrentPosition = -1;

    private View mTabFood;
    private View mTabKitchen;
    private View mTabHome;
    private TextView mTabHomeNewMark;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MessageQueuePolicy.ACTION_NEW_MESSAGE.equals(action)
                    || UserProperties.INTENT_LOGOUT.equals(action)) {
                updateNewMark();
            } else if (DishRecommendFragment.ACTION_FINISH_LOAD_FEED.equals(action)) {
                if (null != kitchenFragment) {
                    kitchenFragment.loadBackImg();
                }
            } else if (MainActivity.ACTION_RELOAD.equals(action)) {
                onResume();
            } else if (UserProperties.INTENT_LOGIN.equals(action)) {
                DishPolicy.get().translateLocalData();
            }
        }
    };
    private ContentFragmentAdapter mAdapter;
    //    private LoadingDialog mLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("launch_app", "MainFragment onCreate time:" + System.currentTimeMillis());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageQueuePolicy.ACTION_NEW_MESSAGE);
        filter.addAction(UserProperties.INTENT_LOGIN);
        filter.addAction(UserProperties.INTENT_LOGOUT);
        filter.addAction(DishRecommendFragment.ACTION_FINISH_LOAD_FEED);
        filter.addAction(MainActivity.ACTION_RELOAD);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, filter);
        return inflater.inflate(R.layout.fragment_main, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTabFood = view.findViewById(R.id.main_tab_food);
        mTabFood.setOnClickListener(this);
        mTabKitchen = view.findViewById(R.id.main_tab_kitchen);
        mTabKitchen.setOnClickListener(this);
        mTabHome = view.findViewById(R.id.main_tab_home);
        mTabHome.setOnClickListener(this);

        mTabHomeNewMark = (TextView) view.findViewById(R.id.main_tab_home_new_mark);

        mContentViewPager = (ViewPager) view.findViewById(R.id.main_content_vp);
        mContentViewPager.setOffscreenPageLimit(3);

//        if (mLoading == null) {
//            mLoading = new LoadingDialog(getContext());
//            mLoading.show();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        createContentViewPager();
        Logger.d("launch_app", "MainFragment onResume time:" + System.currentTimeMillis());
    }

    public void createContentViewPager() {
        if (mAdapter == null) {
            mAdapter = new ContentFragmentAdapter(getChildFragmentManager());
            mContentViewPager.setAdapter(mAdapter);
            mContentViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        }

        int pos = SharePreferenceProperties.get("main_current_position", TAB_FOOD);
        setCurrent(pos);
        updateCurrent(pos);
    }

    /**
     * 设置当前位置
     *
     * @param position
     */
    private void setCurrent(int position) {
        if (mContentViewPager != null && mCurrentPosition != position) {
            mContentViewPager.setCurrentItem(position);
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        switch (mCurrentPosition) {
            case TAB_FOOD:
                dishRecommendFragment.onCardIn(null);
                break;
        }
    }

    /**
     * 更新选中
     *
     * @param position
     */
    private void updateCurrent(int position) {
        BaseFragment preFragment = (BaseFragment) ListUtils.getItem(fragmentList, mCurrentPosition);
        mCurrentPosition = position;
        SharePreferenceProperties.set("main_current_position", position);
        if (preFragment != null) {
            preFragment.onCardOut();
        }
        switch (position) {
            case TAB_FOOD:
                dishRecommendFragment.onCardIn(null);
                mTabFood.setSelected(true);
                mTabKitchen.setSelected(false);
                mTabHome.setSelected(false);
                break;
            case TAB_KITCHEN:
                kitchenFragment.onCardIn(null);
                mTabFood.setSelected(false);
                mTabKitchen.setSelected(true);
                mTabHome.setSelected(false);
                break;
            case TAB_HOME:
                homeFragment.onCardIn(null);
                mTabFood.setSelected(false);
                mTabKitchen.setSelected(false);
                mTabHome.setSelected(true);
                updateNewMark();
                break;
        }
    }

    public void updateNewMark() {
        if (mTabHomeNewMark != null) {
            if (MessageQueuePolicy.getInstance().hasNewMessage()) {
                mTabHomeNewMark.setVisibility(View.VISIBLE);
                if (MessageQueuePolicy.getInstance().getNewMessageCount() > 99) {
                    mTabHomeNewMark.setText("99+");
                } else {
                    mTabHomeNewMark.setText(MessageQueuePolicy.getInstance().getNewMessageCount() + "");
                }
            } else {
                mTabHomeNewMark.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_tab_food:
                setCurrent(TAB_FOOD);
                break;
            case R.id.main_tab_kitchen:
                setCurrent(TAB_KITCHEN);
                break;
            case R.id.main_tab_home:
                setCurrent(TAB_HOME);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mCurrentPosition != TAB_FOOD) {
            setCurrent(TAB_FOOD);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 各个界面适配
     */
    private class ContentFragmentAdapter extends FragmentStatePagerAdapter {

        public ContentFragmentAdapter(FragmentManager fm) {
            super(fm);
            dishRecommendFragment = new DishRecommendFragment();
            fragmentList.add(dishRecommendFragment);

            kitchenFragment = new KitchenFragment();
            fragmentList.add(kitchenFragment);

            homeFragment = new HomeFragment();
            fragmentList.add(homeFragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {

            return super.getItemPosition(object);
        }
    }
}
