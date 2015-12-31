package com.wecook.common.modules.asynchandler;

/**
 * 简单实现的同步接口
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/16/14
 */
public abstract class SimpleSync<T> implements SyncHandler.Sync<T> {

    private static final int MAX_WAITING_TIME = 10 * 1000;//最长等待10秒

    private boolean waiting;

    private int waitingCount;

    private T object;

    @Override
    public final void syncStart() {
        waiting = true;
        waitingCount = 0;
        sync(new Callback<T>() {
            @Override
            public void callback(T obj) {
                object = obj;
                waiting = false;
            }
        });
    }

    @Override
    public final boolean waiting() {
        waitingCount++;
        if (waitingCount > MAX_WAITING_TIME / SyncHandler.WAIT_TIME) {
            waiting = false;
        }
        return waiting;
    }

    @Override
    public final T syncEnd() {
        return object;
    }

    public abstract void sync(Callback<T> callback);

    public static interface Callback<T> {
        public void callback(T obj);
    }
}
