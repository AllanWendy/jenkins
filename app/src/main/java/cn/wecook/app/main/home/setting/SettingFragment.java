package cn.wecook.app.main.home.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wecook.common.modules.asynchandler.AsyncUIHandler;
import com.wecook.common.modules.cache.HttpCache;
import com.wecook.common.modules.downer.image.ImageFetcher;
import com.wecook.common.modules.messager.XMPushMessager;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.policy.UpdatePolicy;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import java.util.ArrayList;

import cn.wecook.app.R;
import cn.wecook.app.dialog.AppUpdateDialog;
import cn.wecook.app.dialog.LoadingDialog;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.main.dish.address.DishAddressListFragment;
import cn.wecook.app.main.home.user.UserBindPhoneActivity;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 设置
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/10/14
 */
public class SettingFragment extends BaseTitleFragment {

    private final String TAG = "SettingFragment";
    private int mUpdateItem = 0;
    private ArrayList<View> mViewsList;
    private LinearLayout mUpdateLayout;
    private TextView mBtnLoginState;
    private TextView mLoginStateTip;
    private TextView mBindedPhone;
    private TextView mUpdatePwd;
    private View mUpdatePwdGroup;
    private TextView mBindedPhoneSubName;
    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (getActivity() == null) return;
            if (UserProperties.INTENT_LOGIN.equals(action)
                    || UserProperties.INTENT_LOGOUT.equals(action) || UserProperties.INTENT_UPDATE_INFO.equals(action)) {
                updateLoginState();
            }
        }
    };

    private void updateLoginState() {
        if (mBtnLoginState != null) {
            if (!UserProperties.isLogin()) {
                mBtnLoginState.setText(R.string.app_my_click_to_login);
                mBtnLoginState.setVisibility(View.GONE);
            } else {
                mBtnLoginState.setVisibility(View.VISIBLE);
                mBtnLoginState.setText(R.string.app_button_logout);
            }
        }
        String accountType = UserProperties.getUserCurrentAccountType();

        if (!UserProperties.isLogin()) {
            mUpdateLayout.setVisibility(View.GONE);
        } else {
            mUpdateLayout.setVisibility(View.VISIBLE);
            if (accountType.equals(UserProperties.USER_LOGIN_STATE_PHONE) || accountType.equals(UserProperties.USER_LOGIN_STATE_PHONE_RAPID)) {//手机
                if (mLoginStateTip != null && mBindedPhone != null && !StringUtils.isEmpty(UserProperties.getUserPhone()) && mUpdatePwd != null) {
                    mLoginStateTip.setText(getString(R.string.app_setting_item_login_state, "手机"));
                    String phoneNum = UserProperties.getUserPhone();
                    String phoneNumSub = phoneNum.substring(0, 3) + "****" + phoneNum.substring(7);
                    mBindedPhone.setText(getString(R.string.app_setting_item_binded_phone, phoneNumSub));
                    mBindedPhoneSubName.setText(getString(R.string.app_setting_item_binded_phone_sub));
                    mBindedPhoneSubName.setVisibility(View.VISIBLE);
                    if (Integer.valueOf(UserProperties.getUserLoginstyle()) == User.PWD_STATE_USER) {
                        mUpdatePwd.setText(getString(R.string.app_setting_item_update_pwd));
                    } else {
                        mUpdatePwd.setText(getString(R.string.app_setting_setpwd_title));
                    }
                }
            } else if (accountType.equals(UserProperties.USER_LOGIN_STATE_EMAIL) || accountType.equals(UserProperties.USER_LOGIN_STATE_WEIXIN)
                    || accountType.equals(UserProperties.USER_LOGIN_STATE_WEIBO) || accountType.equals(UserProperties.USER_LOGIN_STATE_QQ)) {//邮箱
                if (mLoginStateTip != null && mBindedPhone != null && mUpdatePwd != null) {
                    String phoneNum = UserProperties.getUserPhone();
                    if (!StringUtils.isEmpty(phoneNum)) {
                        String phoneNumSub = phoneNum.substring(0, 3) + "****" + phoneNum.substring(7);
                        mBindedPhone.setText(getString(R.string.app_setting_item_binded_phone, phoneNumSub));
                        mUpdatePwd.setVisibility(View.VISIBLE);
                        mUpdatePwd.setText(getString(R.string.app_setting_item_update_pwd));
                        mBindedPhoneSubName.setText(getString(R.string.app_setting_item_binded_phone_sub));
                        mBindedPhoneSubName.setVisibility(View.VISIBLE);
                    } else {
                        mBindedPhone.setText(getString(R.string.app_setting_item_unbind_phone));
                        mUpdatePwdGroup.setVisibility(View.GONE);
                        mBindedPhoneSubName.setText(getString(R.string.app_bind));
                    }
                    if (Integer.valueOf(UserProperties.getUserLoginstyle()) == User.PWD_STATE_USER) {
                        mUpdatePwd.setText(getString(R.string.app_setting_item_update_pwd));
                    } else {
                        mUpdatePwd.setText(getString(R.string.app_setting_setpwd_title));
                    }
                    if (accountType.equals(UserProperties.USER_LOGIN_STATE_EMAIL)) {
                        mLoginStateTip.setText(getString(R.string.app_setting_item_login_state, "邮箱"));
                    } else if (accountType.equals(UserProperties.USER_LOGIN_STATE_WEIXIN)) {
                        mLoginStateTip.setText(getString(R.string.app_setting_item_login_state, "微信"));
                    } else if (accountType.equals(UserProperties.USER_LOGIN_STATE_WEIBO)) {
                        mLoginStateTip.setText(getString(R.string.app_setting_item_login_state, "微博"));
                    } else if (accountType.equals(UserProperties.USER_LOGIN_STATE_QQ)) {
                        mLoginStateTip.setText(getString(R.string.app_setting_item_login_state, "QQ"));
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, null);
        mViewsList = new ArrayList<View>();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserProperties.INTENT_LOGIN);
        filter.addAction(UserProperties.INTENT_LOGOUT);
        filter.addAction(UserProperties.INTENT_UPDATE_INFO);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUserReceiver, filter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUserReceiver);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TitleBar titleBar = getTitleBar();
        titleBar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        titleBar.setBackgroundColor(getResources().getColor(R.color.uikit_white));

        titleBar.setTitle(getResources().getString(R.string.app_title_my_setting));

        titleBar.enableBottomDiv(true);
        titleBar.setBottomDivLineColor(getResources().getColor(R.color.uikit_grey));

        titleBar.setBackDrawable(R.drawable.uikit_bt_back_pressed);
        titleBar.setBackgroundColor(getResources().getColor(R.color.uikit_white));

        mUpdateLayout = (LinearLayout) view.findViewById(R.id.app_setting_update_layout);
        mLoginStateTip = (TextView) view.findViewById(R.id.app_setting_item_login_state);

        String[] itemNames = getResources().getStringArray(R.array.app_setting_items_name);

        int[] itemIds = {
                R.id.app_setting_item_update_pwd,
                R.id.app_setting_item_binded_phone,
                R.id.app_setting_item_account_bind,
                R.id.app_setting_item_share_app,
                R.id.app_setting_item_feedback,
                R.id.app_setting_item_yummy,
                R.id.app_setting_item_clear,
                R.id.app_setting_item_push,
                R.id.app_setting_item_about,
                R.id.app_setting_item_address,
                R.id.app_setting_item_check_upd,
                R.id.app_setting_item_provisions
        };

        //适配每一个item   初始化控件和监听
        for (int i = 0; i < itemIds.length; i++) {
            int id = itemIds[i];
            View layout = view.findViewById(id);
            TextView name = (TextView) layout.findViewById(R.id.app_my_feature_name);
            final TextView subName = (TextView) layout.findViewById(R.id.app_my_feature_sub_name);
            name.setText(itemNames[i]);
            if (itemIds[i] == R.id.app_setting_item_binded_phone) {
                mBindedPhoneSubName = subName;
                mBindedPhone = name;
            } else if (itemIds[i] == R.id.app_setting_item_update_pwd) {
                mUpdatePwd = name;
                mUpdatePwdGroup = layout;
            } else if (itemIds[i] == R.id.app_setting_item_push) {
                boolean openState = SharePreferenceProperties.get("push_open_state", true);
                subName.setText(openState ? "开启" : "关闭");
                subName.setVisibility(View.VISIBLE);
            } else if (itemIds[i] == R.id.app_setting_item_check_upd) {
                if (UpdatePolicy.get().hasLocalApk(getContext())) {
                    String localVersion = UpdatePolicy.get().getLocalApkVersionName(getContext());
                    subName.setText("发现新版本：V" + localVersion);
                    subName.setTextColor(getResources().getColor(R.color.uikit_orange));
                } else {
                    subName.setText("仅WIFI下载");
                }
                subName.setVisibility(View.VISIBLE);
            }
            mViewsList.add(layout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.app_setting_item_update_pwd:
                            next(ChangePwdFragment.class);
                            break;
                        case R.id.app_setting_item_binded_phone:
                            if (!StringUtils.isEmpty(UserProperties.getUserPhone())) {
                                next(VerifyPhoneFragment.class);
                            } else {
                                startActivity(new Intent(getContext(), UserBindPhoneActivity.class));
                            }
                            break;
                        case R.id.app_setting_item_account_bind:
                            next(AccountBindFragment.class);
                            break;
                        case R.id.app_setting_item_share_app:
                            shareApp();
                            break;
                        case R.id.app_setting_item_feedback:
                            if (UserProperties.isLogin()) {
                                next(FeedbackFragment.class, R.string.app_title_feedback);
                            } else {
                                startActivity(new Intent(getContext(), UserLoginActivity.class));
                            }
                            break;
                        case R.id.app_setting_item_yummy://点赞
                            requestStar();
                            break;
                        case R.id.app_setting_item_clear:
                            clearAllCache();
                            break;
                        case R.id.app_setting_item_push:
                            boolean openState = SharePreferenceProperties.get("push_open_state", true);
                            if (openState) {
                                XMPushMessager.pausePush(getContext());
                            } else {
                                XMPushMessager.resumePush(getContext());
                            }
                            openState = !openState;
                            SharePreferenceProperties.set("push_open_state", openState);
                            subName.setText(openState ? "开启" : "关闭");
                            break;
                        case R.id.app_setting_item_about:
                            next(AboutFragment.class);
                            break;
                        case R.id.app_setting_item_check_upd:
                            UpdatePolicy.get().checkUpdateVersion(getContext(), new AppUpdateDialog(getContext()), true);
                            break;
                        case R.id.app_setting_item_provisions:
                            next(ProvisionsFragment.class);
                            break;
                        case R.id.app_setting_item_address:
                            if (!UserProperties.isLogin()) {
                                startActivity(new Intent(getContext(), UserLoginActivity.class));
                                return;
                            }
                            LogGather.onEventMyAddress();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean(DishAddressListFragment.EXTRA_FOR_SHOW, true);
                            next(DishAddressListFragment.class, bundle);
                            break;
                    }
                }
            });
        }

        mBtnLoginState = (TextView) view.findViewById(R.id.app_setting_item_logout);
        updateLoginState();
        mBtnLoginState.findViewById(R.id.app_setting_item_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!UserProperties.isLogin()) {
                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                    return;
                }

                final LoadingDialog loadingDialog = new LoadingDialog(getContext());
                loadingDialog.setText(R.string.app_tip_loading);
                loadingDialog.show();
                AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {

                    @Override
                    public void run() {
                        UserProperties.logout();
                        PlatformManager.getInstance().enableShowNotify(false);
                        ThirdPortDelivery.logoutAll(getContext());
                    }

                    @Override
                    public void postUi() {
                        super.postUi();
                        loadingDialog.cancel();
                        back();
                    }
                });

            }
        });
    }

    /**
     * 分享APP
     */
    private void shareApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_share_app_content));
            startActivity(intent);
        } catch (Throwable throwable) {
        }
    }

    /**
     * 请求市场评分
     */
    private void requestStar() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + PhoneProperties.getPackageName()));
            startActivity(intent);
        } catch (Throwable e) {
            ToastAlarm.show("没有找到符合的应用市场，快去下载一个吧...");
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
    }

    private void clearAllCache() {
        ToastAlarm.show(R.string.app_tip_clear_cache_ing);
        AsyncUIHandler.post(new AsyncUIHandler.AsyncJob() {
            @Override
            public void postUi() {
                ToastAlarm.show(R.string.app_tip_clear_cache_finish);
            }

            @Override
            public void run() {
                //网络数据缓存
                HttpCache.clearCache();

                //图片内存和目录缓存
                ImageFetcher.asInstance().clear();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewsList.clear();
    }
}
