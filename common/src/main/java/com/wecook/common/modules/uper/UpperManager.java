package com.wecook.common.modules.uper;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.thread.ThreadManager;
import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.ModuleManager;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.BitmapUtils;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上传管理器
 *
 * @author kevin
 * @version v1.0
 * @since 2015-1/28/15
 */
public class UpperManager extends BaseModule {

    public static final String TAG = "upload";
    private static final String KEY_ONLY_WIFI_UPPER_LOAD = "key_only_wifi_upper_load";
    private static final int MAX_WIDTH = 960;
    private static final int MAX_HEIGHT = 960;
    private static UpperManager sUpperManager;
    private List<UpperListener> mListeners = Collections.synchronizedList(new ArrayList<UpperListener>());
    private List<String> mUploadImageList = Collections.synchronizedList(new ArrayList<String>());
    //上传任务集合
    private ConcurrentHashMap<String, Runnable> taskMap = new ConcurrentHashMap<>();
    private String mUserId;
    private int mSuccessCount;
    private int mUploadCount;
    private int mReqestCount;

    public static UpperManager asInstance() {
        if (sUpperManager == null) {
            sUpperManager = (UpperManager) ModuleManager.asInstance().getModule(UpperManager.class);
        }
        return sUpperManager;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public void upImage(String path) {
        upImages(new String[]{path});
    }

    public void upImages(String[] imagePaths) {
        if (imagePaths == null
                || imagePaths.length == 0
                || StringUtils.isEmpty(mUserId)) {
            Logger.warn(TAG, "upImages...", imagePaths, mUserId);
            return;
        }


        boolean needUpdateQueue = mUploadImageList.isEmpty();

        for (String imagePath : imagePaths) {
            if (!taskMap.containsKey(imagePath)) {
                mUploadImageList.add(imagePath);
                mReqestCount++;
            }
        }

        if (needUpdateQueue) {
            Logger.debug(TAG, "post uploadQueue...");
            uploadQueue();
        }
    }

    public void cancelImage(String path) {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        if (taskMap.containsKey(path)) {
            ThreadManager.getLongPool().cancel(taskMap.get(path));
            dispatchListenerCancel(path);
        }
    }


    /**
     * 上传队列
     */
    private void uploadQueue() {
        threadPoolTaskRunnable();
    }

    /**
     * 多线程执行任务
     *
     * @return
     */
    private void threadPoolTaskRunnable() {
        synchronized (mUploadImageList) {
            if (mUploadImageList != null && mUploadImageList.size() > 0) {
                while (mUploadImageList.size() > 0) {
                    final String path = mUploadImageList.remove(0);
                    if (!StringUtils.isEmpty(path)) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                doUpload(mUserId, path);
                            }
                        };
                        taskMap.put(path, runnable);
                        ThreadManager.getLongPool().execute(runnable);
                    }
                }
            }
        }
    }

    /**
     * 处理单一同步上传
     *
     * @param uid
     * @param path
     */
    private void doUpload(final String uid, final String path) {
        byte[] bitmapBytes = processStart(path);
        processConnect(path, uid, bitmapBytes);
    }

    /**
     * 处理准备数据
     *
     * @param path
     * @return
     */
    private byte[] processStart(String path) {
        byte[] bitmapBytes = FileUtils.getFileBytes(path);
        if (bitmapBytes.length / FileUtils.ONE_MB >= 2) {
            BitmapUtils.BitmapInfo info = BitmapUtils.getBitmapBestFit(path, MAX_WIDTH,
                    MAX_HEIGHT, 2d * FileUtils.ONE_MB);
            if (info != null) {
                bitmapBytes = info.srcBytes;
            }
        }

        Logger.i(TAG, "prepare upload path:" + path + "\n " +
                "size:" + (bitmapBytes != null ? bitmapBytes.length / FileUtils.ONE_KB : 0));
        mUploadCount++;

        Logger.i(TAG, "start upload path:" + path);
        dispatchListenerStart(path);
        return bitmapBytes;
    }

    /**
     * 处理链接
     *
     * @param path
     * @param uid
     * @param bitmapBytes
     */
    private void processConnect(final String path, String uid, byte[] bitmapBytes) {
        //仅wifi下，手机网络直接上传失败
        boolean onlyWifi = SharePreferenceProperties.get(KEY_ONLY_WIFI_UPPER_LOAD, true);
        Logger.debug(TAG, "process connect ... ", "onlyWifi ? " + onlyWifi);
        if (onlyWifi && NetworkState.availableOnlyMobile()) {
            processResult(path, "0", "", false);
            return;
        }
        UploadApi api = new UploadApi();
        api.with("/media/uploadimages")
                .addParams("uid", uid, true)
                .setBody("upfile", bitmapBytes)
                .toModel(new UploadMedia())
                .setApiCallback(new ApiCallback<UploadMedia>() {
                    @Override
                    public void onResult(UploadMedia result) {
                        String mediaId = "0";
                        String mediaUrl = "";
                        if (result.available()) {
                            mediaId = result.getId();
                            mediaUrl = result.getUrl();
                        }
                        processResult(path, mediaId, mediaUrl, result.available());
                    }
                })
                .executePut(true);

        dispatchListenerUploading(path);
    }

    /**
     * 处理结果
     *
     * @param path
     */
    private void processResult(String path, String mediaId, String url, boolean success) {
        Logger.info(TAG, "end upload", path, mediaId, url, "count:" + mUploadImageList.size());
        if (success) {
            mSuccessCount++;
        }
        taskMap.remove(path);
        dispatchListenerEnd(path, mediaId, url, success);
        processFinish();
    }

    private void processFinish() {
        if (taskMap.isEmpty()) {
            Logger.i(TAG, "finish all upload.");
            dispatchListenerFinish(mSuccessCount, mUploadCount);
            release();
        }
    }

    private void dispatchListenerStart(String path) {
        for (UpperListener listener : mListeners) {
            if (listener != null) {
                listener.onStart(path);
            }
        }
    }

    private void dispatchListenerUploading(String path) {
        for (UpperListener listener : mListeners) {
            if (listener != null) {
                listener.onUploading(path);
            }
        }
    }

    private void dispatchListenerEnd(String path, String mediaId, String url, boolean success) {
        for (UpperListener listener : mListeners) {
            if (listener != null) {
                listener.onEnd(path, mediaId, url, success);
            }
        }
    }

    private void dispatchListenerFinish(int success, int total) {
        for (UpperListener listener : mListeners) {
            if (listener != null) {
                listener.onFinish(success, total);
            }
        }
    }

    private void dispatchListenerCancel(String path) {
        for (UpperListener listener : mListeners) {
            if (listener != null) {
                listener.onCancel(path);
            }
        }
    }

    @Override
    public void release() {
        super.release();
        if (mUploadImageList != null) {
            mUploadImageList.clear();
        }
        if (!taskMap.isEmpty()) {
            Iterator<Map.Entry<String, Runnable>> iterator = taskMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Runnable> entry = iterator.next();
                Runnable runnable = entry.getValue();
                ThreadManager.getLongPool().cancel(runnable);
            }
            taskMap.clear();
        }

        if (mListeners != null) {
            mListeners.clear();
        }

        mSuccessCount = 0;
        mUploadCount = 0;
        mReqestCount = 0;
    }

    public void addUpperListener(UpperListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeUpperListener(UpperListener listener) {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
    }

    public boolean isWorking() {
        return !taskMap.isEmpty();
    }

    public static interface UpperListener {
        void onStart(String path);

        void onUploading(String path);

        void onEnd(String path, String id, String url, boolean success);

        void onFinish(int success, int total);

        void onCancel(String path);
    }

    public static class SimpleUpperListener implements UpperListener {

        @Override
        public void onStart(String path) {

        }

        @Override
        public void onUploading(String path) {

        }

        @Override
        public void onEnd(String path, String id, String url, boolean success) {

        }

        @Override
        public void onFinish(int success, int total) {

        }

        @Override
        public void onCancel(String path) {

        }
    }

    private class UploadApi extends Api {
    }

    private class UploadMedia extends ApiModel {
        @SerializedName("media_id")
        private String id;

        @SerializedName("url")
        private String url;

        @Override
        public void parseJson(String json) throws JSONException {
            JSONObject jsonObject = new JSONObject(json);
            id = jsonObject.optString("id");
            url = jsonObject.optString("url");
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
