// Copy right WeCook Inc.
package com.wecook.common.modules.logstatistics;
/**
 * 日志统计配置文件
 * 
 * @author 	kevin
 * @since 	2014-Sep 30, 2014
 * @version	v1.0
 */
public class LogConfiger {

	private Class mLogServerCls;
	private CountlyConfiger mCountlyConfiger;
	private boolean mEnable = true;
	
	public boolean isEnable() {
		return mEnable;
	}
	
	public void enable(boolean enable) {
		mEnable = enable;
	}
	
    /**
     * 设置日志服务
     * @param logServerCls
     */
    public void setLogServerCls(Class logServerCls) {
        mLogServerCls = logServerCls;
    }

    public Class<?> getLogServerClass() {
        return mLogServerCls;
    }
    
	public void setCountlyConfiger(CountlyConfiger countlyConfiger) {
		mCountlyConfiger = countlyConfiger;
	}
	
	public CountlyConfiger getCountlyConfiger() {
		return mCountlyConfiger;
	}

	public static class CountlyConfiger {
		private String mServerUrl;
		private String mAppKey;
		private boolean mEnableLogging;
		
		public CountlyConfiger(String serverUrl, String appKey) {
			mServerUrl = serverUrl;
			mAppKey = appKey;
		}
		
		public void enableLogging(boolean enable) {
			mEnableLogging = enable;
		}
		
		public boolean isEnableLogging() {
			return mEnableLogging;
		}
		
		public String getServerUrl() {
			return mServerUrl;
		}

		public String getAppKey() {
			return mAppKey;
		}
	}

	/**
	 * 检查是否
	 * @return
	 */
	public boolean checkValidate() {
		if (mLogServerCls == null) {
			return false;
		}
		return true;
	}
	
}
