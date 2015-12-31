package com.wecook.sdk.userinfo;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.wecook.common.app.BaseApp;
import com.wecook.common.modules.messager.XMPushMessager;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.modules.uper.UpperManager;
import com.wecook.common.utils.SecurityUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.DishPolicy;
import com.wecook.sdk.policy.MessageQueuePolicy;
import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 用户信息（包括登录，注册等）
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/1/14
 */
public class UserProperties {

    public static final String USER_GENDER_GIRL = "女";
    public static final String USER_GENDER_GIRL_CODE = "0";
    public static final String USER_GENDER_BOY = "男";
    public static final String USER_GENDER_BOY_CODE = "1";
    public static final String USER_GENDER_X = "保密";
    public static final String USER_GENDER_X_CODE = "2";

    public static final String CHAT_SESSION_ID = "chat_session_id";

    public static final String USER_ID = "user_id";

    public static final String USER_OPEN_ID = "user_open_id";
    public static final String USER_IS_LOGIN = "user_is_login";
    public static final String USER_NAME = "user_name";
    public static final String USER_AVATAR = "user_avatar";
    public static final String USER_GENDER = "user_gender";
    public static final String USER_BIRTHDAY = "user_birthday";
    public static final String USER_CITY = "user_city";
    public static final String USER_LOGINSTYLE = "user_loginstyle";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_QQ = "user_qq";
    public static final String USER_WEIXIN = "user_weixin";
    public static final String USER_WEIBO = "user_weibo";
    public static final String USER_PHONE = "user_phone";

    public static final String USER_LOGIN_STATE_PHONE_RAPID = "user_login_state_phone_rapid";
    public static final String USER_LOGIN_STATE_PHONE = "user_login_state_phone";
    public static final String USER_LOGIN_STATE_EMAIL = "user_login_state_email";
    public static final String USER_LOGIN_STATE_QQ = "qq";
    public static final String USER_LOGIN_STATE_WEIXIN = "weixin";
    public static final String USER_LOGIN_STATE_WEIBO = "weibo";

    public static final String USER_CURRENT_ACCOUNT_TYPE = "user_current_account_type";
    public static final String USER_VERIFY_PHONE_OLD_TIME = "user_verify_phone_old_time";

    public static final String INTENT_LOGIN = "cn.wecook.app.intent_login";
    public static final String INTENT_BIND = "cn.wecook.app.intent_bind";
    public static final String INTENT_LOGOUT = "cn.wecook.app.intent_logout";
    public static final String INTENT_UPDATE_INFO = "cn.wecook.app.intent_update_info";

    public static String getUserChatSessionId() {
        return String.valueOf(SharePreferenceProperties.get(CHAT_SESSION_ID, ""));
    }

    /**
     * 获得用户ID
     *
     * @return
     */
    public static String getUserId() {
        return String.valueOf(SharePreferenceProperties.get(USER_ID, ""));
    }

    public static String getUserCurrentAccountType() {
        return String.valueOf(SharePreferenceProperties.get(USER_CURRENT_ACCOUNT_TYPE, ""));
    }

    public static String getUserOpenId() {
        return String.valueOf(SharePreferenceProperties.get(USER_OPEN_ID, ""));
    }

    public static boolean isLogin() {
        Boolean isLogin = (Boolean) SharePreferenceProperties.get(USER_IS_LOGIN, false);
        if (StringUtils.isEmpty(getUserId())) {
            return false;
        }
        return isLogin;
    }

    public static String getUserName() {
        return String.valueOf(SharePreferenceProperties.get(USER_NAME, ""));
    }

    public static String getUserAvatar() {
        return String.valueOf(SharePreferenceProperties.get(USER_AVATAR, ""));
    }

    public static String getUserBirthday() {
        return String.valueOf(SharePreferenceProperties.get(USER_BIRTHDAY, ""));
    }

    public static String getUserCity() {
        return String.valueOf(SharePreferenceProperties.get(USER_CITY, ""));
    }

    public static String getUserLoginstyle() {
        return String.valueOf(SharePreferenceProperties.get(USER_LOGINSTYLE, ""));
    }

    public static String getUserGender() {
        String gender = String.valueOf(SharePreferenceProperties.get(USER_GENDER, ""));
        if (USER_GENDER_BOY_CODE.equals(gender)) {
            return USER_GENDER_BOY;
        } else if (USER_GENDER_GIRL_CODE.equals(gender)) {
            return USER_GENDER_GIRL;
        } else if (USER_GENDER_X_CODE.equals(gender)) {
            return USER_GENDER_X;
        }

        return USER_GENDER_GIRL;
    }

