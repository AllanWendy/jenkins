package com.wecook.uikit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPagerWrapper;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.Api;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.uikit.R;
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.widget.viewpage.HeightWrappingViewPager;
import com.wecook.uikit.widget.viewpage.transforms.ConvertStackTransformer;

import java.util.List;
import java.util.Stack;

/**
 * 滑动
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/8/14
 */
public abstract class BaseSwipeActivity extends BaseFragmentActivity implements Swappable {

    private Stack<Fragment> mFragments = new Stack<Fragment>();
    private ViewPagerWrapper mViewPager;
    private SwipePageAdapter mSwipePageAdapter;
    private BaseFragment mPendingRemovedFragment;

    private Stack<Stack<SwapCard>> mCardStack = new Stack<Stack<SwapCard>>();
    private Stack<SwapCard> mCurrentCards;

    private Bundle bundle;

    private boolean mIsAppRootActivity;
    private boolean mPageTranslating;
    //是否被Home键到后台了
    private boolean mIsInBackgrounded;

    public void setIsAppRootActivity(boolean isAppRootActivity) {
        mIsAppRootActivity = isAppRootActivity;
    }

    public boolean isAppRootActivity() {
        return mIsAppRootActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onPreCreate();
        setContentView(getContentLayoutId());
        Intent intent = getIntent();
        if (intent != null) {
            bundle = intent.getExtras();
        }
        mViewPager = (ViewPagerWrapper) findViewById(R.id.uikit_swipe_viewpager);
        initAdapter();
        BaseFragment root = onCreateFragment(bundle);
        next(null, root, bundle);
    }

    /**
     * 获得内容layoutId
     *
     * @return
     */
    protected int getContentLayoutId() {
        return R.layout.uikit_activity_swipe;
    }

    protected void onPreCreate() {

    }

    private void initAdapter() {
        mSwipePageAdapter = new SwipePageAdapter(getSupportFragmentManager());
        mViewPager.setPageTransformer(true, new ConvertStackTransformer());
        mViewPager.setAdapter(mSwipePageAdapter);
        mViewPager.setOffscreenPageLimit(20);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                BaseFragment fragment = getPreFragment();
                if (fragment != null && fragment.getView() != null && fragment.getView().getBackground() == null) {
                    fragment.getView().setBackgroundColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (((HeightWrappingViewPager) mViewPager).isDragBack()) {
                    back(null);

                    BaseFragment fragment = getPreFragment();
                    if (fragment != null && fragment.getView() != null) {
                        fragment.getView().setBackgroundDrawable(null);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Fragment fragment : mFragments) {
            List<Fragment> fragments = fragment.getChildFragmentManager().getFragments();
            fragment.onActivityResult(requestCode, resultCode, data);
            if (fragments != null) {
                for (Fragment child : fragments) {
                    if (child != null && child.isVisible()) {
                        child.onActivityResult(requestCode, resultCode, data);
                    }
                }
            }
        }
    }

    class SwipePageAdapter extends FragmentStatePagerAdapter {

        public SwipePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position > 0 && position < mFragments.size()) {
                return mFragments.get(position);
            }
            return onCreateFragment(bundle);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public int getItemPosition(Object object) {
            if (mPendingRemovedFragment != null && ((Object) mPendingRemovedFragment).getClass().getName()
                    .equalsIgnoreCase(object.getClass().getName())) {
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
//            Logger.i("setPrimaryItem #pos:" + position + " #fragment:" + object);
            if (position < mFragments.size()) {
                mFragments.set(position, (Fragment) object);
            }
            if (position < mCurrentCards.size()) {
                mCurrentCards.set(position, (SwapCard) object);
            }
        }
    }

    protected abstract BaseFragment onCreateFragment(Bundle intentBundle);

    public void setFixed(boolean isFixed) {
        if (mViewPager != null) {
            mViewPager.setFixed(isFixed);
        }
    }

    public boolean isFixed() {
        if (mViewPager != null) {
            return mViewPager.isFixed();
        }
        return false;
    }

    public void next(Class<? extends BaseFragment> card) {
        next(card, null);
    }

    public void next(Class<? extends BaseFragment> card, Bundle data) {
        next(getCurrentFragment(), BaseFragment.getInstance(card, data), data);
    }

    @Override
    public void next(SwapCard currentCard, final SwapCard nextCard, Bundle data) {
        if (mPageTranslating) {
            return;
        }
        setFixed(false);
        if (nextCard != null) {

            mPageTranslating = true;
            if (currentCard != null) {
                currentCard.onCardOut();
            }
            if (mCurrentCards == null) {
                mCurrentCards = new Stack<SwapCard>();
                mCardStack.push(mCurrentCards);
            }

            if (!mCurrentCards.isEmpty()) {
                SwapCard current = mCurrentCards.peek();
                if (current != currentCard) {
                    mCurrentCards = new Stack<SwapCard>();
                    mCurrentCards.push(currentCard);
                    mCardStack.push(mCurrentCards);
                }
            }

            mCurrentCards.push(nextCard);

            Fragment fragment = nextCard.getFragment();
            if (fragment != null) {
                mFragments.push(fragment);
                if (mViewPager != null && mViewPager.getAdapter() != null) {
                    mViewPager.getAdapter().notifyDataSetChanged();
                    mViewPager.setCurrentItem(mFragments.size() - 1);
                }
            }
            mPageTranslating = false;
        }
    }

    @Override
    public boolean back(Bundle data) {
        if (mFragments.size() > 1) {
            if (mPageTranslating) {
                return false;
            }
            mPageTranslating = true;
            if (mCardStack.size() > 0 && !mCurrentCards.isEmpty()) {
                SwapCard current = mCurrentCards.pop();
                current.onCardOut();
                if (mCurrentCards.isEmpty()) {
                    mCardStack.remove(mCurrentCards);
                    if (!mCardStack.isEmpty()) {
                        mCurrentCards = mCardStack.peek();
                        if (!mCurrentCards.isEmpty()) {
                            SwapCard realCurrent = mCurrentCards.pop();
                            realCurrent.onCardOut();
                        }
                    }
                }
            }
            mPendingRemovedFragment = (BaseFragment) mFragments.peek();

            if (mViewPager.getAdapter() != null) {
                mViewPager.setCurrentItem(mFragments.size() - 2);
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mFragments.isEmpty()) {
                            mFragments.pop();
                        }
                        mViewPager.getAdapter().notifyDataSetChanged();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.remove(mPendingRemovedFragment);
                        ft.commit();
                        mPendingRemovedFragment = null;
                    }
                });
            }

