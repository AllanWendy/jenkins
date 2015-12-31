package cn.wecook.app.main.home.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wecook.common.core.debug.Logger;
import com.wecook.common.core.internet.Api;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.core.internet.ApiModelList;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.UserBindState;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.SwitchButton;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;

/**
 * 账号绑定界面
 *
 * @author sc.wang created at 15.6.1
 * @version 2.3.9
 */
public class AccountBindFragment extends BaseTitleFragment {

    private SwitchButton weixin;
    private SwitchButton qq;
    private SwitchButton weibo;

    private View itemWeiXin;
    private View itemQQ;
    private View itemWeiBo;

    private String mWeiXinId;
    private String mQQId;
    private String mWeiBoId;
    private String mCurrentPlatform;

    private final String WEIXIN = "weixin";
    private final String QQ = "qq";
    private final String WEIBO = "weibo";

    private ConfirmDialog confirmDialog;

    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UserProperties.INTENT_BIND.equals(intent.getAction())) {
                if (!StringUtils.isEmpty(mCurrentPlatform)) {
                    Logger.d("BroadcastReceiver INTENT_BIND");
                    requestBind(false, mCurrentPlatform, UserProperties.getUserOpenId());
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_bind, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TitleBar titleBar = getTitleBar();
        titleBar.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));
        titleBar.setTitle(getResources().getString(R.string.app_bind_account));
        titleBar.enableBottomDiv(false);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UserProperties.INTENT_BIND);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUserReceiver, filter);

        initView(view);
    }

    private void initView(View view) {

        confirmDialog = new ConfirmDialog(getContext(), R.string.app_alarm_continue_dialog_title2).setConfirm(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBind(true, mCurrentPlatform, UserProperties.getUserOpenId());
            }
        });
        confirmDialog.setCancel(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCheckState(mCurrentPlatform, false);
            }
        });
        confirmDialog.setConfirmText(getString(R.string.app_alarm_continue));

        itemQQ = view.findViewById(R.id.app_account_bind_item_qq);
        itemWeiBo = view.findViewById(R.id.app_account_bind_item_weibo);
        itemWeiXin = view.findViewById(R.id.app_account_bind_item_weixin);

        if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_QQ)) {
            itemQQ.setVisibility(View.GONE);
        } else if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_WEIXIN)) {
            itemWeiXin.setVisibility(View.GONE);
        } else if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_WEIBO)) {
            itemWeiBo.setVisibility(View.GONE);
        }

        String[] itemNames = getResources().getStringArray(R.array.app_account_bind_item_name);
        int[] itemDrawables = {
                R.drawable.app_ic_wexin,
                R.drawable.app_ic_qq,
                R.drawable.app_ic_weibo,
        };

        int[] itemIds = {
                R.id.app_account_bind_item_weixin,
                R.id.app_account_bind_item_qq,
                R.id.app_account_bind_item_weibo,
        };

        //适配每一个item   初始化控件和监听
        for (int i = 0; i < itemIds.length; i++) {
            View layout = view.findViewById(itemIds[i]);
            ImageView icon = (ImageView) layout.findViewById(R.id.app_my_feature_icon);
            TextView name = (TextView) layout.findViewById(R.id.app_my_feature_name);
            switch (itemIds[i]) {
                case R.id.app_account_bind_item_weixin:
                    weixin = (SwitchButton) layout.findViewById(R.id.app_my_feature_switch_button);
                    break;
                case R.id.app_account_bind_item_qq:
                    qq = (SwitchButton) layout.findViewById(R.id.app_my_feature_switch_button);
                    break;
                case R.id.app_account_bind_item_weibo:
                    weibo = (SwitchButton) layout.findViewById(R.id.app_my_feature_switch_button);
                    break;
            }
            icon.setImageResource(itemDrawables[i]);
            name.setText(itemNames[i]);
//            layout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    switch (v.getId()) {
//                        case R.id.app_account_bind_item_weixin:
//                            isWeiXinChecked = !isWeiXinChecked;
//                            weixin.setChecked(isWeiXinChecked);
//                            requestBind(WEIXIN, mWeiXinId);
//                            break;
//                        case R.id.app_account_bind_item_qq:
//                            isQQChecked = !isQQChecked;
//                            qq.setChecked(isQQChecked);
//                            requestBind(QQ, mQQId);
//                            break;
//                        case R.id.app_account_bind_item_weibo:
//                            isWeiBoChecked = !isWeiBoChecked;
//                            weibo.setChecked(isWeiBoChecked);
//                            requestBind(WEIBO, mWeiBoId);
//                            break;
//                    }
//                }
//            });
        }
    }

    private void setListener() {

        weixin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCurrentPlatform = WEIXIN;
                    if (StringUtils.isEmpty(mWeiXinId)) {
                        ThirdPortDelivery.bind(AccountBindFragment.this, PlatformManager.PLATFORM_WECHAT);
                    } else {
                        requestBind(false, WEIXIN, mWeiXinId);
                    }
                } else {
                    requestUnBind(WEIXIN);
                }
            }
        });
        weibo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCurrentPlatform = WEIBO;
                    if (StringUtils.isEmpty(mWeiBoId)) {
                        ThirdPortDelivery.bind(AccountBindFragment.this, PlatformManager.PLATFORM_WEBLOG);
                    } else {
                        requestBind(false, WEIBO, mWeiBoId);
                    }
                } else {
                    requestUnBind(WEIBO);
                }
            }
        });
        qq.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCurrentPlatform = QQ;
                    if (StringUtils.isEmpty(mQQId)) {
                        ThirdPortDelivery.bind(AccountBindFragment.this, PlatformManager.PLATFORM_QQ);
                    } else {
                        requestBind(false, QQ, mQQId);
                    }
                } else {
                    requestUnBind(QQ);
                }
            }
        });
    }

    private void requestBind(final boolean force, final String platform, String socialId) {
        if (NetworkState.available()) {
            Api.startNoCacheMode();
            UserApi.bindSocial(force, platform, socialId, new ApiCallback<State>() {
                @Override
                public void onResult(State result) {
                    if (result != null) {
                        if (result.available()) {
                            setCheckState(platform, true);
                        } else {
                            setCheckState(platform, false);
                            if (force) {
                                ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                if (result.getStatusState() == -2) {
                                    confirmDialog.show();
                                } else {
                                    ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else {
                        hideLoading();
                        setCheckState(platform, false);
                        ToastAlarm.makeToastAlarm(getContext(), getResources().getString(R.string.app_error_bind),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Api.stopNoCacheMode();
        } else {
            hideLoading();
        }
    }

    private void requestUnBind(final String platform) {
        if (NetworkState.available()) {
            Api.startNoCacheMode();
            UserApi.unbindSocial(platform, new ApiCallback<State>() {
                @Override
                public void onResult(State result) {
                    if (result != null) {
                        if (result.available()) {

                        } else {
                            ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                    Toast.LENGTH_SHORT).show();
                            setCheckState(platform, false);
                        }
                    } else {
                        setCheckState(platform, false);
                        hideLoading();
                        ToastAlarm.makeToastAlarm(getContext(), getResources().getString(R.string.app_error_unbind),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Api.stopNoCacheMode();
        } else {
            hideLoading();
        }
    }

    private void setCheckState(String platform, boolean checkFlag) {
        if (platform.equals(WEIXIN)) {
            weixin.setOnCheckedChangeListener(null);
            weixin.setChecked(checkFlag);
        } else if (platform.equals(QQ)) {
            qq.setOnCheckedChangeListener(null);
            qq.setChecked(checkFlag);
        } else if (platform.equals(WEIBO)) {
            weibo.setOnCheckedChangeListener(null);
            weibo.setChecked(checkFlag);
        }
        setListener();
    }

    private void resetCheckState() {
        weixin.setOnCheckedChangeListener(null);
        qq.setOnCheckedChangeListener(null);
        weibo.setOnCheckedChangeListener(null);
        weixin.setChecked(false);
        qq.setChecked(false);
        weibo.setChecked(false);
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        showLoading();
        UserApi.getSocialAccountList(new ApiCallback<ApiModelList<UserBindState>>() {
            @Override
            public void onResult(ApiModelList<UserBindState> result) {
                resetCheckState();
                if (result != null) {
                    if (result.available()) {
                        for (UserBindState userBindState : result.getList()) {
                            if (userBindState.getPlatformName().equals(WEIXIN)) {
                                mWeiXinId = userBindState.getSocialId();
                                weixin.setChecked(true);
                            } else if (userBindState.getPlatformName().equals(QQ)) {
                                mQQId = userBindState.getSocialId();
                                qq.setChecked(true);
                            } else if (userBindState.getPlatformName().equals(WEIBO)) {
                                mWeiBoId = userBindState.getSocialId();
                                weibo.setChecked(true);
                            }
                        }
                    } else {
                        if (result.getStatusState() != 0) {
                            ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    setListener();
                    hideLoading();
                } else {
                    hideLoading();
                    ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_net,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUserReceiver);
    }
}
