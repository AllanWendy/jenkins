package com.wecook.common.core.internet;

import android.os.Bundle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wecook.common.utils.JavaRefactorUtils;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Api数据模型
 *
 * @author kevin created at 9/19/14
 * @version 1.0
 */
public abstract class ApiModel implements Serializable, Cloneable {
    /**
     * 状态成功
     */
    public static final int STATE_OK = 1;
    /**
     * 状态失败
     */
    public static final int STATE_FAIL = 0;

    public int statusCode;

    public int statusState;

    public String errorMsg;

    //不进行序列化
    private transient Bundle extra = new Bundle();

    public Bundle getExtra() {
        return extra;
    }

    /**
     * 解析基础数据
     *
     * @param json
     */
    public final void parseResult(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            if (JsonUtils.isJsonObject(json)) {
                JsonObject obj = JsonUtils.getJsonObject(json);
                if (obj != null) {
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        JsonElement element = entry.getValue();
                        if (element.isJsonPrimitive()) {
                            extra.putString(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                    if (obj.has("status")) {
                        statusState = obj.get("status").getAsInt();
                    }
                    if (obj.has("info")) {
                        errorMsg = obj.get("info").getAsString();
                    }
                    if (obj.has("result")) {
                        Object object = obj.get("result");
                        parseJson(object.toString());
                    } else {
                        parseJson(json);
                    }
                } else {
                    parseJson(json);
                }
            }
        }
    }

    /**
     * 解析json数据
     *
     * @param json
     * @throws JSONException
     */
    public abstract void parseJson(String json) throws JSONException;

    /**
     * 获得列表数据
     *
     * @param json
     * @return
     * @throws JSONException
     */
    public JSONArray findJSONArray(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);
            if (element.isJsonObject()) {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.optJSONArray("list");
            }
        }
        return null;
    }

    /**
     * 可用
     *
     * @return
     */
    public boolean available() {
        return statusState == ApiModel.STATE_OK &&
                (statusCode >= 200 && statusCode <= 207);
    }

    public void setAvailable() {
        statusState = ApiModel.STATE_OK;
        statusCode = 200;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getStatusState() {
        return statusState;
    }

    public String getErrorMsg() {
        if (null != errorMsg && (errorMsg.contains("socket") || errorMsg.contains("connection"))) {
            errorMsg = "请求超时，请重试";
        }
        return errorMsg;
    }

    @Override
    public ApiModel clone() {
        ApiModel obj = null;
        try {
            obj = (ApiModel) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public <T extends ApiModel> List<T> parseJSONArray(JSONArray array, T t) throws JSONException {
        if (array == null || array.length() == 0)
            return null;
        ArrayList<T> list = new ArrayList<T>();
        int length = array.length();
        T item = t;
        Object obj = null;
        for (int index = 0; index < length; index++) {
            obj = array.opt(index);
            if (obj != null) {
                if (item != null) {
                    item.parseJson(obj.toString());
                    list.add(item);
                }
            }
            try {
                item = (T) JavaRefactorUtils.newInstanceForClass(((Object) t).getClass());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
