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
public class FavoriteArtist extends Favorite {

    @SerializedName("id")
    private String id;

    @SerializedName("create_time")
    private String createTime;

    private Topic topic;

    @Override
    public void parseJson(String json) throws JSONException {
        Gson gson = new Gson();
        FavoriteArtist artist = gson.fromJson(json, FavoriteArtist.class);
        if (artist != null) {
            id = artist.id;
            createTime = artist.createTime;
        }

        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("topic")) {
            topic = new Topic();
            topic.parseJson(jsonObject.optJSONObject("topic").toString());
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

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }
}
