package cn.wecook.app.main.home.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wecook.sdk.api.legacy.UserApi;
import com.wecook.uikit.fragment.BaseTitleFragment;
import com.wecook.uikit.widget.TitleBar;

import cn.wecook.app.R;
import cn.wecook.app.features.webview.WebViewActivity;
import cn.wecook.app.features.webview.WebViewFragment;

/**
 * 法律条款
 * Created by LK on 2015/9/21.
 */
public class ProvisionsFragment extends BaseTitleFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_provisions, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TitleBar titleBar = getTitleBar();
        titleBar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        titleBar.setTitle("法律条款");
        titleBar.setBackgroundColor(getResources().getColor(R.color.uikit_white));


        initView(R.id.app_provisions_item_trading);//交易
        initView(R.id.app_provisions_item_service);//服务
        initView(R.id.app_provisions_item_privacy);//隐私
        initView(R.id.app_provisions_item_semi_finished_products);//半成品
    }

    /**
     * 初始化View
     * 资源布局
     *
     * @param rid
     */
    private void initView(int rid) {
        //Title
        TextView textView = (TextView) getView().findViewById(rid).findViewById(R.id.app_my_feature_name);
        String title = "";
        switch (rid) {
            case R.id.app_provisions_item_trading:
                title = "味库交易条款";
                break;
            case R.id.app_provisions_item_service:
                title = "味库服务协议";
                break;
            case R.id.app_provisions_item_privacy:
                title = "味库隐私条款";
                break;
            case R.id.app_provisions_item_semi_finished_products:
                title = "食品安全责任保险条款";
                break;
        }
        textView.setText(title);
        //监听
        getView().findViewById(rid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "";
                String title_ = "";
                switch (v.getId()) {
                    case R.id.app_provisions_item_trading:
                        url = UserApi.TRADING_PATH;
                        title_ = "交易条款";
                        break;
                    case R.id.app_provisions_item_service:
                        url = UserApi.SERVICE_PATH;
                        title_ = "服务协议";
                        break;
                    case R.id.app_provisions_item_privacy:
                        url = UserApi.PRIVACY_PATH;
                        title_ = "隐私条款";
                        break;
                    case R.id.app_provisions_item_semi_finished_products:
                        url = UserApi.BCP_PATH;
                        title_ = "食品安全责任保险条款";
                        break;
                }
                if (!"".equals(url)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(WebViewFragment.EXTRA_URL, url);
                    bundle.putString(WebViewFragment.EXTRA_TITLE, title_);
                    startActivity(new Intent(getContext(), WebViewActivity.class), bundle);
                }
            }
        });
    }
}
