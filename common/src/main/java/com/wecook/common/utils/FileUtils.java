package com.wecook.common.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.wecook.common.app.BaseApp;
import com.wecook.common.modules.filemaster.FileMaster;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 文件辅助类
 *
 * @author kevin
 * @version v1.0
 * @since 2014-9/23/14
 */
public class FileUtils {

    public static final int BUFFER = 2048;

    private static final String APP_ROOT_DIR = "WeCook";
    /**
     * KB
     */
    public static final long ONE_KB = 1024;
    /**
     * MB
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;
    /**
     * GB
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * 获取SD卡可用大小
     *
     * @return
     */
    public static long getSDCardAvailableSpace() {
        File file = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        StatFs stat = new StatFs(file.getPath());

        long blockSize = stat.getBlockSize();

        long availableBlocks = stat.getAvailableBlocks();

        return availableBlocks * blockSize;
    }

    /**
     * 检查SD卡可用空间是否满足需要
     *
     * @return
     */
    public static boolean checkSDCardHasEnoughSpace(long size) {
        return getSDCardAvailableSpace() > size;
    }

    /**
     * 获得Sdcard/Android/data/package_name/cache/cacheDir目录
     *
     * @param cacheDir
     * @return
     */
    public static File getSdcardDir(Context context, String cacheDir) {
        return createDir(context.getExternalCacheDir(), cacheDir);
    }

    /**
     * 获得Sdcard/WeCook/cacheDir目录
     *
     * @param cacheDir
     * @return
     */
    public static File getAppRootSdcardDir(Context context, String cacheDir) {
        File appRootDir = getAppRootSdcardDir();
        return createDir(appRootDir, cacheDir);
    }

    public static File getAppRootSdcardDir() {
        return createDir(Environment.getExternalStorageDirectory(), APP_ROOT_DIR);
    }

    /**
     * 创建目录
     *
     * @param parent
     * @param dir
     * @return
     */
    public static File createDir(File parent, String dir) {
        File target = new File(parent, dir);
        if (!target.exists()) {
            target.mkdirs();
        }
        return target;
    }

