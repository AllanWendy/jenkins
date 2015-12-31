package com.wecook.common.modules.thirdport.object;

import android.graphics.Bitmap;
import android.os.Parcel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源基类
 *
 * @author zhoulu
 * @date 13-12-11
 */
public abstract class BaseShareObject implements IShareObject {

	public static final int TYPE_THUMBNAIL_LOCAL = 0;
	public static final int TYPE_THUMBNAIL_ONLINE = 1;
	private String title = "";
    private String secondTitle = "";
    private String message = "";
    private byte[] bitmapBytes;
    private Bitmap bitmap;
    private String redirectUrl;
	private Map<Integer, List<String>> thumbnailUrlMap;

    public BaseShareObject() {
		thumbnailUrlMap = new HashMap<Integer, List<String>>();
		List<String> localUrls = new ArrayList<String>();
		List<String> onlineUrls = new ArrayList<String>();
		thumbnailUrlMap.put(TYPE_THUMBNAIL_LOCAL, localUrls);
		thumbnailUrlMap.put(TYPE_THUMBNAIL_ONLINE, onlineUrls);
	}

    public BaseShareObject(Parcel source) {
        title = source.readString();
        secondTitle = source.readString();
        message = source.readString();
        int length = source.readInt();
        if (length > 0) {
            bitmapBytes = new byte[length];
            source.readByteArray(bitmapBytes);
        }
        redirectUrl = source.readString();
		List<String> local = source.createStringArrayList();
		List<String> online = source.createStringArrayList();
		if (thumbnailUrlMap != null) {
			thumbnailUrlMap.clear();
			thumbnailUrlMap.put(TYPE_THUMBNAIL_LOCAL, local);
			thumbnailUrlMap.put(TYPE_THUMBNAIL_ONLINE, online);
		}
    }
    /**
     * 设置标题
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * 设置副标题
     * @param secondTitle
     */
    public void setSecondTitle(String secondTitle) {
        this.secondTitle = secondTitle;
    }
    /**
     * 设置评论描述
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

	/**
	 *
	 * @param bitmap
	 */
    public void setThumbnail(byte[] bitmap) {
        this.bitmapBytes = bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

	/**
	 *
	 * @param localImagePath
	 */
	public void addLocalThumbnailPaths(String localImagePath) {
		thumbnailUrlMap.get(TYPE_THUMBNAIL_LOCAL).add(localImagePath);
	}

	/**
	 *
	 * @param redirectUrl
	 */
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

	/**
	 * 设置图片链接
	 * @param thumbnailUrls
	 */
    public void addOnlineThumbnailUrls(String[] thumbnailUrls) {
		for (String it : thumbnailUrls) {
			thumbnailUrlMap.get(TYPE_THUMBNAIL_ONLINE).add(it);
		}
	}

	@Override
	public String[] getLocalThumbnailPaths() {
		try {
            List<String> locals = thumbnailUrlMap.get(TYPE_THUMBNAIL_LOCAL);
            String[] strings = new String[locals.size()];
			return locals.toArray(strings);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
     * 获得一个媒体链接：音乐、视频、网址
     *
     * @return
     */
    @Override
    public String getMediaUrl() {
        return "";
    }

    /**
     * 获得一个低品质的媒体链接：音乐、视频、网址
     *
     * @return
     */
    @Override
    public String getLowBandMediaUrl() {
        return "";
    }

    /**
     * 主标题：歌曲名称等
     *
     * @return
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * 副标题：歌手名等（可选）
     *
     * @return
     */
    @Override
    public String getSecondTitle() {
        return secondTitle;
    }

    /**
     * 评论描述
     *
     * @return
     */
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public byte[] getThumbnailBytes() {
        return bitmapBytes;
    }

    @Override
    public String[] getOnlineThumbnailUrl() {
        List<String> locals = thumbnailUrlMap.get(TYPE_THUMBNAIL_ONLINE);
        String[] strings = new String[locals.size()];
        return locals.toArray(strings);
    }

    @Override
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * 数据大小：音频长度，视频长度
     *
     * @return
     */
    @Override
    public int getContentSize() {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(secondTitle);
        dest.writeString(message);

        if (bitmapBytes != null && bitmapBytes.length != 0) {
            dest.writeInt(bitmapBytes.length);
            dest.writeByteArray(bitmapBytes);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(redirectUrl);
		List<String> local = thumbnailUrlMap.get(TYPE_THUMBNAIL_LOCAL);
		dest.writeStringList(local);
		List<String> online = thumbnailUrlMap.get(TYPE_THUMBNAIL_ONLINE);
		dest.writeStringList(online);
    }
}
