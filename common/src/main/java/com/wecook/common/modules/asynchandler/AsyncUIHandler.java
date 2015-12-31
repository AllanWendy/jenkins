package com.wecook.common.modules.asynchandler;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.wecook.common.core.debug.Logger;

/**
 * 异步操作处理UI事件
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/18/14
 */
public class AsyncUIHandler {

    private static Handler asyncHandler;

    private static HandlerThread sMainThread;

    static {
        sMainThread = new AsyncHandlerThread("async-ui-handler");
        sMainThread.start();
        asyncHandler = new AsyncHandler(sMainThread.getLooper());
    }

    private AsyncHandlerThread mHandlerThread;
    private Handler mHandler;

    private AsyncUIHandler() {
    }

    /**
     * 提交一个异步工作请求
     *
     * @param job
     */
    public static void post(AsyncJob job) {
        if (job != null) {
            job.isParallel = false;
            asyncHandler.post(job);
        }
    }

    /**
     * 提交一个异步工作请求
     *
     * @param job
     */
    public static void post(AsyncJob job, AsyncUIHandler selfHandler) {
        if (job != null) {
            job.isParallel = false;
        }
        if (selfHandler != null && selfHandler.inLoop()) {
            selfHandler.mHandler.post(job);
        } else {
            if (job != null) {
                job.postUi();
            }
        }

    }

    /**
     * 提交一个顺序执行的任务
     *
     * @param job
     */
    public static void postParallel(AsyncJob job) {
        if (job != null) {
            job.isParallel = true;
            asyncHandler.post(job);
        }
    }

    /**
     * 提交一个顺序执行的任务
     *
     * @param job
     */
    public static void postParallel(AsyncJob job, AsyncUIHandler selfHandler) {
        if (job != null) {
            job.isParallel = true;
        }

        if (selfHandler != null && selfHandler.inLoop()) {
            selfHandler.mHandler.post(job);
        } else {
            if (job != null) {
                job.postUi();
            }
        }
    }

    public static AsyncUIHandler asNewHandler(String threadName) {
        AsyncUIHandler handler = new AsyncUIHandler();
        handler.mHandlerThread = new AsyncHandlerThread(threadName);
        handler.mHandlerThread.start();
        handler.mHandler = new AsyncHandler(handler.mHandlerThread.getLooper());
        return handler;
    }

    public boolean inLoop() {
        return mHandler != null && mHandlerThread != null && mHandlerThread.isPrepared();
    }

    public void quit() {
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
    }

    /**
     * 异步工作对象
     */
    public static abstract class AsyncJob implements Runnable {

        private boolean isParallel;

        public void postUi() {
            Logger.i("async-uihander", "post ui...");
        }
    }

    private static class AsyncHandlerThread extends HandlerThread {

        boolean mHasPrepared;

        public AsyncHandlerThread(String name) {
            super(name);
        }

        public boolean isPrepared() {
            return mHasPrepared;
        }

        @Override
        protected void onLooperPrepared() {
            mHasPrepared = true;
        }

        @Override
        public boolean quit() {
            mHasPrepared = false;
            return super.quit();
        }

        @Override
        public boolean quitSafely() {
            mHasPrepared = false;
            if (Build.VERSION.SDK_INT >= 18) {
                return super.quitSafely();
            }
            return quit();
        }
    }

    private static class AsyncHandler extends Handler {

        private AsyncHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void dispatchMessage(Message msg) {
            handleMessage(msg);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                final Runnable callback = msg.getCallback();
                if (callback != null && callback instanceof AsyncJob) {
                    if (!((AsyncJob) callback).isParallel) {
                        AsyncTask<AsyncJob, Void, AsyncJob> asyncTask = new AsyncTask<AsyncJob, Void, AsyncJob>() {
                            @Override
                            protected AsyncJob doInBackground(AsyncJob... params) {
                                if (params != null) {
                                    params[0].run();
                                    return params[0];
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(final AsyncJob job) {
                                super.onPostExecute(job);
                                if (job != null) {
                                    UIHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            job.postUi();
                                        }
                                    });
                                }
                            }
                        };
                        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (AsyncJob) callback);
                    } else {
                        callback.run();
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ((AsyncJob) callback).postUi();
                            }
                        });
                    }
                }
            }
        }
    }
}
