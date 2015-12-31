package cn.wecook.app.main.home.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.utils.EditTextUtils;

/**
 * 注册界面
 *
 * @author kevin created at 9/18/14
 * @version 1.0
 */
public class UserRegisterFragment extends BaseTitleFragment {

    private static final int MAX_WAITING_TIME = 60;

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

    private TextView mRegisterServerTip;
    private TextView mRegisterVoiceVerityTip;


    private View mDiv1;
    private View mDiv2;

    private boolean mWaitingVerity;
    private boolean mWaitingRegister;
    private boolean refView;

    private Spannable mVoiceVerityTip;
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
                mRegisterVoiceVerityTip.setText(mVoiceVerityTip);
            } else {
                mPhoneTip.setEnabled(false);
                mPhoneTip.setText(getString(R.string.app_my_waiting_verity_time, mWaitTime + ""));
                mWaitTime--;
                UIHandler.postDelayed(this, 1000);

                if (mWaitTime < MAX_WAITING_TIME - 10 && mPhoneVerityEdit.getText().length() == 0) {
                    mRegisterVoiceVerityTip.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TitleBar titleBar = getTitleBar();
        titleBar.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));
        titleBar.setTitle(getResources().getString(R.string.app_title_register));
        titleBar.enableBottomDiv(false);

        mRegisterVoiceVerityTip = (TextView) view.findViewById(R.id.app_register_voice_verity);
        mVoiceVerityTip = StringUtils.getSpanClickable(getContext(), mRegisterVoiceVerityTip.getText().toString(),
                new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        // 请求语音验证码
                        final String phone = mPhoneEdit.getText().toString();
                        mRegisterVoiceVerityTip.setText("正在给您拨打电话，请等待接听...");
                        UserApi.voiceVerify(phone, new ApiCallback<State>() {
                            @Override
                            public void onResult(State result) {
                                if (!result.available()) {
                                    ToastAlarm.show(result.getErrorMsg());
                                    mRegisterVoiceVerityTip.setText(mVoiceVerityTip);
                                }
                            }
                        });
                    }
                }, R.color.uikit_dark_light);

        mRegisterVoiceVerityTip.setText(mVoiceVerityTip);
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRegisterVoiceVerityTip.setMovementMethod(LinkMovementMethod.getInstance());
                mRegisterVoiceVerityTip.setVisibility(View.GONE);
            }
        }, 50);

        mRegisterServerTip = (TextView) view.findViewById(R.id.app_register_server_tip);
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

        Spannable serverSpan1 = StringUtils.getSpanClickable(getContext(),
                R.string.app_about_server, new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Bundle bundle = new Bundle();
                        bundle.putString(WebViewFragment.EXTRA_URL, UserApi.SERVICE_PATH);
                        bundle.putString(WebViewFragment.EXTRA_TITLE, "服务协议");
                        startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                    }
                }, R.color.uikit_dark_light
        );
        Spannable serverSpan2 = StringUtils.getSpanClickable(getContext(),
                R.string.app_about_protocol, new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Bundle bundle = new Bundle();
                        bundle.putString(WebViewFragment.EXTRA_URL, UserApi.PRIVACY_PATH);
                        bundle.putString(WebViewFragment.EXTRA_TITLE, "隐私条款");
                        startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                    }
                }, R.color.uikit_dark_light
        );

        Spannable serverSpan3 = StringUtils.getSpanClickable(getContext(),
                R.string.app_about_trading, new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Bundle bundle = new Bundle();
                        bundle.putString(WebViewFragment.EXTRA_URL, UserApi.TRADING_PATH);
                        bundle.putString(WebViewFragment.EXTRA_TITLE, "交易条款");
                        startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                    }
                }, R.color.uikit_dark_light
        );

        stringBuilder.append("注册即视为您已同意");
//        stringBuilder.append(serverSpan3);
//        stringBuilder.append("、");
        stringBuilder.append(" ");
        stringBuilder.append(serverSpan1);
        stringBuilder.append(" 和 ");
        stringBuilder.append(serverSpan2);
