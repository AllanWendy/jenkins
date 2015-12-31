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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wecook.common.app.BaseApp;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.UIHandler;
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

/**
 * 忘记密码界面
 *
 * @author kevin
 * @version v1.0
 * @since 2014-10/24/14
 */
public class UserForgetPwdFragment extends BaseTitleFragment {
    private static final int MAX_WAITING_TIME = 60;
    private static final String ACCOUNT_TYPE_PHONE = "phone";
    private static final String ACCOUNT_TYPE_EMAIL = "email";

    private static final int STATE_IDLE = 1;
    private static final int STATE_CHECK_PWD_ERROR = 2;
    private static final int STATE_CHECK_PWD_ENSURE_ERROR = 3;
    private static final int STATE_CLEAR_PWD = 4;
    private static final int STATE_CLEAR_PWD_ENSURE = 5;
    private static final int STATE_PWD_REQUEST_ING = 6;
    private static final int STATE_PWD_RESPONSE_ERROR = 7;
    private static final int STATE_PWD_RESPONSE_SUCCESS = 8;
    private static final int STATE_INPUT_PWD = 9;
    private static final int STATE_INPUT_PWD_ENSURE = 10;

    private EditText mPhoneEdit;
    private EditText mPhoneVerityEdit;
    private EditText mPasswordEdit;
    private TextView mPhoneTip;
    private View mPhoneClear;
    private View mPhoneVerityClear;
    private View mPasswordClear;

    private TextView mRegisterDo;
    private View mRegisterGroup;
    private View mRegisterLoading;


    private View mDiv1;
    private View mDiv2;

    private boolean mWaitingVerity;
    private int mCurrentState = -1;
    private String account_type = ACCOUNT_TYPE_PHONE;

