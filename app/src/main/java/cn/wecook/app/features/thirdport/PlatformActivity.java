package cn.wecook.app.features.thirdport;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.wecook.common.app.BaseApp;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.modules.thirdport.object.IShareObject;
import com.wecook.common.modules.thirdport.platform.QQConnect;
import com.wecook.common.modules.thirdport.platform.WeiBlog;
import com.wecook.common.modules.thirdport.platform.WeiChat;
import com.wecook.common.modules.thirdport.platform.base.BasePlatform;
import com.wecook.common.modules.thirdport.platform.base.IPlatform;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.ShareState;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.activity.BaseActivity;
import com.wecook.uikit.alarm.IAlarm;
import com.wecook.uikit.alarm.ToastAlarm;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.wecook.app.R;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.dialog.ShareDialog;
import cn.wecook.app.main.home.user.UserBindPhoneActivity;

/**
 * 第三方平台
 *
 * @author kevin
 * @version v1.0
 * @since 2014-11/3/14
 */
public class PlatformActivity extends BaseActivity implements IPlatform.IPlatformEventListener {

    public static final String ACTION_SHARE = "cn.wecook.app.action_share";
    public static final String ACTION_LOGIN = "cn.wecook.app.action_login";

    public static final String EXTRA_DATA = "extra_data";
    public static final String EXTRA_PLATFORM = "extra_platform";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_ONLY_WEIXIN = "extra_only_weixin";
    public static final String EXTRA_TO_BIND = "extra_to_bind";

