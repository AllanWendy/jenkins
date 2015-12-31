package com.wecook.common.modules.thirdport.object;

import android.os.Parcel;

/**
 * 图片数据源
 *
 * @author zhoulu
 * @date 13-12-11
 */
public class ShareImage extends BaseShareObject {

    String[] mShareImageUrl;

    /**
     * 构造
     * @param shareImage 图片
     */
    public ShareImage(byte[] shareImage) {
        setThumbnail(shareImage);
    }

    /**
     * 构造
     * @param shareImageUrl 图片url
     */
    public ShareImage(String[] shareImageUrl) {
        mShareImageUrl = shareImageUrl;
    }

    public ShareImage(Parcel source) {
        super(source);

        int N = source.readInt();
        if (N > 0) {
            mShareImageUrl = new String[N];
            source.readStringArray(mShareImageUrl);
        }
    }

    /**
     * 类型
     *
     * @return
     */
    @Override
    public TYPE getType() {
        return TYPE.TYPE_IMAGE;
    }

    @Override
    public String[] getOnlineThumbnailUrl() {
        return mShareImageUrl;
    }

    public static Creator<ShareImage> CREATOR = new Creator<ShareImage>() {
        @Override
        public ShareImage createFromParcel(Parcel source) {
            return new ShareImage(source);
        }

        @Override
        public ShareImage[] newArray(int size) {
            return new ShareImage[0];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mShareImageUrl == null ? -1 : mShareImageUrl.length);
        if (mShareImageUrl != null && mShareImageUrl.length != 0) {
            dest.writeStringArray(mShareImageUrl);
        }
    }
}
