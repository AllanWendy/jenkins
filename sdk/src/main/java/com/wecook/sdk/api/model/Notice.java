package com.wecook.sdk.api.model;

import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;

import java.util.List;

/**
 * 公告
 *
 * @author kevin
 * @version v1.0
 * @since 2015-15/7/1
 */
public class Notice extends ApiModel {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("desc")
    private String desc;

    @SerializedName("img")
    private String image;

    @SerializedName("url")
    private String url;

    private List<Note> notes;

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public void parseJson(String json) throws JSONException {
        Notice notice = JsonUtils.getModel(json, Notice.class);
        if (notice != null) {
            id = notice.id;
            title = notice.title;
            desc = notice.desc;
            image = notice.image;
            url = notice.url;
        }
    }

    public static class Note {
        public String title;
        public int color = -1;
        public List<NoteItem> items;
    }

    public static class NoteItem {
        public String icon;
        public String name;
    }
}
