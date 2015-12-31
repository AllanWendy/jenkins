package com.wecook.common.modules.filemaster;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.wecook.common.modules.BaseModule;
import com.wecook.common.modules.ModuleManager;
import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文件控制者
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public class FileMaster extends BaseModule{

    private static final String DIR_IMAGE = "image";
    private static final String DIR_FILE = "file";
    private static final String DIR_DOWNLOAD = "download";
    private static final String DIR_LOG = "log";
    private static final String DIR_CACHE = "cache";
    private static final String DIR_DATA = "data";

    private static FileMaster sInstance;

    private FileMaster(){}

    private static List<String> sTemporaryDir = new ArrayList<String>();

    public static FileMaster getInstance() {
        if (sInstance == null) {
            sInstance = (FileMaster) ModuleManager.asInstance().getModule(FileMaster.class);
        }
        return sInstance;
    }

    @Override
    public void setup(Context context) {
        super.setup(context);
        //StrictMode
        AsyncUIHandler.postParallel(new AsyncUIHandler.AsyncJob() {
            @Override
            public void run() {
                sTemporaryDir.add(getCacheDir().getAbsolutePath());
                sTemporaryDir.add(getFileDir().getAbsolutePath());
                sTemporaryDir.add(getImageDir().getAbsolutePath());
                sTemporaryDir.add(getLogDir().getAbsolutePath());
                sTemporaryDir.add(getDownloadDir().getAbsolutePath());
            }
        });
    }

    /**
     * 图片目录
     * @return
     */
    public File getImageDir() {
        return FileUtils.getSdcardDir(getContext(), DIR_IMAGE);
    }

    /**
     * 长期图片目录
     * @return
     */
    public File getLongImageDir() {
        return FileUtils.getAppRootSdcardDir(getContext(), DIR_IMAGE);
    }

    /**
     * 文件目录
     * @return
     */
    public File getFileDir() {
        return FileUtils.getSdcardDir(getContext(), DIR_FILE);
    }

    /**
     * 长期文件目录
     * @return
     */
    public File getLongFileDir() {
        return FileUtils.getAppRootSdcardDir(getContext(), DIR_FILE);
    }
    /**
     * 日志目录
     * @return
     */
    public File getLogDir() {
        return FileUtils.getSdcardDir(getContext(), DIR_LOG);
    }

    /**
     * 长期日志目录
     * @return
     */
    public File getLongLogDir() {
        return FileUtils.getAppRootSdcardDir(getContext(), DIR_LOG);
    }

    /**
     * 下载目录
     * @return
     */
    public File getDownloadDir() {
        return FileUtils.getSdcardDir(getContext(), DIR_DOWNLOAD);
    }

    /**
     * 长期下载目录
     * @return
     */
    public File getLongDownloadDir() {
        return FileUtils.getAppRootSdcardDir(getContext(), DIR_DOWNLOAD);
    }

    /**
     * 缓存目录
     * @return
     */
    public File getCacheDir() {
        return FileUtils.getSdcardDir(getContext(), DIR_CACHE);
    }

    /**
     * 长期缓存目录
     * @return
     */
    public File getLongCacheDir() {
        return FileUtils.getAppRootSdcardDir(getContext(), DIR_DATA);
    }

    public static boolean isSdcardWritable() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED) || status.equals(Environment.MEDIA_CHECKING)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSdcardMounted() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED) || status.equals(Environment.MEDIA_CHECKING) || status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断SD卡是否存在
     *
     * @return
     */
    public static boolean isSdcardExist() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !isExternalStorageRemovable())
            return true;
        else
            return false;
    }

    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT > 9) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static boolean isInTemporaryDir(String path) {
        File file = new File(path);
        String dir = file.getParentFile().getAbsolutePath();
        return Collections.binarySearch(sTemporaryDir, dir) > 0;
    }
}
