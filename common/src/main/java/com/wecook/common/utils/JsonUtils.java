package com.wecook.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.core.internet.ApiModelList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

/**
 * JSON解析帮助类
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/7/14
 */
public class JsonUtils {

    public static boolean isJsonObject(String json) {
        if (!StringUtils.isEmpty(json)) {

            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);

            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();

            return element.isJsonObject();
        }
        return false;
    }

    public static boolean isJsonArray(String json) {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
            return element.isJsonArray();
        }
        return false;
    }

    public static boolean isJsonString(String json) {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
            return element.isJsonPrimitive();
        }
        return false;
    }

    public static boolean isJsonNull(String json) {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
            return element.isJsonNull();
        }
        return true;
    }

    public static JsonElement getJsonElement(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
            return element;
        }
        return null;
    }

    public static JsonObject getJsonObject(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
            if (element.isJsonObject()) {
                return element.getAsJsonObject();
            }
        }
        return null;
    }

    public static JSONObject getJSONObject(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
            if (element.isJsonObject()) {
                return new JSONObject(json);
            }
        }
        return null;
    }

    public static JsonArray getJsonArray(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
            if (element.isJsonArray()) {
                return element.getAsJsonArray();
            }
        }
        return null;
    }

    public static JSONArray getJSONArray(String json) throws JSONException {
        if (!StringUtils.isEmpty(json)) {
            JsonParser parser = new JsonParser();
            StringReader reader = new StringReader(json);
            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.setLenient(true);
            JsonElement element = parser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
            if (element.isJsonArray()) {
                return new JSONArray(json);
            }
        }
        return null;
    }

    /**
     * ！！！对属性数量较多的时候，进行解析有性能问题
     *
     * @param json
     * @param tClass
     * @param <T>
     * @return T
     *
     * @deprecated 不建议再使用，性能消耗严重，使用getModelItemAsXXX替换
     */
    public static <T> T getModel(String json, Class<T> tClass) {
        Gson gson = new Gson();
        return gson.fromJson(json, tClass);
    }

    public static String toJson(Object model) {
        Gson gson = new Gson();
        return gson.toJson(model);
    }

    public static String getModelItemAsString(JsonObject jsonObject, String name) {
        return getModelItemAsString(jsonObject, name, "");
    }

    public static String getModelItemAsString(JsonObject jsonObject, String name, String def) {
        if (jsonObject != null && jsonObject.has(name)) {
            JsonElement item = jsonObject.get(name);
            if (!item.isJsonNull()) {
                return item.getAsString();
            }
        }
        return def;
    }

    public static boolean getModelItemAsBoolean(JsonObject jsonObject, String name, boolean def) {
        if (jsonObject != null && jsonObject.has(name)) {
            JsonElement item = jsonObject.get(name);
            if (!item.isJsonNull()) {
                return item.getAsBoolean();
            }
        }
        return def;
    }

    public static int getModelItemAsInteger(JsonObject jsonObject, String name, int def) {
        if (jsonObject != null && jsonObject.has(name)) {
            JsonElement item = jsonObject.get(name);
            if (!item.isJsonNull()) {
                return item.getAsBigInteger().intValue();
            }
        }
        return def;
    }

    public static <T extends ApiModel> T getModelItemAsObject(JsonObject jsonObject, String name, T item)
            throws JSONException {
        if (jsonObject != null && jsonObject.has(name)) {
            item.parseJson(jsonObject.get(name).toString());
            return item;
        }
        return item;
    }

    public static <T extends ApiModel> ApiModelList<T> getModelItemAsList(JsonObject jsonObject, String name, T item) throws JSONException {
        ApiModelList<T> list = null;
        if (jsonObject != null && jsonObject.has(name)) {
            list = new ApiModelList<>(item);
            list.parseJson(jsonObject.get(name).toString());
        }
        return list;
    }
}
