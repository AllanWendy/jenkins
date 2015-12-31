package cn.wecook.app.features.preferential;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wecook.common.core.internet.ApiCallback;
import com.wecook.common.modules.property.PhoneProperties;
import com.wecook.common.utils.KeyboardUtils;
import com.wecook.common.utils.StringUtils;
import com.wecook.sdk.api.legacy.PreferentialApi;
import com.wecook.sdk.api.model.Preferential;
import com.wecook.sdk.userinfo.UserProperties;
import com.wecook.uikit.alarm.ToastAlarm;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;
import cn.wecook.app.main.home.user.UserLoginActivity;

/**
 * 获取优惠卷界面
 * Created by simon on 15/9/7.
 */
public class PreferentialFragment extends BaseTitleFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferential, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView(view);
    }

    /**
     * 初始化View
     *
     * @param view
     */
    private void initView(View view) {
        initTitleBar(view);
        initContentView(view);

    }

    /**
     * 初始化内容部分UI
     *
     * @param view
     */
    private void initContentView(View view) {
        final EditText editText = (EditText) view.findViewById(R.id.app_preferential_content_edit);
        final ImageView clearView = (ImageView) view.findViewById(R.id.app_preferential_edit_clear);
        final TextView commit = (TextView) view.findViewById(R.id.app_preferential_bt);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
                    clearView.setVisibility(View.VISIBLE);
                } else {
                    clearView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                commit.setEnabled(false);
                String inputText = editText.getText().toString();
                if (!StringUtils.isEmpty(inputText.trim())) {
                    PreferentialApi.getPreferential(inputText, new ApiCallback<Preferential>() {
                        @Override
                        public void onResult(Preferential result) {
                            commit.setEnabled(true);
                            if (result.available()) {
                                if (UserProperties.isLogin() && !StringUtils.isEmpty(result.getUrl())) {
                                    Uri.Builder builder = Uri.parse(result.getUrl()).buildUpon();
                                    builder.appendQueryParameter("wid", PhoneProperties.getDeviceId());
                                    builder.appendQueryParameter("uid", UserProperties.getUserId());

                                    Bundle gainCouponBundle = new Bundle();
                                    gainCouponBundle.putBoolean(WebViewFragment.EXTRA_FIXED_VIEW, true);
                                    gainCouponBundle.putString(WebViewFragment.EXTRA_URL, builder.toString());
                                    startActivity(new Intent(getContext(), WebViewActivity.class), gainCouponBundle);
                                    KeyboardUtils.closeKeyboard(getContext(), editText);
                                } else {
                                    startActivity(new Intent(getContext(), UserLoginActivity.class));
                                }
                            }
                            ToastAlarm.show(result.getInfo());
                        }
                    });
                } else {
                    commit.setEnabled(true);
                    ToastAlarm.show("请检查优惠码是否正确");
                }
            }
        });
    }

    /**
     * 初始化titleBar
     *
     * @param view
     */
    private void initTitleBar(View view) {
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack(true);
        titleBar.setTitle("获得优惠");
    }

    /**
     * 初始化数据
     */
    private void initData() {

    }
}
