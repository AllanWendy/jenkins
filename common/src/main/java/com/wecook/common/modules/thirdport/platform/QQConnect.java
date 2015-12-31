package com.wecook.common.modules.thirdport.platform;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.wecook.common.R;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.modules.thirdport.platform.base.BasePlatform;
import com.wecook.common.utils.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * QQ开放平台
 *
 * @author zhoulu
 * @date 13-12-13
 */
public abstract class QQConnect extends BasePlatform implements IUiListener {

    public static Tencent mTencent;
    private boolean mIsNeedUpdate;
    private QQUserInfo mUserInfo = new QQUserInfo();

    protected QQConnect(Context context) {
        super(context);
    }

    @Override
    public boolean isSupportSSOShare() {
        return isInstalledApp() && !isNeedUpdate();
    }

    @Override
    public boolean isSupportWebShare() {
        return false;
    }

    @Override
    public boolean isNeedUpdate() {
        return mIsNeedUpdate;
    }

    public static boolean ready() {
        if (mTencent == null) {
            return false;
        }
        boolean ready = mTencent.isSessionValid()
                && mTencent.getQQToken().getOpenId() != null;
        return ready;
    }

    @Override
    public boolean isInstalledApp() {
        PackageManager pm = getContext().getPackageManager();
        boolean isInstalled = false;
        try {
            String qqPackageName = "com.tencent.mobileqq";
            pm.getApplicationInfo(qqPackageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            isInstalled = true;
            //首次安装BUG
            try {
                PackageInfo pi = pm.getPackageInfo(qqPackageName, PackageManager.GET_ACTIVITIES);
                int versionCode = pi.versionCode;
                if (versionCode <= 13) {
                    mIsNeedUpdate = true;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            try {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "QQ 未安装", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return isInstalled;
    }

    @Override
    public boolean isValidSession() {
        if (mTencent != null) {
            return mTencent.isSessionValid();
        }
        return super.isValidSession();
    }

    @Override
    public boolean onCreate() {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(getShareAppId(), getContext());
        }
        mUserInfo = QQUserInfo.load();
        return true;
    }

    @Override
    public void onLogin() {
        if (mTencent != null) {
            //无效的时候进行登录
            if (!mTencent.isSessionValid()) {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTencent.login((Activity) getContext(), getShareScope(), QQConnect.this);
                    }
                });
            } else {
                performLogin(true, "");
            }
        }
    }

    @Override
    public void onLogout() {
        mTencent.logout(getContext());
        performLogout(true, "");
    }

    @Override
    public boolean onEvent(Context context, Intent intent) {
        return false;
    }

    /*  IUiListener接口回调  */
    @Override
    public void onComplete(Object jsonObject) {
        String action = getCurrentAction();
        if (ACTION_LOGIN.equals(action)) {
            requestUserInfo();
        } else if (ACTION_LOGOUT.equals(action)) {
            performLogout(true, "");
        } else if (ACTION_SHARE.equals(action)) {
            performShare(true, getString(R.string.share_status_success));
        }
    }

    private void requestUserInfo() {
        final UserInfo userInfo = new UserInfo(getContext(), mTencent.getQQToken());
        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object o) {
                if (o != null) {
                    try {
                        JSONObject object = JsonUtils.getJSONObject(o.toString());
                        if (object != null) {
                            String result = object.getString("ret");
                            if ("0".equals(result)) {
                                mUserInfo.setUid(mTencent.getOpenId());
                                mUserInfo.parse(o.toString());
                                performLogin(true, getString(R.string.share_status_login_success));
                            } else {
                                onError(null);
                            }
                        } else {
                            onError(null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onError(null);
                    }
                } else {
                    onError(null);
                }
            }

            @Override
            public void onError(UiError uiError) {
                performLogin(false, getString(R.string.share_errcode_login_fail));
            }

            @Override
            public void onCancel() {
                performLogin(false, getString(R.string.share_errcode_login_cancel));
            }
        });
    }

    @Override
    public void onError(UiError uiError) {
        String action = getCurrentAction();
        if (ACTION_LOGIN.equals(action)) {
            performLogin(false, getString(R.string.share_errcode_login_fail));
        } else if (ACTION_LOGOUT.equals(action)) {
            performLogout(false, getString(R.string.share_errcode_login_fail));
        } else if (ACTION_SHARE.equals(action)) {
            performShare(false, getString(R.string.share_status_fail));
        }
    }

    @Override
    public void onCancel() {
        String action = getCurrentAction();
        if (ACTION_LOGIN.equals(action)) {
            performLogin(false, getString(R.string.share_errcode_login_cancel));
        } else if (ACTION_LOGOUT.equals(action)) {
            performLogout(false, getString(R.string.share_errcode_login_cancel));
        } else if (ACTION_SHARE.equals(action)) {
            performShare(false, getString(R.string.share_status_cancel));
        }
    }

    @Override
    public Object getUserInfo() {
        return mUserInfo;
    }

    public static class QQUserInfo {

        @SerializedName("uid")
        public String uid;

        @SerializedName("nickname")
        public String nickName;

        @SerializedName("figureurl_qq_2")
        public String avatar;

        @SerializedName("gender")
        public String gender;

        public String token;

        public boolean isBoy() {
            return "男".equals(gender);
        }

        public boolean isGirl() {
            return "女".equals(gender);
        }


        public void parse(String json) {
            Gson gson = new Gson();
            QQUserInfo userInfo = gson.fromJson(json, QQUserInfo.class);
            if (userInfo != null) {
                nickName = userInfo.nickName;
                avatar = userInfo.avatar;
                gender = userInfo.gender;
                token = mTencent.getAccessToken();
            }

            SharePreferenceProperties.set("qq_user_info", gson.toJson(this));
        }

        public static QQUserInfo load() {
            Gson gson = new Gson();
            String json = (String) SharePreferenceProperties.get("qq_user_info", "");
            QQUserInfo info = gson.fromJson(json, QQUserInfo.class);
            if (info == null) {
                return new QQUserInfo();
            } else {
                info.token = mTencent.getAccessToken();
                return info;
            }
        }

        public void setUid(String openId) throws JSONException {
            uid = openId;
        }
    }
}