//        stringBuilder.append("。");

        mRegisterServerTip.setText(stringBuilder);
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRegisterServerTip.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }, 400);

        //手机号码的输入
        mPhoneEdit = (EditText) view.findViewById(R.id.app_register_account_phone);
        EditTextUtils.addTrimInflate(mPhoneEdit, 11);
        //验证码的输入
        mPhoneVerityEdit = (EditText) view.findViewById(R.id.app_register_account_verity);
        //密码的输入
        mPasswordEdit = (EditText) view.findViewById(R.id.app_register_account_password);
        //密码眼睛
        //昵称的输入
        //获取验证码的控件
        mPhoneTip = (TextView) view.findViewById(R.id.app_register_account_phone_tip);

        mPhoneClear = view.findViewById(R.id.app_register_account_phone_clear);
        mPhoneVerityClear = view.findViewById(R.id.app_register_account_verity_clear);
        mPasswordClear = view.findViewById(R.id.app_register_account_password_clear);

        mDiv1 = view.findViewById(R.id.div_1);
        mDiv2 = view.findViewById(R.id.div_2);

        //注册并登陆
        mRegisterDo = (TextView) view.findViewById(R.id.app_register_button_do);
        mRegisterLoading = view.findViewById(R.id.app_register_waiting);

        //注册并登陆的控件组
        mRegisterGroup = view.findViewById(R.id.app_register_button_group);
        mRegisterGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRegister();
            }
        });

        initEditor(mPhoneEdit, mPhoneClear);
        initEditor(mPhoneVerityEdit, mPhoneVerityClear);
        initEditor(mPasswordEdit, mPasswordClear);

        mPhoneTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVerityCode();
            }
        });
        KeyboardUtils.openKeyboard(getContext(), mPhoneEdit);

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
                        if (mRegisterVoiceVerityTip != null) {
                            mRegisterVoiceVerityTip.setVisibility(View.GONE);
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
        String phoneNum = mPhoneEdit.getText().toString().trim();
        if (null == phoneNum || "".equals(phoneNum)) {
            ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_phone_null,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StringUtils.validatePhoneNumber(phoneNum)) {
            return true;
        }
        ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_phone_format,
                Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 请求注册
     */
    private void requestRegister() {
        if (!checkRegisterValidate()) {
            return;
        }

        if (!mWaitingRegister) {
            mWaitingRegister = true;
            mRegisterDo.setText(R.string.app_button_title_waiting);
            mRegisterLoading.setVisibility(View.VISIBLE);

            ApiCallback<User> callback = new ApiCallback<User>() {
                @Override
                public void onResult(User result) {
                    if (result != null) {
                        LogGather.onEventLoginUseVerityCode(mPhoneEdit.getText().toString(),
                                mPhoneVerityEdit.getText().toString(), result.getErrorMsg());
                        if (result.available()) {
                            UserProperties.login(result);
                            UserProperties.saveUserCurrentAccountType(UserProperties.USER_LOGIN_STATE_PHONE);//手机密码登录
                            ToastAlarm.makeToastAlarm(getContext(), R.string.app_tip_register_success,
                                    Toast.LENGTH_SHORT).show();
                            mRegisterDo.setText(R.string.app_button_title_finish);
                            finishAll();
                        } else {
                            ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                    Toast.LENGTH_SHORT).show();
                            mRegisterDo.setText(R.string.app_button_title_register_and_login);
                        }
                    } else {
                        mRegisterDo.setText(R.string.app_button_title_register_and_login);
                    }

                    mRegisterLoading.setVisibility(View.GONE);
                    mWaitingRegister = false;
                }
            };
            UserApi.registerOfPhone(mPhoneEdit.getText().toString(),
                    mPhoneVerityEdit.getText().toString(),
                    StringUtils.getPhoneName(mPhoneEdit.getText().toString()),
                    mPasswordEdit.getText().toString(), callback);
        }
    }

    /**
     * 检查注册数据可用性
     *
     * @return
     */
    private boolean checkRegisterValidate() {
        String phoneNum = mPhoneEdit.getText().toString().trim();
        String verityCode = mPhoneVerityEdit.getText().toString();
        String pwd = mPasswordEdit.getText().toString();
        if (null == phoneNum || "".equals(phoneNum)) {
            ToastAlarm.makeToastAlarm(getContext(), "手机号不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (null == verityCode || "".equals(verityCode)) {
            ToastAlarm.makeToastAlarm(getContext(), "验证码不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (null == pwd || "".equals(pwd)) {
            ToastAlarm.makeToastAlarm(getContext(), "密码不能为空",
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

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        if (null != getArguments()) {
            if (getArguments().containsKey("refresh")) {
                //刷新界面
                refView = getArguments().getBoolean("refresh");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (refView) {
            mPhoneEdit.setText("");
            mPhoneVerityEdit.setText("");
            mPasswordEdit.setText("");
            refView = false;
        }
    }
}
