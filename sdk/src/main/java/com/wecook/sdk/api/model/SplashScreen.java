package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.filemaster.FileMaster;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;

import java.io.File;

/**
 * 启动画面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/3/14
 */
public class SplashScreen extends ApiModel {
    private static final String LOCAL_LAUNCH_IMAGE_NAME = "launch_image";

    public boolean show = true;

    @SerializedName("id")
    public String id;

    @SerializedName("image")
    public String image;

    @SerializedName("time_start")
    public String timeStart;

    @SerializedName("url")
    public String url;

    @SerializedName("time_end")
    public String timeEnd;


    @Override
    public void parseJson(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            JsonObject jsonObject = JsonUtils.getJsonObject(json);
            id = JsonUtils.getModelItemAsString(jsonObject, "id");
            image = JsonUtils.getModelItemAsString(jsonObject, "image");
            timeStart = JsonUtils.getModelItemAsString(jsonObject, "time_start");
            timeEnd = JsonUtils.getModelItemAsString(jsonObject, "time_end");
            url = JsonUtils.getModelItemAsString(jsonObject, "url");
        }
    }

    /**
     * 是否可用
     *
     * @return
     */
    public boolean isEnable() {
        File imageDir = FileMaster.getInstance().getImageDir();
        File imageFile = new File(imageDir, LOCAL_LAUNCH_IMAGE_NAME);
        return show && imageFile.exists();
    }

    public String getFilePath() {
        File imageDir = FileMaster.getInstance().getImageDir();
        File imageFile = new File(imageDir, LOCAL_LAUNCH_IMAGE_NAME);
        return imageFile.getAbsolutePath();
    }

    public File getImageFile() {
        File imageDir = FileMaster.getInstance().getImageDir();
        return new File(imageDir, LOCAL_LAUNCH_IMAGE_NAME);
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * 获得图片下载地址
     *
     * @return
     */
    public String getUrl() {
        return image;
    }

    /**
     * 获得广告url地址
     *
     * @return
     */
    public String getAdUrl() {
        return url;
    }

    public boolean checkValidate() {
        boolean isValid = false;
        if (Api.getGlobalConfig() != null) {
            String json = (String) SharePreferenceProperties.get(LOCAL_LAUNCH_IMAGE_NAME, "");
            try {
                parseJson(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            long timeCurrent = System.currentTimeMillis();
            timeCurrent /= 1000;
            long timeEnd = StringUtils.parseLong(this.timeEnd);
            long timeStart = StringUtils.parseLong(this.timeStart);
            if (timeCurrent >= timeStart && timeCurrent <= timeEnd && isEnable()) {
                isValid = true;
            } else {
                isValid = false;
            }
        }
        return isValid;
    }

    public void updateRemoteImage(SplashScreen splashScreen) {
        if (splashScreen != null) {
            Logger.i("updateRemoteImage", "mSplashImage json : " + splashScreen.toJson());
            SharePreferenceProperties.set(LOCAL_LAUNCH_IMAGE_NAME, splashScreen.toJson());
            if (isNeedDownload()) {
                downloadImage(splashScreen.getUrl(), ScreenUtils.getScreenWidthInt(),
                        ScreenUtils.getScreenHeightInt());
            }
        } else {
            SharePreferenceProperties.set(LOCAL_LAUNCH_IMAGE_NAME, "");
        }
    }

    /**
     * 判断是否需要下载新图片
     *
     * @return
     */
    private boolean isNeedDownload() {
        SplashScreen oldRemoteImage = new SplashScreen();
        String json = (String) SharePreferenceProperties.get(LOCAL_LAUNCH_IMAGE_NAME, "");
        try {
            oldRemoteImage.parseJson(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return equals(oldRemoteImage);
    }

    /**
     * 下载图片
     *
     * @param url
     */
    private void downloadImage(String url, int width, int height) {
        if (!StringUtils.isEmpty(url)) {
            File image = getImageFile();
            if (image.exists()) {
                image.delete();
            }
            Logger.i("downloadImage", "start");
            ImageFetcher.asInstance().download(url, width, height, new ImageFetcher.Callback<File>() {
                @Override
                public void callback(File obj) {
                    Logger.i("downloadImage", "end");
                    FileUtils.moveTo(obj, new File(getFilePath()));
                }
            });
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SplashScreen that = (SplashScreen) o;

        if (show != that.show) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        if (timeStart != null ? !timeStart.equals(that.timeStart) : that.timeStart != null)
            return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return !(timeEnd != null ? !timeEnd.equals(that.timeEnd) : that.timeEnd != null);

    }

    @Override
    public int hashCode() {
        int result = (show ? 1 : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        result = 31 * result + (timeStart != null ? timeStart.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (timeEnd != null ? timeEnd.hashCode() : 0);
        return result;
    }
}
