package com.wecook.sdk.api.legacy;

import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.utils.SecurityUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.api.model.UserBindState;
import com.wecook.sdk.userinfo.UserProperties;

/**
 * 用户信息相关API
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/6/14
 */
public class UserApi extends Api {

    //WEBVIEW   PATH
    public static final String TRADING_PATH = "http://m.wecook.cn/policy/trading";
    public static final String SERVICE_PATH = "http://m.wecook.cn/policy/service";
    public static final String PRIVACY_PATH = "http://m.wecook.cn/policy/privacy";
    public static final String BCP_PATH = "http://m.wecook.cn/policy/insurance";//食品安全责任保险条款
    public static final String BCP_COUPON_INFO = "http://m.wecook.cn/policy/coupon";//优惠券使用说明
    public static final String ZTDDSXY_PATH = "http://m.wecook.cn/policy/logistics_store";//自提点代收协议

    /**
     * 验证码
     *
     * @param mobile
     * @param callback
     */
    public static void verify(String mobile, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/verify")
                .isHttps(true)
                .addParams("account", mobile, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 请求语音验证码
     *
     * @param mobile
     * @param callback
     */
    public static void voiceVerify(String mobile, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/verify")
                .isHttps(true)
                .addParams("account", mobile, true)
                .addParams("type", "voice")
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 注册
     *
     * @param account
     * @param code
     * @param nickname
     * @param password
     * @param callback
     */
    public static void registerOfPhone(String account, String code, String nickname, String password,
                                       ApiCallback<User> callback) {
        Api.get(UserApi.class)
                .with("/user/register")
                .isHttps(true)
                .addParams("account", account, true)
                .addParams("nickname", nickname, true)
                .addParams("password", SecurityUtils.encodeBySHA1(password), true)
                .addParams("code", code)
                .toModel(new User())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 注册
     *
     * @param email
     * @param nickname
     * @param password
     * @param callback
     */
    public static void registerOfEmail(String email, String nickname, String password,
                                       ApiCallback<User> callback) {
        Api.get(UserApi.class)
                .with("/user/register")
                .isHttps(true)
                .addParams("account", email, true)
                .addParams("nickname", nickname, true)
                .addParams("password", SecurityUtils.encodeBySHA1(password), true)
                .toModel(new User())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 登录
     *
     * @param name
     * @param password
     * @param callback
     */
    public static void login(String name, String password, ApiCallback<User> callback) {
        Api.get(UserApi.class)
                .with("/user/login")
                .isHttps(true)
                .addParams("account", name, true)
                .addParams("password", SecurityUtils.encodeBySHA1(password), true)
                .toModel(new User())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 平台登录
     */
    public static void platformLogin(String platform, String userId, String nickName, String avatar,
                                     String gender, String token, ApiCallback<User> callback) {
        Api.get(UserApi.class)
                .with("/user/social_login")
                .isHttps(true)
                .addParams("type", platform, true)
                .addParams("foreign_id", userId, true)
                .addParams("nickname", nickName, true)
                .addParams("avatar", StringUtils.isEmpty(avatar) ? "http://u1.wecook.cn/avatar.png" : avatar, true)
                .addParams("gender", gender)
                .addParams("access_token", token)
                .toModel(new User())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获取用户信息
     *
     * @param uid
     * @param callback
     */
    public static void getInfo(String uid, ApiCallback<User> callback) {
        Api.get(UserApi.class)
                .with("/user/info")
                .isHttps(true)
                .addParams("uid", uid, true)
                .toModel(new User())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 更新用户信息接口
     *
     * @param uid
     * @param nickName
     * @param gender
     * @param birthday
     * @param callback
     */
    public static void updateInfo(String uid, String nickName, String gender, String birthday, String city,
                                  ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/updateinfo")
                .isHttps(true)
                .addParams("uid", uid, true)
                .addParams("nickname", nickName, true)
                .addParams("gender", gender, true)
                .addParams("birthday", birthday, true)
                .addParams("city", city)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 上传头像
     *
     * @param uid
     * @param avatar
     * @param callback
     */
    public static void uploadAvatar(String uid, byte[] avatar, ApiCallback<User> callback) {
        Api.get(UserApi.class)
                .with("/user/updateavatar")
                .isHttps(true)
                .addParams("uid", uid, true)
                .setBody("upfile", avatar)
                .toModel(new User())
                .setApiCallback(callback)
                .executePut();
    }

    /**
     * 修改密码
     *
     * @param uid
     * @param oldp
     * @param newp
     * @param callback
     */
    public static void updatePassword(String uid, String oldp, String newp, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/updatepassword")
                .isHttps(true)
                .addParams("uid", uid, true)
                .addParams("old", oldp, true)
                .addParams("new", newp, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 重置密码
     *
     * @param account
     * @param verifyCode
     * @param password
     * @param callback
     */
    public static void applyNewPassword(String account, String verifyCode, String password, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/resetpassword")
                .isHttps(true)
                .addParams("account", account, true)
                .addParams("password", SecurityUtils.encodeBySHA1(password), true)
                .addParams("code", verifyCode, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    //手机快捷登录
    public static void rapidLogin(String mobile, String code, ApiCallback<User> callback) {
        Api.get(UserApi.class)
                .with("/user/simplelogin")
                .isHttps(true)
                .addParams("mobile", mobile, true)
                .addParams("code", code, true)
                .toModel(new User())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 修改密码 或是给手机快捷登录的设置密码
     */
    public static void updatePWD(String oldpwd, String newpwd, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/updatepassword")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("oldpassword", SecurityUtils.encodeBySHA1(oldpwd), true)
                .addParams("password", SecurityUtils.encodeBySHA1(newpwd), true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 获得账号绑定的社交账号列表
     */
    public static void getSocialAccountList(ApiCallback<ApiModelList<UserBindState>> callback) {
        Api.get(UserApi.class)
                .with("/user/social")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .toModel(new ApiModelList<>(new UserBindState()))
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 绑定社交账号
     *
     * @param force
     * @param platform
     * @param socialId
     * @param callback
     */
    public static void bindSocial(boolean force, String platform, String socialId, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/social_bind")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("type", platform, true)
                .addParams("foreign_id", socialId, true)
                .addParams("confirm", (force ? 1 : 0) + "")
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 解绑社交账号
     *
     * @param platformName
     * @param callback
     */
    public static void unbindSocial(String platformName, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/social_unbind")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("type", platformName, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 绑定手机号
     *
     * @param phone
     * @param verify
     * @param callback
     */
    public static void bindMobile(String phone, boolean force, String verify, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/mobile_bind")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("mobile", phone, true)
                .addParams("confirm", (force ? 1 : 0) + "")
                .addParams("code", verify, true)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

    /**
     * 解绑手机号
     *
     * @param phone    手机号
     * @param verify   验证码
     * @param callback
     */
    public static void unbindMobile(String phone, String verify, ApiCallback<State> callback) {
        Api.get(UserApi.class)
                .with("/user/mobile_unbind")
                .isHttps(true)
                .addParams("uid", UserProperties.getUserId(), true)
                .addParams("mobile", phone, true)
                .addParams("code", verify)
                .toModel(new State())
                .setApiCallback(callback)
                .executeGet();
    }

}
