package cn.wecook.app.features.suggestion;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.asynchandler.UIHandler;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.SuggestionApi;
import com.wecook.sdk.api.model.Suggestion;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;

import cn.wecook.app.R;

/**
 * 意见反馈页面
 * Created by simon on 15/9/20.
 */
public class SuggestionFragment extends BaseTitleFragment {
    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private TextView mSuggestionCommit;
    private EditText mSuggestionContent;
    private TextView mSuggestionPhoneNum;
    private String orderId;
    private View rootView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String extra_orderId = arguments.getString(EXTRA_ORDER_ID);
            if (extra_orderId != null) {
                orderId = extra_orderId;
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mSuggestionPhoneNum != null) {
            mSuggestionPhoneNum.setText(UserProperties.getUserPhone());
        }
        if (mSuggestionContent != null) {
            mSuggestionContent.setText("");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_suggestion, null);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        KeyboardUtils.closeKeyboard(getContext(), mSuggestionContent);
    }

    /**
     * 初始化view
     *
     * @param view
     */
    private void initView(View view) {
        initTitleBar(view);
        final InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(getContext().INPUT_METHOD_SERVICE);
        mSuggestionCommit = (TextView) view.findViewById(R.id.app_my_feature_suggestion_commit);
        mSuggestionContent = (EditText) view.findViewById(R.id.app_my_feature_suggestion_content);
        mSuggestionPhoneNum = (TextView) view.findViewById(R.id.app_my_feature_suggestion_phonenum);
        mSuggestionPhoneNum.setText(UserProperties.getUserPhone());
        mSuggestionContent.setText("");
        mSuggestionCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //提交建议判断
                if (StringUtils.isEmpty(mSuggestionContent.getText().toString())) {
                    ToastAlarm.show("投诉内容不能为空");
                    return;
                }
                if (StringUtils.isEmpty(mSuggestionPhoneNum.getText().toString())) {
                    ToastAlarm.show("电话号码不能为空");
                    return;
                }
                if (!StringUtils.validatePhoneNumber(mSuggestionPhoneNum.getText().toString())) {
                    ToastAlarm.show("电话号码格式错误");
                    return;
                }
                //提交建议
                SuggestionApi.sendSuggestion(orderId, mSuggestionPhoneNum.getText().toString(), mSuggestionContent.getText().toString(), new ApiCallback<Suggestion>() {
                    @Override
                    public void onResult(Suggestion result) {
                        if (result.available()) {
                            if (result.getStatusState() == 1) {
                                //提交成功
                                ToastAlarm.show("提交成功");
                                UIHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        back();
                                    }
                                }, 300);
                            } else {
                                //提交失败
                                ToastAlarm.show("提交失败");
                            }
                        } else {
                            ToastAlarm.show("提交失败");

                        }
                    }
                });
            }
        });
    }

    /**
     * 初始化titilebar
     *
     * @param view
     */
    private void initTitleBar(View view) {
        setTitle("投诉商家");
        getTitleBar().setBackgroundColor(Color.WHITE);
    }
}
