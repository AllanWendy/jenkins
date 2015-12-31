package com.wecook.common.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.filemaster.FileMaster;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * 媒体库选取工具
 *
 * @author kevin
 * @version v1.0
 * @since 2014-12/23/14
 */
public class MediaStorePicker {

    private static final String DEFAULT_ALBUM_PATH = FileMaster.getInstance().getLongImageDir().getAbsolutePath();
    private static final int DEFAULT_MIN_WIDTH = 120;
    private static final int DEFAULT_MIN_HEIGHT = 120;
    private static final long DEFAULT_MIN_SIZE = FileUtils.ONE_KB * 100;

    public static class MediaImage implements Parcelable {
        public String id;
        public String path;
        public String albumId;
        public String albumName;
        public String title;
        public String desc;
        public int selected; //0 unselected 1 selected

        public boolean isSelected() {
            return selected == 1;
        }

        public void setSelected(boolean isSelected) {
            selected = isSelected ? 1 : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MediaImage) {
                if (!StringUtils.isEmpty(path) && path.equals(((MediaImage) o).path)) {
                    return true;
                }
            }
            return super.equals(o);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(path);
            dest.writeString(albumId);
            dest.writeString(albumName);
            dest.writeString(title);
            dest.writeString(desc);
            dest.writeInt(selected);
        }

        private void readFromParcel(Parcel in) {
            id = in.readString();
            path = in.readString();
            albumId = in.readString();
            albumName = in.readString();
            title = in.readString();
            desc = in.readString();
            selected = in.readInt();
        }

