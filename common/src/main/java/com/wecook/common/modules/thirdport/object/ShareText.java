package com.wecook.common.modules.thirdport.object;

import android.os.Parcel;

/**
 * 分享文本
 *
 * @author zhoulu
 * @date 13-12-11
 */
public class ShareText extends BaseShareObject {

    private String mText;

    /**
     * 构造
     * @param text 文本内容
     */
    public ShareText(String text){
        mText = text;
    }

    public ShareText(Parcel source) {
        super(source);
        mText = source.readString();
    }

    /**
     * 类型
     *
     * @return
     */
    @Override
    public TYPE getType() {
        return TYPE.TYPE_TEXT;
    }

    @Override
    public String getMessage() {
        return mText;
    }

    public static Creator<ShareText> CREATOR = new Creator<ShareText>() {
        @Override
        public ShareText createFromParcel(Parcel source) {
            return new ShareText(source);
        }

        @Override
        public ShareText[] newArray(int size) {
            return new ShareText[0];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mText);
    }
}
