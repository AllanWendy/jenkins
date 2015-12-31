package com.wecook.common.modules.asynchandler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.SparseArray;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.common.utils.StringUtils;

/**
 * UI线程Handler
 * <p/>
 * 支持循环、单一功能
 *
 * @author by kevin on 3/11/14.
 */
public class UIHandler {
    /**
     * 循环执行
     */
    private static final String ACTION_LOOP = "cn.wecook.app.ui_handler_loop";

    private static SparseArray<LoopRunnable> sLoopRunnerMap = new SparseArray<LoopRunnable>();

    private static Handler uiHandler = new Handler(Looper.getMainLooper());

    public static class UIHandlerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AndroidUtils.isMainProcess(context)) {
                String action = intent.getAction();
                if (ACTION_LOOP.equals(action)) {
                    int requestCode = intent.getIntExtra("requestCode", 0);
                    Logger.i("action loop[" + requestCode + "]");
                    Runnable runnable = sLoopRunnerMap.get(requestCode);
                    if (runnable != null) {
                        runnable.run();
                    } else {
                        stopLoop(requestCode);
                    }
                }
            }
        }
    }

    private static Context mApp;
    private static Object token = new Object();

    private UIHandler() {
    }

    public static Handler getUiHandler() {
        return uiHandler;
    }

    /**
     * 在UI线程中执行操作
     *
     * @param r 操作逻辑
     * @return
     */
    public static boolean post(Runnable r) {
        return uiHandler != null && uiHandler.post(r);
    }

    /**
     * 延时执行UI线程中的操作
     *
     * @param r           操作逻辑
     * @param delayMillis 等待时间
     * @return
     */
    public static boolean postDelayed(Runnable r, long delayMillis) {
        return uiHandler != null && uiHandler.postDelayed(r, delayMillis);
    }

    /**
     * 执行逻辑过程中仅保留一次执行
     *
     * @param r
     * @return
     */
    public static boolean postOnce(Runnable r) {
        if (uiHandler == null) {
            return false;
        }
        uiHandler.removeCallbacks(r, token);
        return uiHandler.postAtTime(r, token, SystemClock.uptimeMillis());
    }

    /**
     * 延时执行逻辑过程中仅保留一次执行
     *
     * @param r
     * @param delayMillis
     * @return
     */
    public static boolean postOnceDelayed(Runnable r, long delayMillis) {
        if (uiHandler == null) {
            return false;
        }
        uiHandler.removeCallbacks(r, token);
        return uiHandler.postAtTime(r, token, SystemClock.uptimeMillis() + delayMillis);
    }

    /**
     * 循环
     * TODO 建议使用JobScheduler替代循环方法
     * @param runnable
     * @param frequency
     */
    public static void loop(String name, LoopRunnable runnable, long frequency) {
        if (mApp == null || runnable == null) {
            return;
        }
        int requestCode = StringUtils.translateToInt(name);
        stopLoop(requestCode);
        Logger.d("request [" + name + "] hash code " + requestCode);
        sLoopRunnerMap.put(requestCode, runnable);
        runnable.run();
        Intent intent = new Intent(ACTION_LOOP);
        intent.putExtra("requestCode", requestCode);
        AlarmManager alarmManager = (AlarmManager) mApp.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mApp, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + frequency, frequency, pendingIntent);
    }


    /**
     * 停止循环
     */
    public static void stopLoop(String name) {
        if (mApp == null) {
            return;
        }
        int requestCode = StringUtils.translateToInt(name);
        stopLoop(requestCode);
    }

    /**
     * 停止循环
     */
    public static void stopLoop(int requestCode) {
        if (mApp == null) {
            return;
        }
        LoopRunnable loopRunnable = sLoopRunnerMap.get(requestCode);
        sLoopRunnerMap.remove(requestCode);
        Intent intent = new Intent(ACTION_LOOP);
        intent.putExtra("requestCode", requestCode);
        AlarmManager alarmManager = (AlarmManager) mApp.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mApp, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        if (loopRunnable != null) {
            loopRunnable.loopEnd();
        }
    }

    public static void stopLoops() {
        if (sLoopRunnerMap.size() > 0) {
            for (int i = 0; i < sLoopRunnerMap.size(); i++) {
                int key = sLoopRunnerMap.keyAt(i);
                stopLoop(key);
            }
        }
    }

    public static void stopPost(int what) {
        if (uiHandler == null) return;
        uiHandler.removeMessages(what);
    }

    public static void stopPost(Runnable runnable) {
        if (uiHandler == null) return;
        uiHandler.removeCallbacks(runnable);
    }

    public static void initInApp(Context context) {
        mApp = context.getApplicationContext();
    }

    public static interface LoopRunnable extends Runnable {

        void loopEnd();
    }
}