            if (!mCurrentCards.isEmpty()) {
                SwapCard pre = mCurrentCards.peek();
                pre.onCardIn(data);
            }
            mPageTranslating = false;
            return true;
        } else {
            if (!mIsAppRootActivity) {
                finishAll();
            }
        }
        return false;
    }

    /**
     * 返回
     *
     * @param data
     * @return
     */
    public boolean backSafely(Bundle data) {
        if (isFixed()) {
            return true;
        }
        return back(data);
    }

    @Override
    public void finishAll() {
        if (!mIsAppRootActivity) {
            mFragments.clear();
            mCardStack.clear();
            if (mSwipePageAdapter != null) {
                mSwipePageAdapter.notifyDataSetChanged();
            }
            mViewPager = null;
            mSwipePageAdapter = null;
            mPendingRemovedFragment = null;
            bundle = null;
            if (mCurrentCards != null) {
                mCurrentCards.clear();
            }
            finish();
        } else {
            back(null);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.i("Activity onRestart mIsInBackgrounded = " + mIsInBackgrounded);
        if (mIsInBackgrounded && AndroidUtils.isMainForeground(this)) {
            //已置后台，启动之后需要重新
            Api.init(getContext(), Api.getGlobalConfig());
        }
        mIsInBackgrounded = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!AndroidUtils.isMainForeground(this)) {
            //已置后台，启动之后需要重新
            mIsInBackgrounded = true;
            Logger.i("Activity onStop mIsInBackgrounded = " + mIsInBackgrounded);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!AndroidUtils.isMainProcess(this)) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public BaseFragment getPreFragment() {
        if (mCurrentCards != null && mCurrentCards.size() >= 2) {
            return (BaseFragment) mCurrentCards.elementAt(mCurrentCards.size() - 2).getFragment();
        }
        return null;
    }

    public BaseFragment getCurrentFragment() {

        if (mCurrentCards != null && !mCurrentCards.isEmpty()) {
            if (mCurrentCards.size() > 0) {
                return (BaseFragment) mCurrentCards.peek().getFragment();
            }
        }
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        List<Fragment> list = getSupportFragmentManager().getFragments();
        if (list != null && !mFragments.isEmpty()) {
            BaseFragment top = (BaseFragment) mFragments.peek();
            for (Fragment fragment : list) {
                BaseFragment f = (BaseFragment) fragment;
                if (f != null
                        && ((Object) f).getClass().getName().equals(((Object) top).getClass().getName())
                        && f.onKeyDown(keyCode, event)) {
                    return true;
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (back(null)) {
                return true;
            } else {
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
