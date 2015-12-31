package cn.wecook.app.main.kitchen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.sdk.policy.KitchenHomePolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.BaseListFragment;
import com.wecook.uikit.widget.TitleBar;

import java.util.List;

import cn.wecook.app.R;

/**
 * 我的食材
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/10/14
 */
public class KitchenHomeFragment extends BaseListFragment {

    private ViewPager mViewPager;
    private KitchenHomeViewAdapter mAdapter;
    private KitchenHomeChildFragment[] fragments;
    private TitleBar mTitleBar;
    private int mCurrentPage = -1;
    private TitleBar.ActionCoveredTextView clear;


    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UserProperties.INTENT_LOGIN.equals(intent.getAction())
                    || UserProperties.INTENT_LOGOUT.equals(intent.getAction())) {
                onStartUILoad();
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserProperties.INTENT_LOGIN);
        filter.addAction(UserProperties.INTENT_LOGOUT);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUserReceiver, filter);
        return inflater.inflate(R.layout.fragment_kitchen_home, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_page", mCurrentPage);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt("current_page", 0);
        }
        mViewPager = (ViewPager) view.findViewById(R.id.app_kitchen_home_viewpager);
        mTitleBar = getTitleBar();
        mTitleBar.setTitle(getString(R.string.app_title_kitchen_home));

        ScreenUtils.rePadding(mTitleBar.getTitleView(), 0, 0, 0, 5);

        clear = new TitleBar.ActionCoveredTextView(getContext(), getString(R.string.app_button_title_clear));
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KitchenHomePolicy.getInstance().isInEditor()) {
                    KitchenHomePolicy.getInstance().setInEditor(false);
                    clear.setText(R.string.app_button_title_clear);
                    mTitleBar.setTitle(getString(R.string.app_title_kitchen_home));
                    KitchenHomePolicy.getInstance().updateChangeList();
                    mTitleBar.enableBack(true);
                    mTitleBar.setBackTitle("");
                    mTitleBar.setBackTitleClickListener(null);
                } else {
                    KitchenHomePolicy.getInstance().setInEditor(true);
                    clear.setText(R.string.app_button_title_finish);
                    mTitleBar.setTitle("已选（0）");
                    mTitleBar.enableBack(false);
                    mTitleBar.setBackTitle(R.string.app_button_title_cancel);
                    mTitleBar.setBackTitleClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            KitchenHomePolicy.getInstance().clearChangeList();
                            clear.performClick();
                        }
                    });
                }

                for (Fragment fragment : getChildFragmentManager().getFragments()) {
                    if (fragment instanceof KitchenHomeChildFragment) {
                        ((KitchenHomeChildFragment) fragment).onEditorMode();
                    }
                }
            }
        });

        mTitleBar.addActionView(clear);

        mAdapter = new KitchenHomeViewAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTitleBar.setViewPager(mViewPager, new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (mCurrentPage != -1) {
            mViewPager.setCurrentItem(mCurrentPage);
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        if (!KitchenHomePolicy.getInstance().isPrepared()) {
            showLoading();
        }

        KitchenHomePolicy.getInstance().prepare(getContext(), new KitchenHomePolicy.OnPreparedListener() {
            @Override
            public void onPrepared(boolean result) {
                checkTitleBarEditorState();
                mAdapter.notifyDataSetChanged();
            }
        });

        KitchenHomePolicy.getInstance().setOnDataChangedListener(new KitchenHomePolicy.OnDataChangedListener() {
            @Override
            public void onResult(int action, boolean success, Object data) {
                if (action == KitchenHomePolicy.ACTION_SELECT_ITEM) {
                    if (KitchenHomePolicy.getInstance().isInEditor()) {
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTitleBar.setTitle("已选（" + KitchenHomePolicy.getInstance().getSelectedCount() + "）");
                            }
                        });
                    }
                } else if (action == KitchenHomePolicy.ACTION_SYNC_PUSH) {
                    if (data != null && data instanceof List[]) {
                        List[] lists = (List[]) data;
                        if (lists[0].size() > 0 && lists[1].size() > 0) {
                            ToastAlarm.show(getString(R.string.app_tip_sync_resource_success_both,
                                    lists[0].size(), lists[1].size()));
                        } else if (lists[0].size() > 0 && lists[1].size() == 0) {
                            ToastAlarm.show(getString(R.string.app_tip_sync_resource_success_add,
                                    lists[0].size()));
                        } else if (lists[0].size() == 0 && lists[1].size() > 0) {
                            ToastAlarm.show(getString(R.string.app_tip_sync_resource_success_remove,
                                    lists[1].size()));
                        }

                        for (Fragment fragment : getChildFragmentManager().getFragments()) {
                            if (fragment instanceof KitchenHomeChildFragment) {
                                ((KitchenHomeChildFragment) fragment).onEditorMode();
                            }
                        }
                    }
                } else if (action == KitchenHomePolicy.ACTION_BATCH_REMOVE
                        || action == KitchenHomePolicy.ACTION_REMOVE
                        || action == KitchenHomePolicy.ACTION_ADD
                        || action == KitchenHomePolicy.ACTION_BATCH_ADD) {
                    checkTitleBarEditorState();
                    for (Fragment fragment : getChildFragmentManager().getFragments()) {
                        if (fragment instanceof KitchenHomeChildFragment) {
                            ((KitchenHomeChildFragment) fragment).onEditorMode();
                        }
                    }
                }
            }
        });
    }

    private void checkTitleBarEditorState() {
        if (getContext() != null) {
            int count = KitchenHomePolicy.getInstance().getAllLocalCount();
            if (count == 0) {
                clear.setTextColor(getResources().getColor(R.color.uikit_bt_default_font));
                clear.setEnabled(false);
            } else {
                clear.setEnabled(true);
                clear.setTextColor(getResources().getColor(R.color.uikit_bt_orange_white_font));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KitchenHomePolicy.getInstance().release(getContext());
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUserReceiver);
    }

    /**
     * 厨房我的食材界面适配
     */
    private class KitchenHomeViewAdapter extends FragmentPagerAdapter {

        private String[] titles;

        public KitchenHomeViewAdapter(FragmentManager fm) {
            super(fm);
            fragments = new KitchenHomeChildFragment[4];
            titles = getResources().getStringArray(R.array.app_kitchen_home_items_name);
            fragments[0] = BaseFragment.getInstance(KitchenHomeChildIngredientFragment.class);
            fragments[1] = BaseFragment.getInstance(KitchenHomeChildCondimentFragment.class);
            fragments[2] = BaseFragment.getInstance(KitchenHomeChildKitchenwareFragment.class);
            fragments[3] = BaseFragment.getInstance(KitchenHomeChildBarcodeFragment.class);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

}
