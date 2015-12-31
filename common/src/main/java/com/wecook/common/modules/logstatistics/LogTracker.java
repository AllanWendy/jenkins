package com.wecook.common.modules.logstatistics;

import android.content.Context;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.ModuleManager;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 日志追踪者
 *
 * 点击事件
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public class LogTracker extends BaseModule {

    private static LogTracker sInstance;

    private static Map<String, ILogEvent> sLogEventList = new Hashtable<String, ILogEvent>();

    private ILogEvent mCurrent;
    private ILogEvent mRootLogEvent;
    private LogConfiger mConfiger = new LogConfiger();
    
    private boolean mCountlyEnable;
    
    private LogTracker(){}

    public static LogTracker asInstance() {
        if (sInstance == null) {
            sInstance = (LogTracker) ModuleManager.asInstance().getModule(LogTracker.class);
        }
        return sInstance;
    }

    public ILogEvent getCurrent() {
        return mCurrent;
    }

    @Override
    public void setup(Context context) {
        super.setup(context);
        updateConfig(context);
        mRootLogEvent = new LogEvent("root");
        mRootLogEvent.assignRoot(true);
        startTracking(mRootLogEvent);
    }

    /**
     * 更新配置
     * @param context
     */
    private void updateConfig(Context context) {
    	if(mConfiger != null) {
    		if (mConfiger.getCountlyConfiger() == null) {
    			mCountlyEnable = false;
    		} else {
//    			if(!mCountlyEnable) {
//    				CountlyConfiger configer = mConfiger.getCountlyConfiger();
//    				Countly.sharedInstance().init(context.getApplicationContext(), configer.getServerUrl(), configer.getAppKey());
//    				Countly.sharedInstance().setLoggingEnabled(configer.isEnableLogging());
//    				mCountlyEnable = true;
//    			}
    		}
        }
	}

	@Override
    public void release() {
        super.release();
    }

	/**
	 * 检查初始化
	 */
	private static void checkInitial() {
		if (asInstance().mConfiger == null || 
				(asInstance().mConfiger.isEnable() && !asInstance().mConfiger.checkValidate())) {
			throw new IllegalStateException("Please make sure the LogConfiger[" 
					+ asInstance().mConfiger + "] is validate!");
		}
	}
	
    /**
     * 开启日志追踪
     * @param event
     */
    public static synchronized void startTracking(ILogEvent event) {
        if (event == null) {
            return;
        }

        checkInitial();
        ILogEvent rootLogEvent = asInstance().getRootLogEvent();
        if (rootLogEvent != null && !rootLogEvent.isRecording() && !event.isRoot()) {
            rootLogEvent.begin();
        }

        //add countly log
        if (asInstance().mCountlyEnable) {
//        	Countly.sharedInstance().onStart();
        }

        //add common log
        Logger.d(event.getLogParams().logName + "|startTracking...");
        final ILogEvent current = asInstance().mCurrent;
        if (current != event || (!event.isRoot() && !event.isRecording())) {
            if (current != null && current.isRecording()) {
                current.addChild(event);
            }
            event.assignGroup(current);
            event.begin();
            asInstance().mCurrent = event;
        }
    }

    /**
     * 停止日志追踪
     * @param event
     */
    public static synchronized void stopTracking(ILogEvent event) {
        if (event == null) {
            return;
        }
        checkInitial();
        //add common log
        Logger.d(event.getLogParams().logName + "|stopTracking...");
        Logger.d("Current#" + asInstance().mCurrent.getLogParams().logName + "|stopTracking...");
        if (event.isRecording()) {
            event.end();
        }

        boolean stopRoot = false;
        String from = event.getLogParams().logName;//从A事件
        String to = asInstance().mCurrent.getLogParams().logName;//到B事件
        if(asInstance().mCurrent.equals(event)) {
        	to = "root";
        	stopRoot = true;
        }
        double duration = event.getLogParams().duration;//A事件的停留时长
        trackUi(duration, from, to);
        
        if(stopRoot) {
        	asInstance().mRootLogEvent.end();
        	from = "root";
        	to = "exit";
        	duration = asInstance().mRootLogEvent.getLogParams().duration;
        	trackUi(duration, from, to);
        }

        //add countly log
        if (asInstance().mCountlyEnable) {
//        	Countly.sharedInstance().onStop();
        }
    }

    private static void trackUi(double duration, String from, String to) {
        Map<String, String> segments = new HashMap<String, String>();
        segments.put(from, to);
        Logger.d("log tracking ui : from " + from + " to " + to + " duration " + duration);
        if (asInstance().mCountlyEnable) {
//        	Countly.sharedInstance().recordEvent("ui-track", segments, 1, duration);
        }
    }

    /**
     * 清理日志
     * @param event
     */
    public static synchronized void clean(ILogEvent event) {
        if (event.isRoot()) {
            asInstance().mCurrent = asInstance().mRootLogEvent;
            for (ILogEvent c : sLogEventList.values()) {
                c.clean();
            }
            sLogEventList.clear();
        }
        event.clean();
    }

    /**
     * 追踪点击
     * @param event
     * @param key
     */
    public static synchronized void trackClick(ILogEvent event, String key) {
        if (event != null && event.isRecording()) {
            event.click(key);
        }
    }

    public ILogEvent getRootLogEvent() {
        return mRootLogEvent;
    }

    public void setLogConfiger(Context context, LogConfiger configer){
    	mConfiger = configer;
    	updateConfig(context);
    }

    /**
     * 对象池的概念
     * @param cls
     * @return
     */
    public static LogEvent getLogEvent(Object cls) {
        if (cls == null) {
            return null;
        }

        String name = cls.getClass().getSimpleName();
        if (sLogEventList.containsKey(name)) {
            ILogEvent logEvent = sLogEventList.get(name);
            if (logEvent != null && logEvent instanceof LogEvent) {
                logEvent.clean();
                return (LogEvent) logEvent;
            }
        }
        LogEvent event = new LogEvent(name);
        sLogEventList.put(name, event);
        return event;
    }

	public Class<?> getLogServerClass() {
		if (mConfiger != null) {
			return mConfiger.getLogServerClass();
		}
		return null;
	}
}
