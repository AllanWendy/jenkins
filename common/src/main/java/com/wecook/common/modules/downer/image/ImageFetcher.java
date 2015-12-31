package com.wecook.common.modules.downer.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.ModuleManager;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.asynchandler.SyncHandler;
import com.wecook.common.modules.downer.image.transfor.BlurTransformation;
import com.wecook.common.modules.downer.image.transfor.RoundedCornersTransformation;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.ScreenUtils;
import com.wecook.common.utils.StringUtils;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * 图片下载缓存
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public class ImageFetcher extends BaseModule {

    /**
     * 250 MB of cache.
     */
    private static final int DEFAULT_DISK_CACHE_SIZE = 50 * 1024 * 1024;
    private static final String DEFAULT_DISK_CACHE_DIR = "image";

    private static GlideBuilder builder;
    private static ImageFetcher sInstance;

    private Context mContext;

    private ImageFetcher() {
    }

    /**
     * 对象实体
     *
     * @return
     */
    public static ImageFetcher asInstance() {
        if (sInstance == null) {
            sInstance = (ImageFetcher) ModuleManager.asInstance().getModule(ImageFetcher.class);
        }
        return sInstance;
    }

    static void setupGlideBuilder(Context context) {
        final int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        FifoPriorityThreadPoolExecutor resizeService = new FifoPriorityThreadPoolExecutor(cores);
        FifoPriorityThreadPoolExecutor diskCacheService = new FifoPriorityThreadPoolExecutor(1);

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        BitmapPool bitmapPool = null;
        //HONEYCOMB
        if (Build.VERSION.SDK_INT >= 11) {
            bitmapPool = new LruBitmapPool(calculator.getBitmapPoolSize());
        } else {
            bitmapPool = new BitmapPoolAdapter();
        }

        MemoryCache memoryCache = new LruResourceCache(calculator.getMemoryCacheSize());

        DiskCache diskCache = null;
        File cacheDir = FileUtils.getSdcardDir(context, DEFAULT_DISK_CACHE_DIR);//Glide.getPhotoCacheDir(context, DEFAULT_DISK_CACHE_DIR);
        if (cacheDir != null) {
            diskCache = DiskLruCacheWrapper.get(cacheDir, DEFAULT_DISK_CACHE_SIZE);
        }
        if (diskCache == null) {
            diskCache = new DiskCacheAdapter();
        }

        builder.setBitmapPool(bitmapPool);
        builder.setDiskCache(diskCache);
        builder.setDiskCacheService(diskCacheService);
        builder.setMemoryCache(memoryCache);
        builder.setResizeService(resizeService);
    }

    @Override
    public void setup(Context context) {
        mContext = context.getApplicationContext();
        if (builder == null) {
            builder = new GlideBuilder(mContext);
            setupGlideBuilder(mContext);
            Glide.setup(builder);
        }
        Glide.get(mContext);
    }

    public void load(String url, ImageView image, RequestListener requestListener) {
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url) || StringUtils.isEmpty(url)) {
            Glide.with(mContext)
                    .load(url)
                    .crossFade()
                    .fitCenter()
                    .listener(requestListener)
                    .into(image);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .crossFade()
                    .fitCenter()
                    .listener(requestListener)
                    .into(image);
        }
    }

    /**
     * 加载图片
     *
     * @param url
     * @param imageView
     */
    public void load(String url, ImageView imageView) {
        if (StringUtils.isEmpty(url)) {
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
        }

    }

    /**
     * 获得请求
     *
     * @param url
     * @param imageView
     * @return
     */
    public Request getRequest(String url, ImageView imageView) {
        return Glide.with(mContext)
                .load(url)
                .centerCrop()
                .crossFade()
                .into(imageView).getRequest();
    }

    public void loadRoundedCorner(String url, ImageView imageView) {
        if (StringUtils.isEmpty(url)) {
            return;
        }

        BitmapPool pool = Glide.get(mContext).getBitmapPool();
        Glide.with(mContext)
                .load(url)
                .bitmapTransform(new CenterCrop(pool),
                        new RoundedCornersTransformation(pool, ScreenUtils.dip2px(2), 2))
                .crossFade()
                .into(imageView);
    }

    public void loadRoundedCorner(String url, ImageView imageView, int radio) {
        if (StringUtils.isEmpty(url)) {
            return;
        }

        BitmapPool pool = Glide.get(mContext).getBitmapPool();
        Glide.with(mContext)
                .load(url)
                .bitmapTransform(new CenterCrop(pool),
                        new RoundedCornersTransformation(pool, radio, 2))
                .crossFade()
                .into(imageView);
    }

    public void loadBlur(String url, ImageView imageView) {
        loadBlur(url, imageView, BlurTransformation.MAX_RADIUS);
    }

    public void loadBlur(String url, ImageView imageView, int mRadius) {
        if (StringUtils.isEmpty(url)) {
            return;
        }

        BitmapPool pool = Glide.get(mContext).getBitmapPool();
        Glide.with(mContext)
                .load(url)
                .bitmapTransform(new CenterCrop(pool),
                        new BlurTransformation(mContext, pool, mRadius, 1, true))
                .crossFade()
                .into(imageView);
    }

    /**
     * 加载图片
     *
     * @param url
     * @param imageView
     */
    public void loadSimple(String url, ImageView imageView) {
        if (StringUtils.isEmpty(url)) {
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .into(imageView);
        }

    }

    /**
     * 加载图片
     *
     * @param url
     * @param imageView
     */
    public void loadSimple(String url, ImageView imageView, int defaultDrawableId) {
        if (StringUtils.isEmpty(url)) {
            imageView.setImageResource(defaultDrawableId);
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .placeholder(defaultDrawableId)
                    .into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .placeholder(defaultDrawableId)
                    .into(imageView);
        }

    }

    /**
     * 加载图片
     *
     * @param url
     * @param imageView
     */
    public void load(String url, ImageView imageView, int width, int height) {
        if (StringUtils.isEmpty(url)) {
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .centerCrop()
                    .crossFade()
                    .override(width, height)
                    .into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .centerCrop()
                    .crossFade()
                    .override(width, height)
                    .into(imageView);
        }
    }

    /**
     * 加载图片
     *
     * @param url
     * @param imageView
     * @param defImageId
     */
    public void load(String url, final ImageView imageView, final int defImageId) {
        if (StringUtils.isEmpty(url)) {
            imageView.setImageResource(defImageId);
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .centerCrop()
                    .placeholder(defImageId)
                    .error(defImageId)
                    .crossFade().into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .centerCrop()
                    .placeholder(defImageId)
                    .error(defImageId)
                    .crossFade()
                    .into(imageView);
        }
    }

    /**
     * 加载图片
     *
     * @param url
     * @param imageView
     * @param defImageId
     */
    public void load(String url, final ImageView imageView, final int defImageId, RequestListener<String, GlideDrawable> listener) {
        if (StringUtils.isEmpty(url)) {
            imageView.setImageResource(defImageId);
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .centerCrop()
                    .placeholder(defImageId)
                    .listener(listener)
                    .crossFade().into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .centerCrop()
                    .placeholder(defImageId)
                    .crossFade()
                    .into(imageView);
        }
    }

    /**
     * 加载图片
     *
     * @param url
     * @param imageView
     * @param defImageId
     */
    public void load(String url, final ImageView imageView, final Drawable defImageId) {
        if (StringUtils.isEmpty(url)) {
            imageView.setImageDrawable(defImageId);
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .placeholder(defImageId)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .placeholder(defImageId)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
        }
    }

    /**
     * 加载图片，制定大小
     *
     * @param url
     * @param imageView
     * @param defImageId
     * @param width
     * @param height
     */
    public void load(String url, final ImageView imageView, final int defImageId, int width, int height) {
        if (StringUtils.isEmpty(url)) {
            imageView.setImageResource(defImageId);
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .centerCrop()
                    .crossFade()
                    .placeholder(defImageId)
                    .override(width, height)
                    .into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .centerCrop()
                    .crossFade()
                    .placeholder(defImageId)
                    .override(width, height)
                    .into(imageView);
        }
    }

    /**
     * 加载图片，不使用内存缓存
     *
     * @param url
     * @param imageView
     */
    public void loadWithoutMemoryCache(String url, final ImageView imageView) {
        if (StringUtils.isEmpty(url)) {
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .skipMemoryCache(true)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
        }
    }

    /**
     * 加载图片，不使用内存缓存
     *
     * @param url
     * @param imageView
     * @param defImageId
     */
    public void loadWithoutMemoryCache(String url, final ImageView imageView, final int defImageId) {
        if (StringUtils.isEmpty(url)) {
            imageView.setImageResource(defImageId);
            return;
        }
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .skipMemoryCache(true)
                    .centerCrop()
                    .placeholder(defImageId)
                    .crossFade()
                    .into(imageView);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .skipMemoryCache(true)
                    .centerCrop()
                    .placeholder(defImageId)
                    .crossFade()
                    .into(imageView);
        }
    }

    /**
     * 单纯加载图片
     *
     * @param url
     */
    public void load(String url, RequestListener requestListener) {
        if (StringUtils.isEmpty(url)) {
            return;
        }
        ImageView emptyImage = new ImageView(mContext);
        emptyImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        if (URLUtil.isHttpUrl(url)
                || URLUtil.isHttpsUrl(url)) {
            Glide.with(mContext)
                    .load(url)
                    .listener(requestListener)
                    .into(emptyImage);
        } else if (FileUtils.isExist(url)) {
            Glide.with(mContext)
                    .load(new File(url))
                    .listener(requestListener)
                    .into(emptyImage);
        }
    }

    /**
     * 下载图片
     *
     * @param url
     * @param width
     * @param height
     * @param callback
     */
    public void download(String url, int width, int height, final Callback<File> callback) {
        final FutureTarget<File> futureTarget = Glide.with(mContext)
                .load(url)
                .downloadOnly(width, height);
        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            File file = null;

            @Override
            public void postUi() {
                if (callback != null) {
                    callback.callback(file);
                }
            }

            @Override
            public void run() {
                try {
                    file = futureTarget.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    Glide.clear(futureTarget);
                }
            }
        });

    }

    public Bitmap syncGetBitmap(final String url, final int w, final int h) {
        Bitmap bitmap = (Bitmap) SyncHandler.sync(new SyncHandler.Sync() {
            Bitmap data;
            boolean waiting;

            @Override
            public void syncStart() {
                getBitmap(url, w, h, new Callback<Bitmap>() {
                    @Override
                    public void callback(Bitmap obj) {
                        data = obj;
                        waiting = false;
                    }
                });

                waiting = true;
            }

            @Override
            public boolean waiting() {
                Logger.d("sync waiting.....");
                return waiting;
            }

            @Override
            public Object syncEnd() {
                return data;
            }
        });
        return bitmap;
    }

    public byte[] syncGetBitmapByte(final String url, final int w, final int h) {
        byte[] bytes = (byte[]) SyncHandler.sync(new SyncHandler.Sync() {
            byte[] data;
            boolean waiting;

            @Override
            public void syncStart() {
                getBitmapByte(url, w, h, new Callback<byte[]>() {
                    @Override
                    public void callback(byte[] obj) {
                        data = obj;
                        waiting = false;
                    }
                });

                waiting = true;
            }

            @Override
            public boolean waiting() {
                Logger.d("sync waiting.....");
                return waiting;
            }

            @Override
            public Object syncEnd() {
                return data;
            }
        });
        return bytes;
    }

    public void getBitmapByte(String url, int w, int h, final Callback<byte[]> callback) {
        Glide.with(mContext)
                .load(url)
                .asBitmap()
                .toBytes()
                .listener(new RequestListener<String, byte[]>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<byte[]> target,
                                               boolean isFirstResource) {
                        if (callback != null) {
                            callback.callback(null);
                        }
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(byte[] resource, String model,
                                                   Target<byte[]> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        if (callback != null) {
                            callback.callback(resource);
                        }
                        return true;
                    }
                })
                .into(w, h);
    }

    public void getBitmap(String url, int w, int h, final Callback<Bitmap> callback) {
        Glide.with(mContext)
                .load(url)
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        if (callback != null) {
                            callback.callback(null);
                        }
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (callback != null) {
                            callback.callback(resource);
                        }
                        return true;
                    }
                })
                .into(w, h);
    }

    /**
     * 低内存内存控制
     *
     * @param level
     */
    public void onLowMemory(int level) {
        try {
            if (level == -1) {
                Glide.get(mContext).clearMemory();
            } else {
                Glide.get(mContext).trimMemory(level);
            }
        } catch (Throwable e) {
        }

    }

    /**
     * 清除内存和目录缓存
     */
    public void clear() {
        try {
            Glide.get(mContext).clearMemory();
            Glide.get(mContext).clearDiskCache();
            FileUtils.clear(FileUtils.getSdcardDir(mContext, DEFAULT_DISK_CACHE_DIR));
        } catch (Throwable e) {
        }

    }

    public static interface Callback<T> {
        void callback(T obj);
    }

}
