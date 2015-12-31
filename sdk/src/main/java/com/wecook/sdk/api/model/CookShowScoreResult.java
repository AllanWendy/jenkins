package com.wecook.sdk.api.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;

import org.json.JSONException;

/**
 * 晒厨艺打分界面
 *
 * @author lenovo
 * @since 2014/12/25.
 */
public class CookShowScoreResult extends ApiModel {

    @SerializedName("score")
    private String score;

    @Override
    public void parseJson(String json) throws JSONException {
        Gson gson = new Gson();
        CookShowScoreResult cookShowScoreResult = gson.fromJson(json, CookShowScoreResult.class);
        if (cookShowScoreResult != null) {
            score = cookShowScoreResult.score;
        }
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
