package com.wecook.sdk.policy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.RequestHandle;
import com.wecook.common.app.BaseApp;
import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.downer.file.FileDownloadListener;
import com.wecook.common.modules.downer.file.FileDownloader;
import com.wecook.common.modules.filemaster.FileMaster;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.FileUtils;
import com.wecook.common.utils.SecurityUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.ConfigApi;
import com.wecook.sdk.api.model.UpdateConfig;
import com.wecook.uikit.alarm.DialogAlarm;
import com.wecook.uikit.alarm.ToastAlarm;

import org.json.JSONException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

/**
 * 更新策略
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/22
 */
public class UpdatePolicy {

    public static final String VERSION_CHECK_ALERT_TIME = "version_check_alert_time";
    public static final String LOCAL_APK_INFO = "local_apk_update_info";
    private static UpdatePolicy sInstance;

    private RequestHandle mDownloadRequest;

    private Map<String, PackageInfo> packageInfoMap = new HashMap<>();

    private UpdatePolicy() {
    }

    public static UpdatePolicy get() {
        if (sInstance == null) {
            sInstance = new UpdatePolicy();
        }
        return sInstance;
    }

    /**
     * 是否需要显示升级提醒(每隔3天)
     *
     * @return
     */
    private boolean isNeedAlert() {
        long checkAlertTime = SharePreferenceProperties.get(VERSION_CHECK_ALERT_TIME, 0L);
        long intervalTime = System.currentTimeMillis() - checkAlertTime;
        if (intervalTime >= 3 * 24 * 60 * 60 * 1000) {
            return true;
        }
        return false;
    }

    /**
     * 下载新版本apk
     */
    private void downloadApk(final Context context, final UpdateConfig config, final UpdateDialog dialog) {
        if (!StringUtils.isEmpty(config.getDownloadUrl())) {
            final File tempApkFile = getTempApkFile(config);
            Logger.debug("update", "download app start");
            mDownloadRequest = FileDownloader.get().start(config.getDownloadUrl(), new FileDownloadListener(tempApkFile) {
                @Override
                public void onFinish() {
                    super.onFinish();
                    Logger.debug("update", "download app finished");
                    FileUtils.moveTo(getTargetFile(), getApkFile(config));
                    if (hasLocalApk(context, config)) {
                        SharePreferenceProperties.set(LOCAL_APK_INFO, config.toJson());
                        //清除不提醒标记
                        SharePreferenceProperties.set(VERSION_CHECK_ALERT_TIME, 0L);
                        UIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //显示新版本升级提醒对话框
                                showUpdateDialog(config, dialog);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 获得apk下载过程中的文件
     *
     * @param config
     * @return
     */
    public File getTempApkFile(UpdateConfig config) {
        return new File(FileMaster.getInstance().getFileDir(),
                "wecook-" + config.getVersionName() + "-" + config.getVersionNo() + ".temp");
    }

    /**
     * 获得apk文件
     *
     * @param config
     * @return
     */
    public File getApkFile(UpdateConfig config) {
        return new File(FileMaster.getInstance().getFileDir(),
                "wecook-" + config.getVersionName() + "-" + config.getVersionNo() + ".apk");
    }

    /**
     * 获得本地最新的版本号
     *
     * @param context
     * @return
     */
    public String getLocalApkVersionNo(Context context) {
        UpdateConfig config = new UpdateConfig();
        config.setVersionNo(PhoneProperties.getVersionCode());
        return getLocalApkVersionNo(context, config);
    }

    /**
     * 获得本地最新的版本名
     *
     * @param context
     * @return
     */
    public String getLocalApkVersionName(Context context) {
        UpdateConfig config = new UpdateConfig();
        config.setVersionNo(PhoneProperties.getVersionCode());
        if (context != null) {
            File dir = FileMaster.getInstance().getFileDir();
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".apk");
                }
            });
            String versionName = "0";
            int versionCode = 0;
            if (files != null) {
                for (File file : files) {
                    PackageInfo packageInfo = getPackageInfo(context, file);
                    if (checkApkFile(context, file, config)) {
                        if (packageInfo.versionCode > versionCode) {
                            versionCode = packageInfo.versionCode;
                            versionName = packageInfo.versionName;
                        }
                    }
                }
            }

            return versionName;
        }
        return "0";
    }

    /**
     * 获得最新本地apk的版本号
     *
     * @param context
     * @return
     */
    private String getLocalApkVersionNo(Context context, UpdateConfig config) {
        if (context != null) {
            File dir = FileMaster.getInstance().getFileDir();
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".apk");
                }
            });
            int versionCode = 0;
            if (files != null) {
                for (File file : files) {
                    PackageInfo packageInfo = getPackageInfo(context, file);
                    if (checkApkFile(context, file, config)) {
                        if (packageInfo.versionCode > versionCode) {
                            versionCode = packageInfo.versionCode;
                        }
                    }
                }
            }

