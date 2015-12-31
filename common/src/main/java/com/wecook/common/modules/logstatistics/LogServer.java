package com.wecook.common.modules.logstatistics;

import android.app.Service;
import android.content.Intent;

import com.wecook.common.core.debug.Logger;

/**
 * 日志发送服务
 *
 * @author kevin created at 9/24/14
 * @version 1.0
 */
public abstract class LogServer extends Service {

    private static final String EXTRA_LOGEVENT = "extra_log_event";

    private boolean mSendSingerLog;

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.d("onCreate....");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ILogEvent pendingToSendLogEvent = null;
        if (intent != null) {
            pendingToSendLogEvent = (LogEvent) intent.getSerializableExtra(EXTRA_LOGEVENT);
        }

        if (pendingToSendLogEvent == null) {
            pendingToSendLogEvent = LogTracker.asInstance().getRootLogEvent();
        } else {
            mSendSingerLog = true;
        }

        preformSend(pendingToSendLogEvent);

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 执行发送日志
     * @param event
     */
    protected void preformSend(ILogEvent event) {

        if (mSendSingerLog) {
            Logger.i("[LogServer]$ event:" + event.getSingleLogMessage());
        } else {
            Logger.i("[LogServer]$ event:" + event.getLogMessage());
        }

        LogTracker.clean(event);

    }

    /**
     * 默认发送全部日志
     */
    public static void start() {
//        Intent intent = new Intent(LogTracker.asInstance().getContext(), LogTracker.asInstance().getLogServerClass());
//        LogTracker.asInstance().getContext().startService(intent);
    }

    /**
     * 制定日志类型进行发送
     * @param event
     */
    public static void start(ILogEvent event) {
//        Intent intent = new Intent(LogTracker.asInstance().getContext(), LogTracker.asInstance().getLogServerClass());
//        intent.putExtra(EXTRA_LOGEVENT, event);
//        LogTracker.asInstance().getContext().startService(intent);
    }


}
