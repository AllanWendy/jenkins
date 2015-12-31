package com.wecook.common.core.internet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kevin
 * @version v1.0
 * @since 2015-15/9/15
 */
public class ApiModelParser {

    private static final int CORE_POOL_SIZE = 20;
    private static final int MAX_POOL_SIZE = 20 * 2 + 1;
    private static final int KEEP_ALIVE = 2;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Parser #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);

    private static final Executor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    private static ApiModelParser sParser = new ApiModelParser();

    public static ApiModelParser get() {
        return sParser;
    }

    public <T extends ApiModel> void parse(T item, String json) {
        THREAD_POOL_EXECUTOR.execute(new ParserRunnable<T>(item, json));
    }

    private class ParserRunnable<T extends ApiModel> implements Runnable {
        private T item;
        private String json;

        public ParserRunnable(T item, String json) {
            this.item = item;
            this.json = json;
        }

        @Override
        public void run() {

        }
    }
}