            return versionCode + "";
        }
        return "0";
    }

    /**
     * 获得本地apk的包信息
     *
     * @param context
     * @param file
     * @return
     */
    private PackageInfo getPackageInfo(Context context, File file) {
        if (packageInfoMap.containsKey(file.getAbsolutePath())) {
            return packageInfoMap.get(file.getAbsolutePath());
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_META_DATA);
        packageInfoMap.put(file.getAbsolutePath(), pi);
        return pi;
    }

    /**
     * 检查apk有效性
     * 1.必须为update渠道
     * 2.md5校验通过
     * 3.版本大于等于本地版本
     *
     * @param context
     * @param file
     * @param config
     * @return
     */
    private boolean checkApkFile(Context context, File file, UpdateConfig config) {
        boolean md5CheckPass = false;
        if (!StringUtils.isEmpty(config.getFileMd5())) {
            //MD5校验
            String fileMd5 = SecurityUtils.encodeByMD5(getApkFile(config));
            if (config.getFileMd5().equals(fileMd5)) {
                md5CheckPass = true;
            }
        } else {
            md5CheckPass = true;
        }

        PackageInfo packageInfo = getPackageInfo(context, file);
        if (packageInfo != null
                && packageInfo.applicationInfo != null
                && packageInfo.applicationInfo.metaData != null) {
            String channel = packageInfo.applicationInfo.metaData.getString("UMENG_CHANNEL");
            Logger.d("update", "local [" + file.getAbsolutePath() + "] channel = " + channel);
            if ("update".equals(channel)
                    && md5CheckPass
                    && packageInfo.versionCode >= config.getVersionNo()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasLocalApk(Context context) {
        UpdateConfig config = new UpdateConfig();
        config.setVersionNo(PhoneProperties.getVersionCode());
        return hasLocalApk(context, config);
    }

    /**
     * 是否存在本地apk
     *
     * @param context
     * @param config
     * @return
     */
    private boolean hasLocalApk(Context context, UpdateConfig config) {
        String localVersion = getLocalApkVersionNo(context, config);
        if (!localVersion.equals("0")) {
            String currentVersion = PhoneProperties.getVersionCode();
            //必须比当前安装版本高
            if (Integer.parseInt(localVersion) > Integer.parseInt(currentVersion)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查升级
     *
     * @param dialog
     */
    public void checkUpdateVersion(final Context context, final UpdateDialog dialog) {
        checkUpdateVersion(context, dialog, false);
    }

    /**
     * 检查升级
     *
     * @param dialog
     */
    public void checkUpdateVersion(final Context context, final UpdateDialog dialog, final boolean showNotify) {
        if (context == null || dialog == null) {
            return;
        }

        if (showNotify) {
            SharePreferenceProperties.set(VERSION_CHECK_ALERT_TIME, 0L);
        }

        String currentVersion = PhoneProperties.getVersionCode();
        final UpdateConfig config = new UpdateConfig();
        //没有发现信息则显示默认
        config.setVersionName("");
        config.setVersionNo(currentVersion);
        config.setUpdateType(UpdateConfig.UPDATE_TYPE_NORMAL);
        config.setDescription("会做菜，更懂爱");

        Logger.debug("update", "app version : " + currentVersion);

        //1.检查本地是否已经有下载的apk，获得当前最新的版本号
        if (hasLocalApk(context, config)) {
            currentVersion = getLocalApkVersionNo(context, config);
            Logger.debug("update", "local app version : " + currentVersion);
        }

        //本地apk更新数据
        String localApkInfo = SharePreferenceProperties.get(LOCAL_APK_INFO, "");
        if (!StringUtils.isEmpty(localApkInfo)) {
            try {
                config.parseResult(localApkInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //2.检查是否存在最新版本
        ConfigApi.checkUpdateApp("" + config.getVersionNo(), new ApiCallback<UpdateConfig>() {
            @Override
            public void onResult(UpdateConfig result) {
                //判断下载条件：
                //1.更新开关开启
                //2.WIFI条件下
                //3.更新版本信息不一样
                //4.更新版本>当前版本
                //5.是否已经下载完毕
                if (result.available()
                        && result.hasNewUpgrade()
                        && NetworkState.availableWifi()
                        && !result.equalUpdateInfo(config)
                        && result.getVersionNo() > config.getVersionNo()
                        && !FileUtils.isExist(getApkFile(result))) {

                    if (mDownloadRequest != null
                            && !mDownloadRequest.isFinished()
                            && !mDownloadRequest.isCancelled()) {
                        //在下载中
                        Logger.debug("update", "new version in downloading");
                        if (showNotify) {
                            ToastAlarm.show("新版本下载中");
                        }
                        return;
                    }

                    //3.仅Wifi下载新版本apk
                    Logger.debug("update", "download app in wifi");
                    if (showNotify) {
                        ToastAlarm.show("新版本开始下载");
                    }
                    downloadApk(context, result, dialog);
                } else {
                    //4.检查是否需要提醒
                    Logger.debug("update", "haven't new version");
                    if (hasLocalApk(context, config)) {
                        Logger.debug("update", "find a new version apk in local");
                        showUpdateDialog(config, dialog);
                    } else {
                        if (showNotify) {
                            ToastAlarm.show("未发现有新版本～");
                        }
                    }
                }
            }
        });
    }

    /**
     * 初始化更新对话框
     *
     * @param config
     * @param dialog
     */
    private void showUpdateDialog(UpdateConfig config, UpdateDialog dialog) {
        //显示新版本升级提醒对话框
        if (isNeedAlert()) {
            if (dialog != null) {
                dialog.setUpdateConfig(config);
                dialog.show();
                Logger.debug("update", "show app update dialog");
            }
        }
    }

    /**
     * App更新提示对话框
     */
    public static abstract class UpdateDialog extends DialogAlarm {
        private View title;
        private View description;
        private View okBtn;
        private View cancelBtn;
        private View ignoreCheck;
        private View closeBtn;

        private UpdateConfig config;

        public UpdateDialog(Context context) {
            super(context);
        }

        void setUpdateConfig(UpdateConfig updateConfig) {
            config = updateConfig;
        }

        @Override
        public void onViewCreated(View view) {
            title = view.findViewById(getTitleId());
            description = view.findViewById(getDescriptionId());
            okBtn = view.findViewById(getOkButtonId());
            cancelBtn = view.findViewById(getCancelButtonId());
            ignoreCheck = view.findViewById(getIgnoreCheckId());
            closeBtn = view.findViewById(getCloseId());

            if (config != null) {
                if (title != null && title instanceof TextView) {
                    ((TextView) title).setText("发现新版本" + config.getVersionName());
                }

                if (description != null && description instanceof TextView) {
                    ((TextView) description).setText(config.getDescription());
                }

                //强制升级
                if (config.getUpdateType() == UpdateConfig.UPDATE_TYPE_FORCE) {
                    cancelable(false);
                    if (cancelBtn != null) {
                        cancelBtn.setVisibility(View.GONE);
                    }
                    if (ignoreCheck != null) {
                        ignoreCheck.setVisibility(View.GONE);
                    }
                    if (closeBtn != null) {
                        closeBtn.setVisibility(View.VISIBLE);
                        closeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ToastAlarm.show("有重要更新，确定不升级么？", 1000).among(new Runnable() {
                                    @Override
                                    public void run() {
                                        BaseApp.quitApp();
                                    }
                                });
                            }
                        });
                    }
                } else {
                    //非强制升级
                    if (closeBtn != null) {
                        closeBtn.setVisibility(View.GONE);
                    }

                    if (ignoreCheck != null) {
                        ignoreCheck.setVisibility(View.VISIBLE);
                        ignoreCheck.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ignoreCheck.setSelected(!ignoreCheck.isSelected());
                            }
                        });
                    }

                    if (cancelBtn != null) {
                        cancelBtn.setVisibility(View.VISIBLE);
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (ignoreCheck != null) {
                                    if (ignoreCheck.isSelected()) {
                                        //设置提醒开始时间
                                        SharePreferenceProperties.set(VERSION_CHECK_ALERT_TIME, System.currentTimeMillis());
                                    }
                                }
                                dismiss();
                            }
                        });
                    }
                }

                if (okBtn != null) {
                    okBtn.setVisibility(View.VISIBLE);
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //安装apk
                            File file = UpdatePolicy.get().getApkFile(config);
                            if (file.exists() && file.canRead()) {
                                Uri fileUri = Uri.fromFile(file);
                                Intent installIntent = new Intent();
                                installIntent.setAction(android.content.Intent.ACTION_VIEW);
                                installIntent.setDataAndType(fileUri,
                                        "application/vnd.android.package-archive");
                                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                BaseApp.getApplication().startActivity(installIntent);
                                dismiss();
                            }
                        }
                    });
                }
            }
        }

        public abstract int getTitleId();

        public abstract int getDescriptionId();

        public abstract int getOkButtonId();

        public abstract int getCancelButtonId();

        public abstract int getIgnoreCheckId();

        public abstract int getCloseId();

    }
}
