package cn.wecook.app.main.home.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.modules.thirdport.PlatformManager;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.CoveredImageView;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.features.thirdport.ThirdPortDelivery;
import cn.wecook.app.utils.EditTextUtils;

/**
 * 登录界面
 *
 * @author sc.wang created at 15.6.1
 * @version 2.3.9
 */
public class UserLoginFragment extends BaseTitleFragment implements View.OnClickListener {

    private static final int STATE_IDLE = 1;
    private static final int STATE_CHECK_ACCOUNT_NAME_ERROR = 2;
    private static final int STATE_CHECK_ACCOUNT_PWD_ERROR = 3;
    private static final int STATE_CLEAR_ACCOUNT_NAME = 4;
    private static final int STATE_CLEAR_ACCOUNT_PWD = 5;
    private static final int STATE_LOGIN_REQUEST_ING = 6;
    private static final int STATE_LOGIN_RESPONSE_ERROR = 7;
    private static final int STATE_LOGIN_RESPONSE_SUCCESS = 8;
    private static final int STATE_INPUT_ACCOUNT_NAME = 9;
    private static final int STATE_INPUT_ACCOUNT_PWD = 10;

    private View mRegisterView;
    private EditText mAccountName;
    private View mAccountNameClear;
    private EditText mAccountPassword;
    private View mAccountPasswordClear;
    private View mForgetPwd;
    private TextView mBtnLogin;
    private View mBtnWeiXin;
    private View mBtnQQ;
    private View mBtnWeiBo;
    private View mLoginGroup;
    private View mLoading;
    private View mBackRapidLogin;

    private ViewGroup mThirdAccountGroup;
    private LinearLayout mOtherWayLayout;
    private CoveredImageView arrows;

    private int mCurrentState = -1;
    private boolean isThirdAccountShow;

    private LinearLayout mLastLoginStateContainer;
    private TextView mLastLoginState;

