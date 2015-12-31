// Copy right WeCook Inc.
package com.wecook.common.core.debug;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.wecook.common.BuildConfig;
import com.wecook.common.app.BaseApp;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.filemaster.FileMaster;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.AndroidUtils;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;

import java.io.File;

/**
 * 调试中心
 *
 * @author kevin
 * @version v1.0
 * @since 2014-Sep 17, 2014
 */
public class DebugCenter {

    private static String LOG_TAG_PREFIX = "wk-";
    private static final int MAX_LOG_TAG_LENGTH = 23;
    private static final int LOG_PREFIX_LENGTH = LOG_TAG_PREFIX.length();
    private static boolean APP_DEBUG = BuildConfig.DEBUG;

    /**
     * 是否开启debug模式
     *
     * @return
     */
    public static boolean isDebugable() {
        return APP_DEBUG;
    }

    public static void openAppDebug(boolean enable) {
        APP_DEBUG = enable;
    }

    /**
     * 生成日志TAG
     *
     * @return
     */
    public static String makeTag() {
        return makeTag("");
    }

    /**
     * 生成日志TAG
     *
     * @param tag
     *         附加描述
     * @return
     */
    public static String makeTag(String tag) {
        if (StringUtils.isEmpty(tag)) {
            return LOG_TAG_PREFIX.substring(0, LOG_PREFIX_LENGTH - 1);
        }
        final String logTag = tag;
        if (logTag.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_TAG_PREFIX + logTag.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }
        return LOG_TAG_PREFIX + logTag;
    }

    /**
     * 生成日志TAG
     *
     * @param cls
     * @return
     */
    public static String makeTag(Class<?> cls) {
        if (cls != null) {
            return makeTag(cls.getSimpleName());
        }
        return makeTag();
    }

    /**
     * 设置全局异常捕获器
     *
     * @param app
     */
    public static void catchGlobalException(Application app) {
        if (Thread.currentThread().equals(Looper.getMainLooper().getThread())
                && isDebugable()) {
            Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionCatcher(app));
        }
    }

    public static boolean isLoggable(String tag, int level) {
        return Log.isLoggable(tag, level);
    }

    private static class GlobalExceptionCatcher implements Thread.UncaughtExceptionHandler {

        private Context mContext;

        private GlobalExceptionCatcher(Context context) {
            mContext = context;
        }

        @Override
        public void uncaughtException(Thread thread, final Throwable ex) {
            Logger.e("AndroidRuntime", "crash thread : " + thread, ex);
            UIHandler.stopLoops();
            BaseApp.hideApp();
            AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
                @Override
                public void run() {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(ex.getMessage());

                    StackTraceElement[] elements = ex.getStackTrace();
                    for (StackTraceElement element : elements) {
                        buffer.append("\n" + element.toString());
                    }

                    byte[] data = buffer.toString().getBytes();
                    File log = FileUtils.newFile(data, "crash.log");
                    String debugVersion = String.valueOf(AndroidUtils.getMetaDataFromApplication(mContext, "DEBUG_VERSION"));
                    if (StringUtils.isEmpty(debugVersion)) {
                        debugVersion = PhoneProperties.getVersionName();
                    }
                    FileUtils.moveTo(log, new File(FileMaster.getInstance().getLongLogDir(),
                            "v" + debugVersion + "-crash-"
                                    + StringUtils.formatTime(System.currentTimeMillis(), "MM.dd-HH.mm.ss") + ".log"));
                    BaseApp.quitApp(false);
                }
            });

        }
    }
}
