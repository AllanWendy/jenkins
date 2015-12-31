package com.wecook.sdk.api.model;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;

import java.io.IOException;
import java.io.StringWriter;

/**
 * 更新信息
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/22
 */
public class UpdateConfig extends ApiModel {

    public static final int UPDATE_TYPE_NORMAL = 0;
    public static final int UPDATE_TYPE_FORCE = 1;

    /**
     * 版本号
     */
    private String versionNo;
    /**
     * 版本名
     */
    private String versionName;
    /**
     * 下载url
     */
    private String downloadUrl;
    /**
     * 更新描述
     */
    private String description;
    /**
     * md5校验串
     */
    private String fileMd5;
    /**
     * 是否需要升级
     */
    private boolean needUpgrade;
    /**
     * 更新类型
     */
    private int updateType;

    @Override
    public void parseJson(String json) throws JSONException {
        //解析结构
        JsonObject object = JsonUtils.getJsonObject(json);
        if (object != null) {
            versionNo = JsonUtils.getModelItemAsString(object, "latest_version");
            versionName = JsonUtils.getModelItemAsString(object, "latest_version_name");
            downloadUrl = JsonUtils.getModelItemAsString(object, "download_link");
            description = JsonUtils.getModelItemAsString(object, "remark");
            fileMd5 = JsonUtils.getModelItemAsString(object, "md5");
            needUpgrade = JsonUtils.getModelItemAsBoolean(object, "upgrade", available());
            updateType = JsonUtils.getModelItemAsInteger(object, "force_update", UPDATE_TYPE_NORMAL);
        }
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * 是否有新版本
     *
     * @return
     */
    public boolean hasNewUpgrade() {
        return needUpgrade;
    }

    public int getUpdateType() {
        return updateType;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionNo() {
        return StringUtils.parseInt(versionNo);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isNeedUpgrade() {
        return needUpgrade;
    }

    public void setNeedUpgrade(boolean needUpgrade) {
        this.needUpgrade = needUpgrade;
    }

    public void setUpdateType(int updateType) {
        this.updateType = updateType;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String toJson() {
        StringWriter json = new StringWriter();
        JsonWriter writer = new JsonWriter(json);
        try {
            writer.beginObject();
            writer.name("latest_version").value(versionNo);
            writer.name("latest_version_name").value(versionName);
            writer.name("download_link").value(downloadUrl);
            writer.name("remark").value(description);
            writer.name("upgrade").value(needUpgrade);
            writer.name("md5").value(fileMd5);
            writer.name("force_update").value(updateType);
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                json.flush();
                json.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return json.toString();
    }

    public boolean equalUpdateInfo(UpdateConfig o) {
        if (o != null) {
            return description.equals(o.description)
                    && downloadUrl.equals(o.downloadUrl)
                    && needUpgrade == o.needUpgrade
                    && fileMd5.equals(o.fileMd5)
                    && updateType == o.updateType;
        }
        return false;
    }
}
