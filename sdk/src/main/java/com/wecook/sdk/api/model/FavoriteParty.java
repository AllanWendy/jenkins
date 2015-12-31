package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.sdk.api.model.base.Favorite;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 收藏数据类型
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/28/14
 */
public class FavoriteParty extends Favorite {

    @SerializedName("id")
    private String id;

    @SerializedName("create_time")
    private String createTime;

    private Party party;

    @Override
    public void parseJson(String json) throws JSONException {
        Gson gson = new Gson();
        FavoriteParty favoriteParty = gson.fromJson(json, FavoriteParty.class);
        if (favoriteParty != null) {
            id = favoriteParty.id;
            createTime = favoriteParty.createTime;
        }

        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("events")) {
            party = new Party();
            party.parseJson(jsonObject.optJSONObject("events").toString());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Party getParty() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }
}
