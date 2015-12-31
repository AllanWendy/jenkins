package cn.wecook.app.main.home.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wecook.common.app.BaseApp;
import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.network.NetworkState;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.sdk.api.model.State;
import com.wecook.sdk.api.model.User;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.activity.BaseSwipeActivity;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;

/**
 * 改变密码、设置密码界面
 *
 * @author wangshichang
 * @version 2.3.9
 * @since 15.6.1
 */
public class ChangePwdFragment extends BaseTitleFragment {

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
    private static final int STATE_CHECK_PWD_NULL = 11;

    private EditText mOldPwd;
    private EditText mNewPwd;
    private EditText mEnturePwd;
    private TextView mUpdateDo;
    private TitleBar mTitleBar;


    private View mOldPwdClearView;
    private View mNewPwdClearView;
    private View mEnturePwdClearView;

    private LinearLayout mUpdateGroup;

    private int mLoginStyle;
    private int mCurrentState = -1;


    private View mDiv;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updatepwd, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitleBar = getTitleBar();
        mTitleBar.setBackgroundColor(getResources().getColor(R.color.uikit_white));
        mTitleBar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginStyle != User.PWD_STATE_USER &&
                        !((BaseSwipeActivity) getActivity()).isAppRootActivity()) {
                    ToastAlarm.makeToastAlarm(getContext(), R.string.app_account_bind_login_success, Toast.LENGTH_SHORT).show();
                }
                finishAll();
            }
        });

        mOldPwd = (EditText) view.findViewById(R.id.app_setting_updatepwd_oldpwd);
        mNewPwd = (EditText) view.findViewById(R.id.app_setting_updatepwd_newpwd);
        mEnturePwd = (EditText) view.findViewById(R.id.app_setting_updatepwd_enturepwd);

        mOldPwdClearView = view.findViewById(R.id.app_register_account_old_pswd_clear);
        mNewPwdClearView = view.findViewById(R.id.app_register_account_new_pswd_clear);
        mEnturePwdClearView = view.findViewById(R.id.app_register_account_enture_pswd_clear);

        mOldPwd.setLongClickable(false);
        mNewPwd.setLongClickable(false);
        mEnturePwd.setLongClickable(false);

        mDiv = view.findViewById(R.id.app_login_div);

        mUpdateGroup = (LinearLayout) view.findViewById(R.id.app_rapidlogin_button_group);
        mUpdateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStateChange(STATE_PWD_REQUEST_ING);
            }
        });

        mUpdateDo = (TextView) view.findViewById(R.id.app_setting_updatepwd_do);
        mUpdateDo.setText("提交");
        mUpdateDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStateChange(STATE_PWD_REQUEST_ING);
            }
        });
        mLoginStyle = Integer.valueOf(UserProperties.getUserLoginstyle());
        diffStyleShow();


        initEditor(mOldPwd, mOldPwdClearView);
        initEditor(mNewPwd, mNewPwdClearView);
        initEditor(mEnturePwd, mEnturePwdClearView);
    }


    private void enableViews(boolean enable) {
        mNewPwd.setEnabled(enable);
        mOldPwd.setEnabled(enable);
        mEnturePwd.setEnabled(enable);
        mUpdateGroup.setEnabled(enable);
        mUpdateDo.setEnabled(enable);
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
            case STATE_CHECK_PWD_NULL:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_pswd_null,
                        Toast.LENGTH_LONG).show();
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
                if (requstUpdate()) {
                    enableViews(false);
                    mUpdateDo.setText(R.string.app_button_title_waiting);
                }
                break;
            case STATE_PWD_RESPONSE_SUCCESS:
                enableViews(true);
                mUpdateDo.setText(R.string.app_button_title_finish);
                UserProperties.saveLoginStyle(String.valueOf(User.PWD_STATE_USER));
                Intent intent = new Intent(UserProperties.INTENT_UPDATE_INFO);
                LocalBroadcastManager.getInstance(BaseApp.getApplication()).sendBroadcast(intent);
                ToastAlarm.makeToastAlarm(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                finishAll();
                break;
            case STATE_PWD_RESPONSE_ERROR:
                enableViews(true);
                mUpdateDo.setText(R.string.app_title_bar_save);
                break;
        }
    }

    private boolean checkInputValues() {
        String oldPwd = mOldPwd.getText().toString().trim();
        String accountPwd1 = mNewPwd.getText().toString().trim();
        String accountPwd2 = mEnturePwd.getText().toString().trim();
        if (mLoginStyle != User.PWD_STATE_USER) {//设置了密码
            oldPwd = "wecook20141204";
        }

        if (null == oldPwd || "".equals(oldPwd) || null == accountPwd1 || "".equals(accountPwd1) || null == accountPwd2 || "".equals(accountPwd2)) {
            doStateChange(STATE_CHECK_PWD_NULL);
            return false;
        }
        if (!StringUtils.validatePassword(oldPwd)) {
            doStateChange(STATE_CHECK_PWD_ERROR);
            return false;
        }

        if (!StringUtils.validatePassword(accountPwd1)) {
            doStateChange(STATE_CHECK_PWD_ERROR);
            return false;
        }

        if (!accountPwd1.equals(accountPwd2)) {
            doStateChange(STATE_CHECK_PWD_ENSURE_ERROR);
            return false;
        }
        return true;
    }


    public boolean requstUpdate() {
        String oldPWD = null;
        if (mLoginStyle == User.PWD_STATE_USER) {//设置了密码
            oldPWD = mOldPwd.getText().toString();
        } else {
            oldPWD = "wecook20141204";
        }
        if (NetworkState.available() && checkInputValues()) {
            String newPWD = mNewPwd.getText().toString();
            UserApi.updatePWD(oldPWD, newPWD, new ApiCallback<State>() {
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
            return true;
        }
        return false;
    }

    public void diffStyleShow() {
        if (mLoginStyle == User.PWD_STATE_USER) {//已设置密码
            mTitleBar.setTitle(getResources().getString(R.string.app_setting_updatepwd_title));
            mOldPwd.setVisibility(View.VISIBLE);
            mNewPwd.setVisibility(View.VISIBLE);
            mEnturePwd.setVisibility(View.VISIBLE);
        } else {//未设置密码
            mTitleBar.setTitle(getResources().getString(R.string.app_setting_setpwd_title));
            mNewPwd.setVisibility(View.VISIBLE);
            mNewPwd.setHint(R.string.app_setting_change_pwd_newpw);
            mEnturePwd.setVisibility(View.VISIBLE);
            mEnturePwd.setHint(R.string.app_setting_setpwd_enturepwd);
            mDiv.setVisibility(View.GONE);
            mOldPwd.setVisibility(View.GONE);
        }
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
                if (s.length() > 0) {
                    clear.setVisibility(View.VISIBLE);
                } else {
                    clear.setVisibility(View.GONE);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLoginStyle != User.PWD_STATE_USER &&
                    !((BaseSwipeActivity) getActivity()).isAppRootActivity()) {
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_account_bind_login_success, Toast.LENGTH_SHORT).show();
            }
            finishAll();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCardOut() {
        super.onCardOut();
        KeyboardUtils.closeKeyboard(getContext(), getView());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mOldPwd) {
            mOldPwd.setText("");
        }
        if (null != mNewPwd) {
            mNewPwd.setText("");
        }
        if (null != mEnturePwd) {
            mEnturePwd.setText("");
        }
    }
}