        public static final Parcelable.Creator<MediaImage> CREATOR =
                new Parcelable.Creator<MediaImage>() {
                    public MediaImage createFromParcel(Parcel in) {
                        MediaImage image = new MediaImage();
                        image.readFromParcel(in);
                        return image;
                    }

                    public MediaImage[] newArray(int size) {
                        return new MediaImage[size];
                    }
                };
    }

    public static class MediaAlbum implements Parcelable{
        public String albumId;
        public String albumName;
        public ArrayList<MediaImage> images;

        public MediaAlbum() {
            albumId = "";
            albumName = "";
            images = new ArrayList<MediaImage>();
        }

        public static final Parcelable.Creator<MediaAlbum> CREATOR =
                new Parcelable.Creator<MediaAlbum>() {
                    public MediaAlbum createFromParcel(Parcel in) {
                        MediaAlbum image = new MediaAlbum();
                        image.readFromParcel(in);
                        return image;
                    }

                    public MediaAlbum[] newArray(int size) {
                        return new MediaAlbum[size];
                    }
                };

        private void readFromParcel(Parcel in) {
            albumId = in.readString();
            albumName = in.readString();
            images = new ArrayList<MediaImage>();
            in.readTypedList(images, MediaImage.CREATOR);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(albumId);
            dest.writeString(albumName);
            dest.writeTypedList(images);
        }
    }

    public static MediaImage build(String path, String title, String desc) {
        MediaImage image = new MediaImage();
        image.path = path;
        image.title = title;
        image.desc = desc;
        return image;
    }

    /**
     * 使用默认配置获取本地图片
     *
     * @param context
     * @param handler
     */
    public static void pick(final Context context, final PickHandler handler) {
        pick(context, DEFAULT_MIN_SIZE, DEFAULT_MIN_WIDTH, DEFAULT_MIN_HEIGHT, handler);
    }

    /**
     * 使用过滤配置获取本地图片
     *
     * @param context
     * @param byteSize 图片大小
     * @param width    图片宽度
     * @param height   图片高度
     * @param handler
     */
    public static void pick(final Context context, final long byteSize, final int width, final int height, final PickHandler handler) {
        AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
            List<MediaImage> images = new ArrayList<MediaImage>();

            List<MediaAlbum> albums = new ArrayList<MediaAlbum>();

            @Override
            public void run() {
                Cursor cursor = null;
                final String[] columns = new String[]{
                        MediaStore.Images.Media._ID,//图片ID
                        MediaStore.Images.Media.DATA,//图片路径
                        MediaStore.Images.Media.BUCKET_ID,//文件夹ID
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,//文件夹名
                };
                String where = MediaStore.Images.Media.SIZE + " > " + byteSize;

                if (Build.VERSION.SDK_INT >= 16) {
                    where += " AND " + MediaStore.Images.Media.WIDTH + " > " + width
                            + " AND " + MediaStore.Images.Media.HEIGHT + " > " + height;
                }
                final String order = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

                try {
                    //扩展卡存储
                    cursor = context.getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, where, null, order
                    );

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            MediaImage image = new MediaImage();
                            image.id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                            image.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            image.albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                            image.albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                            images.add(image);

                            MediaAlbum intoAlbum = null;
                            for (MediaAlbum album : albums) {
                                if (album.albumId.equals(image.albumId)
                                        && album.albumName.equals(image.albumName)) {
                                    intoAlbum = album;
                                    break;
                                }
                            }

                            if (intoAlbum == null) {
                                intoAlbum = new MediaAlbum();
                                intoAlbum.albumId = image.albumId;
                                intoAlbum.albumName = image.albumName;
                                albums.add(intoAlbum);
                            }

                            intoAlbum.images.add(image);
                        }

                        cursor.close();
                        cursor = null;
                    }

                    //内部存储
                    cursor = context.getContentResolver().query(
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, where, null, order
                    );

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            MediaImage image = new MediaImage();
                            image.id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                            image.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            image.albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                            image.albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                            images.add(image);

                            MediaAlbum intoAlbum = null;
                            for (MediaAlbum album : albums) {
                                if (album.albumId.equals(image.albumId)
                                        && album.albumName.equals(image.albumName)) {
                                    intoAlbum = album;
                                    break;
                                }
                            }

                            if (intoAlbum == null) {
                                intoAlbum = new MediaAlbum();
                                intoAlbum.albumId = image.albumId;
                                intoAlbum.albumName = image.albumName;
                                albums.add(intoAlbum);
                            }

                            intoAlbum.images.add(image);
                        }

                        cursor.close();
                        cursor = null;
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                }

                //默认相册
                String defaultAlbumName = FileMaster.getInstance().getLongImageDir().getName();
                MediaAlbum intoAlbum = null;
                for (MediaAlbum album : albums) {
                    if (album.albumName.equals(defaultAlbumName)) {
                        intoAlbum = album;
                        break;
                    }
                }

                if (intoAlbum == null) {
                    intoAlbum = new MediaAlbum();
                    intoAlbum.albumId = "-1";
                    intoAlbum.albumName = defaultAlbumName;
                    albums.add(intoAlbum);
                }

                File[] wcImages = FileMaster.getInstance().getLongImageDir().listFiles();
                if (wcImages != null && wcImages.length > 0) {
                    for (File imageFile : wcImages) {
                        String name = imageFile.getName().toLowerCase();
                        if (imageFile.isFile()
                                && imageFile.exists()
                                && (name.endsWith(".png")
                                ||name.endsWith(".jpg")
                                ||name.endsWith(".jpeg"))) {

                            MediaImage image = build(imageFile.getAbsolutePath(), imageFile.getName(), "");
                            if (!intoAlbum.images.contains(image)) {
                                intoAlbum.images.add(image);
                            }

                            if (!images.contains(image)) {
                                images.add(0, image);
                            }
                        }
                    }
                }

                if (intoAlbum.images.size() == 0) {
                    albums.remove(intoAlbum);
                }

                if (handler != null) {
                    handler.picked(images, albums);
                }

            }

        });


    }

    /**
     * 插入新数据
     *
     * @param context
     * @param image
     * @return
     */
    public static boolean insert(Context context, MediaImage image) {
        if (image == null || context == null) {
            return false;
        }

        if (StringUtils.isEmpty(image.path)) {
            return false;
        }

        String insertUrl = null;
        try {
            insertUrl = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    image.path, image.title, image.desc);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return !StringUtils.isEmpty(insertUrl);
    }

    public static interface PickHandler {

        /**
         * 获取信息回馈
         *
         * @param all
         * @param albums
         */
        void picked(List<MediaImage> all, List<MediaAlbum> albums);
    }
}
