package com.wecook.common.modules.thirdport.object;

import android.graphics.Bitmap;
import android.os.Parcelable;

/**
 * 分享的数据对象
 *
 * @author zhoulu
 * @date 13-12-11
 */
public interface IShareObject extends Parcelable {

    /**
     * 获得一个媒体链接：音乐、视频、网址
     * @return
     */
    String getMediaUrl();

    /**
     * 获得一个低品质的媒体链接：音乐、视频、网址
     * @return
     */
    String getLowBandMediaUrl();

    /**
     * 主标题：歌曲名称等
     * @return
     */
    String getTitle();

    /**
     * 副标题：歌手名等（可选）
     * @return
     */
    String getSecondTitle();

    /**
     * 评论描述
     * @return
     */
    String getMessage();

    /**
     * 类型（必选）
     * @return
     */
    TYPE getType();

    /**
     * 获得图片数据
     * @return
     */
    byte[] getThumbnailBytes();

	/**
	 * 获取本地图片路径
	 * @return
	 */
	String[] getLocalThumbnailPaths();

    /**
     * 多张在线图片Url
     * @return
     */
    String[] getOnlineThumbnailUrl();

    /**
     * 数据大小：音频长度，视频长度
     * @return
     */
    int getContentSize();

    /**
     * 获得调转URL：音乐查看详情等.
     * @return
     */
    String getRedirectUrl();

    /**
     * 获得图片
     * @return
     */
    Bitmap getBitmap();

    /**
     * 分享数据类型
     */
    public enum TYPE {
        TYPE_TEXT("text"),//文本
        TYPE_MUSIC("music"),//音乐
        TYPE_VIDEO("video"),//视频
        TYPE_IMAGE("image"),//图片
        TYPE_WEBURL("weburl");//网页

        String mValue;

        TYPE(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }
}
