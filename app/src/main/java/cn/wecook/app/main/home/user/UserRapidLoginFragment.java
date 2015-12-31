package cn.wecook.app.main.home.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.wecook.uikit.fragment.BaseFragment;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.utils.EditTextUtils;

/**
 * 登录界面
 *
 * @author sc.wang created at 15.6.1
 * @version 2.3.9
 */
public class UserRapidLoginFragment extends BaseTitleFragment {

    private static final int STATE_CHECK_PHONE_NAME_ERROR = 2;
    private static final int STATE_CLEAR_PHONE_NAME = 4;
    private static final int STATE_CLEAR_PHONE_CODE = 5;
    private static final int STATE_RAPIDLOGIN_REQUEST_ING = 6;
    private static final int STATE_RAPIDLOGIN_RESPONSE_ERROR = 7;
    private static final int STATE_RAPIDLOGIN_RESPONSE_SUCCESS = 8;
    private static final int STATE_INPUT_PHONE_NAME = 9;
    private static final int STATE_INPUT_PHONE_CODE = 10;
    private static final int MAX_WAITING_TIME = 60;
    //快速登录
    private LinearLayout rapidlayout;
    private LinearLayout otherloginlayout;
    private EditText mPhoneName;
    private View mPhoneNameClear;
    private EditText mVerityCode;
    private View mVerityCodeClear;
    private TextView mObtainVerity;
    private TextView mBtnRapidLogin;
    private View mRapidLoginGroup;
    private View mRapidLoading;
    private TextView mVoiceVerityTip;
    private BaseFragment mFragment;
    private int mWaitTime;
    private boolean mWaitingVerity;
    private int mCurrentState = -1;
    private int mLastDialogNum;

    private Spannable mVerityTip;

