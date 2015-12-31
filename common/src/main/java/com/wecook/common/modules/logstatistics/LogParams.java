package com.wecook.common.modules.logstatistics;

import android.os.SystemClock;

import java.io.Serializable;

/**
 * 日志属性
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/28/14
 */
public class LogParams implements Serializable {
    public String logName;
    public long startTime;
    public long endTime;
    public double duration;
    public ILogEvent parent;

    public LogParams(String name) {
        logName = name;
    }

    public long durationNow() {
        if (startTime == 0) {
            return 0;
        }
        return SystemClock.uptimeMillis() - startTime;
    }
}
