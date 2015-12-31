package com.wecook.common.modules.thirdport.object;

import android.os.Parcel;

import com.wecook.common.utils.StringUtils;


/**
 * 视频
 *
 * @author zhoulu
 * @date 13-12-11
 */
public class ShareVideo extends BaseShareObject {

    int duration;
    String trackTitle;
    String description;
    String mediaUrl;
    String lowBandMediaUrl;
    String detailUrl;

    /**
     * 构造
     * @param videoImage MV图片
     * @param trackTitle MV名
     * @param description 描述
     * @param mediaUrl 播放url
     * @param lowBandMediaUrl 低码率播放url
     * @param detailUrl 详情url
     * @param duration 音频时长
     */
    public ShareVideo(byte[] videoImage, String trackTitle, String description, String mediaUrl, String lowBandMediaUrl, String detailUrl, int duration) {
        setThumbnail(videoImage);
        this.trackTitle = trackTitle;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.lowBandMediaUrl = lowBandMediaUrl;
        this.duration = duration;
        this.detailUrl = detailUrl;
    }

    public ShareVideo(Parcel source) {
        super(source);
        trackTitle = source.readString();
        description = source.readString();
        mediaUrl = source.readString();
        lowBandMediaUrl = source.readString();
        detailUrl = source.readString();
        duration = source.readInt();
    }

    /**
     * 类型
     *
     * @return
     */
    @Override
    public TYPE getType() {
        return TYPE.TYPE_VIDEO;
    }

    @Override
    public String getMediaUrl() {
        if (StringUtils.isEmpty(mediaUrl)) {
            return lowBandMediaUrl;
        }
        return mediaUrl;
    }

    @Override
    public String getLowBandMediaUrl() {
        return lowBandMediaUrl;
    }

    @Override
    public String getTitle() {
        return trackTitle;
    }

    @Override
    public String getSecondTitle() {
        return description;
    }

    @Override
    public int getContentSize() {
        return duration;
    }

    @Override
    public String getRedirectUrl() {
        return detailUrl;
    }

    public static Creator<ShareVideo> CREATOR = new Creator<ShareVideo>() {
        @Override
        public ShareVideo createFromParcel(Parcel source) {
            return new ShareVideo(source);
        }

        @Override
        public ShareVideo[] newArray(int size) {
            return new ShareVideo[0];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(trackTitle);
        dest.writeString(description);
        dest.writeString(mediaUrl);
        dest.writeString(lowBandMediaUrl);
        dest.writeString(detailUrl);
        dest.writeInt(duration);
    }

}