    /**
     * 文件移动
     *
     * @param from
     * @param to
     */
    public static void moveTo(File from, File to) {
        if (to == null || from == null) {
            return;
        }
        if (to.exists()) {
            to.delete();
        }

        if (from.exists()) {
            boolean result = from.renameTo(to.getAbsoluteFile());
            if (!result) {
                try {
                    to.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    InputStream is = new BufferedInputStream(new FileInputStream(from));
                    FileOutputStream fo = new FileOutputStream(to);
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len = is.read(bytes)) > 0) {
                        fo.write(bytes, 0, len);
                    }
                    fo.close();

                    is.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 新建文件
     *
     * @param fileName
     * @return
     */
    public static File newFileInTemplate(String fileName) {
        File file = new File(FileMaster.getInstance().getFileDir(), fileName);
        clear(file);
        return file;
    }

    /**
     * 通过字节数据新建文件
     *
     * @param data
     * @param fileName
     * @return
     */
    public static File newFile(byte[] data, String fileName) {
        return newFileWithFullPath(data, newFileInTemplate(fileName));
    }

    /**
     * 通过字节数据新建文件
     *
     * @param data
     * @param dir
     * @param fileName
     * @return
     */
    public static File newFile(byte[] data, File dir, String fileName) {
        File file = new File(dir, fileName);
        clear(file);
        return newFileWithFullPath(data, file);
    }

    public static File newFileWithFullPath(byte[] data, File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 删除目录文件
     * 请在线程中调用
     *
     * @param dir
     */
    public static void clear(File dir) {
        if (dir == null) {
            return;
        }

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        } else if (dir.isFile()) {
            dir.delete();
            try {
                dir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 从steam中写到文件中
     *
     * @param stream
     * @param file
     * @return
     * @throws IOException
     */
    public static File writeStreamToFile(InputStream stream, File file) throws IOException {
        if (stream == null) {
            return null;
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = stream.read(bytes)) > 0) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.close();
        return file;
    }

    /**
     * 从steam中写到文件中
     *
     * @param stream
     * @param name
     * @return
     * @throws IOException
     */
    public static File newFileInStream(InputStream stream, String name) throws IOException {
        if (stream == null) {
            return null;
        }
        File file = newFileInTemplate(name);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = stream.read(bytes)) > 0) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.close();
        return file;
    }

    /**
     * 判断文件是否存在
     *
     * @param file
     * @return
     */
    public static boolean isExist(File file) {
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    public static boolean isExist(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.isFile() && file.exists();
    }

    /**
     * 压缩文件
     *
     * @param sourceDir
     * @param destFilePath
     * @return
     */
    public static boolean zip(String sourceDir, String destFilePath) {
        try {
            BufferedInputStream origin = null;
            File outFile = new File(destFilePath);
            if (!outFile.exists()) outFile.createNewFile();
            FileOutputStream dest = new FileOutputStream(outFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];
            File f = new File(sourceDir);
            if (f.isDirectory()) {
                File files[] = f.listFiles();

                for (int i = 0; i < files.length; i++) {
                    FileInputStream fi = new FileInputStream(files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(files[i].getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.flush();
                    origin.close();
                }
                out.close();
            } else {
                FileInputStream fi = new FileInputStream(f);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(f.getName());
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                out.flush();
                out.close();
                origin.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解压一个压缩文档 到指定位置
     *
     * @param zipFileString 压缩包的名字
     * @param outPathString 指定的路径
     * @throws Exception
     */
    public static void unzipFolder(String zipFileString, String outPathString)
            throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName = "";

        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPathString + File.separator + szName);
                file.createNewFile();
                // get the output stream of the file
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // read (len) bytes into buffer
                while ((len = inZip.read(buffer)) != -1) {
                    // write (len) byte from buffer at the position 0
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }

        inZip.close();
    }

    /**
     * zip解压
     *
     * @param zipFile   压缩文件
     * @param unzipDir 解压目录
     */
    public static void unzip(File zipFile, File unzipDir) {
        if (zipFile != null && unzipDir != null) {
            unzip(zipFile.getAbsolutePath(), unzipDir.getAbsolutePath());
        }
    }

    /**
     * 解压
     *
     * @param sourceZipFilePath
     * @param destDir
     * @return
     */
    public static boolean unzip(String sourceZipFilePath, String destDir) {
        try {
            String fileName = sourceZipFilePath;
            String filePath = destDir;
            ZipFile zipFile = new ZipFile(fileName);
            Enumeration<? extends ZipEntry> emu = zipFile.entries();
            while (emu.hasMoreElements()) {
                ZipEntry entry = emu.nextElement();
                if (entry.isDirectory()) {
                    new File(filePath + entry.getName()).mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(
                        zipFile.getInputStream(entry));
                File file = new File(filePath + "/" + entry.getName());
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);
                int count;
                byte data[] = new byte[BUFFER];
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    bos.write(data, 0, count);
                }
                bos.flush();
                bos.close();
                bis.close();
            }
            zipFile.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    public static void deleteFile(String filePath) {
        deleteFile(new File(filePath));
    }

    /**
     * 获得文件字节数组
     * @param path
     * @return
     */
    public static byte[] getFileBytes(String path){
        return getFileBytes(new File(path));
    }

    /**
     * 获得文件字节数组
     *
     * @param file
     * @return
     */
    public static byte[] getFileBytes(File file) {
        byte[] ret = new byte[0];
        try {
            if (!file.exists()) {
                // log.error("helper:the file is null!");
                return ret;
            }
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
            in.close();
            out.close();
            ret = out.toByteArray();
        } catch (IOException e) {
            // log.error("helper:get bytes from file process error!");
            e.printStackTrace();
        }
        return ret;
    }

    public static String readAssetsFile(String name) {
        try {
            InputStream is = BaseApp.getApplication().getAssets().open(name);
            return StringUtils.newStringFromStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFileString(File dir, String name) {
        File file = new File(dir, name);
        if (file.exists() && file.isFile() && file.canRead()) {
            byte[] content = getFileBytes(file);
            if (content.length > 0) {
                return new String(content);
            }
        }
        return "";
    }

    public static void deleteFiles(File[] files, File... exclude) {
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                boolean isExclude = false;
                if (exclude != null) {
                    for (int j = 0; j < exclude.length; j++) {
                        if (files[i].equals(exclude[j])) {
                            isExclude = true;
                            break;
                        }
                    }
                }

                if (!isExclude) {
                    FileUtils.deleteFile(files[i]);
                }
            }
        }
    }
}
