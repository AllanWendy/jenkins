package com.wecook.sdk.api.legacy;

import android.graphics.Bitmap;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.utils.BitmapUtils;
import com.wecook.sdk.api.model.Media;

/**
 * 媒体数据API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/6/14
 */
public class MediaApi extends Api {

    /**
     * 上传单张图片，尺寸小于2M
     *
     * @param uid
     * @param bitmap
     * @param callback
     * @return
     */
    public static void uploadSingleMedia(String uid, Bitmap bitmap, ApiCallback<Media> callback) {
        byte[] bitmapBytes = BitmapUtils.bitmap2Bytes(bitmap, 100);
        Api.get(MediaApi.class)
                .with("/media/uploadimages")
                .addParams("uid", uid, true)
                .setBody("upfile", bitmapBytes)
                .toModel(new Media())
                .setApiCallback(callback)
                .executePut();
    }
}
