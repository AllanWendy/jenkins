package com.wecook.common.modules.downer.file;

import com.loopj.android.http.RequestHandle;
import com.wecook.common.core.internet.Api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件下载器
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/21
 */
public class FileDownloader extends Api {

    private Map<String, RequestHandle> mRequestHandles;

    private static FileDownloader sInstance;

    private FileDownloader() {
        mRequestHandles = new ConcurrentHashMap<>();
    }

    public static FileDownloader get() {
        if (sInstance == null) {
            sInstance = new FileDownloader();
        }
        return sInstance;
    }

    /**
     * 开启一个下载
     *
     * @param url
     * @param downloadListener
     * @return
     */
    public RequestHandle start(String url, FileDownloadListener downloadListener) {
        RequestHandle requestHandle = sAsyncHttpClient.get(url, downloadListener);
        if (requestHandle != null) {
            mRequestHandles.put(url, requestHandle);
        }
        return requestHandle;
    }

    /**
     * 关闭一个下载
     *
     * @param url
     * @param mayInterruptIfRunning
     */
    public void cancel(String url, boolean mayInterruptIfRunning) {
        RequestHandle handle = mRequestHandles.get(url);
        if (handle != null) {
            handle.cancel(mayInterruptIfRunning);
        }
    }

    /**
     * 关闭所有下载
     */
    public void cancelAll() {
        for (String url : mRequestHandles.keySet()) {
            cancel(url, true);
        }
    }
}
