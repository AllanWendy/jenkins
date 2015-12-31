package com.wecook.common.modules.asynchandler;

/**
 * 同步助手
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/13/14
 */
public class SyncHandler{

    public static final int WAIT_TIME = 500;

    public static <T> T sync(final Sync<T> in) {
        if (in != null) {
            in.syncStart();
            while (in.waiting()) {
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return in.syncEnd();
        }
        return null;
    }

    public static interface Sync<T> {
        public void syncStart();
        public boolean waiting();
        public T syncEnd();
    }

}
