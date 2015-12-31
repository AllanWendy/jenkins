package cn.wecook.app.main.home.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wecook.common.app.BaseApp;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.policy.LogGather;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.dialog.ConfirmDialog;
import cn.wecook.app.main.home.setting.ChangePwdFragment;
import cn.wecook.app.utils.EditTextUtils;

/**
 * 绑定手机号
 *
 * @author sc.wang created at 15.6.1
 * @version 2.3.9
 */
public class UserBindPhoneFragment extends BaseTitleFragment {

    private static final int STATE_IDLE = 1;
    private static final int STATE_CHECK_PHONE_NAME_ERROR = 2;
    private static final int STATE_CHECK_PHONE_CODE_ERROR = 3;
    private static final int STATE_CLEAR_PHONE_NAME = 4;
    private static final int STATE_CLEAR_PHONE_CODE = 5;
    private static final int STATE_BIND_REQUEST_ING = 6;
    private static final int STATE_BIND_RESPONSE_ERROR = 7;
    private static final int STATE_BIND_RESPONSE_SUCCESS = 8;
    private static final int STATE_INPUT_PHONE_NAME = 9;
    private static final int STATE_INPUT_PHONE_CODE = 10;
    private static final int STATE_VERITY_CODE_ERROR = 11;

    private EditText mPhoneName;
    private EditText mVertifyCode;

    private View mPhoneNameClear;
    private View mVertifyCodeClear;
    private View mBindGroup;
    private View mBindLoading;

    private TextView mObtainVertify;
    private TextView mBtnBind;
    private TextView mTip;

    private TitleBar mTitleBar;

    private static final int MAX_WAITING_TIME = 60;
    private int mWaitTime;
    private boolean mWaitingVerity;
    private boolean isNewSocialAccount;
    private int mCurrentState = -1;
    private int mLastDialogNum;
    private ConfirmDialog confirmDialog;
    private final String TAG = "UserBindPhoneFragment";

