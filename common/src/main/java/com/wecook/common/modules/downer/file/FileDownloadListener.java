package com.wecook.common.modules.downer.file;

import com.loopj.android.http.RangeFileAsyncHttpResponseHandler;
import com.wecook.common.core.debug.Logger;

import java.io.File;

import cz.msebera.android.httpclient.Header;

/**
 * 下载文件
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/21
 */
public class FileDownloadListener extends RangeFileAsyncHttpResponseHandler {

    public FileDownloadListener(File file) {
        super(file);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, File file) {

    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
        Logger.v("download", String.format("Progress %d from %d (%2.0f%%)", bytesWritten, totalSize, (totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1));
    }
}