    private int mWaitTime;
    private Runnable mUpdatePhoneVerity = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) return;
            if (mWaitTime < 0) {
                mWaitTime = 0;
            }
            if (mWaitTime == 0) {
                mPhoneTip.setEnabled(true);
                mPhoneTip.setText(R.string.app_register_phone_get_verity);
                mWaitingVerity = false;
            } else {
                mPhoneTip.setEnabled(false);
                mPhoneTip.setText(getString(R.string.app_my_waiting_verity_time, mWaitTime + ""));
                mWaitTime--;
                UIHandler.postDelayed(this, 1000);
                if (mWaitTime < MAX_WAITING_TIME - 10 && mPhoneVerityEdit.getText().length() == 0) {
                    mVoiceVerityTip.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    private TextView mVoiceVerityTip;
    private Spannable mVerityTip;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        UIHandler.stopPost(mUpdatePhoneVerity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forget, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setBackgroundColor(getResources().getColor(R.color.uikit_white));
        titleBar.setTitle(getString(R.string.app_title_find_password));


        //手机号码的输入
        mPhoneEdit = (EditText) view.findViewById(R.id.app_forget_account_phone);
        //验证码的输入
        mPhoneVerityEdit = (EditText) view.findViewById(R.id.app_forget_account_verity);
        //密码的输入
        mPasswordEdit = (EditText) view.findViewById(R.id.app_forget_account_password);
        //密码眼睛

        mVoiceVerityTip = (TextView) view.findViewById(R.id.app_forget_voice_verity);
        mVerityTip = StringUtils.getSpanClickable(getContext(), mVoiceVerityTip.getText().toString(),
                new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        // 请求语音验证码
                        final String phone = mPhoneEdit.getText().toString();
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

        //获取验证码的控件
        mPhoneTip = (TextView) view.findViewById(R.id.app_forget_account_phone_tip);

        mPhoneClear = view.findViewById(R.id.app_forget_account_phone_clear);
        mPhoneVerityClear = view.findViewById(R.id.app_forget_account_verity_clear);
        mPasswordClear = view.findViewById(R.id.app_forget_account_password_clear);

        mDiv1 = view.findViewById(R.id.div_1);
        mDiv2 = view.findViewById(R.id.div_2);

        //注册并登陆
        mRegisterDo = (TextView) view.findViewById(R.id.app_forget_button_do);
        mRegisterLoading = view.findViewById(R.id.app_forget_waiting);

        //注册并登陆的控件组
        mRegisterGroup = view.findViewById(R.id.app_forget_button_group);
        mRegisterGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.closeKeyboard(getContext(), getView());
                requestChangePswd();
            }
        });

        initEditor(mPhoneEdit, mPhoneClear);
        initEditor(mPhoneVerityEdit, mPhoneVerityClear);
        initEditor(mPasswordEdit, mPasswordClear);

        mPhoneTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.closeKeyboard(getContext(), getView());
                requestVerityCode();
            }
        });
    }

    private void initEditor(final EditText text, final View clear) {
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (text == mPasswordEdit) {
                    if (s.length() > 0) {
                        clear.setVisibility(View.VISIBLE);
                    } else {
                        clear.setVisibility(View.GONE);
                    }
                } else if (text == mPhoneVerityEdit) {
                    if (s.length() > 0) {
                        if (mVoiceVerityTip != null) {
                            mVoiceVerityTip.setVisibility(View.GONE);
                        }
                    }
                } else {
                    if (s.length() > 0) {
                        clear.setVisibility(View.VISIBLE);
                    } else {
                        clear.setVisibility(View.GONE);
                    }
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("");
            }
        });
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
            final String phone = mPhoneEdit.getText().toString();
            UserApi.verify(phone, new ApiCallback<State>() {
                @Override
                public void onResult(State result) {
                    LogGather.onEventLoginReqVerityCode(phone, result.getErrorMsg());
                    if (result.available()) {
                        if (result.getStatusCode() == 200) {
                            if (account_type == ACCOUNT_TYPE_EMAIL) {
                                ConfirmDialog confirmDialog = new ConfirmDialog(getContext(), "味库君已将验证码发到您的邮箱，请前往查看.");
                                confirmDialog.setTitle("提示");
                                confirmDialog.show();
                            } else {
                            }
                        } else {
                            ToastAlarm.show(result.getErrorMsg());
                        }
                    }
                }
            });
            if (getContext() != null && null != mPhoneVerityEdit) {
                mPhoneVerityEdit.requestFocus();
                KeyboardUtils.openKeyboard(getContext(), mPhoneVerityEdit);
            }
        }
    }

    /**
     * 检查验证码可用性
     *
     * @return
     */
    private boolean checkVerityValidate() {
        String phoneNum = mPhoneEdit.getText().toString();
        if (StringUtils.validatePhoneNumber(phoneNum)) {
            account_type = ACCOUNT_TYPE_PHONE;
            return true;
        }
        if (StringUtils.validateEmailAddress(phoneNum)) {
            account_type = ACCOUNT_TYPE_EMAIL;
            return true;
        }
        ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_account_format,
                Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 请求修改验证码
     */
    private void requestChangePswd() {
        if (!checkRegisterValidate()) {
            return;
        }

        UserApi.applyNewPassword(mPhoneEdit.getText().toString(), mPhoneVerityEdit.getText().toString(), mPasswordEdit.getText().toString(), new ApiCallback<State>() {
            @Override
            public void onResult(State result) {
                if (result != null) {
                    if (result.available()) {
                        doStateChange(STATE_PWD_RESPONSE_SUCCESS);
                    } else {
                        ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                Toast.LENGTH_SHORT).show();
                        doStateChange(STATE_PWD_RESPONSE_ERROR);
                    }
                } else {
                    doStateChange(STATE_PWD_RESPONSE_ERROR);
                }
            }
        });
    }


    /**
     * 检查注册数据可用性
     *
     * @return
     */
    private boolean checkRegisterValidate() {
        String phoneNum = mPhoneEdit.getText().toString().trim();
        String verityCode = mPhoneVerityEdit.getText().toString().trim();
        String pwd = mPasswordEdit.getText().toString().trim();
        if (StringUtils.isEmpty(phoneNum)) {
            ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_phone_null,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.isEmpty(verityCode)) {
            ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_verity_null,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!StringUtils.validatePassword(pwd)) {
            ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_pwd_format,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
                break;
            case STATE_CHECK_PWD_ERROR:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_pwd_format,
                        Toast.LENGTH_LONG).show();
                break;
            case STATE_CHECK_PWD_ENSURE_ERROR:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_pwd_verify,
                        Toast.LENGTH_LONG).show();
                break;
            case STATE_PWD_REQUEST_ING:
                break;
            case STATE_PWD_RESPONSE_SUCCESS:
                mRegisterDo.setText(R.string.app_button_title_finish);
                UserProperties.saveLoginStyle(String.valueOf(User.PWD_STATE_USER));
                Intent intent = new Intent(UserProperties.INTENT_UPDATE_INFO);
                LocalBroadcastManager.getInstance(BaseApp.getApplication()).sendBroadcast(intent);
                ToastAlarm.makeToastAlarm(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                if (null != mPhoneEdit) {
                    mPhoneEdit.setText("");
                }
                if (null != mPhoneVerityEdit) {
                    mPhoneVerityEdit.setText("");
                }
                if (null != mPasswordEdit) {
                    mPasswordEdit.setText("");
                }
                back();
                break;
            case STATE_PWD_RESPONSE_ERROR:
                mRegisterDo.setText(R.string.app_title_bar_save);
                break;
        }
    }
}
