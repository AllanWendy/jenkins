package com.wecook.common.modules.thirdport.object;

import android.os.Parcel;

/**
 * 网址
 *
 * @author zhoulu
 * @date 13-12-11
 */
public class ShareWebpage extends BaseShareObject {
    private String url;

    /**
     * 构造
     *
     * @param url 跳转的url
     */
    public ShareWebpage(String url) {
        this.url = url;
        setRedirectUrl(url);
    }

    public ShareWebpage(Parcel source) {
        super(source);
        url = source.readString();
        setRedirectUrl(url);
    }

    /**
     * 类型
     *
     * @return
     */
    @Override
    public TYPE getType() {
        return TYPE.TYPE_WEBURL;
    }

    @Override
    public String getRedirectUrl() {
        return url;
    }

    public static Creator<ShareWebpage> CREATOR = new Creator<ShareWebpage>() {
        @Override
        public ShareWebpage createFromParcel(Parcel source) {
            return new ShareWebpage(source);
        }

        @Override
        public ShareWebpage[] newArray(int size) {
            return new ShareWebpage[0];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
    }
}
