package com.wecook.sdk.api.model;

import android.content.Context;

import com.google.gson.JsonElement;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONException;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 渠道配置
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/10/22
 */
public class ChannelConfig extends ApiModel {

    private Context context;

    private String shoufa;

    private String channel;

    private String logo;

    private String version;

    private String stime;

    private String etime;

    public ChannelConfig(Context context) {
        this.context = context;
    }

    public void load() {
        try {
            InputStream is = context.getAssets().open("splash_config.txt");
            String json = StringUtils.newStringFromStream(is);
            if (!StringUtils.isEmpty(json)) {
                parseJson(json);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否有效性
     *
     * @return
     */
    public boolean validate() {
        if ("1".equals(shoufa)
                && PhoneProperties.getChannel().equals(channel)
                && PhoneProperties.getDebugVersionName().equals(version)
                && inTheTime()) {
            return true;
        }
        return false;
    }

    private boolean inTheTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startTime = dateFormat.parse(stime);
            Date endTime = dateFormat.parse(etime);
            Date current = new Date();
            if (current.after(startTime) && current.before(endTime)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null && element.isJsonObject()) {
            shoufa = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "sf");
            channel = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "channel");
            logo = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "logo");
            version = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "version");
            stime = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "stime");
            etime = JsonUtils.getModelItemAsString(element.getAsJsonObject(), "etime");
        }
    }

    public String getChannel() {
        return channel;
    }

    public Context getContext() {
        return context;
    }

    public String getEtime() {
        return etime;
    }

    public String getLogo() {
        return logo;
    }

    public String getShoufa() {
        return shoufa;
    }

    public String getStime() {
        return stime;
    }

    public String getVersion() {
        return version;
    }
}
