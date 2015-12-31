package com.wecook.uikit.fragment;

/**
 * Fragment生命周期监听器
 *
 * @author kevin created at 9/19/14
 * @version 1.0
 */
public interface FragmentLifecycleListener {

    public static final int LF_onCreate = 0;
    public static final int LF_onActivityCreated = 1;
    public static final int LF_onActivityResult = 2;
    public static final int LF_onAttach = 3;
    public static final int LF_onConfigurationChanged = 4;
    public static final int LF_onCreateContextMenu = 5;
    public static final int LF_onCreateOptionsMenu = 6;
    public static final int LF_onDestroy = 7;
    public static final int LF_onDestroyOptionsMenu = 8;
    public static final int LF_onDestroyView = 9;
    public static final int LF_onDetach = 10;
    public static final int LF_onHiddenChanged = 11;
    public static final int LF_onInflate = 12;
    public static final int LF_onLowMemory = 13;
    public static final int LF_onOptionsMenuClosed = 14;
    public static final int LF_onPause = 15;
    public static final int LF_onPrepareOptionsMenu = 16;
    public static final int LF_onResume = 17;
    public static final int LF_onSaveInstanceState = 18;
    public static final int LF_onStart = 19;
    public static final int LF_onStop = 20;
    public static final int LF_onViewCreated = 21;
    public static final int LF_onViewStateRestored = 22;
    public static final int LF_onStartUILoad = 23;
    public static final int LF_onStopUILoad = 24;
    public static final int LF_onFinishedToUpdateUI = 25;
    public static final int LF_onCardIn = 26;
    public static final int LF_onCardOut = 27;

    public static String[] sLifecycleNames = {
            "onCreate",
            "onActivityCreated",
            "onActivityResult",
            "onAttach",
            "onConfigurationChanged",
            "onCreateContextMenu",
            "onCreateOptionsMenu",
            "onDestroy",
            "onDestroyOptionsMenu",
            "onDestroyView",
            "onDetach",
            "onHiddenChanged",
            "onInflate",
            "onLowMemory",
            "onOptionsMenuClosed",
            "onPause",
            "onPrepareOptionsMenu",
            "onResume",
            "onSaveInstanceState",
            "onStart",
            "onStop",
            "onViewCreated",
            "onViewStateRestored",
            "onStartUILoad",
            "onStopUILoad",
            "onFinishedToUpdateUI",
            "onCardIn",
            "onCardOut",
    };
    /**
     * 生命周期变化
     *
     * @param fragment
     * @param lifeState
     */
    public void onLifecycleChanged(IFragment fragment, int lifeState);
}
