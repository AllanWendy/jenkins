package com.wecook.sdk.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 标签
 *
 * @author kevin
 * @since 2014/12/24.
 */
public class Tags extends ApiModel implements Parcelable {

    private String name;
    private String icon;

    public Tags() {
    }

    public Tags(Parcel source) {
        name = source.readString();
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonPrimitive()) {
                name = json;
                icon = json;
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("icon")) {
                    JsonElement item = object.get("icon");
                    if (!item.isJsonNull()) {
                        icon = item.getAsString();
                    }
                }
                if (object.has("text")) {
                    JsonElement item = object.get("text");
                    if (!item.isJsonNull()) {
                        name = item.getAsString();
                    }
                }
            }
        }
    }

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        if (jsonObject.has("tags")) {
            return jsonObject.optJSONArray("tags");
        }

        if (jsonObject.has("list")) {
            return jsonObject.optJSONArray("list");
        }

        if (jsonObject.has("keywords")) {
            return jsonObject.optJSONArray("keywords");
        }

        return super.findJSONArray(json);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    public static final Creator<Tags> CREATOR = new Creator<Tags>() {
        @Override
        public Tags createFromParcel(Parcel source) {
            return new Tags(source);
        }

        @Override
        public Tags[] newArray(int size) {
            return new Tags[0];
        }
    };
}
