package com.wecook.sdk.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 图片
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/7/14
 */
public class Image extends ApiModel implements Parcelable {
    /**
     * 上传图片后获得的id
     */
    private String id;
    /**
     * 本地图url
     */
    private String url;
    /**
     * 上传后获得的网络地址
     */
    private String updateUrl;
    /**
     * 展示大图url
     */
    private String image_origin;
    /**
     * 缩略图url
     */
    private String image;
    public static Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            Image image = new Image();
            image.readFromParcel(source);
            return image;
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[0];
        }
    };

    @Override
    public void parseJson(String json) throws JSONException {
        if (JsonUtils.isJsonString(json)) {
            url = json;
        }
    }

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        if (JsonUtils.isJsonArray(json)) {
            return JsonUtils.getJSONArray(json);
        } else if (JsonUtils.isJsonObject(json)) {
            JSONObject jsonObject = JsonUtils.getJSONObject(json);
            return jsonObject.getJSONArray("image");
        }
        return super.findJSONArray(json);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private void readFromParcel(Parcel source) {
        id = source.readString();
        url = source.readString();
        image = source.readString();
        image_origin = source.readString();
        updateUrl = source.readString();
    }

    /**
     * 智能获得最优小图url展示
     */
    public String getShowSmallImagePath() {
        //先获取本地url
        if (url != null && !StringUtils.isEmpty(url)) {
            return url;
        }
        //获取网络缩略图url
        if (image != null && !StringUtils.isEmpty(image)) {
            return image;
        }

        //获取网络大图url
        if (image_origin != null && !StringUtils.isEmpty(image_origin)) {
            return image_origin;
        }
        //获取网络大图url
        if (updateUrl != null && !StringUtils.isEmpty(updateUrl)) {
            return updateUrl;
        }
        return null;
    }

    /**
     * 智能获得最优大图url展示
     *
     * @return
     */
    public String getShowBigImagePath() {
        //先获取本地url
        if (!StringUtils.isEmpty(url)) {
            return url;
        }
        //获取网络大图url
        if (!StringUtils.isEmpty(image_origin)) {
            return image_origin;
        }
        //获取网络大图url
        if (!StringUtils.isEmpty(updateUrl)) {
            return updateUrl;
        }
        //获取网络缩略图url
        if (!StringUtils.isEmpty(image)) {
            return image;
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(image);
        dest.writeString(image_origin);
        dest.writeString(updateUrl);
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getImage_origin() {
        return image_origin;
    }

    public void setImage_origin(String image_origin) {
        this.image_origin = image_origin;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