    private Runnable mUpdatePhoneVerity = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) return;
            if (mWaitTime < 0) {
                mWaitTime = 0;
            }
            if (mWaitTime == 0) {
                mObtainVertify.setEnabled(true);
                mObtainVertify.setText(R.string.app_register_phone_get_verity);
                mWaitingVerity = false;
            } else {
                mObtainVertify.setEnabled(false);
                mObtainVertify.setText("(" + mWaitTime + ")" + "秒重新获取验证码");
                mWaitTime--;
                UIHandler.postDelayed(this, 1000);
                if (mWaitTime < MAX_WAITING_TIME - 10 && mVertifyCode.getText().length() == 0) {
                    mVoiceVerityTip.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    private TextView mVoiceVerityTip;
    private Spannable mVerityTip;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bind_phone, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTitleBar = getTitleBar();
        mTitleBar.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));
        mTitleBar.enableBottomDiv(false);

        mTitleBar.setBackListener(new View.OnClickListener() {//返回前清除登录状态
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(UserProperties.getUserPhone())) {
                    UserProperties.logout();
                }
                finishAll();
            }
        });

        //手机号码的编辑和监听
        mPhoneName = (EditText) view.findViewById(R.id.app_bind_phone_num);
        KeyboardUtils.openKeyboard(getContext(), mPhoneName);
        EditTextUtils.addTrimInflate(mPhoneName, 11);
        mPhoneName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    doStateChange(STATE_CLEAR_PHONE_NAME);
                } else {
                    doStateChange(STATE_INPUT_PHONE_NAME);
                }
            }
        });
        //手机号编辑框的清除按钮的监听
        mPhoneNameClear = view.findViewById(R.id.app_bind_phone_num_clear);
        mPhoneNameClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStateChange(STATE_CLEAR_PHONE_NAME);
            }
        });
        //验证码的输入框的初始化和监听
        mVertifyCode = (EditText) view.findViewById(R.id.app_bind_phone_code);
        mVertifyCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            //输入验证码后的监听的变化
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    doStateChange(STATE_CLEAR_PHONE_CODE);
                } else {
                    doStateChange(STATE_INPUT_PHONE_CODE);
                }
            }
        });
        //清除验证码的监听
        mVertifyCodeClear = view.findViewById(R.id.app_bind_phone_code_clear);
        mVertifyCodeClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStateChange(STATE_CLEAR_PHONE_CODE);
            }
        });

        mVoiceVerityTip = (TextView) view.findViewById(R.id.app_bind_phone_voice_verity);
        mVerityTip = StringUtils.getSpanClickable(getContext(), mVoiceVerityTip.getText().toString(),
                new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        // 请求语音验证码
                        final String phone = mPhoneName.getText().toString();
                        mVoiceVerityTip.setText("正在给您拨打电话，请等待接听...");
                        UserApi.voiceVerify(phone, new ApiCallback<State>() {
                            @Override
                            public void onResult(State result) {
                                if (!result.available()) {
                                    ToastAlarm.show(result.getErrorMsg());
                                    mVoiceVerityTip.setText(mVerityTip);
                                }
                            }
                        });
                    }
                }, R.color.uikit_dark_light);

        mVoiceVerityTip.setText(mVerityTip);
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVoiceVerityTip.setMovementMethod(LinkMovementMethod.getInstance());
                mVoiceVerityTip.setVisibility(View.GONE);
            }
        }, 50);

        //获取验证码的初始化和监听
        mObtainVertify = (TextView) view.findViewById(R.id.app_bind_phone_obtain_authcode);
        mObtainVertify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取验证码的请求：
                requestVerityCode();
            }
        });

        mBindLoading = view.findViewById(R.id.app_bind_phone_waiting);

        //登陆按钮的初始化
        mBtnBind = (TextView) view.findViewById(R.id.app_bind_phone_button_do);
        mBindGroup = view.findViewById(R.id.app_bind_phone_group);
        mBindGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStateChange(STATE_BIND_REQUEST_ING);
            }
        });
        confirmDialog = new ConfirmDialog(getContext(), R.string.app_alarm_continue_dialog_title).setConfirm(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNewSocialAccount) {
                    requestBindSocialAccount(true);
                } else {
                    requestBind(true);
                }
            }
        });
        confirmDialog.setConfirmText(getString(R.string.app_alarm_continue));
        mTip = (TextView) view.findViewById(R.id.app_bind_phone_tip);
        diffStyleShow();
    }

    public void diffStyleShow() {
        if (StringUtils.isEmpty(UserProperties.getUserId())) {//绑定手机号
            mTip.setVisibility(View.VISIBLE);
            mTitleBar.setTitle(getResources().getString(R.string.app_bind_phone));
            mPhoneName.setHint(R.string.app_rapid_login_account_name_hint);
            mVertifyCode.setHint(R.string.app_rapid_login_auth_code);
        } else {//绑定新手机
            mTip.setVisibility(View.GONE);
            mTitleBar.setTitle(getResources().getString(R.string.app_bind_new_phone));
            mPhoneName.setHint(R.string.app_bind_new_phone_hint_num);
            mVertifyCode.setHint(R.string.app_bind_new_phone_hint_code);
        }
    }

    /**
     * 维护状态变化
     *
     * @param state
     */
    private void doStateChange(int state) {

        if (mCurrentState == state) {
            return;
        }
        mCurrentState = state;
        switch (state) {
            case STATE_IDLE:
                enableViews(true);
                mPhoneNameClear.setVisibility(View.GONE);
                mVertifyCodeClear.setVisibility(View.GONE);
                mPhoneName.setText("");
                mVertifyCode.setText("");
                mBtnBind.setText(R.string.app_button_title_login);
                break;
            case STATE_CHECK_PHONE_NAME_ERROR:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_phone_format,
                        Toast.LENGTH_LONG).show();
                mBindLoading.setVisibility(View.GONE);
                break;
            case STATE_VERITY_CODE_ERROR:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_verity_null,
                        Toast.LENGTH_LONG).show();
                mBindLoading.setVisibility(View.GONE);
                break;
            case STATE_CHECK_PHONE_CODE_ERROR:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_pwd_format,
                        Toast.LENGTH_LONG).show();
                mBindLoading.setVisibility(View.GONE);
                break;
            case STATE_CLEAR_PHONE_NAME:
                mPhoneName.setText("");
                mPhoneNameClear.setVisibility(View.GONE);
                break;
            case STATE_CLEAR_PHONE_CODE:
                mVertifyCode.setText("");
                mVertifyCodeClear.setVisibility(View.GONE);
                break;
            case STATE_INPUT_PHONE_NAME:
                if (mPhoneName.getText().length() > 0) {
                    mPhoneNameClear.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_INPUT_PHONE_CODE:
                if (mVertifyCode.getText().length() > 0) {
                    mVertifyCodeClear.setVisibility(View.VISIBLE);
                }
                if (mVoiceVerityTip != null) {
                    mVoiceVerityTip.setVisibility(View.GONE);
                }
                break;
            case STATE_BIND_REQUEST_ING:
                enableViews(false);
                mBindLoading.setVisibility(View.VISIBLE);
                mBtnBind.setText(R.string.app_button_title_waiting);
                if (!requestBind()) {
                    doStateChange(STATE_BIND_RESPONSE_ERROR);
                }
                break;
            case STATE_BIND_RESPONSE_SUCCESS:
                enableViews(true);
                mBtnBind.setText(R.string.app_button_title_finish);
                mBindLoading.setVisibility(View.GONE);
                if (Integer.valueOf(UserProperties.getUserLoginstyle()) != User.PWD_STATE_USER) {
                    next(ChangePwdFragment.class);
                } else {
                    Intent intent = new Intent(UserProperties.INTENT_UPDATE_INFO);
                    LocalBroadcastManager.getInstance(BaseApp.getApplication()).sendBroadcast(intent);
                    finishAll();
                }
                break;
            case STATE_BIND_RESPONSE_ERROR:
                enableViews(true);
                mBtnBind.setText(R.string.app_bind);
                mBindLoading.setVisibility(View.GONE);
                break;
        }

    }

    private void requestBindSocialAccount(boolean force) {
        UserApi.bindSocial(force, UserProperties.getUserCurrentAccountType(), UserProperties.getUserOpenId(), new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (result != null) {
                    if (result.available()) {
                        doStateChange(STATE_BIND_RESPONSE_SUCCESS);
                    } else {
                        if (result.getStatusState() == -2) {
                            isNewSocialAccount = true;
                            confirmDialog.show();
                        } else {
                            ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        doStateChange(STATE_BIND_RESPONSE_ERROR);
                    }
                } else {
                    doStateChange(STATE_BIND_RESPONSE_ERROR);
                }
            }
        });
    }

    private void requestBind(final boolean force) {
        final String phoneName = mPhoneName.getText().toString();
        final String verityCode = mVertifyCode.getText().toString();
        UserApi.bindMobile(phoneName, force, verityCode, new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (result != null) {
                    LogGather.onEventLoginUseVerityCode(phoneName, verityCode, result.getErrorMsg());
                    if (result.available()) {
                        UserProperties.savePhone(phoneName);
                        doStateChange(STATE_BIND_RESPONSE_SUCCESS);
                    } else {
                        if (force) {
                            ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (result.getStatusState() == -2) {
                                isNewSocialAccount = false;
                                confirmDialog.show();
                            } else {
                                ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        doStateChange(STATE_BIND_RESPONSE_ERROR);
                    }
                } else {
                    doStateChange(STATE_BIND_RESPONSE_ERROR);
                }
            }
        });
    }

    private boolean requestBind() {
        if (NetworkState.available() && checkInputValues()) {
            final String phoneName = mPhoneName.getText().toString();
            String vertifycode = mVertifyCode.getText().toString();
            if (StringUtils.isEmpty(UserProperties.getUserId())) {//uid 为空先快捷登陆获取信息，再绑定第三方账号
                UserApi.rapidLogin(phoneName, vertifycode, new ApiCallback<User>() {
                    @Override
                    public void onResult(User result) {
                        if (result != null) {
                            if (result.available()) {
                                UserProperties.login(result);
                                requestBindSocialAccount(false);
                            } else {
                                ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                        Toast.LENGTH_SHORT).show();
                                doStateChange(STATE_BIND_RESPONSE_ERROR);
                            }
                        } else {
                            doStateChange(STATE_BIND_RESPONSE_ERROR);
                        }
                    }
                });
            } else {
                requestBind(false);
            }
            return true;
        }
        return false;
    }

    /**
     * 检查输入数据
     *
     * @return
     */
    private boolean checkInputValues() {
        String phoneName = mPhoneName.getText().toString();
        String code = mVertifyCode.getText().toString().trim();
        boolean phoneNameError = true;
        if (StringUtils.validatePhoneNumber(phoneName)
                || StringUtils.validateEmailAddress(phoneName)) {
            phoneNameError = false;
        }
        if (phoneNameError) {
            doStateChange(STATE_CHECK_PHONE_NAME_ERROR);
            return false;
        }
        if (StringUtils.isEmpty(code) || code.length() != 6) {
            doStateChange(STATE_VERITY_CODE_ERROR);
            return false;
        }
        return true;
    }

    /**
     * 试图
     *
     * @param enable
     */
    private void enableViews(boolean enable) {
        mPhoneName.setEnabled(enable);
        mVertifyCode.setEnabled(enable);
        mPhoneNameClear.setEnabled(enable);
        mVertifyCodeClear.setEnabled(enable);
        mObtainVertify.setEnabled(enable);
        mBtnBind.setEnabled(enable);
    }


    /**
     * 请求验证码
     */
    private void requestVerityCode() {
        if (!checkVerityValidate()) {
            return;
        }
        if (!mWaitingVerity) {
            mWaitingVerity = true;
            mWaitTime = MAX_WAITING_TIME;
            UIHandler.post(mUpdatePhoneVerity);
            final String phone = mPhoneName.getText().toString();
            UserApi.verify(phone, new ApiCallback<State>() {
                @Override
                public void onResult(State result) {
                    LogGather.onEventLoginReqVerityCode(phone, result.getErrorMsg());
                }
            });
            if (getContext() != null && null != mVertifyCode) {
                mVertifyCode.requestFocus();
                KeyboardUtils.openKeyboard(getContext(), mVertifyCode);
            }
        }
    }

    /**
     * 检查验证码可用性
     *
     * @return
     */
    private boolean checkVerityValidate() {
        String phoneNum = mPhoneName.getText().toString();
        if (StringUtils.validatePhoneNumber(phoneNum)) {
            return true;
        }
        ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_phone_format,
                Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && StringUtils.isEmpty(UserProperties.getUserPhone())) {
            UserProperties.logout();
        }
        return super.onKeyDown(keyCode, event);
    }
}
