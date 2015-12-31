package cn.wecook.app.main.home.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wecook.common.modules.property.SharePreferenceProperties;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.main.home.user.UserBindPhoneActivity;
import cn.wecook.app.utils.EditTextUtils;

/**
 * 验证手机号码界面
 *
 * @author sc.wang created at 15.6.1
 * @version 2.3.9
 */
public class VerifyPhoneFragment extends BaseTitleFragment {

    private static final int STATE_IDLE = 1;
    private static final int STATE_CHECK_PHONE_VERIFY_ERROR = 3;
    private static final int STATE_CHECK_PHONE_VERIFY_SUCCESS = 4;
    private static final int STATE_CLEAR_PHONE_VERIFY = 5;
    private static final int STATE_INPUT_PHONE_VERIFY = 10;

    private TextView mPhoneNum;
    private EditText mVerifyPhoneNum;
    private View mVerifyPhoneNumClear;
    private TextView mVerifyTip;
    private TextView mBtnVerifyPhone;

    private View mVerifyPhoneGroup;

    private View mVerifyPhoneLoading;
    private int mCurrentState = -1;
    private int mVerifyCount;

    private final String TAG = "VerifyPhoneFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_phone, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TitleBar titleBar = getTitleBar();
        titleBar.setBackgroundColor(getContext().getResources().getColor(R.color.uikit_white));
        titleBar.setTitle(getResources().getString(R.string.app_bind_phone));
        titleBar.enableBottomDiv(true);

        mPhoneNum = (TextView) view.findViewById(R.id.app_verify_phone_num);
        mVerifyPhoneNum = (EditText) view.findViewById(R.id.app_verify_phone_code);
        mVerifyPhoneNum.requestFocus();
        EditTextUtils.addTrimInflate(mVerifyPhoneNum, 11);

        mVerifyPhoneNumClear = view.findViewById(R.id.app_verify_phone_code_clear);
        mVerifyTip = (TextView) view.findViewById(R.id.app_verify_phone_top_tip);
        mVerifyPhoneLoading = view.findViewById(R.id.app_verify_phone_waiting);
        mBtnVerifyPhone = (TextView) view.findViewById(R.id.app_verify_phone_button_do);
        //登陆按钮的初始化
        mVerifyPhoneGroup = view.findViewById(R.id.app_verify_phone_button_group);
        initData();
        setListener();
    }

    private void initData() {
        if (!StringUtils.isEmpty(UserProperties.getUserPhone())) {
            String phoneNum = UserProperties.getUserPhone();
            String phoneNumSub = phoneNum.substring(0, 3) + "****" + phoneNum.substring(7);
            mPhoneNum.setText(phoneNumSub);
        }
    }

    @Override
    public void onStartUILoad() {
        super.onStartUILoad();
        initData();
    }

    private void setListener() {

        mVerifyPhoneGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllowVerify()) {
                    startActivity(new Intent(getContext(), UserBindPhoneActivity.class));
                    back();
                }
            }
        });

        mVerifyPhoneNumClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStateChange(STATE_CLEAR_PHONE_VERIFY);
            }
        });

        mVerifyPhoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    doStateChange(STATE_CLEAR_PHONE_VERIFY);
                } else {
                    doStateChange(STATE_INPUT_PHONE_VERIFY);
                }
            }
        });
    }

    /**
     * 维护状态变化
     *
     * @param state
     */
    private void doStateChange(int state) {

        if (mCurrentState == state && state != STATE_CHECK_PHONE_VERIFY_ERROR) {
            return;
        }
        mCurrentState = state;
        switch (state) {
            case STATE_IDLE:
                enableViews(true);
                mVerifyPhoneNumClear.setVisibility(View.GONE);
                mPhoneNum.setText("");
                mVerifyPhoneNum.setText("");
                break;
            case STATE_CHECK_PHONE_VERIFY_ERROR:
                mVerifyCount++;
                if (mVerifyCount < 5) {
                    String errorMsg = getString(R.string.app_error_verify_phone, 5 - mVerifyCount);
                    ToastAlarm.makeToastAlarm(getContext(), errorMsg,
                            Toast.LENGTH_SHORT).show();
                } else {
                    SharePreferenceProperties.set(UserProperties.USER_VERIFY_PHONE_OLD_TIME, System.currentTimeMillis());
                    ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_verify_phone_max_count,
                            Toast.LENGTH_SHORT).show();
                }
                mVerifyPhoneLoading.setVisibility(View.GONE);
                break;
            case STATE_CHECK_PHONE_VERIFY_SUCCESS:
                ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_pwd_format,
                        Toast.LENGTH_LONG).show();
                mVerifyPhoneLoading.setVisibility(View.GONE);
                break;
            case STATE_CLEAR_PHONE_VERIFY:
                mVerifyPhoneNum.setText("");
                mVerifyPhoneNumClear.setVisibility(View.GONE);
                break;
            case STATE_INPUT_PHONE_VERIFY:
                if (mVerifyPhoneNum.getText().length() > 0) {
                    mVerifyPhoneNumClear.setVisibility(View.VISIBLE);
                }
                break;
        }

    }

    /**
     * 检查输入数据
     *
     * @return
     */
    private boolean isAllowVerify() {
        long oldTime = 0;
        try {
            oldTime = (long) SharePreferenceProperties.get(UserProperties.USER_VERIFY_PHONE_OLD_TIME, 0l);
        } catch (ClassCastException e) {
        }
        if (oldTime != 0 && DateUtils.isToday(oldTime)) {
            ToastAlarm.makeToastAlarm(getContext(), R.string.app_error_verify_phone_max_count,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        String verifyPhoneNum = mVerifyPhoneNum.getText().toString().trim();
        if (null == verifyPhoneNum || "".equals(verifyPhoneNum)) {
            ToastAlarm.show("手机号不能为空");
            return false;
        }
        if (!verifyPhoneNum.equals(UserProperties.getUserPhone())) {
            doStateChange(STATE_CHECK_PHONE_VERIFY_ERROR);
            return false;
        }
        return true;
    }

    private void enableViews(boolean enable) {
        mPhoneNum.setEnabled(enable);
        mVerifyPhoneNum.setEnabled(enable);
        mVerifyPhoneNumClear.setEnabled(enable);
        mBtnVerifyPhone.setEnabled(enable);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (null != mVerifyPhoneNum && null != getContext()) {
            KeyboardUtils.openKeyboard(getContext(), mVerifyPhoneNum);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mVerifyPhoneNum && null != getContext()) {
            KeyboardUtils.closeKeyboard(getContext(), mVerifyPhoneNum);
        }
    }
}