    private Runnable mUpdatePhoneVerity = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) return;
            if (mWaitTime < 0) {
                mWaitTime = 0;
            }
            if (mWaitTime == 0) {
                mObtainVerity.setEnabled(true);
                mObtainVerity.setText(R.string.app_register_phone_get_verity);
                mWaitingVerity = false;
                mVoiceVerityTip.setText(mVerityTip);
            } else {
                mObtainVerity.setEnabled(false);
                mObtainVerity.setText("(" + mWaitTime + ")" + "秒重新获取验证码");
                mWaitTime--;
                UIHandler.postDelayed(this, 1000);
                if (mWaitTime < MAX_WAITING_TIME - 10 && mVerityCode.getText().length() == 0) {
                    mVoiceVerityTip.setVisibility(View.VISIBLE);
                }
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rapid_login, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TitleBar titleBar = getTitleBar();
        titleBar.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));
        titleBar.setTitle(getResources().getString(R.string.app_login_phone_rapid));
        titleBar.enableBottomDiv(false);

        //手机号码的编辑和监听
        mPhoneName = (EditText) view.findViewById(R.id.app_rapidlogin_account_name);
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
        KeyboardUtils.openKeyboard(getContext(), mPhoneName);
        //手机号编辑框的清除按钮的监听
        mPhoneNameClear = view.findViewById(R.id.app_rapidlogin_account_clear);
        mPhoneNameClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStateChange(STATE_CLEAR_PHONE_NAME);
            }
        });
        //验证码的输入框的初始化和监听
        mVerityCode = (EditText) view.findViewById(R.id.app_rapidlogin_code);
        mVerityCode.addTextChangedListener(new TextWatcher() {
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
        mVerityCodeClear = view.findViewById(R.id.app_rapidlogin_code_clear);
        mVerityCodeClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStateChange(STATE_CLEAR_PHONE_CODE);
            }
        });
        //获取验证码的初始化和监听
        mObtainVerity = (TextView) view.findViewById(R.id.app_rapidlogin_obtain_authcode);
        mObtainVerity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取验证码的请求：
                requestVerityCode();
            }
        });

        mRapidLoading = view.findViewById(R.id.app_rapidlogin_waiting);

        //登陆按钮的初始化
        mBtnRapidLogin = (TextView) view.findViewById(R.id.app_rapidlogin_button_do);
        mRapidLoginGroup = view.findViewById(R.id.app_rapidlogin_button_group);
        mRapidLoginGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });


        mVoiceVerityTip = (TextView) view.findViewById(R.id.app_rapidlogin_voice_verity);
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
    }

    /**
     * 请求登录中
     */
    private void doLogin() {
        doStateChange(STATE_RAPIDLOGIN_REQUEST_ING);
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
            case STATE_CHECK_PHONE_NAME_ERROR:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_account_name_format,
                        Toast.LENGTH_LONG).show();
                mRapidLoading.setVisibility(View.GONE);
                break;
            case STATE_CLEAR_PHONE_NAME:
                mPhoneName.setText("");
                mPhoneNameClear.setVisibility(View.GONE);
                break;
            case STATE_CLEAR_PHONE_CODE:
                mVerityCode.setText("");
                mVerityCodeClear.setVisibility(View.GONE);
                break;
            case STATE_INPUT_PHONE_NAME:
                if (mPhoneName.getText().length() > 0) {
                    mPhoneNameClear.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_INPUT_PHONE_CODE:
                if (mVerityCode.getText().length() > 0) {
                    mVerityCodeClear.setVisibility(View.VISIBLE);
                }
                if (mVoiceVerityTip != null) {
                    mVoiceVerityTip.setVisibility(View.GONE);
                }
                break;
            case STATE_RAPIDLOGIN_REQUEST_ING:
                enableViews(false);
                mRapidLoading.setVisibility(View.VISIBLE);
                mBtnRapidLogin.setText(R.string.app_button_title_waiting);
                if (!requestLogin()) {
                    doStateChange(STATE_RAPIDLOGIN_RESPONSE_ERROR);
                }
                break;
            case STATE_RAPIDLOGIN_RESPONSE_SUCCESS:
                enableViews(true);
                mBtnRapidLogin.setText(R.string.app_button_title_finish);
                mRapidLoading.setVisibility(View.GONE);
                finishAll();
                break;
            case STATE_RAPIDLOGIN_RESPONSE_ERROR:
                enableViews(true);
                //清除验证码
                mVerityCodeClear.performClick();
                mBtnRapidLogin.setText(R.string.app_button_title_login);
                mRapidLoading.setVisibility(View.GONE);
                break;
        }

    }

    private boolean requestLogin() {
        if (NetworkState.available() && checkInputValues()) {
            final String phoneName = mPhoneName.getText().toString();
            final String verityCode = mVerityCode.getText().toString();
            UserApi.rapidLogin(phoneName, verityCode, new ApiCallback<User>() {
                @Override
                public void onResult(User result) {
                    LogGather.onEventLoginUseVerityCode(phoneName, verityCode, result.getErrorMsg());
                    if (result.available()) {
                        UserProperties.savePhone(phoneName);
                        UserProperties.saveUserCurrentAccountType(UserProperties.USER_LOGIN_STATE_PHONE_RAPID);//手机快速登录
                        UserProperties.login(result);
                        ToastAlarm.makeToastAlarm(getContext(), R.string.app_tip_login_success, Toast.LENGTH_SHORT).show();
                        doStateChange(STATE_RAPIDLOGIN_RESPONSE_SUCCESS);
                    } else {
                        ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                Toast.LENGTH_SHORT).show();
                        doStateChange(STATE_RAPIDLOGIN_RESPONSE_ERROR);
                    }
                }
            });
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
        boolean phoneNameError = true;
        if (StringUtils.validatePhoneNumber(phoneName)) {
            phoneNameError = false;
        }
        if (phoneNameError) {
            doStateChange(STATE_CHECK_PHONE_NAME_ERROR);
            return false;
        }

        String verityCode = mVerityCode.getText().toString();
        if (StringUtils.isEmpty(verityCode.trim())) {
            ToastAlarm.show("请检查验证码是否正确");
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
        mVerityCode.setEnabled(enable);
        mPhoneNameClear.setEnabled(enable);
        mVerityCodeClear.setEnabled(enable);
        mObtainVerity.setEnabled(enable);
        mBtnRapidLogin.setEnabled(enable);
    }


    /**
     * 请求验证码
     */
    private void requestVerityCode() {
        if (checkVerityValidate()) {
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
                if (getContext() != null && null != mVerityCode) {
                    mVerityCode.requestFocus();
                    KeyboardUtils.openKeyboard(getContext(), mVerityCode);
                }
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

}
