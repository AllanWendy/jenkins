package com.wecook.sdk.api.model;

import android.util.JsonWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wecook.common.utils.JsonUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.base.Selector;

import org.json.JSONException;

import java.io.IOException;
import java.io.StringWriter;

/**
 * 地址
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/6/24
 */
public class Address extends Selector {

    protected String id;
    protected String city = "北京";
    protected String name;
    protected String tel;
    protected String street = "";
    protected String addon = "";//门牌号等详细信息
    protected boolean isDefault;
    protected Location location;
    protected boolean isAvailable = true;//是否可用

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Location getLocation() {
        return location;
    }

    public String getLat() {
        if (location != null) {
            return location.getLatitude();
        }
        return "0";
    }

    public String getLon() {
        if (location != null) {
            return location.getLongitude();
        }
        return "0";
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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getFullStreet() {
        String fullStreet = "";
        if (!StringUtils.isEmpty(city)) {
            fullStreet += city;
        }
        if (!StringUtils.isEmpty(street)) {
            fullStreet += street;
        }
        if (!StringUtils.isEmpty(addon)) {
            fullStreet += addon;
        }
        return fullStreet;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAddon() {
        return addon;
    }

    public void setAddon(String addon) {
        this.addon = addon;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JsonElement element = JsonUtils.getJsonElement(json);
        if (element != null) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (object.has("id")) {
                    if (object.get("id").isJsonPrimitive()) {
                        id = object.get("id").getAsString();
                    }
                }
                if (object.has("city")) {
                    if (object.get("city").isJsonPrimitive()) {
                        city = object.get("city").getAsString();
                    }
                }
                if (object.has("name")) {
                    if (object.get("name").isJsonPrimitive()) {
                        name = object.get("name").getAsString();
                    }
                }
                if (object.has("tel")) {
                    if (object.get("tel").isJsonPrimitive()) {
                        tel = object.get("tel").getAsString();
                    }
                }
                if (object.has("street")) {
                    if (object.get("street").isJsonPrimitive()) {
                        street = object.get("street").getAsString();
                    }
                } else if (object.has("address")) {
                    if (object.get("address").isJsonPrimitive()) {
                        street = object.get("address").getAsString();
                    }
                }
                if (object.has("addon")) {
                    if (object.get("addon").isJsonPrimitive()) {
                        addon = object.get("addon").getAsString();
                    }
                }
                if (object.has("is_default")) {
                    isDefault = (1 == object.get("is_default").getAsInt());
                }
                if (object.has("location")) {
                    location = new Location();
                    location.parseJson(object.get("location").toString());
                }
            }
        }

    }

    public String toJson() throws IOException {

        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent(" ");
        jsonWriter.setLenient(true);
        jsonWriter.beginObject()
                .name("id").value(id)
                .name("city").value(city)
                .name("name").value(name)
                .name("tel").value(tel)
                .name("street").value(street)
                .name("is_default").value(isDefault ? 1 : 0)
                .name("location")
                .beginObject()
                .name("lat").value(location != null ? location.getLatitude() : "0")
                .name("lon").value(location != null ? location.getLongitude() : "0")
                .endObject()
                .endObject()
                .flush();
        jsonWriter.close();
        writer.flush();
        writer.close();
        return writer.toString();
    }

    /**
     * 城市+街道
     *
     * @return
     */
    public String getAddress() {
        if (null != city && null != street && !"".equals(city) && street.contains(city)) {
            return city + street.replace(city, "");
        }
        return city + street;
    }

    /**
     * 城市+街道+小区
     *
     * @return
     */
    public String getFullAddress() {
        String address = getAddress();
        String fullStreet = getFullStreet();
        if (null != fullStreet && !"".equals(fullStreet) && !address.contains(fullStreet)) {
            if (fullStreet.contains(address)) {
                address = fullStreet;
            } else {
                address += fullStreet;
            }
        }
        return address;
    }


    @Override
    public Address clone() {
        Address address = new Address();
        address.id = id;
        address.city = city;
        address.name = name;
        address.tel = tel;
        address.street = street;
        address.addon = addon;
        address.isDefault = isDefault;
        address.location = location;
        return address;
    }
}
