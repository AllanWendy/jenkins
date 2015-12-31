package com.wecook.common.modules.logstatistics;

import android.os.SystemClock;

import com.wecook.common.core.debug.Logger;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 日志事件
 *
 * @author kevin created at 9/24/14
 * @version 1.0
 */
public class LogEvent implements ILogEvent {

    private boolean mRecording;

    private Map<String,ILogEvent> mChildLogEvents = Collections.synchronizedMap(new Hashtable<String, ILogEvent>());

    private Map<String, Integer> mClickEvents = Collections.synchronizedMap(new HashMap<String, Integer>());

    private LogParams logParams;

    private boolean isRoot;

    LogEvent(String name) {
        logParams = new LogParams(name);
    }

    /**
     * 开始日志追踪
     */
    public void begin() {
        Logger.d(logParams.logName + "...begin()..../n");
        mRecording = true;
        logParams.startTime = SystemClock.uptimeMillis();
    }

    /**
     * 结束日志追踪
     */
    public void end() {
        Logger.d(logParams.logName + "...end()..../n");
        logParams.endTime = SystemClock.uptimeMillis();
        double f = (logParams.endTime - logParams.startTime) / 1000f;
		BigDecimal b = new BigDecimal(f);
        logParams.duration = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        //如果开始时间为0，则为错误数据，过滤掉
        if (logParams.startTime == 0) {
            logParams.duration = 0;
        }
        mRecording = false;
        LogServer.start(this);
    }

    @Override
    public LogParams getLogParams() {
        return logParams;
    }

    public void send() {
    	end();
    }

    /**
     * 记录点击事件
     *
     * @param key
     */
    public void click(String key) {
        int count = 0;
        if (mClickEvents.containsKey(key)) {
            count = mClickEvents.get(key);
        } else {
            mClickEvents.put(key, count);
        }
        mClickEvents.put(key, ++count);
//        Countly.sharedInstance().recordEvent(key, 1);
    }

    /**
     * 格式：{xx[xx-xx,xx[xx-xx,xx-xx,...],xx[xx-xx],xx[xx-xx,xx-xx],...]}
     * 获得事件轨迹日志文本
     *
     * @return
     */
    public String getLogMessage() {

        String eventMessage = getClickEventLogMessage();

        String childLogMessage = getChildrenLogMessage();

        String logMessage = getFormatLogMessage(eventMessage, childLogMessage);

        return checkRoot(logMessage);
    }

    /**
     * 坚持日志是否标记为根事件
     * @param logMessage
     * @return
     */
    private String checkRoot(String logMessage) {
        //根事件串
        if (isRoot) {
            return "{" + logMessage + "}";
        }
        return logMessage;
    }

    /**
     * 只获得本事件的属性，用于独自发送的日志
     * @return
     */
    public String getSingleLogMessage() {
        String eventMessage = getClickEventLogMessage();
        return getFormatLogMessage(eventMessage, "");
    }

    /**
     * 构造格式化的日志文本
     * @param eventMessage
     * @param childLogMessage
     * @return
     */
    private String getFormatLogMessage(String eventMessage, String childLogMessage) {
        return logParams.logName + "[startlog-" + logParams.startTime + ",endlog-" + logParams.endTime
                    + ",duration-" + logParams.duration + ",events[" + eventMessage
                    + "],childs[" + childLogMessage + "]]";
    }

    /**
     * 获得点击事件的日志文本
     * @return
     */
    private String getClickEventLogMessage() {
        //点击事件
        String eventMessage = "";
        for (String eventName : mClickEvents.keySet()) {
            eventMessage += eventName + "-" + mClickEvents.get(eventName) + ",";
        }
        return eventMessage;
    }

    /**
     * 获得子事件的日志文本
     * @return
     */
    private String getChildrenLogMessage() {
        //子事件
        String childLogMessage = "";
        if (hasChild()) {
            for (ILogEvent child : mChildLogEvents.values()) {
                childLogMessage += child.getLogMessage() + ",";
            }
        }
        return childLogMessage;
    }

    /**
     * 标记为根节点
     * @param root
     */
    public void assignRoot(boolean root) {
        isRoot = root;
    }

    /**
     * 标记为事件组
     * @param parent
     */
    public void assignGroup(ILogEvent parent) {
        if (parent != null) {
            logParams.parent = parent;
        }
    }

    /**
     * 添加子事件
     * @param event
     */
    public void addChild(ILogEvent event) {
        if (event != null && !event.equals(this)) {
            mChildLogEvents.put(event.getLogParams().logName, event);
        }
    }

    @Override
    public final void clean() {
        if (!isRoot()) {
            String name = logParams.logName;
            logParams = new LogParams(name);
        }
        mChildLogEvents.clear();
        mClickEvents.clear();
    }

    /**
     * 获得子事件集合
     * @return
     */
    Collection<ILogEvent> getChildEvent() {
        return mChildLogEvents.values();
    }

    /**
     * 是否有子事件
     * @return
     */
    public boolean hasChild() {
        return !mChildLogEvents.isEmpty();
    }

    /**
     * 正在记录中
     *
     * @return
     */
    public boolean isRecording() {
        return mRecording;
    }

    /**
     * 是否是根事件
     * @return
     */
    public boolean isRoot() {
        return isRoot;
    }

}

