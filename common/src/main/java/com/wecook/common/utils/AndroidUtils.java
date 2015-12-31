package com.wecook.common.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Android工具
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/26/14
 */
public class AndroidUtils {

    public static Object getMetaDataFromApplication(Context context, String key) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null
                    && applicationInfo.metaData != null
                    && applicationInfo.metaData.containsKey(key)) {
                return applicationInfo.metaData.get(key);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isMainProcess(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return context.getPackageName().equals(appProcess.processName);
            }
        }
        return false;
    }

    public static int getMainProcessId(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (context.getPackageName().equals(appProcess.processName)) {
                return appProcess.pid;
            }
        }
        return -1;
    }

    public static List<Integer> getAllProcessId(Context context, String[] filterProcessNames) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = activityManager.getRunningAppProcesses();
        List<Integer> processList = new ArrayList<Integer>();
        for (ActivityManager.RunningAppProcessInfo appProcess : runningAppProcessInfos) {
            if (filterProcessNames == null || Arrays.binarySearch(filterProcessNames, appProcess.processName) < 0) {
                processList.add(appProcess.pid);
            }
        }
        return processList;
    }

    public static android.content.pm.ActivityInfo[] getActivityInfos(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return info.activities;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 主进程在前台显示
     * @param context
     * @return
     */
    public static boolean isMainForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (context.getPackageName().equals(appProcess.processName)) {
                return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }

    /**
     * 主进程在后台
     * @param context
     * @return
     */
    public static boolean isMainBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (context.getPackageName().equals(appProcess.processName)) {
                return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
            }
        }
        return false;
    }

    public static boolean isUIThread() {
        return Thread.currentThread().equals(Looper.getMainLooper().getThread());
    }
}