    private TitleBar titleBar;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));
        titleBar.setTitle(getResources().getString(R.string.app_login_title));
        titleBar.enableBottomDiv(false);

        mLastLoginStateContainer = (LinearLayout) view.findViewById(R.id.app_login_account_last_state_container);
        mLastLoginState = (TextView) view.findViewById(R.id.app_login_account_last_state);

        //注册监听
        mRegisterView = view.findViewById(R.id.app_login_goto_register);
        mAccountName = (EditText) view.findViewById(R.id.app_login_account_name);
        EditTextUtils.addTrimInflate(mAccountName, 0);
        mAccountNameClear = view.findViewById(R.id.app_login_account_clear);
        mAccountPassword = (EditText) view.findViewById(R.id.app_login_account_pwd);

        arrows = (CoveredImageView) view.findViewById(R.id.app_login_turn_on_third_login_arrows);

        mAccountPasswordClear = view.findViewById(R.id.app_login_password_clear);
        mForgetPwd = view.findViewById(R.id.app_login_forget_pwd);
        mLoading = view.findViewById(R.id.app_login_waiting);
        mThirdAccountGroup = (ViewGroup) view.findViewById(R.id.app_login_third_account_group);
        mBtnLogin = (TextView) view.findViewById(R.id.app_login_button_do);
        mLoginGroup = view.findViewById(R.id.app_login_button_group);
        mBtnWeiXin = view.findViewById(R.id.app_login_weixin);
        mBtnQQ = view.findViewById(R.id.app_login_qq);
        mBtnWeiBo = view.findViewById(R.id.app_login_weibo);
        mOtherWayLayout = (LinearLayout) view.findViewById(R.id.app_account_other_layout);
        mBackRapidLogin = view.findViewById(R.id.app_login_turnto_rapidlogin);

        initTitle();
        setListener();
    }

    private void setListener() {
        mForgetPwd.setOnClickListener(this);
        mAccountPasswordClear.setOnClickListener(this);
        mBtnLogin.setOnClickListener(this);
        mLoginGroup.setOnClickListener(this);
        mBtnWeiXin.setOnClickListener(this);
        mBtnQQ.setOnClickListener(this);
        mBtnWeiBo.setOnClickListener(this);
        mOtherWayLayout.setOnClickListener(this);
        mBackRapidLogin.setOnClickListener(this);
        mAccountNameClear.setOnClickListener(this);
        mRegisterView.setOnClickListener(this);

        mAccountName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    doStateChange(STATE_CLEAR_ACCOUNT_NAME);
                } else {
                    doStateChange(STATE_INPUT_ACCOUNT_NAME);
                }
            }
        });
        mAccountPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    doStateChange(STATE_CLEAR_ACCOUNT_PWD);
                } else {
                    doStateChange(STATE_INPUT_ACCOUNT_PWD);
                }
            }
        });
    }

    private void initTitle() {
        if (!StringUtils.isEmpty(UserProperties.getUserCurrentAccountType())) {
            mLastLoginStateContainer.setVisibility(View.VISIBLE);
            titleBar.getTitleView().setVisibility(View.GONE);
            if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_QQ)) {
                mLastLoginState.setText(getString(R.string.app_login_last_state, getString(R.string.app_login_third_qq)));
            } else if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_WEIBO)) {
                mLastLoginState.setText(getString(R.string.app_login_last_state, getString(R.string.app_login_third_weibo)));
            } else if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_WEIXIN)) {
                mLastLoginState.setText(getString(R.string.app_login_last_state, getString(R.string.app_login_third_wexin)));
            } else if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_EMAIL)) {
                mLastLoginState.setText(getString(R.string.app_login_last_state, getString(R.string.app_login_third_email)));
            } else if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_PHONE)) {
                mLastLoginState.setText(getString(R.string.app_login_last_state, getString(R.string.app_login_third_phone)));
            } else if (UserProperties.getUserCurrentAccountType().equals(UserProperties.USER_LOGIN_STATE_PHONE_RAPID)) {
                mLastLoginState.setText(getString(R.string.app_login_last_state, getString(R.string.app_login_third_phone_rapid)));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_login_turnto_rapidlogin:
                next(UserRapidLoginFragment.class);
                break;
            case R.id.app_account_other_layout:
                if (!isThirdAccountShow) {
                    showThirdAccountPanel();
                } else {
                    hideThirdAccountPanel();
                }
                break;
            case R.id.app_login_qq:
                ThirdPortDelivery.login(UserLoginFragment.this, PlatformManager.PLATFORM_QQ);
                finishAll();
                break;
            case R.id.app_login_weixin:
                ThirdPortDelivery.login(UserLoginFragment.this, PlatformManager.PLATFORM_WECHAT);
                finishAll();
                break;
            case R.id.app_login_button_group:
                doLogin();
                break;
            case R.id.app_login_forget_pwd:
                next(UserForgetPwdFragment.class);
                break;
            case R.id.app_login_password_clear:
                doStateChange(STATE_CLEAR_ACCOUNT_PWD);
                break;
            case R.id.app_login_account_clear:
                doStateChange(STATE_CLEAR_ACCOUNT_NAME);
                break;
            case R.id.app_login_goto_register:
                Bundle bundle = new Bundle();
                bundle.putBoolean("refresh", true);
                next(UserRegisterFragment.class, bundle);
                break;
            case R.id.app_login_weibo:
                ThirdPortDelivery.login(UserLoginFragment.this, PlatformManager.PLATFORM_WEBLOG);
                finishAll();
                break;
        }
    }

    /**
     * 请求登录中
     */
    private void doLogin() {
        doStateChange(STATE_LOGIN_REQUEST_ING);
    }

    private void showThirdAccountPanel() {
        arrows.setSelected(true);
        Animation animation = AnimationUtils.loadAnimation(getContext(),
                R.anim.anim_score_fade_in);
        LayoutAnimationController controller = new LayoutAnimationController(animation, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mThirdAccountGroup.setLayoutAnimation(controller);
        mThirdAccountGroup.setLayoutAnimationListener(null);
        //显示打分区域
        mThirdAccountGroup.setVisibility(View.VISIBLE);
        isThirdAccountShow = true;
    }

    private void hideThirdAccountPanel() {
        arrows.setSelected(false);
        Animation animation = AnimationUtils.loadAnimation(getContext(),
                R.anim.anim_score_fade_out);
        animation.setFillAfter(true);
        LayoutAnimationController controller = new LayoutAnimationController(animation, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        mThirdAccountGroup.setLayoutAnimation(controller);
        mThirdAccountGroup.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mThirdAccountGroup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mThirdAccountGroup.startLayoutAnimation();
        isThirdAccountShow = false;
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
                mAccountNameClear.setVisibility(View.GONE);
                mAccountPasswordClear.setVisibility(View.GONE);
                mAccountName.setText("");
                mAccountPassword.setText("");
                mBtnLogin.setText(R.string.app_button_title_login);

                break;
            case STATE_CHECK_ACCOUNT_NAME_ERROR:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_account_name_format,
                        Toast.LENGTH_LONG).show();
                mLoading.setVisibility(View.GONE);
                break;
            case STATE_CHECK_ACCOUNT_PWD_ERROR:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_pwd_format,
                        Toast.LENGTH_LONG).show();
                mLoading.setVisibility(View.GONE);
                break;
            case STATE_CLEAR_ACCOUNT_NAME:
                mAccountName.setText("");
                mAccountNameClear.setVisibility(View.GONE);
                break;
            case STATE_CLEAR_ACCOUNT_PWD:
                mAccountPassword.setText("");
                mAccountPasswordClear.setVisibility(View.GONE);
                break;
            case STATE_INPUT_ACCOUNT_NAME:
                if (mAccountName.getText().length() > 0) {
                    mAccountNameClear.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_INPUT_ACCOUNT_PWD:
                if (mAccountPassword.getText().length() > 0) {
                    mAccountPasswordClear.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_LOGIN_REQUEST_ING:
                enableViews(false);
                mLoading.setVisibility(View.VISIBLE);
                mBtnLogin.setText(R.string.app_button_title_waiting);
                boolean result = requestLogin();
                if (!result) {
                    doStateChange(STATE_LOGIN_RESPONSE_ERROR);
                }
                break;
            case STATE_LOGIN_RESPONSE_SUCCESS:
                enableViews(true);
                mBtnLogin.setText(R.string.app_button_title_finish);
                mLoading.setVisibility(View.GONE);
                break;
            case STATE_LOGIN_RESPONSE_ERROR:
                enableViews(true);
                mBtnLogin.setText(R.string.app_button_title_login);
                mLoading.setVisibility(View.GONE);
                break;
        }

    }

    private boolean requestLogin() {
        if (checkInputValues()) {
            if (!NetworkState.available()) {
                ToastAlarm.show("请检查当前网络");
                return false;
            }
            final String accountName = mAccountName.getText().toString();
            String accountPwd = mAccountPassword.getText().toString();
            UserApi.login(accountName, accountPwd, new ApiCallback<User>() {
                @Override
                public void onResult(User result) {
                    if (result != null && null != getContext()) {
                        if (result.available()) {
                            UserProperties.login(result);
                            if (StringUtils.validateEmailAddress(accountName)) {
                                UserProperties.saveEmail(accountName);
                                UserProperties.saveUserCurrentAccountType(UserProperties.USER_LOGIN_STATE_EMAIL);//邮箱登录
                            } else if (StringUtils.validatePhoneNumber(accountName)) {
                                UserProperties.savePhone(accountName);
                                UserProperties.saveUserCurrentAccountType(UserProperties.USER_LOGIN_STATE_PHONE);//手机登录
                            }
                            ToastAlarm toastAlarm = ToastAlarm.makeToastAlarm(getContext(), R.string.app_tip_login_success,
                                    Toast.LENGTH_SHORT);
                            if (null != toastAlarm) {
                                toastAlarm.show();
                            }
                            doStateChange(STATE_LOGIN_RESPONSE_SUCCESS);
                            finishAll();
                        } else {
                            ToastAlarm toastAlarm = ToastAlarm.makeToastAlarm(getContext(), result.getErrorMsg(),
                                    Toast.LENGTH_SHORT);
                            if (null != toastAlarm) {
                                toastAlarm.show();
                            }
                            doStateChange(STATE_LOGIN_RESPONSE_ERROR);
                        }
                    } else {
                        doStateChange(STATE_LOGIN_RESPONSE_ERROR);
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
        String accountName = mAccountName.getText().toString().trim();
        boolean accountNameError = true;
        if (StringUtils.validatePhoneNumber(accountName)
                || StringUtils.validateEmailAddress(accountName)) {
            accountNameError = false;
        }

        if (accountNameError) {
            doStateChange(STATE_CHECK_ACCOUNT_NAME_ERROR);
            return false;
        }

        boolean accountPwdError = true;
        String accountPwd = mAccountPassword.getText().toString();
        if (StringUtils.validatePassword(accountPwd)) {
            accountPwdError = false;
        }

        if (accountPwdError) {
            doStateChange(STATE_CHECK_ACCOUNT_PWD_ERROR);
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
        mRegisterView.setEnabled(enable);
        mAccountName.setEnabled(enable);
        mAccountPassword.setEnabled(enable);
        mAccountNameClear.setEnabled(enable);
        mAccountPasswordClear.setEnabled(enable);
        mForgetPwd.setEnabled(enable);
        mBtnLogin.setEnabled(enable);
        mBtnWeiBo.setEnabled(enable);
        mBtnQQ.setEnabled(enable);
        mBtnWeiXin.setEnabled(enable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (UserProperties.isLogin()) {
            finishFragment();
        }
    }

    @Override
    public void onCardIn(Bundle data) {
        super.onCardIn(data);
        if (null != mAccountName) {
            mAccountName.requestFocus();
        }
    }
}
