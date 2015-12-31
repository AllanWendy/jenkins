package com.wecook.sdk.api.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

/**
 * 地址建议
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/29
 */
public class AddressSuggestion extends ApiModel {

    private String name;
    private String address;
    private Location location;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void parseJson(String json) throws JSONException {

        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object != null) {
                    if (object.has("name")) {
                        JsonElement item = object.get("name");
                        if (!item.isJsonNull()) {
                            name = item.getAsString();
                        }
                    }
                    if (object.has("address")) {
                        JsonElement item = object.get("address");
                        if (!item.isJsonNull()) {
                            address = item.getAsString();
                        }
                    }
                    if (object.has("location")) {
                        location = new Location();
                        location.parseJson(object.get("location").toString());
                    }
                }
            }
        }
    }


    @Override
    public String toString() {
        return "AddressSuggestion{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", location=" + location +
                '}';
    }
}