    public static void logout() {
        XMPushMessager.unsubscribe(BaseApp.getApplication(), SecurityUtils.encodeByMD5(getUserId()));
        MessageQueuePolicy.getInstance().clearMessageList();
        DishPolicy.get().clearLocal();

        SharePreferenceProperties.set(USER_IS_LOGIN, false);
        SharePreferenceProperties.set(UserProperties.USER_ID, "");
        SharePreferenceProperties.set(UserProperties.USER_NAME, "");
        SharePreferenceProperties.set(UserProperties.USER_AVATAR, "");
        SharePreferenceProperties.set(UserProperties.USER_GENDER, "");
        SharePreferenceProperties.set(UserProperties.USER_BIRTHDAY, "");
        SharePreferenceProperties.set(UserProperties.USER_CITY, "");
        SharePreferenceProperties.set(UserProperties.USER_LOGINSTYLE, "");
        SharePreferenceProperties.set(UserProperties.USER_EMAIL, "");
        SharePreferenceProperties.set(UserProperties.USER_QQ, "");
        SharePreferenceProperties.set(UserProperties.USER_WEIXIN, "");
        SharePreferenceProperties.set(UserProperties.USER_WEIBO, "");
        SharePreferenceProperties.set(UserProperties.USER_PHONE, "");
        SharePreferenceProperties.set(UserProperties.CHAT_SESSION_ID, "");
//        SharePreferenceProperties.set(UserProperties.USER_CURRENT_ACCOUNT_TYPE, "");
        SharePreferenceProperties.set(UserProperties.USER_OPEN_ID, "");
        Intent intent = new Intent(INTENT_LOGOUT);
        LocalBroadcastManager.getInstance(BaseApp.getApplication()).sendBroadcast(intent);
    }

    public static void login(User user) {
        if (user != null) {
            save(user);
            SharePreferenceProperties.set(UserProperties.USER_IS_LOGIN, user.getStatusState() == User.STATE_OK);
            Intent intent = new Intent(INTENT_LOGIN);
            LocalBroadcastManager.getInstance(BaseApp.getApplication()).sendBroadcast(intent);
            JSONObject jsonObject = UserProperties.getUserJSONObject();
            ZhugeSDK.getInstance().identify(BaseApp.getApplication(), UserProperties.getUserId(), jsonObject);
        }
    }

    public static void save(User user) {
        if (user != null) {
            SharePreferenceProperties.set(UserProperties.USER_ID, user.getUid());
            SharePreferenceProperties.set(UserProperties.USER_NAME, user.getNickname());
            SharePreferenceProperties.set(UserProperties.USER_AVATAR, user.getAvatar());
            SharePreferenceProperties.set(UserProperties.USER_GENDER, user.getGender());
            SharePreferenceProperties.set(UserProperties.USER_CITY, user.getCity());
            SharePreferenceProperties.set(UserProperties.USER_PHONE, user.getPhone());
            if (StringUtils.isLongNumber(user.getBirthday())) {
                String birthday = StringUtils.formatTime(Long.parseLong(user.getBirthday()), "yyyy-MM-dd");
                SharePreferenceProperties.set(UserProperties.USER_BIRTHDAY, birthday);
            } else {
                SharePreferenceProperties.set(UserProperties.USER_BIRTHDAY, user.getBirthday());
            }

            if (!SharePreferenceProperties.get(UserProperties.USER_ID, "").equals(user.getUid())) {//新用户登录清除验证限制
                SharePreferenceProperties.set(UserProperties.USER_VERIFY_PHONE_OLD_TIME, 0l);
            }

            //设置推送的唯一id别名
            XMPushMessager.setAlias(BaseApp.getApplication(), SecurityUtils.encodeByMD5(user.getUid()));
            XMPushMessager.subscribe(BaseApp.getApplication(), SecurityUtils.encodeByMD5(user.getUid()));
            SharePreferenceProperties.set(UserProperties.USER_LOGINSTYLE, user.getLoginStyle());
            UpperManager.asInstance().setUserId(user.getUid());
        }
    }

