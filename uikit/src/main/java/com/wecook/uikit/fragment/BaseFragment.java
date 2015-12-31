// Copy right WeCook Inc.
package com.wecook.uikit.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.umeng.analytics.MobclickAgent;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.Api;
import com.wecook.common.modules.logstatistics.LogEvent;
import com.wecook.common.modules.logstatistics.LogTracker;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.uikit.R;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.activity.SwapCard;
import com.wecook.uikit.activity.Swappable;
import com.wecook.uikit.loader.IUIAsyncTasker;
import com.wecook.uikit.loader.UIAsyncTaskerImpl;
import com.wecook.uikit.loader.UILoader;

import java.util.HashSet;

/**
 * 抽象Fragment
 *
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public abstract class BaseFragment extends SwapCard implements IFragment, IUIAsyncTasker, Swappable {

    public static final String EXTRA_FIXED_VIEW = "extra_fixed_view";
    private static String mCurrentClickMarker;
    private HashSet<FragmentLifecycleListener> lifecycleListeners = new HashSet<FragmentLifecycleListener>();
    private LogEvent mLogEvent = LogTracker.getLogEvent(this);
    private IUIAsyncTasker mUiAsyncTasker;
    private boolean isFinishLoaded;
    private boolean isStoped;
    private boolean enableDebug = true;
    private boolean mIsCardOut;
    private boolean mIsLoading;
    private boolean mIsInited;

    public static <T extends BaseFragment> T getInstance(Class<T> fclass) {
        return (T) instantiate(BaseApp.getApplication(), fclass.getName(), null);
    }

    public static <T extends BaseFragment> T getInstance(Class<T> fclass, Bundle bundle) {
        return (T) instantiate(BaseApp.getApplication(), fclass.getName(), bundle);
    }

    public boolean isStoped() {
        return isStoped;
    }

    @Override
    public void registerFragmentLifecycleListener(FragmentLifecycleListener listener) {
        if (listener != null) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void unregisterFragmentLifecycleListener(FragmentLifecycleListener listener) {
        if (listener != null) {
            lifecycleListeners.remove(listener);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUiAsyncTasker = new UIAsyncTaskerImpl(this);
        dispatchFragmentLifecycleListener(LF_onActivityCreated);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dispatchFragmentLifecycleListener(LF_onActivityResult);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        registerFragmentLifecycleListener(this);
        dispatchFragmentLifecycleListener(LF_onAttach);
        onCardIn(null);
        Bundle bundle = getArguments();
        if (bundle != null) {
            isFixed = bundle.getBoolean(EXTRA_FIXED_VIEW, false);
            setFixed(isFixed);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dispatchFragmentLifecycleListener(LF_onConfigurationChanged);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispatchFragmentLifecycleListener(LF_onCreate);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        dispatchFragmentLifecycleListener(LF_onCreateContextMenu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        dispatchFragmentLifecycleListener(LF_onCreateOptionsMenu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispatchFragmentLifecycleListener(LF_onDestroy);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        dispatchFragmentLifecycleListener(LF_onDestroyOptionsMenu);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dispatchFragmentLifecycleListener(LF_onDestroyView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isFinishLoaded = false;
        dispatchFragmentLifecycleListener(LF_onDetach);
        unregisterFragmentLifecycleListener(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        dispatchFragmentLifecycleListener(LF_onHiddenChanged);
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        dispatchFragmentLifecycleListener(LF_onInflate);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        dispatchFragmentLifecycleListener(LF_onLowMemory);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        dispatchFragmentLifecycleListener(LF_onOptionsMenuClosed);
    }

    @Override
    public void onPause() {
        super.onPause();
        dispatchFragmentLifecycleListener(LF_onPause);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        dispatchFragmentLifecycleListener(LF_onPrepareOptionsMenu);
    }

    @Override
    public void onResume() {
        super.onResume();
        dispatchFragmentLifecycleListener(LF_onResume);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        dispatchFragmentLifecycleListener(LF_onSaveInstanceState);
    }

    @Override
    public void onStart() {
        if (!mIsInited) {
            onStartUILoad();
            startAllUILoaders();
            mIsInited = true;
        }
        super.onStart();
        dispatchFragmentLifecycleListener(LF_onStart);
    }

    @Override
    public void onStop() {
        onStopUILoad();
        stopAllUILoaders();
        super.onStop();
        dispatchFragmentLifecycleListener(LF_onStop);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dispatchFragmentLifecycleListener(LF_onViewCreated);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        dispatchFragmentLifecycleListener(LF_onViewStateRestored);
    }

    @Override
    public void onStartUILoad() {
        dispatchFragmentLifecycleListener(LF_onStartUILoad);
    }

    @Override
    public void onStopUILoad() {
        dispatchFragmentLifecycleListener(LF_onStopUILoad);
    }

    @Override
    public void onFinishedToUpdateUI(boolean success) {
        dispatchFragmentLifecycleListener(LF_onFinishedToUpdateUI);
        isFinishLoaded = true;
    }

    @Override
    public String dumpUITree() {
        return "Root:" + ((Object) this).getClass().getSimpleName() + "|";
    }

    @Override
    public void onLifecycleChanged(IFragment fragment, int lifeState) {
        if (lifeState >= 0 && lifeState < sLifecycleNames.length && enableDebug) {
            Logger.i("#" + this + " LF: " + sLifecycleNames[lifeState]);
        }
        switch (lifeState) {
            case LF_onStart:
                LogTracker.startTracking(mLogEvent);
                isStoped = false;
                break;
            case LF_onStop:
                LogTracker.stopTracking(mLogEvent);
                isStoped = true;
                break;
            case LF_onResume:
                MobclickAgent.onPageStart(((Object) this).getClass().getSimpleName());
                MobclickAgent.onResume(getContext());
                break;
            case LF_onPause:
                MobclickAgent.onPageEnd(((Object) this).getClass().getSimpleName());
                MobclickAgent.onPause(getContext());
                break;

        }
    }

    private void dispatchFragmentLifecycleListener(int lifecycleState) {
        for (FragmentLifecycleListener listener : lifecycleListeners) {
            if (listener != null) {
                listener.onLifecycleChanged(this, lifecycleState);
            }
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public final boolean addUILoader(UILoader loader) {
        return mUiAsyncTasker.addUILoader(loader);
    }

    @Override
    public final void restartUILoaders() {
        mUiAsyncTasker.restartUILoaders();
    }

    @Override
    public final void removeUILoader(UILoader loader) {
        mUiAsyncTasker.removeUILoader(loader);
    }

    @Override
    public final void removeUILoader(int id) {
        mUiAsyncTasker.removeUILoader(id);
    }

    @Override
    public final void stopAllUILoaders() {
        mUiAsyncTasker.stopAllUILoaders();
    }

    @Override
    public final void startAllUILoaders() {
        mUiAsyncTasker.startAllUILoaders();
    }

    @Override
    public final void startUILoader(UILoader loader, Bundle args) {
        mUiAsyncTasker.startUILoader(loader, args);
    }

    @Override
    public final void deliveryFinish() {
        mUiAsyncTasker.deliveryFinish();
    }

    public void finishAllLoaded(boolean finish) {
        isFinishLoaded = finish;
    }

    @Override
    public boolean isDataLoaded() {
        return isFinishLoaded;
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        dispatchFragmentLifecycleListener(LF_onCardIn);
        setFixed(isFixed);
        if (mIsCardOut) {
            mIsCardOut = false;
            onRefreshList();
        }

        Api.setRequestTag(getClass().getName());
    }

    @Override
    public void onCardOut() {
        super.onCardOut();
        dispatchFragmentLifecycleListener(LF_onCardOut);
        Api.stopAllRequest(getClass().getName());
        Api.setRequestTag(null);
        mIsCardOut = true;
    }

    @Override
    public void finishAll() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            ((BaseSwipeActivity) activity).finishAll();
        }
    }

    @Override
    public void next(SwapCard current, SwapCard card, Bundle data) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            ((BaseSwipeActivity) activity).next(current, card, data);
        }
    }

    public void next(SwapCard card) {
        next(this, card, null);
    }

    public void next(SwapCard card, Bundle data) {
        next(this, card, data);
    }

    public void next(Class<? extends BaseFragment> card) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            ((BaseSwipeActivity) activity).next(this, BaseFragment.getInstance(card), null);
        }
    }

    public void next(Class<? extends BaseFragment> card, int titleResId) {
        next(card, getString(titleResId));
    }

    public void next(Class<? extends BaseFragment> card, String title) {
        Bundle bundle = new Bundle();
        bundle.putString(BaseTitleFragment.EXTRA_TITLE, title);
        next(card, bundle);
    }

    public void next(Class<? extends BaseFragment> card, Bundle bundle) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            ((BaseSwipeActivity) activity).next(this, BaseFragment.getInstance(card, bundle), bundle);
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
            activity.overridePendingTransition(R.anim.uikit_slide_in_left, R.anim.uikit_slide_out_left);
        }
    }

    public void startActivity(Intent intent) {
        startActivity(intent, null);
    }

    public void startActivity(Intent intent, Bundle bundle) {
        Activity activity = getActivity();
        if (activity != null) {
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.uikit_slide_in_left, R.anim.uikit_slide_out_left);
        }
    }

    @Override
    public boolean isFixed() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            return ((BaseSwipeActivity) activity).isFixed();
        }
        return super.isFixed();
    }

    public void setFixed(boolean isFixedFragment) {
        super.setFixed(isFixedFragment);
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            ((BaseSwipeActivity) activity).setFixed(isFixedFragment);
        }
    }

    public boolean back() {
        return back(null);
    }

    @Override
    public boolean back(Bundle data) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            KeyboardUtils.closeKeyboard(activity, getView());
            return ((BaseSwipeActivity) activity).backSafely(data);
        }
        return false;
    }

    /**
     * 退出
     */
    public boolean finishFragment() {
        return finishFragment(null);
    }

    public boolean finishFragment(Bundle bundle) {
        KeyboardUtils.closeKeyboard(getContext(), getView());
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            return ((BaseSwipeActivity) activity).back(bundle);
        }
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return back(null);
        }
        return false;
    }

    public final String getFrom() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseSwipeActivity) {
            BaseFragment preFragment = ((BaseSwipeActivity) activity).getPreFragment();
            if (preFragment != null) {
                return preFragment.getClickMarker();
            }
        }
        return "";
    }

    public String getClickMarker() {
        return mCurrentClickMarker;
    }

    public void setClickMarker(String marker) {
        mCurrentClickMarker = marker;
    }

    public void enableDebug(boolean enable) {
        enableDebug = enable;
    }

    public void showLoading() {
        mIsLoading = true;
    }

    public void hideLoading() {
        mIsLoading = false;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public boolean isCardOuted() {
        return mIsCardOut;
    }

    public void onRefreshList() {
    }
}
