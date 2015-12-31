package com.wecook.sdk.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.core.internet.ApiModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用户
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class User extends ApiModel implements Parcelable {
    public static final int PWD_STATE_DEFAULT = 1;//未设置密码，为默认密码
    public static final int PWD_STATE_USER = 0;//设置了密码
    public static final int PWD_STATE_PLATFORM = -1;//第三方登录,未设置密码
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    @SerializedName("uid")
    private String uid;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("avatar")
    private String avatar;
    @SerializedName("gender")
    private String gender;
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("dpw")
    private String loginStyle;
    @SerializedName("city")
    private String city;
    private String qq;
    private String email;
    @SerializedName("mobile")
    private String phone;
    private String weixin;
    private String weibo;

    public User() {
    }

    public User(Parcel source) {
        uid = source.readString();
        nickname = source.readString();
        avatar = source.readString();
        gender = source.readString();
        birthday = source.readString();
        loginStyle = source.readString();
        city = source.readString();
        qq = source.readString();
        email = source.readString();
        phone = source.readString();
        weixin = source.readString();
        weibo = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(nickname);
        dest.writeString(avatar);
        dest.writeString(gender);
        dest.writeString(birthday);
        dest.writeString(loginStyle);
        dest.writeString(city);
        dest.writeString(qq);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(weixin);
        dest.writeString(weibo);
    }

    @Override
    public void parseJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("account")) {
            json = jsonObject.optJSONObject("account").toString();
        }

        Gson gson = new Gson();
        User user = gson.fromJson(json, User.class);
        if (user != null) {
            uid = user.uid;
            nickname = user.nickname;
            avatar = user.avatar;
            gender = user.gender;
            birthday = user.birthday;
            loginStyle = user.loginStyle;
            phone = user.phone;
            city = user.city;
        }
    }

    @Override
    public JSONArray findJSONArray(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has("favourite")) {
            return jsonObject.optJSONArray("favourite");
        }
        return super.findJSONArray(json);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLoginStyle() {
        return loginStyle;
    }

    public void setLoginStyle(String loginStyle) {
        this.loginStyle = loginStyle;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    public String getWeibo() {
        return weibo;
    }

    public void setWeibo(String weibo) {
        this.weibo = weibo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", gender='" + gender + '\'' +
                ", birthday='" + birthday + '\'' +
                ", loginStyle='" + loginStyle + '\'' +
                ", city='" + city + '\'' +
                ", qq='" + qq + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", weixin='" + weixin + '\'' +
                ", weibo='" + weibo + '\'' +
                '}';
    }
}