    public static void saveQQ(String qq) {
        if (!StringUtils.isEmpty(qq)) {
            SharePreferenceProperties.set(UserProperties.USER_QQ, qq);
        }
    }

    public static void saveUserCurrentAccountType(String type) {
        if (!StringUtils.isEmpty(type)) {
            SharePreferenceProperties.set(UserProperties.USER_CURRENT_ACCOUNT_TYPE, type);
        }
    }

    public static void saveUserOpenId(String openId) {
        if (!StringUtils.isEmpty(openId)) {
            SharePreferenceProperties.set(UserProperties.USER_OPEN_ID, openId);
        }
    }

    public static void saveEmail(String email) {
        if (!StringUtils.isEmpty(email)) {
            SharePreferenceProperties.set(UserProperties.USER_EMAIL, email);
        }
    }

    public static void saveWeiXin(String weixin) {
        if (!StringUtils.isEmpty(weixin)) {
            SharePreferenceProperties.set(UserProperties.USER_WEIXIN, weixin);
        }
    }

    public static void savePhone(String phone) {
        if (!StringUtils.isEmpty(phone)) {
            SharePreferenceProperties.set(UserProperties.USER_PHONE, phone);
        }
    }

    public static void saveWeiBo(String weibo) {
        if (!StringUtils.isEmpty(weibo)) {
            SharePreferenceProperties.set(UserProperties.USER_WEIBO, weibo);
        }
    }

    public static void saveAvatar(String avatar) {
        if (!StringUtils.isEmpty(avatar)) {
            SharePreferenceProperties.set(UserProperties.USER_AVATAR, avatar);
        }
    }

    public static void saveSessionId(String sessionId) {
        if (!StringUtils.isEmpty(sessionId)) {
            SharePreferenceProperties.set(UserProperties.CHAT_SESSION_ID, sessionId);
        }
    }

    public static void saveLoginStyle(String loginstyle) {
        if (!StringUtils.isEmpty(loginstyle)) {
            SharePreferenceProperties.set(UserProperties.USER_LOGINSTYLE, loginstyle);
        }
    }

    public static User getUser() {
        User user = new User();
        user.setAvatar(getUserAvatar());
        user.setBirthday(getUserBirthday());
        user.setNickname(getUserName());
        user.setGender(getUserGender());
        user.setUid(getUserId());
        user.setCity(getUserCity());
        user.setPhone(getUserPhone());
        user.setLoginStyle(getUserLoginstyle());
        return user;
    }

    public static String toJson() {
        OutJson outJson = new OutJson();

        if (isLogin()) {
            outJson.status = 1;
            outJson.info = "success";
            Result result = new Result();
            User user = getUser();
            result.avatar = user.getAvatar();
            result.birthday = user.getBirthday();
            result.gender = user.getGender();
            result.loginStyle = user.getLoginStyle();
            result.nickname = user.getNickname();
            result.uid = user.getUid();
            result.wid = PhoneProperties.getDeviceId();
            outJson.result = result;
        } else {
            outJson.status = 0;
            outJson.info = "no login";
        }
        Gson gson = new Gson();
        return gson.toJson(outJson);
    }

    public static JSONObject getUserJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("avatar", getUserAvatar());
            jsonObject.put("name", getUserName());
            jsonObject.put("gender", getUserGender());
            jsonObject.put("email", getUserEmail());
            jsonObject.put("mobile", getUserPhone());
            jsonObject.put("qq", getUserQQ());
            jsonObject.put("weixin", getUserWeiXin());
            jsonObject.put("weibo", getUserWeiBo());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private static String getUserEmail() {
        return String.valueOf(SharePreferenceProperties.get(USER_EMAIL, ""));
    }

    public static String getUserPhone() {
        return String.valueOf(SharePreferenceProperties.get(USER_PHONE, ""));
    }

    private static String getUserQQ() {
        return String.valueOf(SharePreferenceProperties.get(USER_QQ, ""));
    }

    private static String getUserWeiXin() {
        return String.valueOf(SharePreferenceProperties.get(USER_WEIXIN, ""));
    }

    private static String getUserWeiBo() {
        return String.valueOf(SharePreferenceProperties.get(USER_WEIBO, ""));
    }

    private static class OutJson {
        @SerializedName("status")
        private int status;

        @SerializedName("info")
        private String info;

        @SerializedName("result")
        private Result result;
    }

    private static class Result {
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

        @SerializedName("wid")
        private String wid;
    }
}