    private LoadingDialog mLoadingDialog;
    private boolean isToBinded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        holdIntent(getIntent());
    }

    private void holdIntent(Intent intent) {
        if (intent == null) {
            finishAfterDone(0);
            return;
        }
        PlatformManager.getInstance().onHandleNewIntent(this, intent);

        final String action = intent.getAction();
        final int platform = intent.getIntExtra(EXTRA_PLATFORM, -1);
        final boolean onlyWeixin = intent.getBooleanExtra(EXTRA_ONLY_WEIXIN, false);
        final CharSequence title = intent.getCharSequenceExtra(EXTRA_TITLE);
        final Serializable extra = intent.getSerializableExtra(EXTRA_DATA);

        if (extra instanceof ShareState) {
            Pattern pattern = Pattern.compile("★");
            Matcher matcher = pattern.matcher(title);
            if (matcher.find()) {
                ((SpannableString) title).setSpan(new ImageSpan(getContext(), R.drawable.app_ic_red_packet),
                        matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        isToBinded = intent.getBooleanExtra(EXTRA_TO_BIND, false);
        if (ACTION_SHARE.equals(action)) {
            final ShareDialog shareDialog;
            if (Build.BRAND.equals("vivo")) {
                shareDialog = new ShareDialog(this, title, onlyWeixin, null);
            } else {
                shareDialog = new ShareDialog(this, title, onlyWeixin);
            }
            shareDialog.setAlarmListener(new IAlarm.AlarmListener() {
                @Override
                public void onShow(IAlarm alarm) {
                }

                @Override
                public void onDismiss(IAlarm alarm) {

                }

                @Override
                public void onCancel(IAlarm alarm) {
                    finishAfterDone(0);
                }
            });
            shareDialog.setOnPositionClickListener(new ShareDialog.OnPositionClickListener() {
                @Override
                public void onClick(int platformType) {
                    showLoading();
                    prepareData(platformType, extra);
                }
            });
            shareDialog.show();
        } else if (ACTION_LOGIN.equals(action)) {
            showLoading();
            PlatformManager.getInstance().bind(getContext(), platform, PlatformActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
    }

    /**
     * 处理数据
     *
     * @param platformType
     * @param extra
     */
    private void prepareData(final int platformType, Serializable extra) {
        PlatformProcessor.dispatchPrepareData(getContext(), platformType, extra, new PlatformProcessor.OnPrepareListener() {
            @Override
            public void onPrepared(IShareObject object) {
                if (object != null) {
                    PlatformManager.getInstance().share(getContext(), platformType, object, PlatformActivity.this);
                }
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finishAfterDone(platformType);
                    }
                });
            }
        });
    }

    /**
     * 完成分享
     */
    private void finishAfterDone(int platformType) {
        if (platformType != PlatformManager.PLATFORM_WEBLOG) {
            hideLoading();
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        holdIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PlatformManager.getInstance().onHandleActivityResult(this, requestCode, resultCode, data);
    }

    public void showLoading() {
        hideLoading();
        mLoadingDialog = new LoadingDialog(getContext());
        mLoadingDialog.setText(R.string.app_tip_loading);
        mLoadingDialog.setAlarmListener(new IAlarm.AlarmListener() {
            @Override
            public void onShow(IAlarm alarm) {

            }

            @Override
            public void onDismiss(IAlarm alarm) {
            }

            @Override
            public void onCancel(IAlarm alarm) {
                hideLoading();
                finish();
            }
        });
        mLoadingDialog.show();
    }

    public void hideLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }


    @Override
    public void onResponseShare(IPlatform option, boolean success, String message) {
        if (option instanceof BasePlatform) {
            String action = ((BasePlatform) option).getCurrentAction();
            if (BasePlatform.ACTION_SHARE.equals(action)) {
                LogGather.onEventShare((BasePlatform) option, success, message);
                finishAfterDone(0);
            }
        }
    }

    @Override
    public void onResponseLogin(IPlatform option, boolean success, String message) {
        if (option instanceof BasePlatform) {
            String action = ((BasePlatform) option).getCurrentAction();
            if (BasePlatform.ACTION_LOGIN.equals(action)) {
                if (success) {
                    if (isToBinded) {
                        requestThirdBind(((BasePlatform) option).getType());
                    } else {
                        requestThirdLogin(((BasePlatform) option).getType());
                    }
                } else {
                    int type = ((BasePlatform) option).getType();
                    if (type == PlatformManager.PLATFORM_WEBLOG) {
                        hideLoading();
                        finish();
                    } else {
                        finishAfterDone(type);
                    }
                }
            }
        }
    }

    private void requestThirdBind(int type) {
        Object object = ThirdPortDelivery.getUserInfo(type);
        if (object == null) {
            return;
        }
        String platform = "";
        String userName = "";
        String nickName = "";
        String avatar = "";
        String gender = "";
        String token = "";
        switch (type) {
            case PlatformManager.PLATFORM_WECHAT: {
                WeiChat.WeChatUserInfo userInfo = (WeiChat.WeChatUserInfo) object;
                platform = "weixin";
                userName = userInfo.unionid;
                nickName = userInfo.nickname;
                avatar = userInfo.headimgurl;
                gender = userInfo.sex == 1 ? "1" : "0";
                token = userInfo.token;
                UserProperties.saveWeiXin(userName);
                UserProperties.saveUserOpenId(userInfo.openid);
                break;
            }
            case PlatformManager.PLATFORM_QQ: {
                QQConnect.QQUserInfo userInfo = (QQConnect.QQUserInfo) object;
                platform = "qq";
                userName = userInfo.uid;
                nickName = userInfo.nickName;
                avatar = userInfo.avatar;
                gender = userInfo.isBoy() ? "1" : "0";
                token = userInfo.token;
                UserProperties.saveQQ(userName);
                UserProperties.saveUserOpenId(userInfo.uid);
                break;
            }
            case PlatformManager.PLATFORM_WEBLOG: {
                WeiBlog.UserInfo userInfo = (WeiBlog.UserInfo) object;
                platform = "weibo";
                userName = userInfo.uid;
                nickName = userInfo.name;
                avatar = userInfo.avatar;
                gender = userInfo.isGirl() ? "0" : "1";
                token = userInfo.token;
                UserProperties.saveWeiBo(userName);
                UserProperties.saveUserOpenId(userInfo.uid);
                break;
            }
        }
        Intent intent = new Intent(UserProperties.INTENT_BIND);
        LocalBroadcastManager.getInstance(BaseApp.getApplication()).sendBroadcast(intent);
        finishAfterDone(0);
    }

    @Override
    public void onResponseLogout(IPlatform option, boolean success, String message) {

    }

    @Override
    public void onResponsePay(IPlatform option, boolean success, String message) {

    }

    private void requestThirdLogin(int type) {
        Object object = ThirdPortDelivery.getUserInfo(type);
        if (object == null) {
            return;
        }
        String platform = "";
        String userName = "";
        String nickName = "";
        String avatar = "";
        String gender = "";
        String token = "";
        switch (type) {
            case PlatformManager.PLATFORM_WECHAT: {
                WeiChat.WeChatUserInfo userInfo = (WeiChat.WeChatUserInfo) object;
                platform = "weixin";
                userName = userInfo.unionid;
                nickName = userInfo.nickname;
                avatar = userInfo.headimgurl;
                gender = userInfo.sex == 1 ? "1" : "0";
                token = userInfo.token;
                UserProperties.saveWeiXin(userName);
                UserProperties.saveUserOpenId(userInfo.openid);
                break;
            }
            case PlatformManager.PLATFORM_QQ: {
                QQConnect.QQUserInfo userInfo = (QQConnect.QQUserInfo) object;
                platform = "qq";
                userName = userInfo.uid;
                nickName = userInfo.nickName;
                avatar = userInfo.avatar;
                gender = userInfo.isBoy() ? "1" : "0";
                token = userInfo.token;
                UserProperties.saveQQ(userName);
                UserProperties.saveUserOpenId(userInfo.uid);
                break;
            }
            case PlatformManager.PLATFORM_WEBLOG: {
                WeiBlog.UserInfo userInfo = (WeiBlog.UserInfo) object;
                platform = "weibo";
                userName = userInfo.uid;
                nickName = userInfo.name;
                avatar = userInfo.avatar;
                gender = userInfo.isGirl() ? "0" : "1";
                token = userInfo.token;
                UserProperties.saveWeiBo(userName);
                UserProperties.saveUserOpenId(userInfo.uid);
                break;
            }
        }

        final String platformInfo = platform;
        UserProperties.saveUserCurrentAccountType(platformInfo);

        UserApi.platformLogin(platformInfo, userName, nickName, avatar, gender, token,
                new ApiCallback<User>() {
                    @Override
                    public void onResult(User result) {
                        if (result != null && result.available()) {
                            UserProperties.login(result);
                            ToastAlarm.show("登录成功");
                            if (StringUtils.isEmpty(result.getPhone())) {
                                startActivity(new Intent(getContext(), UserBindPhoneActivity.class));
                            }
                        } else {
                            if (result.getStatusState() == -2) {//未注册
                                startActivity(new Intent(getContext(), UserBindPhoneActivity.class));
                            }
                            if (result != null) {
                                ToastAlarm.show(result.getErrorMsg());
                            } else {
                                ToastAlarm.show("未知原因");
                            }
                        }
                        finishAfterDone(0);
                    }
                });
    }
}
